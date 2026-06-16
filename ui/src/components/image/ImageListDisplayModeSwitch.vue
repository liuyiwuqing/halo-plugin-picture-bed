<script lang="ts" setup>
import {VButton, VSpace} from '@halo-dev/components'
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
  <VSpace spacing="xs">
    <VButton
        v-for="mode in imageListDisplayModes"
        :key="mode.value"
        v-tooltip="mode.label"
        :class="{ '!border-primary !bg-primary/10 !text-primary': displayMode === mode.value }"
        size="sm"
        @click="displayMode = mode.value"
    >
      <template #icon>
        <component :is="iconMap[mode.value]" class="h-full w-full"/>
      </template>
      {{ mode.label }}
    </VButton>
  </VSpace>
</template>
