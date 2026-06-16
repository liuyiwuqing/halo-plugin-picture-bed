<script lang="ts" setup>
import {
  Dialog,
  IconCheckboxCircle,
  IconCheckboxFill,
  IconDeleteBin,
  Toast,
  VButton,
  VEmpty,
  VLoading,
  VSpace,
} from '@halo-dev/components'
import {computed, onMounted, ref, watch} from 'vue'
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
const allImageList = ref<ImageVO[]>([])
const selectedAlbum = ref<AlbumVO>()
const selectedImage = ref<ImageVO | undefined>()
const uploadVisible = ref(false)
const detailVisible = ref(false)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const albumListIsLoading = ref(false)
const isLoading = ref(false)
const lastFileId = ref('')
const {displayMode} = useImageListDisplayMode()
const picturebedType = computed(() => props.pictureBedKey.split('_')[0])
const pictureBedId = computed(() => props.pictureBedKey.split('_')[1])
const albumList = ref<AlbumVO[]>([
  {
    id: '',
    name: '全部',
    description: '全部图片',
  },
])

const { data: imageList, refetch } = useQuery({
  queryKey: [`imageList_${picturebedType.value}`, selectedAlbum, page, size, keyword],
  queryFn: async () => {
    isLoading.value = true
    const { data } = await pictureBedApisClient.pictureBed.images({
      pictureBedId: pictureBedId.value,
      type: picturebedType.value,
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      albumId: selectedAlbum.value?.id,
    })

    let imagesResult: ImageVO[] = allImageList.value
    if (data.list) {
      data.list.forEach((image: ImageVO) => {
        if (image.mediaType == 'folder') {
          if (albumList.value.findIndex((album) => album.id === image.id) === -1) {
            if (image.name === 'lastFileId') {
              lastFileId.value = image.id as string
            } else {
              albumList.value.push({
                id: image.id,
                name: image.name,
                description: image.name,
              })
            }
          }
        } else {
          imagesResult.push(image)
        }
      })
    }
    isLoading.value = false
    return imagesResult
  },
  enabled: computed(() => selectedAlbum.value !== undefined),
})

function loadMore() {
  keyword.value = lastFileId.value
  refetch()
}

const handleSelectAlbum = (album: AlbumVO) => {
  selectedAlbum.value = album
  selectedImages.value.clear()
  lastFileId.value = ''
  keyword.value = lastFileId.value
  allImageList.value = []
  page.value = 1
}

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

watch(lastFileId, () => {
  selectedImages.value.clear()
  page.value = 1
})

onMounted(() => {
  // 默认看全部
  selectedAlbum.value = albumList.value[0]
})
</script>

<template>
  <!-- 123盘接口不支持关键词搜索，不支持常规分页，所以隐藏搜索框，用keyword代替lastFileId -->
  <!-- <div>
    <SearchInput placeholder="回车搜索" v-model="keyword" />
  </div> -->

  <div class="mb-5 grid grid-cols-2 gap-x-2 gap-y-3 md:grid-cols-3 lg:grid-cols-4 2xl:grid-cols-6">
    <VLoading v-if="albumListIsLoading" />
    <div
      v-else
      v-for="(album, index) in albumList"
      :key="index"
      :class="{
        '!bg-gray-100 shadow-sm': album.id === selectedAlbum?.id,
      }"
      class="inline-flex h-full w-full items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-2.5 text-sm font-medium text-gray-800 hover:bg-gray-50 hover:shadow-sm"
      @click="handleSelectAlbum(album)"
    >
      <div class="flex flex-1 items-center truncate">
        <span class="inline-flex w-full flex-1 gap-x-2 break-all text-left">
          {{ album.name }}
        </span>
        <div class="flex-none" v-show="album.id === selectedAlbum?.id">
          <svg viewBox="0 0 24 24" width="1.2em" height="1.2em" class="text-primary">
            <path
                fill="currentColor"
                d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10s-4.477 10-10 10Zm0-2a8 8 0 1 0 0-16a8 8 0 0 0 0 16Zm-.997-4L6.76 11.757l1.414-1.414l2.829 2.829l5.657-5.657l1.414 1.414L11.003 16Z"
            ></path>
          </svg>
        </div>
      </div>
    </div>
  </div>
  <VSpace>
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

  <!-- <VSpace>
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
    <VButton type="danger" v-if="selectedImages.size > 0" @click="deleteSelected">
      <template #icon>
        <IconDeleteBin class="h-full w-full" />
      </template>
      删除
    </VButton>
  </VSpace> -->

  <VLoading v-if="isLoading" />
  <VEmpty
    v-else-if="imageList?.length === 0"
    message="当前分组没有附件，你可以尝试刷新或者上传附件"
    title="当前分组没有附件"
  >
    <template #actions>
      <VSpace>
        <VButton @click="refetch">刷新</VButton>
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

  <div class="mt-4" v-if="lastFileId != '-1'">
    <VButton @click="loadMore"> 查看更多</VButton>
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
      <span v-else @click="selectedImage && handleSelect(selectedImage)">
        <IconCheckboxCircle />
      </span>
    </template>
  </ImageDetailModal>

  <ImageUploadModal
    :visible="uploadVisible"
    :picBedType="picturebedType"
    :picBedId="pictureBedId"
    :albumId="selectedAlbum?.id"
    :key="selectedAlbum?.id"
    @close="handleUploadClose"
  />
</template>
