import {type AttachmentSelectProvider, definePlugin,} from "@halo-dev/console-shared";
import {markRaw, ref} from "vue";
import "./styles/tailwind.css";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import PictureBeds from "@/views/PictureBeds.vue";
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import ImgtpSelectorProvider from "@/components/ImgtpSelectorProvider.vue";
import axios from "axios";


const pictureBedSelectProviders = ref({
    'lsky': {
        id: "lsky-selector",
        label: "兰空图床",
        component: markRaw(LskySelectorProvider),
    },
    'smms': {
        id: "smms-selector",
        label: "SM.MS图床",
        component: markRaw(SmmsSelectorProvider),
    },
    'imgtp': {
        id: "imgtp-selector",
        label: "ImgTP图床",
        component: markRaw(ImgtpSelectorProvider),
    }
});

export default definePlugin({
    components: {},
    routes: [
        {
            parentName: "ToolsRoot",
            route: {
                path: "picture-bed",
                name: "PictureBed",
                component: PictureBeds,
                meta: {
                    title: "图床管理",
                    searchable: true,
                    description: "图床管理后台",
                    menu: {
                        name: "图床管理",
                        group: "tool",
                        icon: markRaw(MdiPicture360Outline),
                        priority: 0,
                    },
                },
            },
        },
    ],
    extensionPoints: {
        "attachment:selector:create": async () => {
            const attachmentSelectProviders: AttachmentSelectProvider[] = [];
            const {data} = await axios.get(
                "/apis/picturebed.muyin.site/v1alpha1/pictureBeds"
            );
            data.forEach(item => {
                if (item.enabled) {
                    attachmentSelectProviders.push(pictureBedSelectProviders.value[item.type]);
                }
            });
            return attachmentSelectProviders;
        },
    },
});
