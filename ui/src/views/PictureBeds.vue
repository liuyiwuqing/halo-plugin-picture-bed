<script setup lang="ts">
import type {Component} from 'vue'
import {computed, ref} from 'vue'
import {VButton, VCard, VEmpty, VLoading, VPageHeader, VSpace} from '@halo-dev/components'
import MdiPicture360Outline from '~icons/mdi/picture-360-outline'
import LskySelectorProvider from '@/components/LskySelectorProvider.vue'
import SmmsSelectorProvider from '@/components/SmmsSelectorProvider.vue'
import ImgtpSelectorProvider from '@/components/ImgtpSelectorProvider.vue'
import Pan123SelectorProvider from '@/components/Pan123SelectorProvider.vue'
import {useQuery} from '@tanstack/vue-query'
import {pictureBedApisClient} from '@/api'
import type {AttachmentLike} from '@halo-dev/ui-shared'

interface PictureBed {
  label: string
  value: string
}

interface PictureBedResponse {
  name: string
  key: string
  enabled: boolean
  type: string
  editorUpload?: boolean
}

// 编辑器上传目标的互斥由后端强制，设置表单里看不出实际生效的是哪个，这里补一个可见提示
const editorUploadTargetName = ref('')

const pictureBedKey = ref('')
const isLoading = ref(false)
const selectedAttachments = ref<AttachmentLike[]>([])
const providerComponentMap: Record<string, Component> = {
  lsky: LskySelectorProvider,
  smms: SmmsSelectorProvider,
  imgtp: ImgtpSelectorProvider,
  pan123: Pan123SelectorProvider,
}
const selectedProviderType = computed(() => pictureBedKey.value.split('_')[0] ?? '')
const currentProviderComponent = computed(
    () => providerComponentMap[selectedProviderType.value] ?? null,
)

const {data: pictureBedsAvailable} = useQuery<PictureBed[]>({
  queryKey: ['pictureBeds'],
  queryFn: async () => {
    isLoading.value = true
    try {
      const { data } = await pictureBedApisClient.pictureBed.pictureBeds()
      const pictureBedsEnabled = (data as PictureBedResponse[])
          .filter((item) => item.enabled)
          .map((item) => ({
            label: item.name,
            value: item.key,
          }))

      const editorTarget = (data as PictureBedResponse[]).find(
          (item) => item.enabled && item.editorUpload,
      )
      editorUploadTargetName.value = editorTarget?.name ?? ''

      pictureBedKey.value = pictureBedsEnabled[0]?.value ?? ''
      return pictureBedsEnabled
    } finally {
      isLoading.value = false
    }
  },
})

const handleAttachmentUpdate = (attachments: AttachmentLike[]) => {
  selectedAttachments.value = attachments
}
</script>

<template>
  <VPageHeader title="图床管理">
    <template #icon>
      <MdiPicture360Outline class="mr-2 self-center" />
    </template>
    <template #actions>
      <FilterDropdown v-model="pictureBedKey" label="图床" :items="pictureBedsAvailable ?? []"/>
    </template>
  </VPageHeader>
  <div class="h-full w-full">
    <div v-if="editorUploadTargetName" class="px-4 pt-2 text-xs text-gray-500">
      当前编辑器上传目标：<span class="font-medium text-gray-700">{{ editorUploadTargetName }}</span>
      —— 在文章编辑器中粘贴或拖入图片会直接上传到该图床
    </div>
    <VLoading v-if="isLoading" />
    <VCard v-else-if="pictureBedKey">
      <component
          :is="currentProviderComponent"
          v-if="currentProviderComponent"
          :pictureBedKey="pictureBedKey"
          :selected="selectedAttachments"
          :key="pictureBedKey"
          @update:selected="handleAttachmentUpdate"
      />
      <VEmpty
        v-else
        message="当前图床类型暂未支持管理页面，请检查配置或插件版本"
        title="暂不支持的图床类型"
      >
        <template #actions>
          <VSpace>
            <VButton :route="{ path: '/plugins/PictureBed?tab=basic' }"> 检查配置</VButton>
          </VSpace>
        </template>
      </VEmpty>
    </VCard>
    <VEmpty v-else message="请选择图床" title="当前未选择图床"/>
  </div>
</template>

<style scoped></style>
