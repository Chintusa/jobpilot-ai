# Use the official Playwright image for Node 20 / Noble
FROM mcr.microsoft.com/playwright:v1.49.1-noble

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy the rest of the application code
COPY . .

# Build the TypeScript code
RUN npm run build

# Set environment variables
ENV NODE_ENV=production

# Expose port (if your worker acts as a server)
EXPOSE 3001

# Command to run the application
CMD ["npm", "run", "start:prod"]
