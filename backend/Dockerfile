# ---------- etapa 1: build (tem Maven + JDK, é pesada) ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Baixa as dependências primeiro: enquanto o pom.xml não muda, o Docker reaproveita essa camada
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
# Os testes já rodam na sua máquina antes do merge; aqui só empacota
RUN mvn -q package -DskipTests

# ---------- etapa 2: execução (só o JRE + o .jar, imagem leve) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# LocalDateTime não tem fuso: sem isso, os pedidos ficariam 3h adiantados (UTC)
ENV TZ=America/Sao_Paulo
# O plano grátis tem 512 MB: o Java usa no máximo 75% disso
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Sao_Paulo"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
