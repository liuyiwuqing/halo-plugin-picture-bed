<script lang="ts" setup>
import {
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
} from "@halo-dev/components";
import LazyImage from "@/components/image/LazyImage.vue";
import type {Image} from "@/types";
import {isImage} from "@/utils/image";
import {ref} from "vue";
import prettyBytes from "pretty-bytes";
import ImagePermalinkList from "@/components/image/ImagePermalinkList.vue";

const props = withDefaults(
  defineProps<{
    imageSelected: Image | undefined;
    mountToBody?: boolean;
  }>(),
  {
    imageSelected: undefined,
    mountToBody: false,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();
const onlyPreview = ref(false);

const msg = ref("点击退出预览");

</script>
<template>
  <VModal
    :title="imageSelected?.name"
    :width="1000"
    :mount-to-body="mountToBody"
    :layer-closable="true"
    height="calc(100vh - 20px)"
    :body-class="['!p-0']"
    @close="emit('close')"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>
    <div class="overflow-hidden bg-white">
      <div
        v-if="onlyPreview && isImage(imageSelected?.mediaType)"
        class="flex justify-center p-4"
      >
        <img
          v-tooltip.bottom="msg"
          :alt="imageSelected?.name"
          :src="imageSelected?.url"
          class="w-auto transform-gpu cursor-pointer rounded"
          @click="onlyPreview = !onlyPreview"
        />
      </div>
      <div v-else>
        <VDescription>
          <VDescriptionItem label="预览"
          >
            <div
              v-if="isImage(imageSelected?.mediaType)"
              @click="onlyPreview = !onlyPreview"
            >
              <LazyImage
                :alt="imageSelected?.name"
                :src="imageSelected?.url"
                classes="max-w-full cursor-pointer rounded sm:max-w-[50%]"
              >
                <template #loading>
                  <span class="text-gray-400">
                    加载中...
                  </span>
                </template>
                <template #error>
                  <span class="text-red-400">
                    加载失败
                  </span>
                </template>
              </LazyImage>
            </div>
            <div v-else-if="imageSelected?.mediaType?.startsWith('video/')">
              <video
                :src="imageSelected?.url"
                controls
                class="max-w-full rounded sm:max-w-[50%]"
              >
                当前浏览器不支持该视频播放
              </video>
            </div>
            <div v-else-if="imageSelected?.mediaType?.startsWith('audio/')">
              <audio :src="imageSelected?.url" controls>
                当前浏览器不支持该音频播放
              </audio>
            </div>
            <span v-else>
              此文件不支持预览
            </span>
          </VDescriptionItem>
          <VDescriptionItem
              label="文件名称"
              :content="imageSelected?.name"
          />
          <VDescriptionItem
              label="文件类型"
              :content="imageSelected?.mediaType"
          />
          <VDescriptionItem
            label="文件大小"
            :content="prettyBytes(imageSelected?.size || 0)"
          />
          <VDescriptionItem
            label="链接"
          >
            <ImagePermalinkList :image="imageSelected" />
          </VDescriptionItem>
        </VDescription>
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="default" @click="emit('close')">
          关闭
        </VButton>
        <slot name="footer" />
      </VSpace>
    </template>
  </VModal>
</template>
