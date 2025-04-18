<script lang="ts" setup>
import {
  IconCheckboxCircle,
  IconCheckboxFill,
  IconDeleteBin,
  IconEye,
  IconRefreshLine,
  IconUpload,
  VButton,
  VCard,
  VEmpty,
  VLoading, VSpace
} from "@halo-dev/components"
import { computed, onMounted, ref, watch } from "vue"
import { isImage } from "@/utils/image"
import type { AttachmentLike } from "@halo-dev/console-shared"
import { matchMediaTypes } from "@/utils/media-type"
import LazyImage from "@/components/image/LazyImage.vue"
import { useQuery } from "@tanstack/vue-query"
import ImageDetailModal from "@/components/image/ImageDetailModal.vue"
import ImageUploadModal from "@/components/image/ImageUploadModal.vue"
import { pictureBedApisClient } from "@/api"
import AttachmentFileTypeIcon from "@/components/icon/AttachmentFileTypeIcon.vue"
import type { AlbumVO, ImageVO } from "@/api/generated"

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
    accepts: () => ["*/*"],
    min: undefined,
    max: undefined,
    pictureBedKey: "",
  }
)

const emit = defineEmits<{
  (event: "update:selected", attachments: AttachmentLike[]): void;
  (event: "change-provider", providerId: string): void
}>()

