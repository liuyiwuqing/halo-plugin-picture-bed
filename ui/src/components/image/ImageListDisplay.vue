<script lang="ts" setup>
import {computed} from 'vue'
import {IconCheckboxFill, IconEye, VCard} from '@halo-dev/components'
import type {ImageVO} from '@/api/generated'
import {isImage} from '@/utils/image'
import AttachmentFileTypeIcon from '@/components/icon/AttachmentFileTypeIcon.vue'
import LazyImage from '@/components/image/LazyImage.vue'
import {getImageListDisplayClasses, type ImageListDisplayMode} from './image-list-display'

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
const cardBodyClass = computed(() => [
  '!p-0',
  'picture-bed-image-list__card-body',
  ...(props.mode === 'masonry' ? ['picture-bed-image-list__card-body--masonry'] : []),
])

const cardClass = (image: ImageVO) => [
  props.mode === 'grid' ? 'hover:shadow' : '',
  displayClasses.value.card,
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
        :body-class="cardBodyClass"
        :class="cardClass(image)"
        role="listitem"
        @click.stop="emit('select', image)"
    >
      <div :class="displayClasses.item">
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
        <p :class="displayClasses.caption">
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
  column-count: 2;
  column-gap: 0.375rem;
}

:deep(.picture-bed-image-list__card-body) {
  height: 100%;
  min-height: 0;
}

.picture-bed-image-list__card--masonry {
  display: inline-block;
  width: 100%;
  margin: 0 0 0.375rem;
  break-inside: avoid;
  -webkit-column-break-inside: avoid;
  border: 0;
  border-radius: 0.25rem;
  background: transparent;
  box-shadow: none;
  vertical-align: top;
}

.picture-bed-image-list__card--masonry:hover {
  box-shadow: none;
}

.picture-bed-image-list__card--masonry :deep(.picture-bed-image-list__card-body--masonry) {
  height: auto;
}

.picture-bed-image-list__frame--masonry {
  line-height: 0;
}

@media (min-width: 640px) {
  .picture-bed-image-list--masonry {
    column-count: 2;
  }
}

@media (min-width: 768px) {
  .picture-bed-image-list--masonry {
    column-count: 3;
  }
}

@media (min-width: 1536px) {
  .picture-bed-image-list--masonry {
    column-count: 4;
  }
}

@media (min-width: 1920px) {
  .picture-bed-image-list--masonry {
    column-count: 5;
  }
}
</style>
