package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.FabricCaUser;
import cn.shijinshi.fabricmanager.dao.entity.UserAndCerts;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FabricCaUserMapper {
    int deleteByPrimaryKey(@Param("userId") String userId, @Param("serverName") String serverName);

    int insert(FabricCaUser record);

    int insertSelective(FabricCaUser record);

    FabricCaUser selectByPrimaryKey(@Param("userId") String userId, @Param("serverName") String serverName);

    int updateByPrimaryKeySelective(FabricCaUser record);

    int updateByPrimaryKeyWithBLOBs(FabricCaUser record);

    int updateByPrimaryKey(FabricCaUser record);






    int deleteByServer(@Param("serverName") String serverName);

    List<UserAndCerts> selectByRequester(@Param("affiliation") String affiliation, @Param("affiliationLike") String affiliationLike);

    List<UserAndCerts> getUserCerts(@Param("userId") String userId, @Param("serverName") String serverName);

    List<UserAndCerts> selectOrgAdminUser();

    int updateUserState(@Param("userId") String userId, @Param("serverName") String serverName, @Param("state") String state);

    List<String> selectCaAdminUser(@Param("serverName") String serverName);

    int updateTlsCert(FabricCaUser record);
}