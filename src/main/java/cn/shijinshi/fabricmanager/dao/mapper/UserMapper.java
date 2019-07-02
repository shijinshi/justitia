package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(String userId);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKeyWithBLOBs(User record);

    int updateByPrimaryKey(User record);




    int selectUserCount();

    List<User> getUsersByAffiliation(@Param("affiliation") String affiliation);

    int updateToken(@Param("userId") String userId, @Param("token") String token);

    int updateUserName(@Param("userId") String userId, @Param("userName") String userName);
}