# WEB部分
WEB部分代码基于[Ant Design Pro](http://pro.ant.design/index-cn)二次开发。


## 使用
### 编译源代码
1. 下载编译依赖
```bash
$ npm install
```

2. 编译，编译后文件输出到dist目录
```bash
npm build
```

### 配置nginx
```
server {
  listen 58090;
  server_name justitia;
  access_log logs/justitia.access.log ;
  error_log logs/justitia.error.log ;

  location / {
    root /home/holmes/spring-boot-admin-portal;
    index index.html;
    try_files $uri /index.html;
  }

  location /api/ {
    proxy_pass http://localhost:8090/;
  }

  error_page 404 /404.html;

  # redirect server error pages to the static page /50x.html
  error_page 500 502 503 504 /50x.html;
  location = /50x.html {
  root html;
  }
}"
```
