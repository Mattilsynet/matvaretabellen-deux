#!/bin/sh

# Start the tracer app in the background
java -jar /app/tracer.jar &

# Start nginx
nginx -g 'daemon off;'
