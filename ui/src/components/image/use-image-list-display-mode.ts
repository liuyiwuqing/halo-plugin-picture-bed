import {ref, watch} from 'vue'
import {
    IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY,
    type ImageListDisplayMode,
    isImageListDisplayMode,
} from './image-list-display'

export function useImageListDisplayMode() {
    const displayMode = ref<ImageListDisplayMode>('grid')

    if (typeof window !== 'undefined') {
        const savedMode = window.localStorage.getItem(IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY)
        if (isImageListDisplayMode(savedMode)) {
            displayMode.value = savedMode
        }
    }

    watch(displayMode, (mode) => {
        if (typeof window === 'undefined') {
            return
        }

        window.localStorage.setItem(IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY, mode)
    })

    return {
        displayMode,
    }
}
