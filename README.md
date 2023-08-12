# Graduate work for course Java-developer

![pink floyd](ads-backend-spring\src\test\resources\picture\images.jpeg)

## Description:
spring-boot app implements backend for advertising site

## ТЗ for development:
[link](https://github.com/11th/graduate-work/wiki/%D0%A2%D0%97-%D0%BD%D0%B0-%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D0%BA%D1%83)

## Stack:
- ![java11](https://img.shields.io/badge/java_11-red)
- ![spring boot](https://img.shields.io/badge/spring_boot-green)
- ![spring security](https://img.shields.io/badge/spring_security-green)
- ![postgresql](https://img.shields.io/badge/postgresql-blue)

## Application launch:
1. to run frontend part use docker image `docker run --rm -p 3000:3000 ghcr.io/akmeevd/ads-frontend-react:latest`
2. for running backend part:
- pack jar file by command `mvn clean install`
- run app from dir target by command `java -Dspring.datasource.username=*** -Dspring.datasource.password=*** -jar ads-0.0.1-SNAPSHOT.jar`,  *** - is username and password to DB

> Warning! For running app you have to install:
> -  JRE version 11 and more
> -  PostgreSQL
