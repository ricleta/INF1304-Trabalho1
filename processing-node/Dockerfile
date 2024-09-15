FROM openjdk:11-jre-slim

WORKDIR /app

# mudar fiscais-pn.jar para o Jar equivalente ao MyGroupDefiner apropriado
COPY target/fiscais-pn.jar .

CMD ["java", "-jar", "fiscais-pn.jar"]
