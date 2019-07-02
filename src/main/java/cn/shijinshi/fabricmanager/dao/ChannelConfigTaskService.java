package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.entity.ChannelConfigTask;
import cn.shijinshi.fabricmanager.dao.mapper.ChannelConfigTaskMapper;
import cn.shijinshi.fabricmanager.service.fabric.channel.MemberManageChaincode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class ChannelConfigTaskService {
    @Autowired
    private ChannelConfigTaskMapper mapper;

    public ChannelConfigTask getChannelConfigTask(String requestId) {
        return mapper.selectByPrimaryKey(requestId);
    }

    public int deleteChannelConfigTask(String requestId) {
        return mapper.deleteByPrimaryKey(requestId);
    }

    public List<ChannelConfigTask> selectChannelConfigTask() {
        return mapper.selectChannelConfigTask();
    }

    public List<ChannelConfigTask> selectMySigningTaskByChannel(String channelId) {
        String requester = Context.getOrganization().getOrgMspId();
        return mapper.selectMySigningTaskByChannel(requester, channelId);
    }

    public int updateStatus(String requestId, String status) {
        return mapper.updateStatus(requestId, status);
    }

    public int updateResponse(String requestId, boolean reject, String reason) {
        return mapper.updateResponse(requestId, reject, reason, new Date());
    }

    public int insertChannelConfigTask(MemberManageChaincode.SignRequest signRequest, String channelId) {
        ChannelConfigTask task = new ChannelConfigTask();
        task.setRequestId(signRequest.getId());
        task.setChannelId(channelId);
        task.setRequester(signRequest.getFrom());
        task.setContent(Base64.getDecoder().decode(signRequest.getContent()));
        task.setDescription(signRequest.getDesc());
        task.setChannelConfigVersion(signRequest.getVersion());
        task.setStatus(signRequest.getStatus());
        task.setRequestTime(new Date(signRequest.getTime()));
        task.setRequestType(signRequest.getType());
        task.setExpectedEndorsement(StringUtils.join(signRequest.getMsp(), ","));
        try {
            return mapper.insertSelective(task);
        }catch (DuplicateKeyException e) {
            return 0;
        }
    }

}