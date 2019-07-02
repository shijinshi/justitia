package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Secret;
import cn.shijinshi.fabricmanager.dao.mapper.SecretMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class SecretService {
    @Autowired
    private SecretMapper mapper;

    public Secret getSecret(String userId) {
        return mapper.selectByPrimaryKey(userId);
    }

    public int addSecret(Secret secret) throws DuplicateKeyException {
        return mapper.insert(secret);
    }

    public int deleteSecret(String userId) {
        return mapper.deleteByPrimaryKey(userId);
    }
}
