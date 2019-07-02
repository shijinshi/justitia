package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import com.github.dockerjava.api.DockerClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;

public class ArchiveHelper extends DockerHelper{

    public ArchiveHelper(DockerClient client) {
        super(client);
    }

    /**
     * 从容器中复制资源（文件）到本地
     *
     * @param containerId 容器id
     * @param resource    容器内资源路径
     * @param savePath    本地文件存放路径
     */
    public String copyArchiveFromContainer(String containerId, String resource, String savePath) throws IOException {
        InputStream response = client.copyArchiveFromContainerCmd(containerId, resource)
                .exec();
        return saveFile(response, savePath);
    }

    private String saveFile(InputStream response, String savePath) throws IOException {
        TarArchiveInputStream tarInputStream = new TarArchiveInputStream(response);
        String saveFile = null;

        TarArchiveEntry tae;
        while ((tae = tarInputStream.getNextTarEntry()) != null) {
            String dir = savePath  + File.separator + tae.getName();
            if (saveFile == null) {
                saveFile = dir;
            }

            File dirFile = new File(dir);
            if (tae.isDirectory()) {
                dirFile.mkdirs();
            } else {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dirFile));

                int count;
                byte[] data = new byte[1024];
                while ((count = tarInputStream.read(data)) != -1 ) {
                    bos.write(data, 0 , count);
                }
                bos.close();
            }
        }
        return saveFile;
    }


    /**
     * 复制本地资源（文件）到容器
     *
     * @param containerId 容器名称
     * @param resource    本地资源路径
     * @param remotePath  容器内接受文件存放路径
     */
    public void copyArchiveToContainer(String containerId, String resource, String remotePath, boolean overwrite) {
        client.copyArchiveToContainerCmd(containerId)
                .withHostResource(resource)
                .withRemotePath(remotePath)         //容器内存放路径
                .withNoOverwriteDirNonDir(overwrite)    //是否覆盖
                .withDirChildrenOnly(true)          //如果路径是文件夹，true表示包含子目录
                .exec();
    }

    /**
     * 复制本地资源（文件）到容器
     *
     * @param containerId 容器id
     * @param resource    tar格式压缩文件输入流
     * @param remotePath  容器内接受文件存放路径
     */
    public void copyArchiveToContainer(String containerId, InputStream resource, String remotePath) {
        client.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(resource)
                .withRemotePath(remotePath)     //容器内存放路径
                .withDirChildrenOnly(true)
                .exec();
    }
}
