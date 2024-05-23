import {type AttachmentSelectProvider, definePlugin,} from "@halo-dev/console-shared";
import {markRaw} from "vue";
import "./styles/tailwind.css";
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import PictureBeds from "@/views/PictureBeds.vue";

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
        "attachment:selector:create": (): AttachmentSelectProvider[] => {
            return attachmentSelectProviders;
        },
    },
});
