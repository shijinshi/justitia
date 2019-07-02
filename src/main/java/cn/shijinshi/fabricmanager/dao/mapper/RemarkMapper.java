package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.Remark;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RemarkMapper {
    int deleteByPrimaryKey(@Param("parentUserId") String parentUserId, @Param("userId") String userId);

    int insert(Remark record);

    int insertSelective(Remark record);

    Remark selectByPrimaryKey(@Param("parentUserId") String parentUserId, @Param("userId") String userId);

    int updateByPrimaryKeySelective(Remark record);

    int updateByPrimaryKey(Remark record);



    List<Remark> getRemarksByParent(@Param("parentUserId") String parentUserId);
}