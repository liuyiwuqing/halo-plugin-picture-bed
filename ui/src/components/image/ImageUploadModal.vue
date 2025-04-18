<script setup lang="ts">
import {Toast, VModal} from "@halo-dev/components";
import type {ErrorResponse, SuccessResponse, UppyFile} from "@uppy/core";

const props = withDefaults(
    defineProps<{
      picBedType?: string;
      picBedId?: string;
      albumId?: string;
      visible?: boolean;
    }>(),
    {
      picBedType: "",
      picBedId: "",
      albumId: "",
      visible: false,
    }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const onUploaded = async (response: SuccessResponse) => {
  Toast.success("上传成功！");
};

const onError = (file: UppyFile, response: ErrorResponse) => {
  console.error("上传失败", file, response, response.body);
};

const uploadUrl = `/apis/picturebed.muyin.site/v1alpha1/uploadImage?type=${props.picBedType}&pictureBedId=${props.picBedId}&albumId=${props.albumId}`;
</script>

<template>
  <VModal
      :visible="props.visible"
      title="上传图片"
      :width="600"
      height="400"
      :layer-closable="true"
      :body-class="['!p-0']"
      @close="emit('close')"
  >
    <UppyUpload
        :restrictions="{
          allowedFileTypes: ['.jpg', '.jpeg', '.png', '.gif', '.svg', '.bmp', '.webp'],
        }"
        width="100%"
        note="仅支持图片格式"
        :endpoint="uploadUrl"
        @uploaded="onUploaded"
        @error="onError"
        :doneButtonHandler="() => onVisibleChange(false)"
    />
  </VModal>
</template>

<style scoped>

</style>