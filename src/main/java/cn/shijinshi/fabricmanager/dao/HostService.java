package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Host;
import cn.shijinshi.fabricmanager.dao.mapper.HostMapper;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostService {
    @Autowired
    private HostMapper mapper;

    public int insertHost(Host host) {
        return mapper.insertSelective(host);
    }

    public int deleteHost(String hostName) {
        return mapper.deleteByPrimaryKey(hostName);
    }

    public Host getHost(String hostName) throws NotFoundBySqlException {
        Host host = mapper.selectByPrimaryKey(hostName);
        if (host == null) {
            throw new NotFoundBySqlException("No host named " + hostName);
        }
        return host;
    }

    public List<Host> selectAllHost() {
        return mapper.selectAllHost();
    }

    public int updateHost(Host host) {
        return mapper.updateByPrimaryKeySelective(host);
    }
}
