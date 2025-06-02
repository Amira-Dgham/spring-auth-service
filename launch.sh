#!/bin/bash

# Usage: ./launch.sh [dev|staging|prod]

ENV=${1:-dev}  # Default to dev if not specified
CONFIG_FILE="./config/.env.$ENV"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "❌ Error: Configuration file $CONFIG_FILE not found!"
  exit 1
fi

# Load environment variables
set -o allexport
source "$CONFIG_FILE"
set +o allexport

export ENV=$ENV

echo "🚫 Stopping and removing previous containers for $ENV..."
docker compose down --remove-orphans

echo "🚀 Starting $ENV environment..."
docker compose up auth --build -d

echo "📜 Following logs..."
docker compose logs -f auth