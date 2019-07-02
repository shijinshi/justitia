package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.ChannelConfigTaskResponse;
import cn.shijinshi.fabricmanager.dao.mapper.ChannelConfigTaskResponseMapper;
import cn.shijinshi.fabricmanager.service.fabric.channel.MemberManageChaincode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

@Service
public class ChannelConfigTaskResponseService {
    @Autowired
    private ChannelConfigTaskResponseMapper mapper;

    public int insertTaskResponse(MemberManageChaincode.SignResponse response) {
        ChannelConfigTaskResponse taskResponse = new ChannelConfigTaskResponse();
        taskResponse.setRequestId(response.getId());
        taskResponse.setResponder(response.getFrom());
        if ("N".equals(response.getReject())) {
            taskResponse.setReject(false);
        } else {
            taskResponse.setReject(true);
        }
        taskResponse.setReason(response.getReason());
        taskResponse.setSignature(Base64.getDecoder().decode(response.getSignature()));
        taskResponse.setResponseTime(new Date(response.getTime()));
        try {
            return mapper.insertSelective(taskResponse);
        } catch (DuplicateKeyException e) {
            return 0;
        }
    }
}
