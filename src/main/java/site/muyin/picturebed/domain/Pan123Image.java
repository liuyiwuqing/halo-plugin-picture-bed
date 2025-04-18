package site.muyin.picturebed.domain;

import lombok.Data;
@Data
public class Pan123Image {
    private String fileId;
    private String filename;
    private String parentFileId;
    private Integer type;
    private String etag;
    private Float size;
    private Integer status;
    private String s3KeyFlag;
    private String storageNode;
    private String createAt;
    private String updateAt;
    private String downloadURL;
    private Integer ossIndex;
    private Integer totalTraffic;
    private String parentFilename;
    private String extension;
    private String userSelfURL;
}