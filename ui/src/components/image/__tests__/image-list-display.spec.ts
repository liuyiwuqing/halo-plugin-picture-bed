import {describe, expect, it} from 'vitest'
import {
    getImageListDisplayClasses,
    IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY,
    isImageListDisplayMode,
} from '../image-list-display'

describe('image list display mode', () => {
    it('keeps Grid Mode as the regular row-and-column image list', () => {
        const classes = getImageListDisplayClasses('grid')

        expect(classes.container).toContain('grid')
        expect(classes.container).toContain('grid-cols-3')
        expect(classes.frame).toContain('aspect-h-8')
        expect(classes.image).toContain('object-cover')
    })

    it('uses natural-height columns for Masonry Mode', () => {
        const classes = getImageListDisplayClasses('masonry')

        expect(classes.container).toContain('picture-bed-image-list--masonry')
        expect(classes.container).not.toContain('picture-bed-image-list--comfortable-preview')
        expect(classes.container).not.toContain('grid')
        expect(classes.item).toContain('bg-transparent')
        expect(classes.frame).toContain('picture-bed-image-list__frame--masonry')
        expect(classes.card).toContain('picture-bed-image-list__card--masonry')
        expect(classes.card).not.toContain('h-full')
        expect(classes.image).toContain('object-contain')
        expect(classes.image).toContain('h-auto')
        expect(classes.image).not.toContain('object-cover')
        expect(classes.caption).toContain('absolute')
        expect(classes.caption).toContain('group-hover:opacity-100')
    })

    it('accepts only supported display modes', () => {
        expect(isImageListDisplayMode('grid')).toBe(true)
        expect(isImageListDisplayMode('masonry')).toBe(true)
        expect(isImageListDisplayMode('table')).toBe(false)
    })

    it('uses a stable storage key for the user selected display mode', () => {
        expect(IMAGE_LIST_DISPLAY_MODE_STORAGE_KEY).toBe('picture-bed:image-list-display-mode')
    })
})
