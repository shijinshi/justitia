package cn.shijinshi.fabricmanager.service.fabric.tools;

import cn.shijinshi.fabricmanager.Context;
import cn.shijinshi.fabricmanager.dao.entity.Organization;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.fabric.certificate.MspHelper;
import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.helper.ssh.CallLocalShell;
import cn.shijinshi.fabricmanager.service.helper.ssh.CallShell;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.YamlFileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigTxGen {
    private static final Logger LOGGER = Logger.getLogger(ConfigTxGen.class);
    private static final String DEFAULT_ORDERER_ORG_NAME = "SampleOrg";
    private static final String CONFIG_TX_TEMPLATE_FILE = "configtx_template.yaml";

    @Autowired
    private MspHelper mspHelper;

    //------------------------------------------------ genesis block ---------------------------------------------------
    public File createGenesisBlock(String systemChainId, String consortium, ArrayList<String> ordererAddresses) throws FabricToolsException {
        File genesisBlockFile = createOutputFile("genesis.block");
        String command = String.format("./configtxgen -profile OrdererGenesis -channelID %s -outputBlock %s", systemChainId, genesisBlockFile.getPath());
        CallShell.Result result = callConfigTxGen(command, consortium, ordererAddresses);

        if (result.isSuccess()) {
            return genesisBlockFile;
        } else {
            throw new FabricToolsException("Configtxgen call failed:" + result.getErrorInfo());
        }
    }

    //---------------------------------------------- organization config -----------------------------------------------
    public File createOrgConfig() throws FabricToolsException {
        Organization organization = Context.getOrganization();
        String command = String.format("./configtxgen -printOrg %s", organization.getOrgName());
        CallShell.Result result = callConfigTxGen(command, null, null);
        if (result.isSuccess()) {
            try {
                return new File(FileUtils.writeStringToFile(ExternalResources.getUniqueTempDir(), result.getPrintInfo(), "org-config.json"));
            } catch (IOException e) {
                LOGGER.error(e);
                throw new FabricToolsException("Configtxgen output stream save to file failed");
            }
        } else {
            throw new FabricToolsException("Configtxgen call failed:" + result.getErrorInfo());
        }
    }

    //--------------------------------------------- create channel tx --------------------------------------------------
    public byte[] createChannelTx(String channelId, String consortium) throws FabricToolsException {
        File outputFile = createOutputFile("channel.tx");
        try {
            String command = String.format("./configtxgen -profile OrgsChannel -outputCreateChannelTx %s -channelID %s", outputFile.getPath(), channelId);
            CallShell.Result result = callConfigTxGen(command, consortium, null);

            if (result.isSuccess()) {
                try {
                    FileInputStream is = new FileInputStream(outputFile);
                    return IOUtils.toByteArray(is);
                } catch (IOException e) {
                    LOGGER.error(e);
                    throw new FabricToolsException("File " + outputFile.getPath() + " read failed.");
                }
            } else {
                throw new FabricToolsException("Configtxgen call failed:" + result.getErrorInfo());
            }
        } finally {
            FileUtils.delete(outputFile.getParent());
        }
    }

    private File createOutputFile(String fileName) throws FabricToolsException {
        String output = ExternalResources.getUniqueTempDir() + File.separator + fileName;
        File outputFile = new File(output);
        try {
            FileUtils.makeDir(outputFile.getParent());
            if (!outputFile.createNewFile()) {
                throw new FabricToolsException("Configtxgen output file " + output + " create failed.");
            }
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("Configtxgen output file " + output + " create failed.");
        }
        return outputFile;
    }

    private synchronized CallShell.Result callConfigTxGen(String command, String consortium, ArrayList<String> ordererAddresses) throws FabricToolsException {
        //修改配置文件
        Organization organization = Context.getOrganization();
        String mspDir;
        try {
            mspDir = mspHelper.generateOrgMsp(organization.getTlsEnable(), organization.getTlsCaServer());
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("Organization msp folder generate failed");
        }
        String configTxFile = modifyConfigFile(organization.getOrgName(), organization.getOrgMspId(), mspDir, consortium, ordererAddresses);
        try {
            //检查configtxgen工具是否存在
            String configtxgen = ExternalResources.getScripts("tools/configtxgen");
            File configtxgenFile = new File(configtxgen);
            if (!configtxgenFile.exists()) {
                throw new FabricToolsException("No such file " + configtxgen);
            }
            CallLocalShell shell = new CallLocalShell();
            return shell.execCmd(command, configtxgenFile.getParentFile());
        } catch (InterruptedException | IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("Configtxgen tool call failed.");
        } finally {
            FileUtils.delete(mspDir);
            FileUtils.delete(configTxFile);
        }
    }


    private synchronized String modifyConfigFile(String orgName, String mspId, String mspDir, String consortiumName,
                                                 ArrayList<String> ordererAddresses) throws FabricToolsException {

        String configTxTempFile = ExternalResources.getScripts("tools/" + CONFIG_TX_TEMPLATE_FILE);
        YamlFileUtils yamlUtils = new YamlFileUtils();
        Map configTxMap;
        try {
            configTxMap = yamlUtils.readYamlFileAsMap(configTxTempFile);
        } catch (FileNotFoundException e) {
            throw new FabricToolsException(configTxTempFile + "文件缺失，无法生成创世区块");
        }
        if (configTxMap == null) {
            throw new FabricToolsException("配置文件模板" + configTxTempFile + "缺失，无法生成创世区块");
        }
        //设置组织信息
        setOrganizationInfo(configTxMap, orgName, mspId, mspDir);
        //设置联盟信息
        if (StringUtils.isNotEmpty(consortiumName)) {
            setConsortiumInfo(configTxMap, consortiumName);
        }
        //set orderers address
        if (ordererAddresses != null && !ordererAddresses.isEmpty()) {
            try {
                Map orderer = (Map) configTxMap.get("Orderer");
                orderer.replace("Addresses", ordererAddresses);
                ((Map) ((Map) ((Map) configTxMap.get("Profiles")).get("OrdererGenesis")).get("Orderer")).replace("Addresses", ordererAddresses);
            } catch (Exception e) {
                throw new ServiceException("resources/tools/" + CONFIG_TX_TEMPLATE_FILE + "配置文件Orderer信息不完整");
            }
        }
        //写入新的配置文件
        String configTxFile = ExternalResources.getScripts("tools/configtx.yaml");
        try {
            yamlUtils.writeYamlFile(configTxMap, configTxFile);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("配置文件写入失败");
        }
        return configTxFile;
    }

    private synchronized void setOrganizationInfo(Map config, String orgName, String mspId, String mspDir) {
        Map sampleOrg = null;
        try {
            if (config != null && config.containsKey("Organizations")) {
                Object organizations = config.get("Organizations");
                if (organizations instanceof List) {
                    ArrayList organizationList = (ArrayList) organizations;
                    if (!organizationList.isEmpty()) {
                        for (Object organization : organizationList) {
                            if (organization == null) {
                                continue;
                            }
                            if (organization instanceof Map) {
                                Map org = (Map) organization;
                                if (org.containsKey("Name") && ConfigTxGen.DEFAULT_ORDERER_ORG_NAME.equals(org.get("Name"))) {
                                    sampleOrg = org;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new ServiceException("resources/tools/" + CONFIG_TX_TEMPLATE_FILE + "配置文件不符合规范");
        }

        if (sampleOrg == null) {
            throw new ServiceException("配置文件resources/tools/" + CONFIG_TX_TEMPLATE_FILE + "中不存在名为" + DEFAULT_ORDERER_ORG_NAME + "的组织");
        }
        if (sampleOrg.containsKey("Name") && sampleOrg.containsKey("ID") && sampleOrg.containsKey("MSPDir")) {
            sampleOrg.replace("Name", orgName);
            sampleOrg.replace("ID", mspId);
            sampleOrg.replace("MSPDir", mspDir);
            if (sampleOrg.containsKey("Policies")) {
                sampleOrg.replace("Policies", generatePolicies(mspId));
            } else {
                sampleOrg.put("Policies", generatePolicies(mspId));
            }
        } else {
            throw new ServiceException("resources/tools/" + CONFIG_TX_TEMPLATE_FILE + "配置文件" + DEFAULT_ORDERER_ORG_NAME + "组织信息不完整");
        }
    }

    private Map generatePolicies(String mspId) {
        Map<String, String> readers = new LinkedHashMap<>();
        String readerRule = "OR('" + mspId + ".member')";
        readers.put("Type", "Signature");
        readers.put("Rule", readerRule);

        Map<String, String> writers = new LinkedHashMap<>();
        String writerRule = "OR('" + mspId + ".member')";
        writers.put("Type", "Signature");
        writers.put("Rule", writerRule);

        Map<String, String> admins = new LinkedHashMap<>();
        String adminRule = "OR('" + mspId + ".admin')";
        admins.put("Type", "Signature");
        admins.put("Rule", adminRule);

        Map<String, Map> policies = new LinkedHashMap<>();
        policies.put("Readers", readers);
        policies.put("Writers", writers);
        policies.put("Admins", admins);
        return policies;
    }

    private synchronized void setConsortiumInfo(Map config, String consortiumName) {
        try {
            if (config.containsKey("Profiles")) {
                Map profiles = (Map) config.get("Profiles");
                if (profiles != null && profiles.containsKey("OrdererGenesis")) {
                    Map ordererGenesis = (Map) profiles.get("OrdererGenesis");
                    if (ordererGenesis != null && ordererGenesis.containsKey("Consortiums")) {
                        Map consortiums = (Map) ordererGenesis.get("Consortiums");
                        if (consortiums != null && consortiums.containsKey("SampleConsortium")) {
                            Map sampleConsortium = (Map) consortiums.remove("SampleConsortium");
                            consortiums.put(consortiumName, sampleConsortium);
                        }
                    }
                }

                if (profiles != null && profiles.containsKey("OrgsChannel")) {
                    Map orgsChannel = (Map) profiles.get("OrgsChannel");
                    if (orgsChannel != null && orgsChannel.containsKey("Consortium")) {
                        orgsChannel.replace("Consortium", consortiumName);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new ServiceException("resources/tools/" + CONFIG_TX_TEMPLATE_FILE + "配置文件不符合规范");
        }
    }
}
