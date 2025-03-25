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
  VLoading,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import {computed, ref, watch} from "vue";
import {isImage} from "@/utils/image";
import type {AttachmentLike} from "@halo-dev/console-shared";
import {matchMediaTypes} from "@/utils/media-type";
import LazyImage from "@/components/image/LazyImage.vue";
import {useQuery} from "@tanstack/vue-query";
import ImageDetailModal from "@/components/image/ImageDetailModal.vue";
import ImageUploadModal from "@/components/image/ImageUploadModal.vue";
import {pictureBedApisClient} from "@/api";
import AttachmentFileTypeIcon from "@/components/icon/AttachmentFileTypeIcon.vue";
import type {AlbumVO, ImageVO} from "@/api/generated";

const props = withDefaults(
    defineProps<{
      selected: AttachmentLike[];
      accepts?: string[];
      min?: number;
      max?: number;
      pictureBedKey: string;
    }>(),
    {
      selected: () => [],
      accepts: () => ["*/*"],
      min: undefined,
      max: undefined,
      pictureBedKey: "",
    }
);

const emit = defineEmits<{
  (event: "update:selected", attachments: AttachmentLike[]): void;
  (event: "change-provider", providerId: string): void;
}>();

const selectedImages = ref<Set<ImageVO>>(new Set());
const deletedImageIds = ref<Set<string>>(new Set());
const selectedAlbum = ref<AlbumVO>();
const selectedImage = ref<ImageVO | undefined>();
const uploadVisible = ref(false);
const detailVisible = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(40);
const keyword = ref("");
const totalLabel = ref("");
const albumListIsLoading = ref(false);
const isLoading = ref(false);

const picturebedType = computed(() => props.pictureBedKey.split("_")[0]);
const pictureBedId = computed(() => props.pictureBedKey.split("_")[1]);

const {data: albumList} = useQuery({
  queryKey: [`albumList_${picturebedType.value}`, page, size, keyword],
  queryFn: async () => {
    albumListIsLoading.value = true;
    const {data} = await pictureBedApisClient.pictureBed.albums({
      pictureBedId: pictureBedId.value,
      type: picturebedType.value,
      page: page.value,
      size: size.value,
      keyword: keyword.value,
    });
    const albums = [
      {
        id: "",
        name: "全部",
        description: "全部图片",
      },
      ...data,
    ];
    selectedAlbum.value = albums[0];
    albumListIsLoading.value = false;
    return albums;
  },
});

const {data: imageList, refetch} = useQuery({
  queryKey: [`imageList_${picturebedType.value}`, selectedAlbum, page, size, keyword],
  queryFn: async () => {
    isLoading.value = true;
    const {data} = await pictureBedApisClient.pictureBed.images({
      pictureBedId: pictureBedId.value,
      type: picturebedType.value,
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      albumId: selectedAlbum.value?.id,
    });

    totalLabel.value = `共 ${data.totalCount} 条`;
    total.value = data.totalCount as number;
    page.value = data.page as number;
    size.value = data.size as number;
    isLoading.value = false;

    return (data.list as ImageVO[]).filter((image) => !deletedImageIds.value.has(image.id as string));
  },
  enabled: computed(() => selectedAlbum.value !== undefined),
});

const handleSelectAlbum = (album: AlbumVO) => {
  selectedAlbum.value = album;
  selectedImages.value.clear();
  page.value = 1;
};

const isChecked = (image: ImageVO) => selectedImages.value.has(image);

const isDisabled = (image: ImageVO) => {
  const isMatchMediaType = matchMediaTypes(image.mediaType || "*/*", props.accepts);
  return props.max !== undefined && props.max <= selectedImages.value.size && !isChecked(image)
      ? true
      : !isMatchMediaType;
};

const deleteSelected = async () => {
  const selected = Array.from(selectedImages.value);
  selected.forEach((image) => {
    pictureBedApisClient.pictureBed.deleteImage({
      pictureBedId: pictureBedId.value,
      type: picturebedType.value,
      imageId: image.id,
    });
    deletedImageIds.value.add(image.id as string);
  });
  selectedImages.value.clear();
  await refetch();
  emit("update:selected", []);
};

const handleSelect = (image: ImageVO) => {
  if (selectedImages.value.has(image)) {
    selectedImages.value.delete(image);
  } else {
    selectedImages.value.add(image);
  }
};

