<script lang="ts" setup>
import {
  Dialog,
  IconCheckboxCircle,
  IconCheckboxFill,
  IconDeleteBin,
  IconRefreshLine,
  IconUpload,
  Toast,
  VButton,
  VEmpty,
  VLoading,
  VPagination,
  VSpace,
} from '@halo-dev/components'
import {computed, ref, watch} from 'vue'
import type {AttachmentLike} from '@halo-dev/ui-shared'
import {matchMediaTypes} from '@/utils/media-type'
import {useQuery} from '@tanstack/vue-query'
import ImageDetailModal from '@/components/image/ImageDetailModal.vue'
import ImageUploadModal from '@/components/image/ImageUploadModal.vue'
import {pictureBedApisClient} from '@/api'
import type {AlbumVO, ImageVO} from '@/api/generated'
import ImageListDisplay from '@/components/image/ImageListDisplay.vue'
import ImageListDisplayModeSwitch from '@/components/image/ImageListDisplayModeSwitch.vue'
import {useImageListDisplayMode} from '@/components/image/use-image-list-display-mode'

const props = withDefaults(
  defineProps<{
    selected: AttachmentLike[]
    accepts?: string[]
    min?: number
    max?: number
    pictureBedKey: string
  }>(),
  {
    selected: () => [],
    accepts: () => ['*/*'],
    min: undefined,
    max: undefined,
    pictureBedKey: '',
  },
)

const emit = defineEmits<{
  (event: 'update:selected', attachments: AttachmentLike[]): void
  (event: 'change-provider', providerId: string): void
}>()

const selectedImages = ref<Set<ImageVO>>(new Set())
const deletedImageIds = ref<Set<string>>(new Set())
const selectedAlbum = ref<AlbumVO>()
const selectedImage = ref<ImageVO | undefined>()
const uploadVisible = ref(false)
const detailVisible = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(40)
const keyword = ref('')
const totalLabel = ref('')
const isLoading = ref(false)
const {displayMode} = useImageListDisplayMode()

const picturebedType = computed(() => props.pictureBedKey.split('_')[0])
const pictureBedId = computed(() => props.pictureBedKey.split('_')[1])

const { data: imageList, refetch } = useQuery({
  queryKey: [`imageList_${picturebedType.value}`, selectedAlbum, page, size, keyword],
  queryFn: async () => {
    isLoading.value = true
    const { data } = await pictureBedApisClient.pictureBed.images({
      pictureBedId: pictureBedId.value,
      type: picturebedType.value,
      page: page.value,
      size: size.value,
    })

    totalLabel.value = `共 ${data.totalCount} 条`
    total.value = (data.size as number) * (data.totalPages as number)
    page.value = data.page as number
    size.value = data.size as number
    isLoading.value = false

    return (data.list as ImageVO[]).filter(
        (image) => !deletedImageIds.value.has(image.id as string),
    )
  },
  enabled: true,
})

const isChecked = (image: ImageVO) => selectedImages.value.has(image)

const isDisabled = (image: ImageVO) => {
  const isMatchMediaType = matchMediaTypes(image.mediaType || '*/*', props.accepts)
  return props.max !== undefined && props.max <= selectedImages.value.size && !isChecked(image)
    ? true
    : !isMatchMediaType
}

const deleteSelected = async () => {
  const selected = Array.from(selectedImages.value)
  Dialog.warning({
    title: '确认删除',
    description: `确定要删除选中的 ${selected.length} 张图片吗?此操作不可恢复。`,
    confirmText: '确定',
    cancelText: '取消',
    onConfirm: async () => {
      const deleteResults = await Promise.allSettled(
          selected.map((image) =>
              pictureBedApisClient.pictureBed
                  .deleteImage({
                    pictureBedId: pictureBedId.value,
                    type: picturebedType.value,
                    imageId: image.id,
                  })
                  .then(({data}) => ({
                    imageId: image.id as string,
                    success: Boolean(data),
                  })),
          ),
      )
      let failedCount = 0

      deleteResults.forEach((result) => {
        if (result.status === 'fulfilled' && result.value.success) {
          deletedImageIds.value.add(result.value.imageId)
          return
        }
        failedCount += 1
      })
      selectedImages.value.clear()
      await refetch()
      emit('update:selected', [])

      if (failedCount === 0) {
        Toast.success(`已删除 ${selected.length} 张图片`)
      } else {
        Toast.warning(`已删除 ${selected.length - failedCount} 张图片，${failedCount} 张删除失败`)
      }
    },
  })
}

