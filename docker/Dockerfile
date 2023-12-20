FROM alpine:latest

# Install JDK and nginx
RUN apk update && apk add openjdk17 nginx

# Set environment variables for Java
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ENV PATH="$JAVA_HOME/bin:${PATH}"

# Copy nginx configuration and build
COPY nginx.conf /etc/nginx/nginx.conf
COPY build /usr/share/nginx/html

# Copy the tracer app
RUN mkdir /app
COPY tracer.jar /app/tracer.jar

# Copy the start-up script
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 8080

# Start both nginx and tracer
CMD ["/start.sh"]
