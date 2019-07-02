package cn.shijinshi.fabricmanager.controller.entity.docker;

import javax.validation.constraints.NotEmpty;

public class CreateVolumeEntity {
    @NotEmpty
    private String volumeName;

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }
}
