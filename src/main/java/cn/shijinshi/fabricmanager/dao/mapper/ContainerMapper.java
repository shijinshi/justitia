package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.Container;
import org.apache.ibatis.annotations.Param;

public interface ContainerMapper {
    int deleteByPrimaryKey(@Param("hostName") String hostName, @Param("containerId") String containerId);

    int insert(Container record);

    int insertSelective(Container record);

    Container selectByPrimaryKey(@Param("hostName") String hostName, @Param("containerId") String containerId);

    int updateByPrimaryKeySelective(Container record);

    int updateByPrimaryKeyWithBLOBs(Container record);

    int updateByPrimaryKey(Container record);



    Container selectCaContainer(String serverName);
}