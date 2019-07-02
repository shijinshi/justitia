package cn.shijinshi.fabricmanager.controller.entity.docker;

import javax.validation.constraints.NotEmpty;

public class AddImageEntity {
    @NotEmpty
    private String imageName;
    @NotEmpty
    private String tag;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
