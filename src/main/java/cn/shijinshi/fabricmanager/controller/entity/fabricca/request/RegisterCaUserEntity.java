package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import cn.shijinshi.fabricmanager.service.ca.entity.RegisterInfo;

import javax.validation.Valid;

public class RegisterCaUserEntity {
    @Valid
    private RegisterInfo registerInfo;

    public RegisterInfo getRegisterInfo() {
        return registerInfo;
    }

    public void setRegisterInfo(RegisterInfo registerInfo) {
        this.registerInfo = registerInfo;
    }
}
