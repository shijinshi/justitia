package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.OrdererAndContainer;
import cn.shijinshi.fabricmanager.dao.entity.OrdererInfo;
import cn.shijinshi.fabricmanager.dao.entity.OrdererNode;
import cn.shijinshi.fabricmanager.dao.mapper.OrdererNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdererNodeService {
    @Autowired
    private OrdererNodeMapper mapper;

    public OrdererNode getOrderer(String ordererName){
        return mapper.selectByPrimaryKey(ordererName);
    }

    public int deleteByPrimaryKey(String ordererName) {
        return mapper.deleteByPrimaryKey(ordererName);
    }

    public List<OrdererAndContainer> selectAllOrderer(){
        return mapper.selectAllOrderer();
    }

    public int insertSelective(OrdererNode record) {
        return mapper.insertSelective(record);
    }

    public List<String> selectOrdererByCaUser(String caUser, String caServerName) {
        return mapper.selectOrdererByCaUser(caUser, caServerName);
    }

    public OrdererInfo getOrdererInfo() {
        List<OrdererInfo> ordererInfos = mapper.selectOrdererInfo();
        if (ordererInfos != null && !ordererInfos.isEmpty()) {
            return ordererInfos.get(0);
        } else {
            return null;
        }
    }

    public List<OrdererInfo> selectOrdererInfo() {
        return mapper.selectOrdererInfo();
    }


}