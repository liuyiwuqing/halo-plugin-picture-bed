<script setup lang="ts">
import {Toast, VModal} from "@halo-dev/components";
import type {ErrorResponse, SuccessResponse, UppyFile} from "@uppy/core";

const props = withDefaults(
    defineProps<{
      picBedType: string;
      visible: boolean;
    }>(),
    {
      picBedType: undefined,
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

const onProcessCompleted = (response: SuccessResponse) => {
  console.log("上传结果", response);
  Toast.success("上传成功！");
};

const onError = (file: UppyFile, response: ErrorResponse) => {
  const body = response.body;
  console.error(`上传失败：${file.name} ${body}`);
};

const uploadUrl = `/apis/picturebed.muyin.site/v1alpha1/uploadImage?type=${props.picBedType}`;
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
        note="仅支持图片格式"
        :endpoint="uploadUrl"
        width="100%"
        @uploaded="onProcessCompleted"
        :done-button-handler="() => onVisibleChange(false)"
        @error="onError"
    />
  </VModal>
</template>

<style scoped>

</style>