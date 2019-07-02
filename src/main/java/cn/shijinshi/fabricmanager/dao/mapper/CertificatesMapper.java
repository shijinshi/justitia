package cn.shijinshi.fabricmanager.dao.mapper;

import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CertificatesMapper {
    int deleteByPrimaryKey(@Param("serialNumber") String serialNumber, @Param("authorityKeyIdentifier") String authorityKeyIdentifier);

    int insert(Certificates record);

    int insertSelective(Certificates record);

    Certificates selectByPrimaryKey(@Param("serialNumber") String serialNumber, @Param("authorityKeyIdentifier") String authorityKeyIdentifier);

    int updateByPrimaryKeySelective(Certificates record);

    int updateByPrimaryKeyWithBLOBs(Certificates record);

    int updateByPrimaryKey(Certificates record);









    int deleteCertByCaUser(@Param("caUserId") String caUserId, @Param("serverName") String serverName);

    int deleteCertByCaServer(@Param("serverName") String serverName);

    List<Certificates> selectCertByUser(@Param("caUserId") String caUserId, @Param("serverName") String serverName);
}