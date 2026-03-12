# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# 先复制 pom，利用缓存（根 pom 声明了所有模块，需全部复制否则 Maven reactor 解析会失败）
COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY common/pom.xml common/pom.xml
COPY enrollment-service/pom.xml enrollment-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY course-service/pom.xml course-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml

# 预下载依赖（加速后续构建）
RUN mvn -q -DskipTests -pl gateway-service -am dependency:go-offline

# 再复制源码
COPY common common
COPY gateway-service gateway-service

# 构建网关 jar
RUN mvn -q -DskipTests -pl gateway-service -am package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# 创建非 root 用户（更安全）
RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /workspace/gateway-service/target/*SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
