FROM gradle:jdk11 as builder

WORKDIR /app
COPY . /app

RUN gradle jar

FROM adoptopenjdk/openjdk11:jre

COPY --from=builder /app/build/libs/*.jar /diagnostyka-scraper.jar

CMD chmod -R 777 /diagnostyka_downloads && java -jar /diagnostyka-scraper.jar