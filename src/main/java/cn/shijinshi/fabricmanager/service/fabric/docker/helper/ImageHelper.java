package cn.shijinshi.fabricmanager.service.fabric.docker.helper;

import cn.shijinshi.fabricmanager.service.fabric.docker.exception.ImageManageException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.util.List;

public class ImageHelper extends DockerHelper{

    public ImageHelper(DockerClient client) {
        super(client);
    }

    /**
     * 检查指定镜像是否存在
     *
     * @param imageName 镜像名称
     * @param tag       镜像版本
     * @return
     */
    public boolean imageExist(String imageName, String tag) {
        List<Image> images = listImagesCmd(imageName);

        if (images != null && !images.isEmpty()) {
            String repoTag = imageName + ":" + tag;
            String[] repoTags = images.get(0).getRepoTags();
            for (String s : repoTags) {
                if (s.equals(repoTag)){
                    return true;
                }
            }
        }
        return false;
    }

    public List<Image> listImagesCmd() {
        return client.listImagesCmd().exec();
    }

    public List<Image> listImagesCmd(String imageName) {
       return client.listImagesCmd().withImageNameFilter(imageName).exec();
    }

    /**
     * 获取指定镜像的信息
     *
     * @param imageId -镜像ID
     * @return
     */
    public InspectImageResponse inspectImage(String imageId) {
        return client.inspectImageCmd(imageId).exec();
    }

    /**
     * 拉取镜像
     *
     * @param imageName 镜像名称
     * @param tag       镜像版本
     * @throws InterruptedException  外部调用执行中断
     */
    public void pullImage(String imageName, String tag) throws InterruptedException, ImageManageException {
        if (imageExist(imageName, tag)) {
            return;
        }

        client.pullImageCmd(imageName)
            .withTag(tag)
//                .withAuthConfig(null)
//                .withPlatform(null)
//                .withRegistry(null)
//                .withRepository(null)
            .exec(new PullImageResultCallback())
            .awaitCompletion();

        if (!imageExist(imageName, tag)) {
            throw new ImageManageException("Pull image " + imageName + ":" + tag + " failed.");
        }
    }

    /**
     * 给镜像增加tag
     *
     * @param imageId                 镜像名称：tag
     * @param imageNameWithRepository 不懂
     * @param tag                     新的tag
     */
    public void tagImage(String imageId, String imageNameWithRepository, String tag) {
        client.tagImageCmd(imageId, imageNameWithRepository, tag)
                .withForce()
                .exec();
    }

    public void removeImage(String imageId) throws ImageManageException {
        InspectImageResponse response = inspectImage(imageId);
        if (response != null) {
            client.removeImageCmd(imageId).exec();
            InspectImageResponse response1 = inspectImage(imageId);
            if (response1 != null) {
                throw new ImageManageException("Remove image " + imageId + " failed.");
            }
        }

    }
}
