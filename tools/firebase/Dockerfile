FROM node:16.4.2

RUN apt-get update && apt-get install -y openjdk-11-jre-headless
RUN npm install -g firebase-tools
RUN firebase setup:emulators:firestore
RUN firebase setup:emulators:ui
