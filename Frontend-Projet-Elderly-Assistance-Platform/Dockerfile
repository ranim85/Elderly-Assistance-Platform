# Stage 1: Build the Angular app
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
# We use output-path to ensure it outputs directly to dist/app for simpler nginx copy
RUN npx ng build --configuration=production

# Stage 2: Serve with Nginx
FROM nginx:alpine
# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*
# Copy built angular files
# Note: Angular 17 default builder outputs to dist/<project-name>/browser
# In this workspace, project is frontend-elderly
COPY --from=build /app/dist/frontend-elderly/browser /usr/share/nginx/html
# If angular creates dist/frontend-elderly instead of browser, fallbacks can be handled but standard ng17 is /browser
# Optional: copy custom nginx configuration if needed for routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
