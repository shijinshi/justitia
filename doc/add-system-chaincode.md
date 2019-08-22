# Fabric系统链码定制开发参考

由于Fabric环境部署都是使用Docker镜像，而系统插件链码可以内置在Peer节点内。通过修改fabric的官方Makefile和Dockerfile.in可以创建一个支持自定义系统链码插件的peer镜像。。

## 系统Chaincode和普通Chaincode的区别
1. 系统链码无背书策略；
2. 系统链码的运行空间是Peer进程，而用户链码的运行空间是独立的进程；
3. 系统链码是和Peer一起升级，而用户链码可以单独升级；
4. 系统链码不需要通过SDKs或者CLI来安装和实例化，在peer启动时自动完成注册和实例化。

## 在Peer中增加自定义系统链码
1. 安装go等；
2. clone一份fabric的代码；
3. 将开发完成的系统链码放置在${GOPATH}/src/github.com/hyperledger/fabric/myscc目录下；
4. 修改${GOPATH}/src/github.com/hyperledger/fabric/Makefile文件:
	a.添加编译系统链码的规则：
	```makefile
        $(BUILD_DIR)/docker/bin/myscc.so:
        $(eval TARGET = ${patsubst $(BUILD_DIR)/docker/bin/%,%,${@}})
        @echo "Building $@"
        @$(DRUN) \
            -v $(abspath $(BUILD_DIR)/docker/bin):/opt/gopath/bin \
            $(BASE_DOCKER_NS)/fabric-baseimage:$(BASE_DOCKER_TAG) \
            go build --buildmode=plugin -tags "$(GO_TAGS)" -o /opt/gopath/bin/myscc.so ./myscc/ $(pkgmap.$(@F))
	```
	b. 修改规则$(BUILD_DIR)/image/peer/payload为：
	```
		$(BUILD_DIR)/image/peer/payload:       $(BUILD_DIR)/docker/bin/peer \
			$(BUILD_DIR)/sampleconfig.tar.bz2 \
			$(BUILD_DIR)/docker/bin/myscc.so
	```
5. 修改peer镜像的Dockerfile.in文件，路径：fabric/images/peer/Dockerfile.in:
	a. 第7行在镜像中创建存放链码插件的目录
	b. 第9行将编译生成的链码插件拷贝到镜像中
	```
    	# Copyright Greg Haskins All Rights Reserved
    	#
    	# SPDX-License-Identifier: Apache-2.0
    	#
    	FROM _BASE_NS_/fabric-baseos:_BASE_TAG_
    	ENV FABRIC_CFG_PATH /etc/hyperledger/fabric
    	RUN mkdir -p /opt/lib /var/hyperledger/production $FABRIC_CFG_PATH
    	COPY payload/peer /usr/local/bin
    	COPY payload/myscc.so /opt/lib/
    	ADD  payload/sampleconfig.tar.bz2 $FABRIC_CFG_PATH
    	CMD ["peer","node","start"]
    ```
6. 修改peer的配置文件：fabric/sampleconfig/core.yaml，chaincode.system和chaincode.systemPlugins启用系统链码插件:
	a. chaincode.system下增加myscc: enable
	b. chaincode.systemPlugins增加myscc的属性
	```yaml
		system:
        	cscc: enable
        	lscc: enable
        	escc: enable
        	vscc: enable
        	qscc: enable
			myscc: enable
    	systemPlugins:
		  - enabled: true
		    name: myscc
			path: /opt/lib/myscc.so
			invokableExternal: true
			invokableCC2CC: true
    ```
7. 设置环境变量：export set GO_TAGS=pluginsenabled
	使编译出的peer启用插件功能
8. 设置环境变量：export set DOCKER_DYNAMIC_LINK=true
	在编译peer时采用动态链接的方式，因为plugins插件的dlopen函数是在动态库中，如果静态链接，会导致peer启动异常。
	```
    编译告警：/workdir/go/src/plugin/plugin_dlopen.go:19 warning: Using 'dlopen' in statically linked applications requires at runtime the shared libraries from the glibc version used for linking
    运行异常：fatal error: unexpected signal during runtime execution
    goroutine 1 [syscall]: runtime.cgocall(0xd25b10, 0xc420310ca0, 0xec6b80)
        /opt/go/src/runtime/cgocall.go:128 +0x64 fp=0xc420310c70 sp=0xc420310c38 pc=0x402134
    plugin._Cfunc_pluginOpen(0xc4203be000, 0xc4200b0ba8, 0x0)
        _cgo_gotypes.go:77 +0x4e fp=0xc420310ca0 sp=0xc420310c70 pc=0x8be3be
    plugin.open.func1(0xc4203be000, 0xc4200b0ba8, 0xc4200ab420)
        /opt/go/src/plugin/plugin_dlopen.go:74 +0xac fp=0xc420310cd8 sp=0xc420310ca0 pc=0x8bf58c
    plugin.open(0xc420037fc0, 0x11, 0xc4200a3790, 0x0, 0x0)
        /opt/go/src/plugin/plugin_dlopen.go:74 +0x357 fp=0xc420310f90 sp=0xc420310cd8 pc=0x8be827
    ```
9. $GOPATH/src/github.com/hyperledger/fabric目录下执行：make peer-docker即可生成定制的peer镜像。
10. 一次修改，整个项目开发过程不用再做变化。且这种方式创建的镜像最小。