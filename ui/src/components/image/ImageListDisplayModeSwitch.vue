<script lang="ts" setup>
import RiLayoutGridLine from '~icons/ri/layout-grid-line'
import RiLayoutMasonryLine from '~icons/ri/layout-masonry-line'
import {type ImageListDisplayMode, imageListDisplayModes,} from '@/components/image/image-list-display'

const displayMode = defineModel<ImageListDisplayMode>({
  required: true,
})

const iconMap = {
  grid: RiLayoutGridLine,
  masonry: RiLayoutMasonryLine,
}
</script>

<template>
  <div
      class="inline-flex items-center rounded-md border border-gray-200 bg-gray-100 p-0.5 shadow-sm"
      aria-label="图片列表展示模式"
      role="radiogroup"
  >
    <button
        v-for="mode in imageListDisplayModes"
        :key="mode.value"
        v-tooltip="mode.label"
        :aria-checked="displayMode === mode.value"
        :class="[
        'inline-flex h-7 min-w-16 items-center justify-center gap-1.5 rounded px-2.5 text-xs font-medium leading-none transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30',
        displayMode === mode.value
          ? 'bg-white text-primary shadow-sm ring-1 ring-primary/20'
          : 'text-gray-600 hover:bg-white/80 hover:text-gray-900',
      ]"
        role="radio"
        type="button"
        @click="displayMode = mode.value"
    >
      <component :is="iconMap[mode.value]" class="h-3.5 w-3.5 shrink-0"/>
      <span class="whitespace-nowrap">{{ mode.label }}</span>
    </button>
  </div>
</template>
