# Use a specific Node.js version for better reproducibility
FROM node:23.3.0-slim AS builder

# Install pnpm globally and install necessary build tools
RUN npm install -g pnpm@9.15.1 && \
    apt-get update && \
    apt-get install -y git python3 make g++ && \
    apt-get install -y python3-pip python3-schedule && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set Python 3 as the default python
RUN ln -s /usr/bin/python3 /usr/bin/python

# Set the working directory
WORKDIR /app

# Copy package.json and other configuration files
COPY package.json ./
COPY pnpm-lock.yaml ./
COPY tsconfig.json ./

# 추가: express, cors, body-parser 패키지 설치
# RUN pnpm add express cors body-parser && \
#     pnpm add -D @types/express @types/cors @types/body-parser

# Copy the rest of the application code
COPY ./src ./src
COPY ./characters ./characters

# Install dependencies and build the project
RUN pnpm install 
RUN pnpm build 

# Create dist directory and set permissions
RUN mkdir -p /app/dist && \
    chown -R node:node /app && \
    chmod -R 755 /app

# Switch to node user
USER node

# Create a new stage for the final image
FROM node:23.3.0-slim

# Install runtime dependencies if needed
RUN npm install -g pnpm@9.15.1

# Python 관련 패키지 설치
COPY requirements.txt /tmp/
RUN apt-get update && \
    apt-get install -y git python3 python3-pip && \
    pip3 install -r /tmp/requirements.txt --break-system-packages && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built artifacts and production dependencies from the builder stage
COPY --from=builder /app/package.json /app/
COPY --from=builder /app/node_modules /app/node_modules
COPY --from=builder /app/src /app/src
COPY --from=builder /app/characters /app/characters
COPY --from=builder /app/dist /app/dist
COPY --from=builder /app/tsconfig.json /app/
COPY --from=builder /app/pnpm-lock.yaml /app/

RUN ls -la /app/src/clients/

EXPOSE 3000
# Set the command to run the application
CMD bash -c "pnpm start --non-interactive & python3 auto_crawler.py && wait"
# CMD ["pnpm", "start", "--non-interactive"]