const handleOpenDetail = (image: ImageVO) => {
  selectedImage.value = image;
  detailVisible.value = true;
};

watch(selectedImages, () => {
  const images = Array.from(selectedImages.value).map((image) => ({
    spec: {
      displayName: image.name,
      mediaType: image.mediaType,
      size: image.size,
    },
    status: {
      permalink: image.url,
    },
  }));
  emit("update:selected", images as AttachmentLike[]);
});

watch(keyword, () => {
  selectedImages.value.clear();
  page.value = 1;
});
</script>

<template>
  <div>
    <SearchInput placeholder="回车搜索" v-model="keyword"/>
  </div>
  <div
      v-if="!keyword"
      class="topics mt-2 flex gap-x-2 gap-y-3 overflow-y-hidden overflow-x-hidden scroll-smooth pb-1"
  >
    <VLoading v-if="albumListIsLoading"/>
    <div
        v-else
        v-for="(album, index) in albumList"
        :key="index"
        :class="{
        '!bg-gray-200 !text-gray-900': album.id === selectedAlbum?.id,
      }"
        class="flex cursor-pointer items-center rounded bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
        @click="handleSelectAlbum(album)"
    >
      <div class="flex flex-1 items-center truncate">
        <span class="truncate text-sm">
          {{ album.name }}
        </span>
      </div>
    </div>
  </div>

  <VSpace>
    <VButton @click="refetch">
      <template #icon>
        <IconRefreshLine class="h-full w-full"/>
      </template>
      刷新
    </VButton>
    <VButton @click="uploadVisible = true">
      <template #icon>
        <IconUpload class="h-full w-full"/>
      </template>
      上传
    </VButton>
    <VButton type="danger" v-if="selectedImages.size > 0" @click="deleteSelected">
      <template #icon>
        <IconDeleteBin class="h-full w-full"/>
      </template>
      删除
    </VButton>
  </VSpace>

  <VLoading v-if="isLoading"/>
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
            <IconUpload class="h-full w-full"/>
          </template>
          上传
        </VButton>
      </VSpace>
    </template>
  </VEmpty>

  <div
      v-else
      class="mt-2 grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-3 md:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10"
      role="list"
  >
    <VCard
        v-for="(image, index) in imageList"
        :key="index"
        :body-class="['!p-0']"
        :class="{
        'ring-1 ring-primary': isChecked(image),
        'pointer-events-none !cursor-not-allowed opacity-50': isDisabled(image),
      }"
        class="hover:shadow"
        @click.stop="handleSelect(image)"
    >
      <div class="group relative bg-white">
        <div class="aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100">
          <LazyImage
              v-if="isImage(image.mediaType)"
              :key="image.id"
              :alt="image.name"
              :src="image.url"
              classes="pointer-events-none object-cover group-hover:opacity-75 transform-gpu"
          >
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
          <AttachmentFileTypeIcon v-else :file-name="image.name"/>
        </div>
        <p class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700">
          {{ image.name }}
        </p>

        <div
            :class="{ '!flex': selectedImages.has(image) }"
            class="absolute left-0 top-0 hidden h-1/3 w-full justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
        >
          <IconEye
              class="mr-1 mt-1 hidden h-6 w-6 cursor-pointer text-white transition-all hover:text-primary group-hover:block"
              @click.stop="handleOpenDetail(image)"
          />
          <IconCheckboxFill
              :class="{ '!text-primary': selectedImages.has(image) }"
              class="mr-1 mt-1 h-6 w-6 cursor-pointer text-white transition-all hover:text-primary"
          />
        </div>
      </div>
    </VCard>
  </div>

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
      :mount-to-body="true"
      :imageSelected="selectedImage"
      @close="detailVisible = false"
  >
    <template #actions>
      <span v-if="selectedImage && selectedImages.has(selectedImage)" @click="handleSelect(selectedImage)">
        <IconCheckboxFill/>
      </span>
      <span v-else @click="handleSelect(selectedImage)">
        <IconCheckboxCircle/>
      </span>
    </template>
  </ImageDetailModal>

  <ImageUploadModal
      :visible="uploadVisible"
      :picBedType="picturebedType"
      :picBedId="pictureBedId"
      :albumId="selectedAlbum?.id"
      :key="selectedAlbum?.id"
      @close="uploadVisible = false; refetch();"
  />
</template>