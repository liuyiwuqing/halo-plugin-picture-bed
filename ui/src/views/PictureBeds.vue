<script setup lang="ts">

import {onMounted, ref} from "vue";
import {VButton, VCard, VEmpty, VPageHeader, VSpace} from "@halo-dev/components";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SMMSSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import apiClient from "@/utils/api-client";

const pictureBedType = ref();

// 获取已对接的图床列表
const pictureBedsAvailable = ref([]);

const pictureBeds = ref({
  'lsky': {
    label: '兰空图床',
    value: 'lsky',
  },
  'smms': {
    label: 'SM.MS图床',
    value: 'smms',
  }
});

const getPictureBeds = () => {
  apiClient.get('/apis/picturebed.muyin.site/v1alpha1/pictureBeds').then(response => {
    response.data.forEach(item => {
      if (item.enabled) {
        pictureBedsAvailable.value.push(pictureBeds.value[item.type]);
      }
    });
    pictureBedType.value = pictureBedsAvailable.value[0].value;
  }).catch(error => {
    console.error(error);
  });
}

onMounted(() => {
  getPictureBeds();
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
          :items="pictureBedsAvailable"
      />
    </template>
  </VPageHeader>
  <div>
    <VCard>
      <template v-if="pictureBedType === 'lsky'">
        <LskySelectorProvider/>
      </template>
      <template v-else-if="pictureBedType === 'smms'">
        <SMMSSelectorProvider/>
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