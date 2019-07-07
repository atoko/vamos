FROM azul/zulu-openjdk:8

WORKDIR /app
EXPOSE 8080
COPY /build/libs/entrado-0.0.1-SNAPSHOT.jar .

CMD java -Xms1024m -Xmx1024m -Dspring.profiles.active="$profile" -jar entrado-0.0.1-SNAPSHOT.jar