export type ImageListDisplayMode = 'grid' | 'masonry'
export type MasonryTileVariant = 'normal' | 'wide' | 'tall' | 'large'

export interface ImageListDisplayClasses {
    container: string
    frame: string
    image: string
    card: string
}

export interface ImageListDisplayImageShape {
    width?: number
    height?: number
}

export const IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY = 'picture-bed:image-list-display-mode'

const masonryFallbackVariants: MasonryTileVariant[] = [
    'large',
    'normal',
    'normal',
    'normal',
    'wide',
    'normal',
    'normal',
    'tall',
    'normal',
    'wide',
    'normal',
    'normal',
]

const masonryTileClassMap: Record<MasonryTileVariant, string> = {
    normal: 'picture-bed-image-list__card--normal',
    wide: 'picture-bed-image-list__card--wide',
    tall: 'picture-bed-image-list__card--tall',
    large: 'picture-bed-image-list__card--large',
}

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

export function getMasonryTileVariant(
    image: ImageListDisplayImageShape,
    index: number,
): MasonryTileVariant {
    const width = Number(image.width)
    const height = Number(image.height)

    if (!Number.isFinite(width) || !Number.isFinite(height) || width <= 0 || height <= 0) {
        return masonryFallbackVariants[index % masonryFallbackVariants.length]
    }

    const ratio = width / height

    if (index % 8 === 0 && ratio >= 0.75 && ratio <= 2) {
        return 'large'
    }

    if (ratio >= 1.75) {
        return 'wide'
    }

    if (ratio <= 0.72) {
        return 'tall'
    }

    if (index % 7 === 0 && ratio >= 0.85 && ratio <= 1.6) {
        return 'large'
    }

    return 'normal'
}

export function getImageListItemClasses(
    mode: ImageListDisplayMode,
    image: ImageListDisplayImageShape,
    index: number,
) {
    if (mode !== 'masonry') {
        return ''
    }

    return masonryTileClassMap[getMasonryTileVariant(image, index)]
}

export function getImageListDisplayClasses(mode: ImageListDisplayMode): ImageListDisplayClasses {
    if (mode === 'masonry') {
        return {
            container:
                'picture-bed-image-list picture-bed-image-list--masonry picture-bed-image-list--comfortable-preview mt-2',
            frame:
                'picture-bed-image-list__frame--masonry block h-full min-h-0 flex-1 cursor-pointer overflow-hidden bg-white',
            image:
                'pointer-events-none h-full w-full object-contain group-hover:opacity-75 transform-gpu',
            card: 'picture-bed-image-list__card--masonry h-full overflow-hidden',
        }
    }

    return {
        container:
            'picture-bed-image-list picture-bed-image-list--grid mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10',
        frame: 'aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100',
        image: 'pointer-events-none object-cover group-hover:opacity-75 transform-gpu',
        card: '',
    }
}
