FROM openjdk:11
ADD target/MoneyTransfer-0.0.1-SNAPSHOT.jar money-transfer-boot.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "money-transfer-boot.jar"]
