#!/bin/sh

echo "Waiting for PostgreSQL..."

until nc -z db 5432; do
  sleep 1
done

echo "PostgreSQL is ready!"

exec java $JAVA_OPTS -jar app.jar