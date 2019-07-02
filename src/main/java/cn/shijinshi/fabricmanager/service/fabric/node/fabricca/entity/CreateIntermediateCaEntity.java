package cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity;

public class CreateIntermediateCaEntity extends CreateCaEntity {
    private String parentServerName;
    private String parentUserId;

    public String getParentServerName() {
        return parentServerName;
    }

    public void setParentServerName(String parentServerName) {
        this.parentServerName = parentServerName;
    }

    public String getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(String parentUserId) {
        this.parentUserId = parentUserId;
    }
}
