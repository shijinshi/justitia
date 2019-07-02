# Fabric管理
本地快速搭建fabric区块链环境，为信工厂和区块链预言等区块链项目提供提供环境支持。并对区块链环境提供管理和维护功能。  
Fabric管理是针对于单个组织（联盟成员）的管理，对fabric网络证书、节点和联盟成员的管理：
- 通过fabric ca管理本组织内部的全部证书；
- 实现peer、orderer、couchdb节点的创建、配置和状态管理等整个生命周期的维护，简化fabric网络创建和维护成本；
- 实现对fabric网络中联盟成员的管理，包括联盟成员的加入和删除。

## 开发环境
fabric管理采用spring boot + React实现前后端分离。后端部分是一个Maven项目，安装开发环境需要满足以下条件：
- JDK 1.8+
- Maven 3
- Mysql 5.7+ 
- Docker server 1.11.x
- Docker remote API v1.23
- fabric 1.3

## 先决条件
1. 项目运行前需要准备至少一个fabric环境配置完整的主机（或虚拟机）作为fabric节点的运行环境，环境配置需要具备以下要求
    - Docker server 1.11.x
    - Docker remote API v1.23
    - fabric 1.3
2. 项目运行环境必须和运行节点的宿主机在同一网络环境下，以确保程序能够请求到节点宿主机的docker服务。
#### 配置主机Docker环境
远程的宿主机启用docker服务有不使用TLS验证和使用TLS验证两种方式，推荐开启TLS验证。
1. 使用TLS验证
使用TLS验证开启docker服务，参考以下文章
https://blog.csdn.net/qq_36956154/article/details/82180551
2. 不使用TLS验证 
不使用TLS验证的方式并不安全，网络内任何人都可以通过docker侵入宿主机。这种方式强烈不推荐。具体开启docker server方式如下： 
打开docker server配置文件
```
vi /lib/systemd/system/docker.service
```
找到Execstart=/usr/bin/dockerd后加上
```
-H tcp://0.0.0.0:2375 -H unix://var/run/docker.sock
```
#### 配置主机fabric环境
在节点宿主机上运行“downloadfabricimage.sh”脚本下载fabric相关镜像

## 部署
1. 配置系统环境  
按照先决条件配置系统环境  
2. 创建数据库  
在mysql数据库上创建一个新的数据库，运行“fabricmanager.sql”脚本创建对应的数据表  
3. 编译打包  
本项目是一个maven项目，将项目打包成一个jar包。并确保源文件中“fabric-ca-server”和“resources"文件夹与jar文件保持同一目录下  
4. 修改jar包中application.yaml配置文件中数据库连接配置和服务端口（可在打包前完成）  
5. 运行jar  
本项目必须运行在linux系统上
