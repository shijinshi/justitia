package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.RegisterCode;
import org.apache.ibatis.annotations.Param;

public interface RegisterCodeMapper {
    int deleteByPrimaryKey(String code);

    int insert(RegisterCode record);

    int insertSelective(RegisterCode record);

    RegisterCode selectByPrimaryKey(String code);

    int updateByPrimaryKeySelective(RegisterCode record);

    int updateByPrimaryKey(RegisterCode record);


    void delOverdueCode(@Param("time") long time);

    String findAffiliationByCode(@Param("code") String code);
}