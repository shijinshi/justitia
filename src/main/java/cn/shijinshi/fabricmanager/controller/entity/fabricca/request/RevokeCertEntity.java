package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import javax.validation.constraints.NotEmpty;

public class RevokeCertEntity extends RevokeEntity {
    @NotEmpty
    private String serial;
    @NotEmpty
    private String aki;


    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAki() {
        return aki;
    }

    public void setAki(String aki) {
        this.aki = aki;
    }
}
