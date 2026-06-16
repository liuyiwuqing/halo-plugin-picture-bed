<script lang="ts" setup>
import {
  IconArrowLeft,
  IconArrowRight,
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
} from '@halo-dev/components'
import LazyImage from '@/components/image/LazyImage.vue'
import {isImage} from '@/utils/image'
import {computed} from 'vue'
import prettyBytes from 'pretty-bytes'
import ImagePermalinkList from '@/components/image/ImagePermalinkList.vue'
import type {ImageVO} from '@/api/generated'
import {getAdjacentImage, getImageIndex} from '@/components/image/image-navigation'

const props = withDefaults(
  defineProps<{
    imageSelected: ImageVO | undefined
    images?: ImageVO[]
    mountToBody?: boolean
  }>(),
  {
    imageSelected: undefined,
    images: () => [],
    mountToBody: false,
  },
)

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:imageSelected', image: ImageVO): void
}>()

const currentImage = computed(() => props.imageSelected)
const currentImageIndex = computed(() => getImageIndex(props.images, currentImage.value))
const showImageNavigation = computed(() => props.images.length > 1 && currentImageIndex.value >= 0)
const hasPreviousImage = computed(() => !!getAdjacentImage(props.images, currentImage.value, -1))
const hasNextImage = computed(() => !!getAdjacentImage(props.images, currentImage.value, 1))

function switchImage(offset: -1 | 1) {
  const image = getAdjacentImage(props.images, currentImage.value, offset)
  if (image) {
    emit('update:imageSelected', image)
  }
}
</script>
<template>
  <VModal
      :title="currentImage?.name"
    :width="1000"
    :mount-to-body="mountToBody"
    :layer-closable="true"
    height="calc(100vh - 20px)"
      :body-class="['!p-0', '!overflow-y-auto']"
    @close="emit('close')"
  >
    <template #actions>
      <template v-if="showImageNavigation">
        <span
            v-tooltip.bottom="'上一张'"
            role="button"
            aria-label="上一张"
            :aria-disabled="!hasPreviousImage"
            :class="{ 'pointer-events-none opacity-40': !hasPreviousImage }"
            @click.stop="switchImage(-1)"
        >
          <IconArrowLeft/>
        </span>
        <span
            v-tooltip.bottom="'下一张'"
            role="button"
            aria-label="下一张"
            :aria-disabled="!hasNextImage"
            :class="{ 'pointer-events-none opacity-40': !hasNextImage }"
            @click.stop="switchImage(1)"
        >
          <IconArrowRight/>
        </span>
      </template>
      <slot name="actions"></slot>
    </template>
    <div class="overflow-hidden bg-white">
      <div>
        <div class="border-b border-gray-100 bg-gray-50 px-4 py-4 sm:px-6">
          <div class="mb-3 text-sm font-medium text-gray-900">预览</div>
          <div
              class="flex min-h-[260px] items-center justify-center overflow-hidden rounded border border-gray-100 bg-white p-3 sm:min-h-[360px]"
          >
            <LazyImage
                v-if="isImage(currentImage?.mediaType)"
                :key="currentImage?.id || currentImage?.url"
                :alt="currentImage?.name || ''"
                :src="currentImage?.url || ''"
                classes="max-h-[56vh] w-auto max-w-full rounded object-contain shadow-sm"
            >
              <template #loading>
                <span class="text-gray-400"> 加载中... </span>
              </template>
              <template #error>
                <span class="text-red-400"> 加载失败 </span>
              </template>
            </LazyImage>
            <video
                v-else-if="currentImage?.mediaType?.startsWith('video/')"
                :src="currentImage?.url"
                controls
                class="max-h-[56vh] max-w-full rounded"
            >
              当前浏览器不支持该视频播放
            </video>
            <audio
                v-else-if="currentImage?.mediaType?.startsWith('audio/')"
                :src="currentImage?.url"
                controls
            >
              当前浏览器不支持该音频播放
            </audio>
            <span v-else class="text-sm text-gray-500"> 此文件不支持预览 </span>
          </div>
        </div>
        <VDescription>
          <VDescriptionItem label="文件名称" :content="currentImage?.name"/>
          <VDescriptionItem label="文件类型" :content="currentImage?.mediaType"/>
          <VDescriptionItem label="文件大小" :content="prettyBytes(currentImage?.size || 0)"/>
          <VDescriptionItem label="链接">
            <ImagePermalinkList :image="currentImage"/>
          </VDescriptionItem>
        </VDescription>
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="default" @click="emit('close')"> 关闭</VButton>
        <slot name="footer" />
      </VSpace>
    </template>
  </VModal>
</template>
