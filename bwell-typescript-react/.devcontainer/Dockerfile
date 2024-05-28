# Dockerfile
# Use the official Node.js 21 image.
# https://hub.docker.com/_/node
FROM node:21

# Create and change to the app directory.
WORKDIR /usr/src/app

# Install production dependencies.
RUN apt-get update && apt-get install -y \
  wget \
  unzip \
  libxss1 \
  libappindicator1 \
  libindicator7 \
  libasound2 \
  libnss3 \
  libxtst6 \
  xauth \
  xvfb \
  libgbm-dev \
  --no-install-recommends \
  && wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
  && dpkg -i google-chrome-stable_current_amd64.deb; apt-get -fy install

# Install Playwright and its dependencies
RUN npx playwright install && npx playwright install-deps