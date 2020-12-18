FROM openjdk:11

COPY build/install/app/ /opt/app/

ENTRYPOINT ["/opt/app/bin/app", "--add-opens java.base/java.lang=ALL-UNNAMED"]
CMD []
