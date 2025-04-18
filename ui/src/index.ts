import { type AttachmentSelectProvider, definePlugin, } from "@halo-dev/console-shared"
import { markRaw } from "vue"
import "./styles/tailwind.css"
import MdiPicture360Outline from "~icons/mdi/picture-360-outline"
import PictureBeds from "@/views/PictureBeds.vue"
import LskySelectorProvider from "@/components/LskySelectorProvider.vue"
import SmmsSelectorProvider from "@/components/SmmsSelectorProvider.vue"
import ImgtpSelectorProvider from "@/components/ImgtpSelectorProvider.vue"
import { pictureBedApisClient } from "@/api"
import { consoleApiClient } from "@halo-dev/api-client"
import Pan123SelectorProvider from "@/components/Pan123SelectorProvider.vue"


function createAttachmentSelectProvider(item: any, component: any) {
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
                component: PictureBeds as any,
                meta: {
                    title: "图床管理",
                    permissions: ["plugin:picturebed:manage"],
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

            const {data: currentPermissions} = await consoleApiClient.user.getPermissions({name: "-"});

            if (currentPermissions.uiPermissions.includes("plugin:picturebed:manage")) {
                const {data: pictureBedData} = await pictureBedApisClient.pictureBed.pictureBeds();

                pictureBedData
                    .filter(item => item.enabled)  // 仅保留已启用的项
                    .forEach(item => {
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
                            case 'pan123':
                                component = Pan123SelectorProvider;
                                break;
                            default:
                                console.warn(`未找到对应组件: ${item.type}`);
                                return;
                        }
                        attachmentSelectProviders.push(createAttachmentSelectProvider(item, component));
                    });
            }

            return attachmentSelectProviders;
        },
    },
});