<script setup lang="ts">

import {ref} from "vue";
import {VButton, VCard, VEmpty, VLoading, VPageHeader, VSpace} from "@halo-dev/components";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import ImgtpSelectorProvider from "@/components/ImgtpSelectorProvider.vue";
import {useQuery} from "@tanstack/vue-query";
import {pictureBedApisClient} from "@/api";
import type {AttachmentLike} from "@halo-dev/console-shared";

interface PictureBed {
  label: string;
  value: string;
}

interface PictureBedResponse {
  name: string;
  key: string;
  enabled: boolean;
}

const pictureBedKey = ref<string>('');
const isLoading = ref(false);
const selectedAttachments = ref<AttachmentLike[]>([]);

// 图床列表
const {
  data: pictureBedsAvailable
} = useQuery<PictureBed[]>({
  queryKey: ['pictureBeds'],
  queryFn: async () => {
    isLoading.value = true;
    try {
      const {data} = await pictureBedApisClient.pictureBed.pictureBeds();
      const pictureBedsEnabled: PictureBed[] = [];

      (data as PictureBedResponse[]).forEach(item => {
        if (item.enabled) {
          pictureBedsEnabled.push({
            label: item.name,
            value: item.key
          });
        }
      });

      if (pictureBedsEnabled.length > 0) {
        pictureBedKey.value = pictureBedsEnabled[0].value;
      }

      return pictureBedsEnabled;
    } finally {
      isLoading.value = false;
    }
  },
  enabled: true,
});

const handleAttachmentUpdate = (attachments: AttachmentLike[]) => {
  selectedAttachments.value = attachments;
};
</script>

<template>
  <VPageHeader title="图床管理">
    <template #icon>
      <MdiPicture360Outline class="mr-2 self-center"/>
    </template>
    <template #actions>
      <FilterDropdown
          v-model="pictureBedKey"
          label="图床"
          :items="pictureBedsAvailable ?? []"
      />
    </template>
  </VPageHeader>
  <div class="w-full h-full">
    <VLoading v-if="isLoading"/>
    <VCard v-else-if="pictureBedKey">
      <template v-if="pictureBedKey.startsWith('lsky')">
        <LskySelectorProvider
            :pictureBedKey="pictureBedKey"
            :selected="selectedAttachments"
            @update:selected="handleAttachmentUpdate"
            :key="pictureBedKey"
        />
      </template>
      <template v-else-if="pictureBedKey.startsWith('smms')">
        <SmmsSelectorProvider
            :pictureBedKey="pictureBedKey"
            :selected="selectedAttachments"
            @update:selected="handleAttachmentUpdate"
            :key="pictureBedKey"
        />
      </template>
      <template v-else-if="pictureBedKey.startsWith('imgtp')">
        <ImgtpSelectorProvider
            :pictureBedKey="pictureBedKey"
            :selected="selectedAttachments"
            @update:selected="handleAttachmentUpdate"
            :key="pictureBedKey"
        />
      </template>
      <VEmpty
          v-else
          message="当前没有可用图床，请前往配置"
          title="当前没有可用图床"
      >
        <template #actions>
          <VSpace>
            <VButton :route="{ path: '/plugins/PictureBed?tab=basic' }">
              配置
            </VButton>
          </VSpace>
        </template>
      </VEmpty>
    </VCard>
    <VEmpty
        v-else
        message="请选择图床"
        title="当前未选择图床"
    />
  </div>
</template>

<style scoped>
</style>