package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FabricCaServerMapper {
    int deleteByPrimaryKey(String serverName);

    int insert(FabricCaServer record);

    int insertSelective(FabricCaServer record);

    FabricCaServer selectByPrimaryKey(String serverName);

    int updateByPrimaryKeySelective(FabricCaServer record);

    int updateByPrimaryKeyWithBLOBs(FabricCaServer record);

    int updateByPrimaryKey(FabricCaServer record);






    int deleteServerByParent(@Param("parentServer") String parentServer);

    List<FabricCaServer> selectAllServer();

    int deleteServerContainer(String serverName);

    List<FabricCaServer> selectByType(@Param("type") String type);

    List<String> selectCAByParent(@Param("parentServer") String parent);

    int updateAffiliations(@Param("serverName") String serverName, @Param("affiliations") String affiliations);
}