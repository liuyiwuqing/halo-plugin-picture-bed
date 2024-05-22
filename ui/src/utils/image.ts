import type {Image} from "@/types";
import {useClipboard} from "@vueuse/core";
import {computed, type Ref} from "vue";
import {matchMediaType} from "@/utils/media-type";
import {Toast} from "@halo-dev/components";

export const imageTypes: string[] = [
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/gif",
    "image/webp",
    "image/svg+xml",
];

export function isImage(mediaType: string | undefined): boolean {
    if (!mediaType) {
        return false;
    }
    return imageTypes.includes(mediaType);
}

export function useAttachmentPermalinkCopy(
    image: Ref<Image | undefined>
) {
    const {copy} = useClipboard({legacy: true});

    const mediaType = computed(() => {
        return image.value?.mediaType;
    });

    const isImage = computed(() => {
        return mediaType.value && matchMediaType(mediaType.value, "image/*");
    });

    const isVideo = computed(() => {
        return mediaType.value && matchMediaType(mediaType.value, "video/*");
    });

    const isAudio = computed(() => {
        return mediaType.value && matchMediaType(mediaType.value, "audio/*");
    });

    const htmlText = computed(() => {
        const permalink = image.value?.url || '';
        const displayName = image.value?.name || '';

        if (isImage.value) {
            return `<img src="${permalink}" alt="${displayName}" />`;
        } else if (isVideo.value) {
            return `<video src="${permalink}"></video>`;
        } else if (isAudio.value) {
            return `<audio src="${permalink}"></audio>`;
        }
        return `<a href="${permalink}">${displayName}</a>`;
    });

    const markdownText = computed(() => {
        const permalink = image.value?.url || '';
        const displayName = image.value?.name || '';
        if (isImage.value) {
            return `![${displayName}](${permalink})`;
        }
        return `[${displayName}](${permalink})`;
    });

    const handleCopy = (format: "markdown" | "html" | "url") => {
        const permalink = image.value?.url || '';

        if (!permalink) return;

        if (format === "url") {
            copy(permalink);
        } else if (format === "markdown") {
            copy(markdownText.value);
        } else if (format === "html") {
            copy(htmlText.value);
        }

        Toast.success("复制成功");
    };

    return {
        htmlText,
        markdownText,
        handleCopy,
    };
}
