FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD *.jar app.jar
ENV JAVA_OPTS="-Xmx3g -Xms3g -Xmn2g -XX:+UseConcMarkSweepGC -XX:+PrintGC"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]