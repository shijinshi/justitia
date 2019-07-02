package cn.shijinshi.fabricmanager.service.fabric.tools;

import cn.shijinshi.fabricmanager.service.helper.ExternalResources;
import cn.shijinshi.fabricmanager.service.helper.ssh.CallLocalShell;
import cn.shijinshi.fabricmanager.service.helper.ssh.CallShell;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.common.Configtx;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ConfigTxLator {
    private static final Logger LOGGER = Logger.getLogger(ConfigTxLator.class);

    public enum ProtoType {
        CONFIG_UPDATE("ConfigUpdate"),
        CONFIG("Config"),
        ENVELOPE("Envelope");

        private String type;

        ProtoType(String protoType) {
            this.type = protoType;
        }

        public String getType() {
            return type;
        }
    }


    public byte[] encode(String data, ProtoType protoType) throws FileNotFoundException, FabricToolsException {
        File configTxLator = getConfigTxLator();
        File input = strToFile(data);
        if (!input.exists()) {
            throw new FabricToolsException("No such file " + input.getPath());
        }
        File output = getUniqueTempFile("pb");

        try {
            String command = String.format("./configtxlator proto_encode --type=common.%s --input=%s --output=%s",
                    protoType.getType(), input.getPath(), output.getPath());
            CallLocalShell shell = new CallLocalShell();
            CallShell.Result result = shell.execCmd(command, configTxLator.getParentFile());
            if (!result.isSuccess()) {
                LOGGER.error(result.getErrorInfo());
                throw new FabricToolsException("Encode string failed");
            }
            if (!output.exists()) {
                throw new FabricToolsException("Output stream redirection to file failed");
            }
            return fileToBytes(output);
        } catch (InterruptedException | IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("configtxlator tool call failed");
        } finally {
            if (!input.delete()) {
                LOGGER.warn("File " + input.getPath() + " delete filed.");
            }
            if (!output.delete()) {
                LOGGER.warn("File " + output.getPath() + " delete filed.");
            }
        }
    }

    public String decode(byte[] data, ProtoType protoType) throws FileNotFoundException, FabricToolsException {
        File configTxLator = getConfigTxLator();
        File input = bytesToFile(data);
        if (!input.exists()) {
            throw new FabricToolsException("No such file " + input.getPath());
        }
        File output = getUniqueTempFile("json");

        try {
            String command = String.format("./configtxlator proto_decode --type=common.%s --input=%s --output=%s",
                    protoType.getType(), input.getPath(), output.getPath());
            CallLocalShell shell = new CallLocalShell();
            CallShell.Result result = shell.execCmd(command, configTxLator.getParentFile());
            if (!result.isSuccess()) {
                LOGGER.error(result.getErrorInfo());
                throw new FabricToolsException("Decode bytes failed");
            }
            if (!output.exists()) {
                throw new FabricToolsException("Output stream redirection to file failed.");
            }
            return fileToStr(output);
        } catch (InterruptedException | IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("configtxlator tool call failed");
        } finally {
            if (!input.delete()) {
                LOGGER.warn("File " + input.getPath() + " delete filed.");
            }
            if (!output.delete()) {
                LOGGER.warn("File " + output.getPath() + " delete filed.");
            }
        }
    }

    @Deprecated
    public byte[] computeUpdate(Configtx.Config original, Configtx.Config updated, String channelName) throws  FabricToolsException {
//        Configtx.Config original = Configtx.Config.parseFrom(originalBytes);
//        Configtx.Config updated = Configtx.Config.parseFrom(updatedBytes);

        ComputeUpdate computeUpdate = new ComputeUpdate();
        return computeUpdate.Compute(original, updated, channelName).toByteArray();
    }

    public byte[] computeUpdate(byte[] original, byte[] updated, String channelName) throws FileNotFoundException, FabricToolsException {
        File configTxLator = getConfigTxLator();
        File originalFile = bytesToFile(original);
        if (!originalFile.exists()) {
            throw new FabricToolsException("No such file " + originalFile.getPath());
        }
        File updatedFile = bytesToFile(updated);
        if (!updatedFile.exists()) {
            throw new FabricToolsException("No such file " + updatedFile.getPath());
        }
        File output = getUniqueTempFile("pb");
        try {
            String command = String.format("./configtxlator compute_update --original=%s --updated=%s --channel_id=%s --output=%s",
                    originalFile.getPath(), updatedFile.getPath(), channelName, output);
            CallLocalShell shell = new CallLocalShell();
            CallShell.Result result = shell.execCmd(command, configTxLator.getParentFile());
            if (!result.isSuccess()) {
                LOGGER.error(result.getErrorInfo());
                throw new FabricToolsException("Compute update failed");
            }
            if (!output.exists()) {
                throw new FabricToolsException("Output stream redirection to file failed.");
            }
            return fileToBytes(output);
        } catch (InterruptedException | IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("configtxlator tool call failed");
        } finally {
            if (!originalFile.delete()) {
                LOGGER.warn("File " + originalFile.getPath() + " delete filed.");
            }
            if (!updatedFile.delete()) {
                LOGGER.warn("File " + updatedFile.getPath() + " delete filed.");
            }
            if (!output.delete()) {
                LOGGER.warn("File " + output.getPath() + " delete filed.");
            }
        }

    }

    private File getUniqueTempFile(String suffix) {
        String fileName = "" + System.currentTimeMillis() + (int) (Math.random() * 100) + "." + suffix;
        String tempFile = ExternalResources.getScripts("tools/" + fileName);
        return new File(tempFile);
    }

    private File getConfigTxLator() throws FileNotFoundException {
        String configtxlator = ExternalResources.getScripts("tools/configtxlator");
        File configtxlatorFile = new File(configtxlator);
        if (!configtxlatorFile.exists()) {
            throw new FileNotFoundException("No such file " + configtxlator);
        }
        return configtxlatorFile;
    }

    private File strToFile(String data) throws FabricToolsException {
        File file = getUniqueTempFile("json");
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(data);
            fileWriter.flush();
            return file;
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("File " + file.getPath() + " write failed");
        }
    }

    private String fileToStr(File file) throws FabricToolsException {
        byte[] bytes = fileToBytes(file);
        return new String(bytes);
    }

    private File bytesToFile(byte[] bytes) throws FabricToolsException {
        File file = getUniqueTempFile("pb");
        try {
            if (!file.createNewFile()) {
                throw new FabricToolsException("File " + file.getPath() + " create failed");
            }
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("File " + file.getPath() + " create failed");
        }

        try (FileOutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("File " + file.getPath() + " write failed");
        }
        return file;
    }

    private byte[] fileToBytes(File file) throws FabricToolsException {
        if (!file.exists()) {
            throw new FabricToolsException("No such file " + file.getPath());
        }

        try (FileInputStream is = new FileInputStream(file)) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            LOGGER.error(e);
            throw new FabricToolsException("File " + file.getPath() + " read failed");
        }
    }


}
