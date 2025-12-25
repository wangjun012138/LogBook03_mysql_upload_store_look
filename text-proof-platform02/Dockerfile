# 1. 基础镜像：使用 OpenJDK 17 (和你 pom.xml 里的版本一致)
FROM eclipse-temurin:17-jdk-jammy

# 2. 设置容器内的工作目录
WORKDIR /app

# 3. 将 Maven 打包生成的 jar 包复制到容器内，并重命名为 app.jar
# 注意：这里假设你的 jar 包名是默认生成的格式，稍后构建时我会教你确认
COPY target/text-proof-platform-0.0.1-SNAPSHOT.jar app.jar

# 4. 暴露容器的 8080 端口
EXPOSE 8080

# 5. 容器启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]