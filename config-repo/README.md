# 配置服务
## `一个Spring Boot应用在启动时，首先要设置自己的name并导入Config Server的URL，`
    `再根据当前活动的profile，由Config Server返回多个配置文件：`
  - {name}-{profile}.yml # name应用 + profile环境下的配置
  - application-{profile}.yml # 对所有应用生效的，profile环境下的配置
  - {name}.yml # 对name应用下所有环境都生效的配置
  - application.yml # 所有应用、所有环境下都生效的全局配置

## 使用Spring Cloud Config时，读取配置文件步骤如下：

1. 启动XxxApplication时，读取自身的application.yml，获得name和Config Server地址；
2. 根据name、profile和Config Server地址，获得一个或多个有优先级的配置文件；
3. 按优先级合并配置项；
4. 如果配置项中存在环境变量，则使用Xxx应用本身的环境变量去替换占位符。
- 环境变量通常用于配置一些敏感信息，如数据库连接口令，它们不适合明文写在config-repo的配置文件里。

## 常见错误
- 启动一个Spring Boot应用时，如果出现Unable to load config data错误：

java.lang.IllegalStateException: Unable to load config data from 'configserver:http://localhost:8888'
at org.springframework.boot.context.config.StandardConfigDataLocationResolver.getReferences
at ...
需要检查是否在pom.xml中引入了spring-cloud-starter-config，因为没有引入该依赖时，应用无法解析本地配置的import: configserver:xxx。

- 如果在启动一个Spring Boot应用时，Config Server没有运行，通常错误信息是因为没有读取到配置导致无法创建某个Bean。