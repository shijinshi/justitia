package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.OrdererAndContainer;
import cn.shijinshi.fabricmanager.dao.entity.OrdererInfo;
import cn.shijinshi.fabricmanager.dao.entity.OrdererNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrdererNodeMapper {
    int deleteByPrimaryKey(String ordererName);

    int insert(OrdererNode record);

    int insertSelective(OrdererNode record);

    OrdererNode selectByPrimaryKey(String ordererName);

    int updateByPrimaryKeySelective(OrdererNode record);

    int updateByPrimaryKey(OrdererNode record);







    List<OrdererAndContainer> selectAllOrderer();

    List<String> selectOrdererByCaUser(@Param("caOrdererUser") String caOrdererUser, @Param("caServerName") String caServerName);

    List<OrdererInfo> selectOrdererInfo();

}