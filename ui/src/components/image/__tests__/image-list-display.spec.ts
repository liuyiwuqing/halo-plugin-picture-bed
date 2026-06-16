import {describe, expect, it} from 'vitest'
import {
    getImageListDisplayClasses,
    getImageListItemClasses,
    getMasonryTileVariant,
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

    it('uses a dense mosaic grid for Masonry Mode', () => {
        const classes = getImageListDisplayClasses('masonry')

        expect(classes.container).toContain('picture-bed-image-list--masonry')
        expect(classes.container).toContain('picture-bed-image-list--comfortable-preview')
        expect(classes.container).not.toContain('columns-2')
        expect(classes.frame).toContain('picture-bed-image-list__frame--masonry')
        expect(classes.card).toContain('picture-bed-image-list__card--masonry')
        expect(classes.image).toContain('object-contain')
        expect(classes.image).not.toContain('object-cover')
    })

    it('sizes masonry tiles from image proportions', () => {
        expect(getMasonryTileVariant({width: 3840, height: 2160}, 0)).toBe('large')
        expect(getMasonryTileVariant({width: 1800, height: 700}, 1)).toBe('wide')
        expect(getMasonryTileVariant({width: 700, height: 1800}, 1)).toBe('tall')
        expect(getMasonryTileVariant({width: 1400, height: 1200}, 7)).toBe('large')
        expect(getMasonryTileVariant({width: 1200, height: 1000}, 1)).toBe('normal')
    })

    it('uses a stable fallback rhythm when image dimensions are missing', () => {
        expect(getMasonryTileVariant({}, 0)).toBe('large')
        expect(getMasonryTileVariant({}, 4)).toBe('wide')
        expect(getMasonryTileVariant({}, 7)).toBe('tall')
        expect(getImageListItemClasses('masonry', {}, 0)).toBe(
            'picture-bed-image-list__card--large',
        )
        expect(getImageListItemClasses('grid', {}, 0)).toBe('')
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
