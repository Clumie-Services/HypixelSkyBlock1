#!/bin/sh
./gradlew shadowJar && docker-compose down && docker-compose up --build -d
