FROM node:16.18 AS build-stage

WORKDIR /app

ENV PATH /app/node_modules/.bin:$PATH

COPY package*.json ./

RUN apt-get update && apt-get install -y libnss3-dev libcups2-dev \
    libgconf-2-4 libxss1 libasound2 libatk1.0-0 libatk-bridge2.0-0 \
    libpangocairo-1.0-0 libgtk-3-0

RUN npm install

#RUN npm audit fix

RUN npm install -g @vue/cli

# Define ARG for build-time variables
ARG VUE_APP_API_BASE_URL
ARG VUE_APP_AWS_REGION
ARG VUE_APP_USER_POOL_ID
ARG VUE_APP_USER_POOL_WEB_CLIENT_ID
ARG VUE_APP_GOOGLE_MAPS_API_KEY

# Set ENV variables using the ARG values
ENV VUE_APP_API_BASE_URL $VUE_APP_API_BASE_URL
ENV VUE_APP_AWS_REGION $VUE_APP_AWS_REGION
ENV VUE_APP_USER_POOL_ID $VUE_APP_USER_POOL_ID
ENV VUE_APP_USER_POOL_WEB_CLIENT_ID $VUE_APP_USER_POOL_WEB_CLIENT_ID
ENV VUE_APP_GOOGLE_MAPS_API_KEY $VUE_APP_GOOGLE_MAPS_API_KEY

COPY ./ .


RUN npm run build

# Install serve for production
RUN npm install -g serve

EXPOSE 8080

CMD ["serve", "-s", "dist", "-l", "8080"]