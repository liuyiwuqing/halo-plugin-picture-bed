<script setup lang="ts">

import {ref} from "vue";
import {VButton, VCard, VEmpty, VLoading, VPageHeader, VSpace} from "@halo-dev/components";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SMMSSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import ImgTPSelectorProvider from "@/components/ImgTPSelectorProvider.vue";
import apiClient from "@/utils/api-client";
import {useQuery} from "@tanstack/vue-query";

const pictureBedType = ref();
const isLoading = ref(false);

const pictureBeds = ref({
  'lsky': {
    label: '兰空图床',
    value: 'lsky',
  },
  'smms': {
    label: 'SM.MS图床',
    value: 'smms',
  },
  'imgtp': {
    label: 'ImgTP图床',
    value: 'imgtp',
  }
});

// 图床列表
const {
  data: pictureBedsAvailable
} = useQuery({
  queryKey: [],
  queryFn: async () => {
    isLoading.value = true;
    const {data} = await apiClient.get(
        "/apis/picturebed.muyin.site/v1alpha1/pictureBeds"
    );
    const pictureBedsEnabled = [];
    data.forEach(item => {
      if (item.enabled) {
        pictureBedsEnabled.push(pictureBeds.value[item.type]);
      }
    });
    if (pictureBedsEnabled.length > 0) {
      pictureBedType.value = pictureBedsEnabled[0].value;
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
          v-model="pictureBedType"
          label="图床"
          :items="pictureBedsAvailable ?pictureBedsAvailable: []"
      />
    </template>
  </VPageHeader>
  <div>
    <VLoading v-if="isLoading"/>
    <VCard v-else>
      <template v-if="pictureBedType === 'lsky'">
        <LskySelectorProvider/>
      </template>
      <template v-else-if="pictureBedType === 'smms'">
        <SMMSSelectorProvider/>
      </template>
      <template v-else-if="pictureBedType === 'imgtp'">
        <ImgTPSelectorProvider/>
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
  </div>

</template>

<style scoped>

</style>