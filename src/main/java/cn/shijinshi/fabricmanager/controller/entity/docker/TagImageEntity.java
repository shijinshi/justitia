package cn.shijinshi.fabricmanager.controller.entity.docker;

import javax.validation.constraints.NotEmpty;

public class TagImageEntity {
    @NotEmpty
    private String imageId;
    @NotEmpty
    private String imageNameWithRepository;
    @NotEmpty
    private String tag;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageNameWithRepository() {
        return imageNameWithRepository;
    }

    public void setImageNameWithRepository(String imageNameWithRepository) {
        this.imageNameWithRepository = imageNameWithRepository;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
