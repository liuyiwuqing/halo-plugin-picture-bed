<script lang="ts" setup>
import {
  IconCheckboxCircle,
  IconCheckboxFill,
  IconRefreshLine,
  IconDeleteBin,
  IconEye,
  IconUpload,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import {computed, ref, watch, watchEffect} from "vue";
import {isImage} from "@/utils/image";
import type {AttachmentLike} from "@halo-dev/console-shared";
import {matchMediaTypes} from "@/utils/media-type";
import LazyImage from "@/components/image/LazyImage.vue";
import type {Album, Image, PageResult} from "@/types";
import {useQuery} from "@tanstack/vue-query";
import apiClient from "@/utils/api-client";
import ImageDetailModal from "@/components/image/ImageDetailModal.vue";
import ImageUploadModal from "@/components/image/ImageUploadModal.vue";

const props = withDefaults(
    defineProps<{
      selected: AttachmentLike[];
      accepts?: string[];
      min?: number;
      max?: number;
    }>(),
    {
      selected: () => [],
      accepts: () => ["*/*"],
      min: undefined,
      max: undefined,
    }
);

const emit = defineEmits<{
  (event: "update:selected", attachments: AttachmentLike[]): void;
  (event: "change-provider", providerId: string): void;
}>();

const selectedImages = ref<Set<Image>>(new Set());
const selectedAlbum = ref<Album>();
const selectedImage = ref<Image | undefined>();
const checkedAll = ref(false);

const uploadVisible = ref(false);
const detailVisible = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(40);
const keyword = ref("");
const totalLabel = ref("");
const isLoading = ref(false);

const picturebedType = "lsky";

// 相册列表
const {
  data: albumList,
  isLoading: albumListIsLoading,
} = useQuery({
  // queryKey: [`albumList_${picturebedType}`, page, size, keyword],
  queryKey: [page, size, keyword],
  queryFn: async () => {
    const {data} = await apiClient.get<Album[]>(
        "/apis/picturebed.muyin.site/v1alpha1/albums",
        {
          params: {
            type: picturebedType,
            page: page.value,
            size: size.value,
            keyword: keyword?.value
          },
        }
    );
    const albums = [{
      id: "",
      name: "未分组",
      description: "未分组",
    }];
    albums.push(...data);
    selectedAlbum.value = albums[0];
    return albums;
  },
});

// 图片列表
const {
  data: imageList,
  refetch,
} = useQuery({
  // queryKey: [`imageList_${picturebedType}`, selectedAlbum, page, size, keyword],
  queryKey: [selectedAlbum, page, size, keyword],
  queryFn: async () => {
    isLoading.value = true;
    const {data} = await apiClient.get<PageResult<Image>>(
        "/apis/picturebed.muyin.site/v1alpha1/images",
        {
          params: {
            type: picturebedType,
            page: page.value,
            size: size.value,
            keyword: keyword?.value,
            albumId: selectedAlbum.value?.id
          },
        }
    );

    totalLabel.value = `共 ${data.totalCount} 条`;
    total.value = data.totalCount;
    page.value = data.page;
    size.value = data.size;
    isLoading.value = false;
    return data.list;
  },
  // keepPreviousData: true,
  enabled: computed(() => selectedAlbum.value !== undefined),
});

const handleSelectAlbum = (album: Album) => {
  selectedAlbum.value = album;
  selectedImages.value = new Set();
  page.value = 1;
};

const isChecked = (image: Image) => {
  return (
      image.id === selectedImages.value?.id ||
      Array.from(selectedImages.value)
          .map((item) => item.id)
          .includes(image.id)
  );
};

const isDisabled = (image: Image) => {
  const isMatchMediaType = matchMediaTypes(
      image.mediaType || "*/*",
      props.accepts
  );

  if (
      props.max !== undefined &&
      props.max <= selectedImages.value.size &&
      !isChecked(image)
  ) {
    return true;
  }

  return !isMatchMediaType;
};

const deleteSelected = async () => {
  const selected = Array.from(selectedImages.value);
  selected.forEach((image) => {
    apiClient.get(
        "/apis/picturebed.muyin.site/v1alpha1/deleteImage",
        {
          params: {
            type: "lsky",
            imageId: image.id,
          },
        }
    );
  });
  selectedImages.value.clear();
  await refetch();
  emit("update:selected", []);
};

const handleSelect = async (image: Image | undefined) => {
  if (!image) return;
  if (selectedImages.value.has(image)) {
    selectedImages.value.delete(image);
    return;
  }
  selectedImages.value.add(image);
};

const handleOpenDetail = (image: Image) => {
  selectedImage.value = image;
  detailVisible.value = true;
};

watchEffect(() => {
  const images = Array.from(selectedImages.value).map((image) => {
    return {
      url: image.url,
      type: image.name as string,
    };
  });
  emit("update:selected", images);
});

watch(
    () => selectedImages.value.size,
    (newValue) => {
      checkedAll.value = newValue === imageList.value?.length;
    }
);
watch(
    () => keyword.value,
    () => {
      selectedImages.value = new Set();
      page.value = 1;
    }
);


const handleReset = () => {
  selectedImage.value = undefined;
  selectedImages.value.clear();
  checkedAll.value = false;
};
</script>
<template>
  <div>
    <SearchInput placeholder="回车搜索" v-model="keyword"/>
  </div>
  <div
      v-if="!keyword"
      class="picture-bed-topics picture-bed-mt-2 picture-bed-flex picture-bed-gap-x-2 picture-bed-gap-y-3 picture-bed-overflow-y-hidden picture-bed-overflow-x-hidden picture-bed-scroll-smooth picture-bed-pb-1"
  >
    <VLoading v-if="albumListIsLoading"/>
    <div v-else
         v-for="(album, index) in albumList"
         :key="index"
         :class="{
        '!picture-bed-bg-gray-200 !picture-bed-text-gray-900':
          album.id === selectedAlbum?.id,
      }"
         class="picture-bed-flex picture-bed-cursor-pointer picture-bed-items-center picture-bed-rounded picture-bed-bg-gray-100 picture-bed-p-2 picture-bed-text-gray-500 picture-bed-transition-all hover:picture-bed-bg-gray-200 hover:picture-bed-text-gray-900 hover:picture-bed-shadow-sm"
         @click="handleSelectAlbum(album)"
    >
      <div
          class="picture-bed-flex picture-bed-flex-1 picture-bed-items-center picture-bed-truncate"
      >
        <span class="picture-bed-truncate picture-bed-text-sm">
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
    <VButton type="danger" v-if="selectedImages?.size > 0" @click="deleteSelected">
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
        <VButton @click="refetch">
          刷新
        </VButton>
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
        'pointer-events-none !cursor-not-allowed opacity-50':
          isDisabled(image),
      }"
        class="hover:shadow"
        @click.stop="handleSelect(image)"
    >
      <div class="group relative bg-white">
        <div
            class="aspect-h-8 aspect-w-10 block h-full w-full cursor-pointer overflow-hidden bg-gray-100"
        >
          <LazyImage
              v-if="isImage(image.mediaType)"
              :key="image.id"
              :alt="image.name"
              :src="image.url"
              classes="pointer-events-none object-cover group-hover:opacity-75 transform-gpu"
          >
            <template #loading>
              <div class="flex h-full items-center justify-center object-cover">
                <span class="text-xs text-gray-400">
                  加载中...
                </span>
              </div>
            </template>
            <template #error>
              <div class="flex h-full items-center justify-center object-cover">
                <span class="text-xs text-red-400">
                  加载异常
                </span>
              </div>
            </template>
          </LazyImage>
          <AttachmentFileTypeIcon
              v-else
              :file-name="image.name"
          />
        </div>
        <p
            class="pointer-events-none block truncate px-2 py-1 text-center text-xs font-medium text-gray-700"
        >
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
              :class="{
              '!text-primary': selectedImages.has(image),
            }"
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
      @close="detailVisible = false;"
  >
    <template #actions>
      <span
          v-if="selectedImage && selectedImages.has(selectedImage)"
          @click="handleSelect(selectedImage)"
      >
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
      @close="uploadVisible = false; refetch();"
  />
</template>
