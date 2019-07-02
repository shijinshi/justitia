package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.OrgRefChannel;
import cn.shijinshi.fabricmanager.dao.mapper.OrgRefChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgRefChannelService {

    @Autowired
    private OrgRefChannelMapper mapper;

    public int insertSelective(OrgRefChannel orgRefChannel) {
        try {
            return mapper.insertSelective(orgRefChannel);
        } catch (DuplicateKeyException e) {
            return 0;
        }
    }

    public List<OrgRefChannel> selectAllOrgRefChannel() {
        return mapper.selectAllOrgRefChannel();
    }

    public List<OrgRefChannel> selectOrgRefChannelByChannel(String channelName) {
        return mapper.selectOrgRefChannelByChannel(channelName);
    }

    public int deleteOrgRefChannelByOrg(String orgName) {
        return mapper.deleteOrgRefChannelByOrg(orgName);
    }

    public int deleteOrgRefChannelByChannel(String channelName) {
        return mapper.deleteOrgRefChannelByChannel(channelName);
    }
}
