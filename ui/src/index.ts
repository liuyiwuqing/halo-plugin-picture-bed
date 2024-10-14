import {type AttachmentSelectProvider, definePlugin,} from "@halo-dev/console-shared";
import {markRaw} from "vue";
import "./styles/tailwind.css";
import MdiPicture360Outline from '~icons/mdi/picture-360-outline';
import PictureBeds from "@/views/PictureBeds.vue";
import LskySelectorProvider from "@/components/LskySelectorProvider.vue";
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue";
import ImgtpSelectorProvider from "@/components/ImgtpSelectorProvider.vue";
import {pictureBedApisClient} from "@/api";

function createAttachmentSelectProvider(item, component) {
    const newComponent = {
        ...component,
        props: {
            ...component.props,
            pictureBedKey: {
                type: String,
                default: item.key,
            }
        }
    };
    return {
        id: `${item.key}-selector`,
        label: item.name,
        component: markRaw(newComponent),
    };
}

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
            const {data} = await pictureBedApisClient.pictureBed.pictureBeds();
            data.forEach(item => {
                if (item.enabled) {
                    let component;
                    switch (item.type) {
                        case 'lsky':
                            component = LskySelectorProvider;
                            break;
                        case 'smms':
                            component = SmmsSelectorProvider;
                            break;
                        case 'imgtp':
                            component = ImgtpSelectorProvider;
                            break;
                        default:
                            console.warn(`未找到对应组件: ${item.type}`);
                            return;
                    }
                    attachmentSelectProviders.push(createAttachmentSelectProvider(item, component));
                }
            });
            return attachmentSelectProviders;
        },
    },
});