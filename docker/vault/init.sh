#!/bin/bash
set -euo pipefail

VAULT_CONTAINER=$1
VAULT_ADDR=$2
VAULT_INIT_FILE=$3
ENV_FILE=$4

mkdir -p "$(dirname "$VAULT_INIT_FILE")"

echo "Iniciando Vault..."
docker exec -e VAULT_ADDR="$VAULT_ADDR" "$VAULT_CONTAINER" \
  vault operator init -key-shares=1 -key-threshold=1 -address="$VAULT_ADDR" | tee "$VAULT_INIT_FILE"

echo "Vault inicializado com sucesso!"

UNSEAL_KEY=$(grep 'Unseal Key 1:' "$VAULT_INIT_FILE" | awk '{print $4}')
ROOT_TOKEN=$(grep 'Initial Root Token:' "$VAULT_INIT_FILE" | awk '{print $4}')

# Adiciona no .env e substitue caso exista
grep -v '^ROOT_TOKEN=' "$ENV_FILE" > "$ENV_FILE.tmp" && mv "$ENV_FILE.tmp" "$ENV_FILE"
echo "ROOT_TOKEN=$ROOT_TOKEN" >> "$ENV_FILE"

echo "Desbloqueando Vault..."
docker exec -it -e VAULT_ADDR="$VAULT_ADDR" "$VAULT_CONTAINER" vault operator unseal "$UNSEAL_KEY"

echo "Fazendo login..."
docker exec -it -e VAULT_ADDR="$VAULT_ADDR" "$VAULT_CONTAINER" vault login "$ROOT_TOKEN"

echo "Habilitando kv-v2 no secret/ (se não estiver habilitado)..."
docker exec -e VAULT_ADDR="$VAULT_ADDR" "$VAULT_CONTAINER" sh -c "vault secrets enable -path=secret kv-v2 || echo 'secret/ já está habilitado'"

echo "Gravando segredos no Vault..."
SECRETS=""
while IFS='=' read -r key value; do
  value=$(echo "$value" | tr -d '\r')
  if [[ -z "$key" || "$key" =~ ^# ]]; then
    continue
  fi
  echo "Gravando $key=$value"
  SECRETS="$SECRETS $key=$value"
done < "$ENV_FILE"

docker exec -e VAULT_ADDR="$VAULT_ADDR" "$VAULT_CONTAINER" vault kv put secret/model $SECRETS # Necessário ser sem aspas se não fica em uma linha só

echo "Segredos gravados com sucesso!"
