package cn.shijinshi.fabricmanager.dao;

import cn.shijinshi.fabricmanager.dao.entity.Certificates;
import cn.shijinshi.fabricmanager.dao.mapper.CertificatesMapper;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateException;
import java.util.List;

@Service
public class CertificatesService {

    @Autowired
    private CertificatesMapper mapper;

    public int insertCert(Certificates certificates){
        try {
            return mapper.insertSelective(certificates);
        } catch (DuplicateKeyException e) { //主键冲突,说明这条记录在数据库中，直接跳过他
            return 1;
        }
    }

    public int deleteCertByPem(String pem) throws CertificateException {
        CertFileHelper helper = new CertFileHelper();
        String serial = helper.getSerialNumber(pem).toString();
        String aki = helper.getAuthorityKeyIdentifierString(pem);
        return deleteCertByPrimaryKey(serial, aki);
    }

    public int deleteCertByPrimaryKey(String serial, String aki) {
        return mapper.deleteByPrimaryKey(serial, aki);
    }

    public int deleteCertByCAUser(String caUserId, String serverName) {
        return mapper.deleteCertByCaUser(caUserId, serverName);
    }

    public int deleteCertByCAServer(String serverName) {
        return mapper.deleteCertByCaServer(serverName);
    }


    public List<Certificates> getCertByUser(String caUserId, String serverName) {
        List<Certificates> certificates = mapper.selectCertByUser(caUserId, serverName);
        if (certificates == null || certificates.isEmpty()) {
            return null;
        }
        return certificates;
    }


    public Certificates selectByPrimaryKey(String serialNumber, String aki) {
        return mapper.selectByPrimaryKey(serialNumber, aki);
    }
}