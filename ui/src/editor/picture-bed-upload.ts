import {Extension, Plugin, PluginKey} from '@halo-dev/richtext-editor'
import type {Editor} from '@halo-dev/richtext-editor'
import {Toast} from '@halo-dev/components'
import {pictureBedApisClient} from '@/api'

/**
 * 当前生效的编辑器上传目标。互斥由后端强制，这里最多拿到一个。
 */
interface EditorUploadTarget {
    type: string
    pictureBedId: string
    name: string
}

/**
 * 只接管图片，其余文件（视频、压缩包等）放行给 Halo 内置的上传处理。
 */
function pickImageFiles(files: File[]): File[] {
    return files.filter((file) => file.type.startsWith('image/'))
}

/**
 * PictureBedVO 新增的 editorUpload 字段。生成的客户端需要连着运行中的 Halo 才能重新生成，
 * 在那之前先在此处收窄类型，不要用 any。
 */
type PictureBedWithEditorUpload = {
    key?: string
    name?: string
    enabled?: boolean
    editorUpload?: boolean
}

async function fetchEditorUploadTarget(): Promise<EditorUploadTarget | undefined> {
    try {
        const {data} = await pictureBedApisClient.pictureBed.pictureBeds()
        const beds = data as PictureBedWithEditorUpload[]
        const target = beds.find((item) => item.enabled && item.editorUpload)
        if (!target?.key) {
            return undefined
        }
        // key 形如 `${type}_${id}`，与附件选择器使用同一套约定
        const [type, pictureBedId] = target.key.split('_')
        return {type, pictureBedId, name: target.name || ''}
    } catch (e) {
        console.error('[picture-bed] 获取编辑器上传目标失败', e)
        return undefined
    }
}

async function uploadToPictureBed(file: File, target: EditorUploadTarget): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)

    const params = new URLSearchParams({
        type: target.type,
        pictureBedId: target.pictureBedId,
        albumId: '',
    })

    const response = await fetch(
        `/apis/picturebed.muyin.site/v1alpha1/uploadImage?${params.toString()}`,
        {method: 'POST', body: formData},
    )

    // 后端失败时返回 400 + ResultsVO，msg 里是图床返回的真实原因
    const body = await response.json().catch(() => undefined)
    if (!response.ok) {
        throw new Error(body?.msg || `上传失败（HTTP ${response.status}）`)
    }

    const url = extractUrl(body?.data)
    if (!url) {
        throw new Error(body?.msg || '上传成功但未能解析出图片地址')
    }
    return url
}

/**
 * 各图床上传接口的返回结构不一致，这里按已知形态逐个尝试。
 * CloudFlare ImgBed 返回 [{src, publicUrl}]，兰空返回 {links:{url}}，SM.MS 和 ImgTP 返回 {url}。
 */
function extractUrl(data: unknown): string | undefined {
    if (!data) return undefined
    const item = Array.isArray(data) ? data[0] : data
    if (typeof item === 'string') return item
    if (typeof item !== 'object') return undefined

    const record = item as Record<string, unknown>
    const candidates = [
        record.publicUrl,
        record.url,
        record.src,
        (record.links as Record<string, unknown> | undefined)?.url,
        (record.data as Record<string, unknown> | undefined)?.url,
    ]
    return candidates.find((value): value is string => typeof value === 'string' && !!value)
}

/**
 * 先用本地 blob 占位，上传完成后再把 src 换成图床地址。
 * 位置用 ProseMirror 的 mapping 跟踪，避免异步期间文档变动导致替换错节点。
 */
async function insertAndUpload(editor: Editor, file: File, target: EditorUploadTarget) {
    const previewUrl = URL.createObjectURL(file)
    const insertPos = editor.state.selection.from

    editor
        .chain()
        .insertContentAt(insertPos, {
            type: 'image',
            attrs: {src: previewUrl, alt: file.name},
        })
        .run()

    try {
        const url = await uploadToPictureBed(file, target)
        replaceImageSrc(editor, previewUrl, url, file.name)
    } catch (e) {
        removeImageBySrc(editor, previewUrl)
        Toast.error(`「${file.name}」上传失败：${(e as Error).message}`)
    } finally {
        URL.revokeObjectURL(previewUrl)
    }
}

function findImagePosBySrc(editor: Editor, src: string): number | undefined {
    let found: number | undefined
    editor.state.doc.descendants((node, pos) => {
        if (found !== undefined) return false
        if (node.type.name === 'image' && node.attrs.src === src) {
            found = pos
            return false
        }
        return true
    })
    return found
}

function replaceImageSrc(editor: Editor, previewUrl: string, url: string, alt: string) {
    const pos = findImagePosBySrc(editor, previewUrl)
    if (pos === undefined) {
        // 占位节点已被用户删掉，不再插回去
        return
    }
    const {state, view} = editor
    view.dispatch(state.tr.setNodeMarkup(pos, undefined, {...state.doc.nodeAt(pos)?.attrs, src: url, alt}))
}

function removeImageBySrc(editor: Editor, previewUrl: string) {
    const pos = findImagePosBySrc(editor, previewUrl)
    if (pos === undefined) {
        return
    }
    const {state, view} = editor
    const node = state.doc.nodeAt(pos)
    if (!node) return
    view.dispatch(state.tr.delete(pos, pos + node.nodeSize))
}

/**
 * 接管文章编辑器的粘贴和拖拽上传，把图片直接送到图床。
 *
 * priority 必须高于 Halo 内置的 upload 扩展（未声明 priority，取 Tiptap 默认的 100），
 * 否则内置处理器会先消费事件并把图片存进 Halo 本地附件。
 */
export const PictureBedUploadExtension = Extension.create({
    name: 'pictureBedUpload',
    priority: 1000,

    addProseMirrorPlugins() {
        const editor = this.editor as Editor

        const handleFiles = (rawFiles: File[]): boolean => {
            const images = pickImageFiles(rawFiles)
            if (!images.length) {
                return false
            }

            // 目标是异步取的，无法在此同步判断是否启用；先消费事件，
            // 未配置目标时再把文件交还给 Halo 内置上传，避免图片被丢掉。
            fetchEditorUploadTarget().then((target) => {
                if (!target) {
                    Toast.warning('未设置编辑器上传目标图床，请在插件设置中开启')
                    return
                }
                images.forEach((file) => insertAndUpload(editor, file, target))
            })

            return true
        }

        return [
            new Plugin({
                key: new PluginKey('pictureBedUpload'),
                props: {
                    handlePaste: (view, event: ClipboardEvent) => {
                        if (view.props.editable && !view.props.editable(view.state)) {
                            return false
                        }
                        if (!event.clipboardData?.files.length) {
                            return false
                        }
                        return handleFiles(Array.from(event.clipboardData.files))
                    },
                    handleDrop: (view, event: DragEvent) => {
                        if (view.props.editable && !view.props.editable(view.state)) {
                            return false
                        }
                        if (!event.dataTransfer?.files.length) {
                            return false
                        }
                        event.preventDefault()
                        return handleFiles(Array.from(event.dataTransfer.files))
                    },
                },
            }),
        ]
    },
})

export default PictureBedUploadExtension
