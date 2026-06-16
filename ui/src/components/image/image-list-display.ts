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
            caption:
                'pointer-events-none absolute inset-x-0 bottom-0 block truncate bg-gradient-to-t from-gray-900/75 to-transparent px-2 pb-1.5 pt-7 text-center text-xs font-medium text-white opacity-0 transition-opacity group-hover:opacity-100',
            card: 'picture-bed-image-list__card--masonry overflow-hidden',
        }
    }

    return {
        container:
            'picture-bed-image-list picture-bed-image-list--grid mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10',
        item: 'group relative flex h-full flex-col bg-white',
        frame: 'aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100',
        image: 'pointer-events-none object-cover group-hover:opacity-75 transform-gpu',
        caption:
            'pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700',
        card: '',
    }
}
