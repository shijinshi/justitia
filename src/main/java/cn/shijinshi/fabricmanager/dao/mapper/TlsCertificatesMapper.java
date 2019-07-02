package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.TlsCertificates;

public interface TlsCertificatesMapper {
    int deleteByPrimaryKey(String serialNumber);

    int insert(TlsCertificates record);

    int insertSelective(TlsCertificates record);

    TlsCertificates selectByPrimaryKey(String serialNumber);

    int updateByPrimaryKeySelective(TlsCertificates record);

    int updateByPrimaryKeyWithBLOBs(TlsCertificates record);
}