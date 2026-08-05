export type ImageListDisplayMode = 'grid' | 'masonry'

export interface ImageListDisplayClasses {
    container: string
    item: string
    frame: string
    image: string
    caption: string
    card: string
}

export const IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY = 'picture-bed:image-list-display-mode'

export const imageListDisplayModes: Array<{
    label: string
    value: ImageListDisplayMode
}> = [
    {
        label: '网格',
        value: 'grid',
    },
    {
        label: '瀑布流',
        value: 'masonry',
    },
]

export function isImageListDisplayMode(value: unknown): value is ImageListDisplayMode {
    return value === 'grid' || value === 'masonry'
}

export function getImageListDisplayClasses(mode: ImageListDisplayMode): ImageListDisplayClasses {
    if (mode === 'masonry') {
        return {
            container: 'picture-bed-image-list picture-bed-image-list--masonry mt-2',
            item: 'group relative flex w-full flex-col overflow-hidden bg-transparent',
            frame: 'picture-bed-image-list__frame--masonry block w-full cursor-pointer overflow-hidden',
            image:
                'pointer-events-none block h-auto w-full object-contain group-hover:opacity-90 transform-gpu',
            // 渐变底色走 scoped CSS 而不是 bg-gradient-to-t/from-gray-900/75：
            // 插件不生成自己的 Tailwind 工具类，只能用 Halo Console 恰好生成过的，
            // 而 Halo 从未使用 from-gray-900/75，该类不存在会让整条渐变失效，白字落在白图上就看不见了。
            caption:
                'picture-bed-image-list__caption--masonry pointer-events-none absolute inset-x-0 bottom-0 block truncate px-2 pb-1.5 pt-7 text-center text-xs font-medium text-white opacity-0 transition-opacity group-hover:opacity-100',
            card: 'picture-bed-image-list__card--masonry overflow-hidden',
        }
    }

    return {
        container:
            'picture-bed-image-list picture-bed-image-list--grid mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10',
        item: 'group relative flex h-full flex-col bg-white',
        // frame 不能带 h-full：它会占满整个 flex 列，把收缩量全部压到文件名那一行上。
        // 又因为 caption 的 truncate 自带 overflow:hidden，其 min-height:auto 计算为 0，
        // 可以被压到内容高度以下，结果就是文件名被纵向切掉半截。宽高比已经决定了 frame 的高度。
        frame: 'aspect-h-8 aspect-w-10 block w-full shrink-0 cursor-pointer overflow-hidden bg-gray-100',
        image: 'pointer-events-none object-cover group-hover:opacity-75 transform-gpu',
        caption:
            'pointer-events-none block shrink-0 truncate px-2 py-1 text-center text-xs font-medium text-gray-700',
        card: '',
    }
}
