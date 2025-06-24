#!/bin/bash

# Vault Setup Script for auth-service Application
# This script sets up all required secrets in HashiCorp Vault

set -euo pipefail

# Calculate project root and fallback ENV_FILE path
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$PROJECT_ROOT/.env}"

# Function to load .env file
load_env() {
    local env_file="${1:-.env}"

    if [[ -f "$env_file" ]]; then
        echo "🔄 Loading environment variables from $env_file..."
        set -a
        # shellcheck source=./.env
        source "$env_file"
        set +a
        echo "✅ Environment variables loaded successfully"
    else
        echo "❌ $env_file not found. Please provide the correct .env file."
        exit 1
    fi
}

# Load environment variables from file (or default .env)
load_env "${ENV_FILE:-.env}"

# Check required variables
required_vars=(
  VAULT_URI VAULT_TOKEN
  DB_USERNAME DB_PASSWORD DB_URL
  REDIS_HOST REDIS_PORT REDIS_PASSWORD
  MAIL_HOST MAIL_PORT MAIL_USERNAME MAIL_PASSWORD
  JWT_EXPIRATION_MS JWT_REFRESH_EXPIRATION_MS
  GRAYLOG_HOST GRAYLOG_PORT GRAYLOG_PASSWORD_SECRET GRAYLOG_ROOT_PASSWORD_SHA2
  FRONTEND_URL
)

for var in "${required_vars[@]}"; do
    if [[ -z "${!var:-}" ]]; then
        echo "❌ Environment variable '$var' is not set."
        exit 1
    fi
done

############################################
# RSA Key Handling
############################################
KEY_DIR="./keys"
PRIVATE_KEY_PATH="$KEY_DIR/private.pem"
PUBLIC_KEY_PATH="$KEY_DIR/public.pem"

mkdir -p "$KEY_DIR"

if [[ ! -f "$PRIVATE_KEY_PATH" ]] || [[ ! -f "$PUBLIC_KEY_PATH" ]]; then
    echo "🔑 Generating RSA key pair..."
    openssl genpkey -algorithm RSA -out "$PRIVATE_KEY_PATH" -pkeyopt rsa_keygen_bits:2048
    openssl rsa -pubout -in "$PRIVATE_KEY_PATH" -out "$PUBLIC_KEY_PATH"
    echo "✅ RSA key pair generated."
else
    echo "ℹ️ RSA key pair already exists – skipping generation."
fi

# Read and escape PEM contents
RSA_PRIVATE_KEY=$(<"$PRIVATE_KEY_PATH")
RSA_PUBLIC_KEY=$(<"$PUBLIC_KEY_PATH")

############################################
# Vault operations
############################################

export VAULT_ADDR="$VAULT_URI"
export VAULT_TOKEN="$VAULT_TOKEN"

echo "🔐 Connecting to Vault at $VAULT_ADDR..."

if ! vault status > /dev/null 2>&1; then
    echo "❌ Error: Cannot connect to Vault at $VAULT_ADDR"
    echo "   Make sure Vault is running and VAULT_TOKEN is correct"
    exit 1
fi

echo "✅ Vault connection successful"
echo "🚀 Writing secrets to Vault..."

vault kv put secret/auth-service \
  database.url="$DB_URL" \
  database.username="$DB_USERNAME" \
  database.password="$DB_PASSWORD" \
  redis.host="$REDIS_HOST" \
  redis.port="$REDIS_PORT" \
  redis.password="$REDIS_PASSWORD" \
  mail.host="$MAIL_HOST" \
  mail.port="$MAIL_PORT" \
  mail.username="$MAIL_USERNAME" \
  mail.password="$MAIL_PASSWORD" \
  jwt.expiration-ms="$JWT_EXPIRATION_MS" \
  jwt.refresh-expiration-ms="$JWT_REFRESH_EXPIRATION_MS" \
  jwt.rsa-private-key="$RSA_PRIVATE_KEY" \
  jwt.rsa-public-key="$RSA_PUBLIC_KEY" \
  graylog.host="$GRAYLOG_HOST" \
  graylog.port="$GRAYLOG_PORT" \
  graylog.password-secret="$GRAYLOG_PASSWORD_SECRET" \
  graylog.root-password-sha2="$GRAYLOG_ROOT_PASSWORD_SHA2" \
  application.frontend-url="$FRONTEND_URL"

echo ""
echo "✅ All secrets successfully stored at Vault path: secret/auth-service"
echo "🟢 You can now start the auth-service application with Vault integration enabled."

# Cleanup RSA key files after successful Vault write
# Cleanup RSA key files and directory after successful Vault write
if [[ -f "$PRIVATE_KEY_PATH" && -f "$PUBLIC_KEY_PATH" ]]; then
    echo "🧹 Cleaning up local RSA key files..."
    rm -f "$PRIVATE_KEY_PATH" "$PUBLIC_KEY_PATH"
    rmdir "$KEY_DIR" 2>/dev/null || true
    echo "✅ RSA keys and directory removed from local disk"
fi
