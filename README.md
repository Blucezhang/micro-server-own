[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/Blucezhang)
![Crates.io](https://img.shields.io/crates/l/rustc-serialize.svg)
[![Gem](https://img.shields.io/gem/dt/rails.svg)](https://github.com/Blucezhang)
![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven/apache-maven.svg)


# micro_server_own
基于SpringCloud 的微服务，包括了常用的功能,包括极光推送模块，用户权限模块，产品模块，工作流模块，商城模块,各大模块，后续还有添加，因为有工作的原因，不能及时更新，希望谅解。以上项目是部分功能，功能不完全。不完全都是RestFul,后续有时间将逐渐更新结构图，部署示意图等，希望谅解，有不足之处欢迎指导，或者Issues。
技术：Spring Cloud Spring boot ,Spring Rest,Neo4j mysql,Spring Data Jpa




2018/04/08 整体结构修改，微服务改进
#own-common 中包含了公共服务、以及对应前端的业务服务（Rest调用不需关注，只关注Bean,Util即可）

**各个服务启动顺序**
===
1. own-eureka-server（EurekaApplication）
2. own-config （ConfigApplication）
3. own-api-gateway （OwnGatewayApplication）  

<br> 剩下的项目可按照自己的需要随意启动   

- own-file (FileApplication)
- own-product (OwnProductApplication)
- own-promotion (PromotionApplication)
- own-end-server (SendServerApplication)
- own-user-party (UserAndPartyApplication)
- own-workflow (WorkFlowApplication)

**swagger2 api 访问路径**
====
- 启动 own-api-gateway （OwnGatewayApplication） 之后,访问http://ip:9632/swagger-ui.html 
- 如果您没有看到各个服务的api，那么请您启动想要看到项目的服务.

**数据库管理**
====

- mysql->username:root password:
- neo4j->username:neo4j password:123456
* 如果您不会使用neo4j ，这里不详述。请参照 [neo4j官网](https://neo4j.com/product/)以及相关doc,使用社区版.

__<font color=#00ffff size=12>\!ATTENTION:</font>__ 禁止商用（如果可以），最终解释权归作者所有！  

**如果你有兴趣的话，join this project.Email:Blucezhang@outlook.com**




**声明**
===
有几个小伙伴联系我之后，项目话术中有个别错误，目前项目中不包含任何的HTML页面，页面还在完善中，由于一个人在开发，平常工作又忙，所以没有时间将html完整的页面放上去（HTML是半成品），还请谅解。后续如果有时间的话，一定将html文件放上去