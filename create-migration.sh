#!/bin/bash

# Usage: ./generate-migration.sh [dev|staging|prod]

# This script explicitly focuses on generating SQL files from schema differences

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Error: Configuration file $CONFIG_FILE not found!"
  exit 1
fi

# Load environment variables
set -o allexport
source $CONFIG_FILE
set +o allexport

# Export ENV for docker-compose
export ENV=$ENV

echo "===== Generating Migration Files for $ENV Environment ====="

# Ensure database is running
if ! docker compose ps db | grep -q "Up"; then
  echo "Starting database container..."
  docker compose up db -d
fi

# Wait for database to be ready
echo "Waiting for database to be ready..."
docker compose exec db sh -c "until pg_isready -U $POSTGRES_USER -d $POSTGRES_DB; do sleep 1; done"

# Force verbose output for debugging
echo "Generating migration files with verbose output..."
docker compose run --rm \
  -e SPRING_JPA_PROPERTIES_HIBERNATE_SHOW_SQL=true \
  -e LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG \
  -e LOGGING_LEVEL_ORG_HIBERNATE_TYPE_DESCRIPTOR_SQL=TRACE \
  -e LOGGING_LEVEL_ORG_FLYWAY=DEBUG \
  -e LOGGING_LEVEL_ROOT=INFO \
  app java -jar app.jar \
  --spring.profiles.active=$ENV \
  --app.flyway.autogenerate=true \
  --app.flyway.debug=true \
  --spring.flyway.enabled=false \
  --spring.jpa.hibernate.ddl-auto=update

echo "Checking for generated migration files..."
MIGRATION_DIR="src/main/resources/db/migration/$ENV"
if [ -d "$MIGRATION_DIR" ]; then
  echo "Migration directory exists at: $MIGRATION_DIR"
  echo "Files in migration directory:"
  ls -la "$MIGRATION_DIR"
else
  echo "Warning: Migration directory doesn't exist at: $MIGRATION_DIR"
fi