import {
  definePlugin,
  type AttachmentSelectProvider,
} from "@halo-dev/console-shared";
import { markRaw } from "vue";
import "./styles/tailwind.css";
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue";

const attachmentSelectProviders: AttachmentSelectProvider[] = [
  {
    id: "lsky-selector",
    label: "兰空图床",
    component: markRaw(LskySelectorProvider),
  },
  {
    id: "smms-selector",
    label: "SM.MS图床",
    component: markRaw(SmmsSelectorProvider),
  },
];


export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    "attachment:selector:create": (): AttachmentSelectProvider[] => {
      return attachmentSelectProviders;
    },
  },
});
