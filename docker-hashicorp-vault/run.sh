#!/bin/sh

VAULT_RETRIES=5

echo "Vault is starting..."
until vault status > /dev/null 2>&1 || [ "$VAULT_RETRIES" -eq 0 ]; do
  echo "Waiting for vault to start...: $((VAULT_RETRIES--))"
  sleep 1
done

echo "Authenticating..."
vault login token=confluent

echo "Adding entries..."
vault kv put -mount=secret master-public WrappingMasterKey=xxx
vault kv put -mount=secret master-private WrappingMasterKey=yyy

echo "Complete..."