package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.CouchdbNode;
import cn.shijinshi.fabricmanager.dao.mapper.CouchdbNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouchdbNodeService {

    @Autowired
    private CouchdbNodeMapper mapper;

    public int insertSelective(CouchdbNode couchdbNode) {
        return mapper.insertSelective(couchdbNode);
    }

    public int deleteByPrimaryKey(String couchdbName) {
        return mapper.deleteByPrimaryKey(couchdbName);
    }

    public CouchdbNode selectByPrimaryKey(String couchdbName) {
        return mapper.selectByPrimaryKey(couchdbName);
    }

    public List<CouchdbNode> selectAllCouchdb() {
        return mapper.selectAllCouchdb();
    }
}