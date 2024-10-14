import {axiosInstance} from "@halo-dev/api-client";
import {PicturebedMuyinSiteV1alpha1PictureBedApi,} from "./generated";

const pictureBedApisClient = {
    pictureBed: new PicturebedMuyinSiteV1alpha1PictureBedApi(undefined, "", axiosInstance),
};

export {pictureBedApisClient};
