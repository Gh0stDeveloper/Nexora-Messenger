FROM node:22-alpine

WORKDIR /app
ENV NODE_ENV=production

COPY package*.json ./
RUN npm install --omit=dev

COPY server.js ./server.js
RUN mkdir -p /app/uploads/avatars

EXPOSE 8080
CMD ["node", "server.js"]
