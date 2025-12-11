# ===========================================
# Dockerfile para Railway - Taller Web I
# ===========================================

# Etapa 1: Compilar el proyecto con Maven
FROM maven:3.8.6-openjdk-11 AS builder

WORKDIR /app

# Copiar archivos de Maven primero (para cachear dependencias)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto (sin tests para acelerar)
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar con Jetty
FROM openjdk:11-jre-slim

# Definir variables de Jetty
ENV JETTY_VERSION=9.4.56.v20240826
ENV JETTY_HOME=/opt/jetty
ENV PORT=8080

# Instalar wget y descargar Jetty
RUN apt-get update && apt-get install -y wget && \
    mkdir -p $JETTY_HOME && \
    wget -q https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/${JETTY_VERSION}/jetty-distribution-${JETTY_VERSION}.tar.gz && \
    tar -xzf jetty-distribution-${JETTY_VERSION}.tar.gz -C $JETTY_HOME --strip-components=1 && \
    rm jetty-distribution-${JETTY_VERSION}.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copiar el WAR desde la etapa de build
COPY --from=builder /app/target/tallerwebi-base-1.0-SNAPSHOT.war $JETTY_HOME/webapps/spring.war

WORKDIR $JETTY_HOME

# Railway usa la variable PORT, configuramos Jetty para usarla
RUN echo "jetty.http.port=$PORT" >> start.ini

# Exponer puerto (Railway asigna dinámicamente)
EXPOSE $PORT

# Iniciar Jetty
CMD ["java", "-jar", "./start.jar"]
