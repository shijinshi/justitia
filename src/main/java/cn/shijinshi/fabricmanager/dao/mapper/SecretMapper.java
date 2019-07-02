package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.Secret;

public interface SecretMapper {
    int deleteByPrimaryKey(String userId);

    int insert(Secret record);

    int insertSelective(Secret record);

    Secret selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(Secret record);

    int updateByPrimaryKey(Secret record);
}