<script lang="ts" setup>
import {computed} from 'vue'
import {IconCheckboxFill, IconEye, VCard} from '@halo-dev/components'
import type {ImageVO} from '@/api/generated'
import {isImage} from '@/utils/image'
import AttachmentFileTypeIcon from '@/components/icon/AttachmentFileTypeIcon.vue'
import LazyImage from '@/components/image/LazyImage.vue'
import {getImageListDisplayClasses, getImageListItemClasses, type ImageListDisplayMode,} from './image-list-display'

const props = withDefaults(
    defineProps<{
      images: ImageVO[]
      mode?: ImageListDisplayMode
      isChecked: (image: ImageVO) => boolean
      isDisabled: (image: ImageVO) => boolean
    }>(),
    {
      images: () => [],
      mode: 'grid',
    },
)

const emit = defineEmits<{
  (event: 'select', image: ImageVO): void
  (event: 'open-detail', image: ImageVO): void
}>()

const displayClasses = computed(() => getImageListDisplayClasses(props.mode))

const cardClass = (image: ImageVO, index: number) => [
  'hover:shadow',
  displayClasses.value.card,
  getImageListItemClasses(props.mode, image, index),
  {
    'ring-1 ring-primary': props.isChecked(image),
    'pointer-events-none !cursor-not-allowed opacity-50': props.isDisabled(image),
  },
]
</script>

<template>
  <div :class="displayClasses.container" :data-display-mode="mode" role="list">
    <VCard
        v-for="(image, index) in images"
        :key="image.id || image.url || index"
        :body-class="['!p-0', 'picture-bed-image-list__card-body']"
        :class="cardClass(image, index)"
        role="listitem"
        @click.stop="emit('select', image)"
    >
      <div class="group relative flex h-full flex-col bg-white">
        <div :class="displayClasses.frame">
          <LazyImage
              v-if="isImage(image.mediaType)"
              :key="image.id"
              :alt="image.name || ''"
              :src="image.url || ''"
              :classes="displayClasses.image"
          >
            <template #loading>
              <div class="flex h-full min-h-24 items-center justify-center">
                <span class="text-xs text-gray-400">加载中...</span>
              </div>
            </template>
            <template #error>
              <div class="flex h-full min-h-24 items-center justify-center">
                <span class="text-xs text-red-400">加载异常</span>
              </div>
            </template>
          </LazyImage>
          <AttachmentFileTypeIcon v-else :file-name="image.name"/>
        </div>
        <p
            class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
        >
          {{ image.name }}
        </p>

        <div
            :class="{ '!flex': isChecked(image) }"
            class="absolute left-0 top-0 hidden h-1/3 w-full justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
        >
          <IconEye
              class="mr-1 mt-1 hidden h-6 w-6 cursor-pointer text-white transition-all hover:text-primary group-hover:block"
              @click.stop="emit('open-detail', image)"
          />
          <IconCheckboxFill
              :class="{ '!text-primary': isChecked(image) }"
              class="mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>

<style scoped>
.picture-bed-image-list--masonry {
  display: grid;
  grid-auto-flow: dense;
  grid-auto-rows: 10rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

:deep(.picture-bed-image-list__card-body) {
  height: 100%;
  min-height: 0;
}

.picture-bed-image-list__card--normal {
  grid-column: span 1 / span 1;
  grid-row: span 1 / span 1;
}

.picture-bed-image-list__card--wide {
  grid-column: span 2 / span 2;
  grid-row: span 1 / span 1;
}

.picture-bed-image-list__card--tall {
  grid-column: span 1 / span 1;
  grid-row: span 2 / span 2;
}

.picture-bed-image-list__card--large {
  grid-column: span 2 / span 2;
  grid-row: span 2 / span 2;
}

@media (min-width: 640px) {
  .picture-bed-image-list--masonry {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 768px) {
  .picture-bed-image-list--masonry {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (min-width: 1280px) {
  .picture-bed-image-list--masonry {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (min-width: 1536px) {
  .picture-bed-image-list--masonry {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}
</style>
