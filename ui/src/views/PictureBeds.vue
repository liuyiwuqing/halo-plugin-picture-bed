<script setup lang="ts">

import {ref} from "vue";
import {VButton, VCard, VEmpty, VLoading, VPageHeader, VSpace} from "@halo-dev/components";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import ImgtpSelectorProvider from "@/components/ImgtpSelectorProvider.vue";
import {useQuery} from "@tanstack/vue-query";
import {pictureBedApisClient} from "@/api";

const pictureBedKey = ref();
const isLoading = ref(false);

// 图床列表
const {
  data: pictureBedsAvailable
} = useQuery({
  queryKey: ['pictureBeds'],
  queryFn: async () => {
    isLoading.value = true;
    const {data} = await pictureBedApisClient.pictureBed.pictureBeds();
    const pictureBedsEnabled = [];
    data.forEach(item => {
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
    isLoading.value = false;
    return pictureBedsEnabled;
  },
  enabled: true,
});
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
          :items="pictureBedsAvailable ? pictureBedsAvailable: []"
      />
    </template>
  </VPageHeader>
  <div>
    <VLoading v-if="isLoading"/>
    <VCard v-else-if="pictureBedKey">
      <template v-if="pictureBedKey.startsWith('lsky')">
        <LskySelectorProvider :pictureBedKey="pictureBedKey" :key="pictureBedKey"/>
      </template>
      <template v-else-if="pictureBedKey.startsWith('smms')">
        <SmmsSelectorProvider :pictureBedKey="pictureBedKey" :key="pictureBedKey"/>
      </template>
      <template v-else-if="pictureBedKey.startsWith('imgtp')">
        <ImgtpSelectorProvider :pictureBedKey="pictureBedKey" :key="pictureBedKey"/>
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
    >
    </VEmpty>
  </div>

</template>

<style scoped>

</style>