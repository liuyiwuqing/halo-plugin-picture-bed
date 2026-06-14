import type {ImageVO} from '@/api/generated'

function getImageKey(image: ImageVO | undefined) {
    return image?.id || image?.url || image?.name
}

export function getImageIndex(images: ImageVO[], image: ImageVO | undefined) {
    const imageKey = getImageKey(image)
    if (!imageKey) {
        return -1
    }

    return images.findIndex((item) => getImageKey(item) === imageKey)
}

export function getAdjacentImage(images: ImageVO[], image: ImageVO | undefined, offset: -1 | 1) {
    const currentIndex = getImageIndex(images, image)
    if (currentIndex < 0) {
        return undefined
    }

    return images[currentIndex + offset]
}
