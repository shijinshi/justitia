package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.controller.entity.host.AddHostEntity;
import cn.shijinshi.fabricmanager.controller.entity.host.UpdateHostEntity;
import cn.shijinshi.fabricmanager.dao.HostService;
import cn.shijinshi.fabricmanager.dao.entity.Host;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.utils.file.exception.FileServerException;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.MultipartFileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class HostManageService {

    @Autowired
    private HostService hostService;
    @Autowired
    private DockerService dockerService;

    /**
     * 增加一个宿主机
     * @param hostConfig
     * @throws IOException
     * @throws FileServerException
     */
    public void addHost(AddHostEntity hostConfig) throws IOException, FileServerException {
        Host host = new Host();
        //保存证书文件
        if (hostConfig.getTlsEnable()) {
            String certPath = saveCert(hostConfig.getHostName(), hostConfig.getCa(), hostConfig.getCert(), hostConfig.getKey());
            host.setCertPath(certPath);
        }

		String hostName = hostConfig.getHostName();
        host.setHostName(hostName);
        host.setProtocol(hostConfig.getProtocol());
        host.setIp(hostConfig.getIp());
        host.setPort(hostConfig.getPort());
        host.setTlsEnable(hostConfig.getTlsEnable());
        if (!dockerService.testLink(host)) {
            throw new ServiceException("新增主机失败,主机配置不正确");
        }
        //保存配置到数据库
        hostService.insertHost(host);
    }

    /**
     * 保存docker tls证书
     * @param hostName
     * @param ca
     * @param cert
     * @param key
     * @return
     * @throws IOException
     * @throws FileServerException
     */
    private String saveCert(String hostName, MultipartFile ca, MultipartFile cert, MultipartFile key) throws IOException, FileServerException {
        if (hostName == null || hostName.isEmpty()) {
            throw new NullPointerException("Host name is null.");
        }

        if (ca == null || cert == null || key == null) {
            throw new ServiceException("Enabled Tls verification but certificate is missing.");
        }

        String certPath = ExternalResources.getDockerCerts(hostName);
        MultipartFileUtils fileUtils = new MultipartFileUtils();
        fileUtils.makeDir(certPath);
        fileUtils.saveMultiFile(certPath, ca, "ca.pem");
        fileUtils.saveMultiFile(certPath, cert, "cert.pem");
        fileUtils.saveMultiFile(certPath, key, "key.pem");
        return certPath;
    }

    /**
     * 删除一个host信息
     */
    public void deleteHost(String hostName) {
        Host host;
        try {
            host = hostService.getHost(hostName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("系统中不存在名称为" + hostName +"的主机");
        }

        hostService.deleteHost(hostName);
        if (host.getTlsEnable() && StringUtils.isNotEmpty(host.getCertPath())) {
            FileUtils.delete(host.getCertPath());
        }
    }

    /**
     * 更新host信息
     * @param hostName
     * @param hostConfig
     * @throws IOException
     * @throws FileServerException
     */
    @Transactional
    public void updateHost(String hostName, UpdateHostEntity hostConfig) throws IOException, FileServerException {
        Host host;
        try {
            host = hostService.getHost(hostName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名称为" + hostName + "的主机");
        }

        Host updatedHost = host.clone();
        updatedHost.setHostName(hostName);

        Boolean tlsEnable = hostConfig.getTlsEnable();
        if (tlsEnable != null && host.getTlsEnable() != tlsEnable) { //TLS变更
            if (tlsEnable) {    //tls状态从false变true
                String certPath = saveCert(hostName, hostConfig.getCa(), hostConfig.getCert(), hostConfig.getKey());
                updatedHost.setCertPath(certPath);
                updatedHost.setTlsEnable(true);
            } else {            //tls状态从true变false
                FileUtils.delete(host.getCertPath());
                updatedHost.setCertPath("");
                updatedHost.setTlsEnable(false);
            }
        }

        if (tlsEnable != null && hostConfig.getTlsEnable() && tlsEnable) {   //tls状态不变仍然为true，但要更新证书
            String certPath = host.getCertPath();
            MultipartFileUtils fileUtils = new MultipartFileUtils();
            fileUtils.makeDir(certPath);

            if (hostConfig.getCa() != null) {
                fileUtils.saveMultiFile(certPath, hostConfig.getCa(), "ca.pem");
            }
            if (hostConfig.getCert() != null) {
                fileUtils.saveMultiFile(certPath, hostConfig.getCert(), "cert.pem");
            }
            if (hostConfig.getKey() != null) {
                fileUtils.saveMultiFile(certPath, hostConfig.getKey(), "key.pem");
            }
        }


        Integer port = hostConfig.getPort();
        if (port != null && !port.equals(host.getPort())) {      //更改端口号
            updatedHost.setPort(port);
        }

        String protocol = hostConfig.getProtocol();
        if (protocol != null && !protocol.isEmpty()) {
            updatedHost.setProtocol(protocol);
        }

        try {
            dockerService.testLink(updatedHost);
        } catch (RuntimeException e) {
            throw new ServiceException("更新后配置无效，拒绝更新操作", e);
        }
        hostService.updateHost(updatedHost);
    }

    /**
     * 获取指定host信息
     * @param hostName
     * @return
     */
    public Host getHost(String hostName) {
        Host host;
        try {
            host = hostService.getHost(hostName);
        } catch (NotFoundBySqlException e) {
            throw new ServiceException("不存在名称为" + hostName + "的主机");
        }
        return host;
    }

    /**
     * 获取全部host
     * @return
     */
    public List<Host> getHosts() {
        return hostService.selectAllHost();
    }

}
