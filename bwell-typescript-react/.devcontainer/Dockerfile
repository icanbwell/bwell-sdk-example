# Dockerfile
# Use root.io hardened Node.js 22 Alpine image from bwell's internal AWS ECR mirror.
# Migrated from node:21 (public Docker Hub) per b.well Root.io/JFrog Migration Guide:
# https://icanbwell.atlassian.net/wiki/spaces/CIE/pages/6195544074
# Node.js-specific guide:
# https://icanbwell.atlassian.net/wiki/spaces/CIE/pages/6336446496
FROM 856965016623.dkr.ecr.us-east-1.amazonaws.com/root-mirror/node:22-alpine

# Create and change to the app directory.
WORKDIR /usr/src/app

# Install Chromium and system dependencies for Playwright on Alpine.
# Replaces the previous apt-get + Google Chrome .deb installation (Debian-only).
RUN apk add --no-cache \
  chromium \
  nss \
  freetype \
  harfbuzz \
  ca-certificates \
  ttf-freefont \
  xvfb \
  udev

# Tell Playwright to use the system Chromium instead of downloading its own binary.
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
ENV PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/bin/chromium-browser

# Install Playwright
RUN npx playwright install