const selectedImages = ref<Set<ImageVO>>(new Set())
const deletedImageIds = ref<Set<string>>(new Set())
const selectedAlbum = ref<AlbumVO>()
const selectedImage = ref<ImageVO | undefined>()
const uploadVisible = ref(false)
const detailVisible = ref(false)
const page = ref(1)
const size = ref(20)
const keyword = ref("")
const albumListIsLoading = ref(false)
const isLoading = ref(false)
const lastFileId = ref("")
const picturebedType = computed(() => props.pictureBedKey.split("_")[0])
const pictureBedId = computed(() => props.pictureBedKey.split("_")[1])
const albumList = ref<AlbumVO[]>([{
  id: "",
  name: "全部",
  description: "全部图片",
}])

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

    let imagesResult: ImageVO[] = []
    if (data.list) {
      data.list.forEach((image: ImageVO) => {
        if (image.mediaType == 'folder') {
          if (albumList.value.findIndex(album => album.id === image.id) === -1) {
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
  lastFileId.value = ""
  page.value = 1
}

const isChecked = (image: ImageVO) => selectedImages.value.has(image)

const isDisabled = (image: ImageVO) => {
  const isMatchMediaType = matchMediaTypes(image.mediaType || "*/*", props.accepts)
  return props.max !== undefined && props.max <= selectedImages.value.size && !isChecked(image)
    ? true
    : !isMatchMediaType
}

const deleteSelected = async () => {
  const selected = Array.from(selectedImages.value)
  selected.forEach((image) => {
    pictureBedApisClient.pictureBed.deleteImage({
      pictureBedId: pictureBedId.value,
      type: picturebedType.value,
      imageId: image.id,
    })
    deletedImageIds.value.add(image.id as string)
  })
  selectedImages.value.clear()
  await refetch()
  emit("update:selected", [])
}

const handleSelect = (image: ImageVO) => {
  if (selectedImages.value.has(image)) {
    selectedImages.value.delete(image)
  } else {
    selectedImages.value.add(image)
    console.log(selectedImages.value)
  }
  // 更新到halo选中
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
  console.log(images)
  emit("update:selected", images as AttachmentLike[])
}

const handleOpenDetail = (image: ImageVO) => {
  selectedImage.value = image
  detailVisible.value = true
}

// watch(selectedImages, () => {
//   const images = Array.from(selectedImages.value).map((image) => ({
//     spec: {
//       displayName: image.name,
//       mediaType: image.mediaType,
//       size: image.size,
//     },
//     status: {
//       permalink: image.url,
//     },
//   }))
//   console.log(images)
//   emit("update:selected", images as AttachmentLike[])
// })

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

  <div
    class="mb-5 grid grid-cols-2 gap-x-2 gap-y-3 md:grid-cols-3 lg:grid-cols-4 2xl:grid-cols-6">
    <VLoading v-if="albumListIsLoading" />
    <div
      v-else
      v-for="(album, index) in albumList"
      :key="index"
      :class="{
        '!bg-gray-100 shadow-sm': album.id === selectedAlbum?.id,
      }"
      class="inline-flex h-full w-full items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-2.5 text-sm font-medium text-gray-800 hover:bg-gray-50 hover:shadow-sm"
      @click="handleSelectAlbum(album)">
      <div class="flex flex-1 items-center truncate">
        <span class="inline-flex w-full flex-1 gap-x-2 break-all text-left">
          {{ album.name }}
        </span>
        <div class="flex-none" v-show="album.id === selectedAlbum?.id">
          <svg viewBox="0 0 24 24" width="1.2em" height="1.2em" class="text-primary">
            <path fill="currentColor"
              d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10s-4.477 10-10 10Zm0-2a8 8 0 1 0 0-16a8 8 0 0 0 0 16Zm-.997-4L6.76 11.757l1.414-1.414l2.829 2.829l5.657-5.657l1.414 1.414L11.003 16Z">
            </path>
          </svg>
        </div>
      </div>
    </div>
  </div>

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
    <VButton type="danger" v-if="selectedImages.size > 0" @click="deleteSelected">
      <template #icon>
        <IconDeleteBin class="h-full w-full" />
      </template>
      删除
    </VButton>
  </VSpace>

  <VLoading v-if="isLoading" />
  <VEmpty
    v-else-if="imageList?.length === 0"
    message="当前分组没有附件，你可以尝试刷新或者上传附件"
    title="当前分组没有附件">
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

  <div
    v-else
    class="mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10"
    role="list">
    <VCard
      v-for="(image, index) in imageList"
      :key="index"
      :body-class="['!p-0']"
      :class="{
        'ring-1 ring-primary': isChecked(image),
        'pointer-events-none !cursor-not-allowed opacity-50': isDisabled(image),
      }"
      class="hover:shadow"
      @click.stop="handleSelect(image)">
      <div class="group relative bg-white">
        <div class="aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100">
          <LazyImage
            v-if="isImage(image.mediaType)"
            :key="image.id"
            :alt="image.name"
            :src="image.url"
            classes="pointer-events-none object-cover group-hover:opacity-75 transform-gpu">
            <template #loading>
              <div class="flex h-full items-center justify-center object-cover">
                <span class="text-xs text-gray-400">加载中...</span>
              </div>
            </template>
            <template #error>
              <div class="flex h-full items-center justify-center object-cover">
                <span class="text-xs text-red-400">加载异常</span>
              </div>
            </template>
          </LazyImage>
          <AttachmentFileTypeIcon v-else :file-name="image.name" />
        </div>
        <p class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700">
          {{ image.name }}
        </p>

        <div
          :class="{ '!flex': selectedImages.has(image) }"
          class="absolute left-0 top-0 hidden h-1/3 w-full justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex">
          <IconEye
            class="mr-1 mt-1 hidden h-6 w-6 cursor-pointer text-white transition-all hover:text-primary group-hover:block"
            @click.stop="handleOpenDetail(image)" />
          <IconCheckboxFill
            :class="{ '!text-primary': selectedImages.has(image) }"
            class="mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary" />
        </div>
      </div>
    </VCard>
  </div>

  <div class="mt-4" v-if="lastFileId != '-1'">
    <VButton @click="loadMore">
      查看更多
    </VButton>
  </div>

  <ImageDetailModal
    v-model:visible="detailVisible"
    :mount-to-body="true"
    :imageSelected="selectedImage"
    @close="detailVisible = false">
    <template #actions>
      <span v-if="selectedImage && selectedImages.has(selectedImage)" @click="handleSelect(selectedImage)">
        <IconCheckboxFill />
      </span>
      <span v-else @click="handleSelect(selectedImage)">
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
    @close="uploadVisible = false; refetch();" />
</template>