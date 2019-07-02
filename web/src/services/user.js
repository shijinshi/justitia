import request from "@/utils/request";

const host =  window.hostIp;
const prefix = process.env.NODE_ENV === "production" ? '/api' : "http://" + host;

export async function query() {
  return request("/api/users");
}

export async function queryCurrent(params) {
  return request(prefix + "/user/" + params.userId, {
    method: "GET",
    headers: {
      Authorization: params.token
    }
  });
}

export async function remarkName(params) {
  return request(prefix + "/user/userName", {
    method: "PUT",
    body: params,
    headers: {
      Authorization: params.token
    }
  });
}

export async function remarkChildName(params) {
  return request(prefix + "/user/remark", {
    method: "POST",
    body: params,
    headers: {
      Authorization: params.token
    }
  });
}

export async function getUserQuestions(params) {
  return request(prefix + "/user/secret/" + params.userId, {
    method: "GET"
  })
}

export async function modifiedPassword(params) {
  return request(prefix + "/user/password", {
    method: "PUT",
    body: params
  })
}

export async function modifiedInformation(params) {
  return request(prefix + "/user", {
    method: "PUT",
    body: params,
    headers: {
      Authorization: params.token
    }
  })
}

export async function getChildrenInformation(params) {
  return request(prefix + "/user", {
    method: "GET",
    headers: {
      Authorization: params.token
    }
  })
}

export async function getRegisterCode(params) {
  return request(prefix + "/user/registerCode", {
    method: "GET",
    headers: {
      Authorization: params.token
    }
  })
}
