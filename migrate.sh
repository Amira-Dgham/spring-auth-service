#!/bin/bash
# db-operations.sh - Comprehensive database operations script

# Usage: ./db-operations.sh [environment] [operation]
# Operations:
#   - migrate: Run migrations
#   - info: Show migration status
#   - validate: Validate migrations
#   - backup: Create database backup
#   - restore: Restore database from backup
#   - rollback: Rollback to previous version

set -e

# Default values
ENV=${1:-dev}
OPERATION=${2:-migrate}
TIMESTAMP=$(date +%Y%m%d%H%M%S)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Validate environment
if [ "$ENV" != "dev" ] && [ "$ENV" != "staging" ] && [ "$ENV" != "prod" ]; then
  echo "Error: Invalid environment. Use dev, staging, or prod."
  exit 1
fi

# Load environment variables
if [ -f "$SCRIPT_DIR/config/.env.$ENV" ]; then
  source "$SCRIPT_DIR/config/.env.$ENV"
else
  echo "Error: Environment file $SCRIPT_DIR/config/.env.$ENV not found"
  exit 1
fi

# Create backup directory if it doesn't exist
BACKUP_DIR="$SCRIPT_DIR/backups/$ENV"
mkdir -p "$BACKUP_DIR"

# Define helper functions
run_flyway() {
  local cmd="$1"
  echo "Running Flyway $cmd in $ENV environment..."

  # For production, add extra safeguards
  local extra_options=""
  if [ "$ENV" = "prod" ]; then
    extra_options="-e FLYWAY_VALIDATE_MIGRATION_NAMING=true"
  fi

  docker run --rm \
    --network=host \
    -v "$SCRIPT_DIR/src/main/resources/db/migration:/flyway/sql" \
    -v "$SCRIPT_DIR/src/main/resources/db/migration/${ENV}:/flyway/sql/${ENV}" \
    -e "FLYWAY_URL=jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}" \
    -e "FLYWAY_USER=${POSTGRES_USER}" \
    -e "FLYWAY_PASSWORD=${POSTGRES_PASSWORD}" \
    -e "FLYWAY_LOCATIONS=filesystem:/flyway/sql,filesystem:/flyway/sql/${ENV}" \
    -e "FLYWAY_BASELINE_ON_MIGRATE=true" \
    -e "FLYWAY_CONNECT_RETRIES=10" \
    $extra_options \
    flyway/flyway:9-alpine \
    $cmd
}

create_backup() {
  echo "Creating backup of $ENV database..."

  # Create backup file
  BACKUP_FILE="$BACKUP_DIR/${POSTGRES_DB}_${TIMESTAMP}.sql"

  # Use pg_dump in a container
  docker run --rm \
    --network=host \
    -v "$BACKUP_DIR:/backups" \
    -e "PGPASSWORD=$POSTGRES_PASSWORD" \
    postgres:13-alpine \
    pg_dump -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -F p -f "/backups/$(basename "$BACKUP_FILE")"

  echo "Backup created at $BACKUP_FILE"
  return 0
}

restore_backup() {
  # List available backups
  echo "Available backups for $ENV environment:"
  ls -1 "$BACKUP_DIR" | grep -E "\.sql$" | cat -n

  # Ask for backup selection
  read -p "Enter backup number to restore (or 0 to cancel): " backup_num

  if [ "$backup_num" = "0" ]; then
    echo "Restore cancelled."
    return 0
  fi

  # Get selected backup file
  SELECTED_BACKUP=$(ls -1 "$BACKUP_DIR" | grep -E "\.sql$" | sed -n "${backup_num}p")

  if [ -z "$SELECTED_BACKUP" ]; then
    echo "Invalid backup selection."
    return 1
  fi

  BACKUP_PATH="$BACKUP_DIR/$SELECTED_BACKUP"

  # Confirmation for production
  if [ "$ENV" = "prod" ]; then
    read -p "WARNING: You are about to restore production database. This is potentially destructive. Type 'CONFIRM' to proceed: " confirmation
    if [ "$confirmation" != "CONFIRM" ]; then
      echo "Restore cancelled."
      return 0
    fi
  fi

  echo "Restoring $ENV database from $BACKUP_PATH..."

  # Drop and recreate database
  docker run --rm \
    --network=host \
    -e "PGPASSWORD=$POSTGRES_PASSWORD" \
    postgres:13-alpine \
    psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -c "DROP DATABASE IF EXISTS ${POSTGRES_DB}_temp; CREATE DATABASE ${POSTGRES_DB}_temp;"

  # Restore to temporary database first
  docker run --rm \
    --network=host \
    -v "$BACKUP_PATH:/backup.sql" \
    -e "PGPASSWORD=$POSTGRES_PASSWORD" \
    postgres:13-alpine \
    psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "${POSTGRES_DB}_temp" -f "/backup.sql"

  # If successful, swap databases

  docker run --rm \
    --network=host \
    -e "PGPASSWORD=$POSTGRES_PASSWORD" \
    postgres:13-alpine \
    psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${POSTGRES_DB}'; DROP DATABASE IF EXISTS ${POSTGRES_DB}_old; ALTER DATABASE ${POSTGRES_DB} RENAME TO ${POSTGRES_DB}_old; ALTER DATABASE ${POSTGRES_DB}_temp RENAME TO ${POSTGRES_DB};"

  echo "Database restored successfully."
  return 0
}

perform_rollback() {
  # Get current version
  current_version=$(run_flyway info | grep "Current version" | grep -oE "[0-9]+\.[0-9]+")

  if [ -z "$current_version" ]; then
    echo "Failed to get current version."
    return 1
  fi

  echo "Current version is $current_version"

  # Find previous version
  previous_version=$(run_flyway info | grep -B 1 "$current_version" | head -1 | grep -oE "[0-9]+\.[0-9]+")

  if [ -z "$previous_version" ]; then
    echo "No previous version found to roll back to."
    return 1
  fi

  echo "Rolling back to version $previous_version"

  # For production, create backup first
  if [ "$ENV" = "prod" ]; then
    echo "Creating backup before rollback..."
    create_backup
  fi

  # Run rollback to specific version
  run_flyway "migrate:${previous_version}"

  echo "Rollback completed."
  return 0
}

# Execute operation
case "$OPERATION" in
  migrate)
    run_flyway "migrate"
    ;;
  info)
    run_flyway "info"
    ;;
  validate)
    run_flyway "validate"
    ;;
  backup)
    create_backup
    ;;
  restore)
    restore_backup
    ;;
  rollback)
    perform_rollback
    ;;
  *)
    echo "Error: Unknown operation '$OPERATION'"
    echo "Valid operations: migrate, info, validate, backup, restore, rollback"
    exit 1
    ;;
esac

echo "Operation '$OPERATION' on '$ENV' environment completed."