# Дипломная работа по курсу Java-разработчик

## Описание:
Spring-boot приложение, которое реализует бэкенд-часть сайта объявлений. 

## ТЗ на разработку:
[Ссылка](https://github.com/11th/graduate-work/wiki/%D0%A2%D0%97-%D0%BD%D0%B0-%D1%80%D0%B0%D0%B7%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D0%BA%D1%83)

## Стек:
- Java 11
- Spring Boot
- Spring Security
- PostgreSQL

## Запуск приложения:
1. Для запуска фронтенд-части используйте docker образ `docker run --rm -p 3000:3000 ghcr.io/11th/ads-frontend-react:latest`
2. Для запуска бэкенд-части:
- собрать jar файл с помощью команды `mvn clean install`
- запустить приложение из папки target с помощью команды `java -Dspring.datasource.username=*** -Dspring.datasource.password=*** -jar ads-0.0.1-SNAPSHOT.jar`, где *** - имя пользователя и пароль к БД.

> Важно! Для запуска приложения должно быть установлено:
> -  JRE версии 11 или выше
> -  PostgreSQL
