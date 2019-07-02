package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import javax.validation.constraints.NotEmpty;

public class RevokeUserEntity extends RevokeEntity {

    @NotEmpty
    private String revokee;     //who is to be revoked.
//    @NotNull
//    private Boolean checked;

    public String getRevokee() {
        return revokee;
    }

    public void setRevokee(String revokee) {
        this.revokee = revokee;
    }

//    public Boolean getChecked() {
//        return checked;
//    }
//
//    public void setChecked(Boolean checked) {
//        this.checked = checked;
//    }
}

