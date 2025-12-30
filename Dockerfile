# 가벼운 Java 21 실행 환경 사용
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 로컬에서 빌드된 standalone jar 파일을 복사
COPY target/gritum-engine-1.0.0-standalone.jar app.jar

# 수정 전: ENV PORT 3000
# 수정 후: 등호(=)를 사용하여 명시적으로 지정
ENV PORT=3000

EXPOSE 3000

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]
