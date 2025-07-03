# 多阶段构建
# 第一阶段：构建应用
FROM maven:3.9.4-openjdk-17-slim AS build

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 第二阶段：运行应用
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 创建应用用户（安全考虑）
RUN addgroup --system spring && adduser --system spring --ingroup spring

# 复制构建好的jar文件
COPY --from=build /app/target/*.jar app.jar

# 更改文件所有者
RUN chown spring:spring app.jar

# 切换到应用用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
