package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.RegisterCode;
import cn.shijinshi.fabricmanager.dao.mapper.RegisterCodeMapper;
import cn.shijinshi.fabricmanager.dao.entity.RegisterCode;
import cn.shijinshi.fabricmanager.dao.mapper.RegisterCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterCodeService {

    @Autowired
    private RegisterCodeMapper mapper;

    public int addCode(RegisterCode registerCode) {
        return mapper.insert(registerCode);
    }

    /**
     * 删除数据库中已经过期的注册码
     * @param expiryDate 注册码有效期，时间单位为毫秒
     */
    public void delOverdueCode(long expiryDate) {
        long time = System.currentTimeMillis() - expiryDate;
        mapper.delOverdueCode(time);
    }

    public String findAffiliationByCode(String code) {
        return mapper.findAffiliationByCode(code);
    }

    public int deleteCode(String code) {
        return mapper.deleteByPrimaryKey(code);
    }
}
