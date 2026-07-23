# Build Angular, serve with nginx
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG API_BASE_URL=http://localhost:8080
RUN sed -i "s|apiBaseUrl: 'https://YOUR-BACKEND-URL'|apiBaseUrl: '${API_BASE_URL}'|g" \
    src/environments/environment.prod.ts \
  && npm run build -- --configuration=production

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/BankOne-Frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
