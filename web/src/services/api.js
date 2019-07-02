import request from "@/utils/request";

const host =  window.hostIp;
const prefix = process.env.NODE_ENV === "production" ? '/api' : "http://" + host;


// 初始配置值
export async function getInitConfig() {
  return request(prefix + "/context/check", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}

export async function getConfigPeer() { //*
  return request(prefix + "/node/peer", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}


export async function getConfigOrderer() { //*
  return request(prefix + "/node/orderer", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}

export async function managePeer(params) { //
  return request(prefix + `/node/peer/${params.peerName}/${params.oper}`, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}

export async function manageOrderer(params) { //
  return request(prefix + `/node/orderer/${params.ordererName}/${params.oper}`, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}


export async function ordererDelete(params) {
  return request(prefix + `/node/orderer/${params.ordererName}`, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}


export async function peerDelete(params) { //*
  return request(prefix + `/node/peer/${params.peerName}`, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}

export async function peerDeploy(params) { //*
  return request(prefix + "/node/peer", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}

export async function ordererDeploy(params) { //*
  return request(prefix + "/node/orderer", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}

export async function createOrderer(params) { //*
  return request(prefix + `/node/orderer/create/${params.createId}`, {
    method: "POST",
    body: params.data,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  });
}


//login & register
export async function register(params) {
  return request(prefix + "/user", {
    method: "POST",
    body: params
  });
}

export async function handleLogin(params) {
  return request(prefix + "/user/login", {
    method: "POST",
    body: params
  })
}

export async function logoutUser(params) {
  return request(prefix + "/user/logout", {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}


//Host
export async function getHostData() {
  return request(prefix + "/host", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function addHost(params) {
  return request(prefix + "/host", {
    method: "POST",
    body: params.formData,
    headers: {
      Authorization: localStorage.getItem("token"),
      // "Content-Type": "multipart/form-data"
    }
  })
}

export async function updateHost(params) {
  return request(prefix + "/host/" + params.hostName, {
    method: "POST",
    body: params.formData,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getOneHost(params) {
  return request(prefix + "/host/" + params.hostName, {
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteHost(params) {
  return request(prefix + "/host/" + params.hostName, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

//Host-Docker
export async function getDockerList(params) {
  return request(prefix + "/docker/container/" + params.hostName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getDockerInfo(params) {
  return request(prefix + "/docker/container/" + params.hostName + "/" + params.containerId, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteDocker(params) {
  return request(prefix + "/docker/container/" + params.hostName + "/" + params.containerId, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function startDocker(params) {
  return request(prefix + "/docker/container/start/" + params.hostName + "/" + params.containerId, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function pauseDocker(params) {
  return request(prefix + "/docker/container/pause/" + params.hostName + "/" + params.containerId, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function unpauseDocker(params) {
  return request(prefix + "/docker/container/unpause/" + params.hostName + "/" + params.containerId, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function restartDocker(params) {
  return request(prefix + "/docker/container/restart/" + params.hostName + "/" + params.containerId, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}


//Host-Image
export async function getDockerImage(params) {
  return request(prefix + "/docker/image/" + params.hostName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function postDockerImage(params) {
  return request(prefix + "/docker/image/" + params.hostName, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteImage(params) {
  return request(prefix + "/docker/image/" + params.hostName + "/" + params.ImageId, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getImageDetail(params) {
  return request(prefix + "/docker/image/" + params.hostName + "/" + params.ImageId, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function imageTag(params) {
  return request(prefix + "/docker/image/tag/" + params.hostName, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

//Host-DockerNet
export async function getDockerNet(params) {
  return request(prefix + "/docker/network/" + params.hostName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function addNet(params) {
  return request(prefix + "/docker/network/" + params.hostName, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteNet(params) {
  return request(prefix + "/docker/network/" + params.hostName + "/" + params.networkName, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getNetDetail(params) {
  return request(prefix + "/docker/network/" + params.hostName + "/" + params.networkName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

//Host-DockerVol
export async function getDockerVol(params) {
  return request(prefix + "/docker/volume/" + params.hostName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function addVol(params) {
  return request(prefix + "/docker/volume/" + params.hostName, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteVol(params) {
  return request(prefix + "/docker/volume/" + params.hostName + "/" + params.volumeName, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getVolDetail(params) {
  return request(prefix + "/docker/volume/" + params.hostName + "/" + params.volumeName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

//Fabric CAManager

export async function addCA(params) {
  return request(prefix + "/ca/server/root", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getCA(params) {
  return request(prefix + "/ca/server", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getOneCA(params) {
  return request(prefix + "/ca/server/" + params.serverName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteCA(params) {
  return request(prefix + "/ca/server/" + params.serverName + "?checked=" + params.checked, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function operCA(params) {
  return request(prefix + "/ca/server/" + params.serverName + "/" + params.oper, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getConfigCA(params) {
  return request(prefix + "/ca/server/config/" + params.serverName, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function setConfigCA(params) {
  return request(prefix + "/ca/server/config/" + params.serverName, {
    method: "PUT",
    body: params.values,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getConfigTpl() {
  return request(prefix + "/ca/server/config", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function updateCert(params) {
  return request(prefix + "/ca/server/config/cert/" + params.serverName, {
    method: "PUT",
    body: params.formData,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function createServer(params) {
  return request(prefix + "/ca/server/create/" + params.createId, {
    method: "POST",
    body: params.data,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

//Fabric CAUser
export async function getCAUser() {
  return request(prefix + "/ca/user", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getChildrenUser() {
  return request(prefix + "/user", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function registerCAUser(params) {
  return request(prefix + "/ca/user/register/" + params.serverName + "/" + params.caUserId, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function enrollCAUser(params) {
  return request(prefix + "/ca/user/enroll/" + params.serverName + "/" + params.userId, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function reenrollCAUser(params) {
  return request(prefix + "/ca/user/reenroll/" + params.serverName + "/" + params.userId, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function deleteCAUser(params) {
  return request(prefix + "/ca/user/revoke/" + params.serverName + "/" + params.userId, {
    method: "DELETE",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function revokeCert(params) {
  return request(prefix + "/ca/cert/revoke/" + params.serverName + "/" + params.userId, {
    method: "DELETE",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function createCrl(params) {
  return request(prefix + "/ca/cert/crl/" + params.serverName + "/" + params.userId, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getCert(params) {
  return request(prefix + "/ca/cert/" + params.serverName + "/" + params.userId, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function getCertInfo(params) {
  return request(prefix + "/ca/cert/" + params.serverName + "/" + params.userId, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function downloadAllCert(params) {
  return request(prefix + "/ca/cert/download/" + params.serverName + "/" + params.userId, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function downloadCert(params) {
  return request(prefix + "/ca/cert/download?serial=" + params.serial + "&aki=" + params.aki, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

//Organiztion
export async function getOrganiztion() {
  return request(prefix + "/organization", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

export async function setORganiztion(params) {
  return request(prefix + "/organization", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}

/************************************* channel通道管理 *************************************/
// 获取通道信息
export async function getChannelList() {
  return request(prefix + "/channel", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 创建通道
export async function createChannel(params) {
  return request(prefix + "/channel", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 将节点加入通道
export async function joinChannel(params) {
  return request(prefix + "/channel/join", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 获取通道配置
export async function getChannelConfig() {
  return request(prefix + "/channel/organization/config", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 发起增加组织的请求
export async function addChannelOrg(params) {
  return request(prefix + "/channel/organization", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 获取指定通道的全部成员MSPID
export async function getChannelMspId(params) {
  return request(prefix + `/channel/msp/${params.channelName}`, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 发起删除组织的请求
export async function delChannelOrg(params) {
  return request(prefix + `/channel/organization`, {
    method: "DELETE",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 获取待处理任务
export async function getChannelTask() {
  return request(prefix + "/channel/task", {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 获取指定任务详情
export async function getTaskDetail(params) {
  return request(prefix + `/channel/task/${params.taskId}`, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 处理任务
export async function dealChannelTask(params) {
  return request(prefix + "/channel/task/response", {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 删除指定任务
export async function delChannelTask(params) {
  return request(prefix + `/channel/task/${params.taskId}`, {
    method: "DELETE",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 撤销指定任务
export async function resChannelTask(params) {
  return request(prefix + `/channel/task/recall/${params.taskId}`, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 提交请求到order
export async function submitChannelTask(params) {
  return request(prefix + `/channel/task/submit/${params.taskId}`, {
    method: "PUT",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}


/************************************* 节点管理，联盟操作 *************************************/
// 获取order节点全部联盟
export async function getConsortium(params) {
  return request(prefix + `/consortium/${params.ordererName}`, {
    method: "GET",
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 增加联盟成员
export async function addConsortium(params) {
  return request(prefix + `/consortium/organization`, {
    method: "POST",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}
// 删除联盟成员
export async function delConsortium(params) {
  return request(prefix + `/consortium/organization`, {
    method: "DELETE",
    body: params,
    headers: {
      Authorization: localStorage.getItem("token")
    }
  })
}