const handleSelect = (image: ImageVO) => {
  if (selectedImages.value.has(image)) {
    selectedImages.value.delete(image)
  } else {
    selectedImages.value.add(image)
  }
}

const handleSelectAll = () => {
  if (!imageList.value) return

  const allSelected = imageList.value.every((image) => selectedImages.value.has(image))

  if (allSelected) {
    // 如果全部已选中，则取消全选
    imageList.value.forEach((image) => {
      if (selectedImages.value.has(image)) {
        selectedImages.value.delete(image)
      }
    })
  } else {
    // 否则全选所有可选的图片
    imageList.value.forEach((image) => {
      if (!isDisabled(image)) {
        selectedImages.value.add(image)
      }
    })
  }
}

const isAllSelected = computed(() => {
  if (!imageList.value || imageList.value.length === 0) return false
  return imageList.value.every((image) => selectedImages.value.has(image))
})

const handleOpenDetail = (image: ImageVO) => {
  selectedImage.value = image
  detailVisible.value = true
}

const handleUploadClose = () => {
  uploadVisible.value = false
  refetch()
}

// `watch` 不起作用可能是因为 `selectedImages` 是一个 `ref` 包装的 `Set`，`Set` 是引用类型，
// 直接修改 `Set` 内部元素不会触发响应式更新，使用 `watch` 的 `deep` 选项来深度监听
watch(
    selectedImages,
    () => {
      const images = Array.from(selectedImages.value).map((image) => ({
        spec: {
          displayName: image.name,
          mediaType: image.mediaType,
          size: image.size,
        },
        status: {
          permalink: image.url,
        },
      }))
      emit('update:selected', images as AttachmentLike[])
    },
    {deep: true},
)

watch(
    keyword,
    () => {
      selectedImages.value.clear()
      page.value = 1
    },
    {deep: true},
)
</script>

<template>
  <VSpace>
    <VButton @click="refetch">
      <template #icon>
        <IconRefreshLine class="h-full w-full" />
      </template>
      刷新
    </VButton>
    <VButton @click="uploadVisible = true">
      <template #icon>
        <IconUpload class="h-full w-full" />
      </template>
      上传
    </VButton>
    <VButton v-if="props.max !== 1" @click="handleSelectAll">
      <template #icon>
        <IconCheckboxCircle class="h-full w-full" />
      </template>
      {{ isAllSelected ? '取消全选' : '全选' }}
    </VButton>
    <VButton type="danger" v-if="selectedImages.size > 0" @click="deleteSelected">
      <template #icon>
        <IconDeleteBin class="h-full w-full" />
      </template>
      删除
    </VButton>
    <ImageListDisplayModeSwitch v-model="displayMode"/>
  </VSpace>

  <VLoading v-if="isLoading" />
  <VEmpty
    v-else-if="imageList?.length === 0"
    message="当前分组没有附件，你可以尝试刷新或者上传附件"
    title="当前分组没有附件"
  >
    <template #actions>
      <VSpace>
        <VButton @click="refetch">刷新</VButton>
        <VButton type="secondary" @click="uploadVisible = true">
          <template #icon>
            <IconUpload class="h-full w-full" />
          </template>
          上传
        </VButton>
      </VSpace>
    </template>
  </VEmpty>

  <ImageListDisplay
    v-else
    :images="imageList || []"
    :mode="displayMode"
    :is-checked="isChecked"
    :is-disabled="isDisabled"
    @select="handleSelect"
    @open-detail="handleOpenDetail"
  />

  <div class="mt-4">
    <VPagination
      v-model:page="page"
      v-model:size="size"
      page-label="页"
      size-label="条 / 页"
      :total-label="totalLabel"
      :total="total"
      :size-options="[40, 80, 120]"
    />
  </div>

  <ImageDetailModal
    v-model:visible="detailVisible"
    v-model:image-selected="selectedImage"
    :mount-to-body="true"
    :images="imageList || []"
    @close="detailVisible = false"
  >
    <template #actions>
      <span
          v-if="selectedImage && selectedImages.has(selectedImage)"
          @click="handleSelect(selectedImage)"
      >
        <IconCheckboxFill />
      </span>
      <span v-else @click="handleSelect(selectedImage as ImageVO)">
        <IconCheckboxCircle />
      </span>
    </template>
  </ImageDetailModal>

  <ImageUploadModal
    :visible="uploadVisible"
    :picBedType="picturebedType"
    :picBedId="pictureBedId"
    @close="handleUploadClose"
  />
</template>
