package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Remark;
import cn.shijinshi.fabricmanager.dao.mapper.RemarkMapper;
import cn.shijinshi.fabricmanager.dao.entity.Remark;
import cn.shijinshi.fabricmanager.dao.mapper.RemarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RemarkService {
    @Autowired
    private RemarkMapper mapper;

    public Map<String, String> getRemarksByParent(String parentUserId) {
        List<Remark> remarks = mapper.getRemarksByParent(parentUserId);
        HashMap<String, String> remarkMap = new HashMap<>();
        for (Remark remark : remarks) {
            remarkMap.put(remark.getUserId(), remark.getRemarks());
        }
        return remarkMap;
    }

    public int insertRemark(Remark remark) {
        return mapper.insert(remark);
    }

    public int updateRemark(Remark remark) {
        return mapper.updateByPrimaryKeySelective(remark);
    }
}
