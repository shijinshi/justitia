package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.Host;

import java.util.List;

public interface HostMapper {
    int deleteByPrimaryKey(String hostName);

    int insert(Host record);

    int insertSelective(Host record);

    Host selectByPrimaryKey(String hostName);

    int updateByPrimaryKeySelective(Host record);

    int updateByPrimaryKeyWithBLOBs(Host record);

    int updateByPrimaryKey(Host record);



    List<Host> selectAllHost();
}