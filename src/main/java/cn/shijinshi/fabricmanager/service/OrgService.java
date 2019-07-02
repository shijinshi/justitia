package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.controller.entity.organization.CreateOrgEntity;
import cn.shijinshi.fabricmanager.dao.OrganizationService;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import cn.shijinshi.fabricmanager.service.helper.TlsCertHelper;
import cn.shijinshi.fabricmanager.service.utils.file.MultipartFileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrgService {
    private static final Logger LOGGER = Logger.getLogger(OrgService.class);
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private ContextServer contextServer;

    public void createOrg(CreateOrgEntity orgConfig) {
        List<Organization> orgs = organizationService.selectAllOrg();
        if (orgs != null && !orgs.isEmpty()){
            contextServer.resetSystemConfig();
        }
        Organization org = new Organization();
        org.setOrgName(orgConfig.getOrgName());
        org.setOrgMspId(orgConfig.getOrgMspId());
        String orgType = orgConfig.getOrgType();
        if (Organization.ORG_TYPE_ORDERER.equals(orgType)) {
            org.setOrgType(Organization.ORG_TYPE_ORDERER);
        } else if (Organization.ORG_TYPE_PEER.equals(orgType)) {
            org.setOrgType(Organization.ORG_TYPE_PEER);
            String ordererIp = orgConfig.getOrdererIp();
            Integer ordererPort = orgConfig.getOrdererPort();
            if (StringUtils.isEmpty(ordererIp) || ordererPort == null || ordererPort == 0) {
                throw new ServiceException("Peer组织必须配置一个有效的Orderer节点");
            }
            org.setOrdererIp(ordererIp);
            org.setOrdererPort(ordererPort);
            if (orgConfig.getTlsEnable()) {
                MultipartFile ordererTlsCert = orgConfig.getOrdererTlsCert();
                try {
                    String tlsCertPem = new MultipartFileUtils().getFileString(ordererTlsCert);
                    new CertFileHelper().pemToX509Cert(tlsCertPem);     //尝试解析为x509证书，以验证文件是否为pem编码的x509证书
                    org.setOrdererTlsCert(tlsCertPem);
                } catch (Exception e) {
                    LOGGER.error(e);
                    throw new ServiceException("Orderer节点TLS证书文件读取失败，请长传有效的TLS证书文件", e);
                }
            }
        } else {
            throw new ServiceException("组织类型只能是ordererOrg或peerOrg，不能是"+orgType);
        }

        if (orgConfig.getTlsEnable()) {
            org.setTlsEnable(true);
            TlsCertHelper tlsCertHelper = new TlsCertHelper();
            try {
                TlsCertHelper.CertAndKeyEntity certAndKeyEntity = tlsCertHelper.generateCaCert(orgConfig.getOrgName(), null);
                org.setTlsCaCert(tlsCertHelper.certToPem(certAndKeyEntity.getCertificate()));
                org.setTlsCaKey(tlsCertHelper.privateKeyToPem(certAndKeyEntity.getPrivateKey()));
            } catch (Exception e) {
                LOGGER.warn(e);
                throw new ServiceException("创建TLS证书失败");
            }
        } else {
            org.setTlsEnable(false);
        }
        organizationService.insertSelective(org);
    }

    public Map getOrgInfo(){
        List<Organization> organizations = organizationService.selectAllOrg();
        if (organizations == null || organizations.isEmpty()) {
            return null;
        }

        Organization org = organizations.get(0);
        Map<String, Object> data = new HashMap<>();
        data.put("orgName", org.getOrgName());
        data.put("orgMspId", org.getOrgMspId());
        data.put("tlsEnable", org.getTlsEnable());
        data.put("orgType", org.getOrgType());
        return data;
    }

    public String getMspId() {
        List<Organization> organizations = organizationService.selectAllOrg();
        if (organizations == null || organizations.isEmpty()) {
            throw new ServiceException("没有配置组织信息");
        }

        return organizations.get(0).getOrgMspId();
    }

}
