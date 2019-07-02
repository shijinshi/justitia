package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.CouchdbNode;

import java.util.List;

public interface CouchdbNodeMapper {
    int deleteByPrimaryKey(String couchdbName);

    int insert(CouchdbNode record);

    int insertSelective(CouchdbNode record);

    CouchdbNode selectByPrimaryKey(String couchdbName);

    int updateByPrimaryKeySelective(CouchdbNode record);

    int updateByPrimaryKey(CouchdbNode record);

    List<CouchdbNode> selectAllCouchdb();
}