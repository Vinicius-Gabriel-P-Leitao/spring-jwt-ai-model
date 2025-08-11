VAULT_CONTAINER := vault
ENV_FILE := ./docker/.env
VAULT_ADDR := http://127.0.0.1:8200
VAULT_INIT_FILE := ./docker/vault/vault-init.txt

.PHONY: start-vault start-app wait init clean-init

start-vault:
	@docker compose -f ./docker/compose.yaml up -d vault

start-app:
	@docker compose -f ./docker/compose.yaml up -d postgres
	@docker compose -f ./docker/compose.yaml up -d app

wait:
	@echo "Esperando o Vault responder em $(VAULT_ADDR)..."
	@until curl -s $(VAULT_ADDR)/v1/sys/health >/dev/null 2>&1; do \
		sleep 1; \
	done
	@echo "Vault está no ar!"

init: start-vault wait
	@./docker/vault/init.sh $(VAULT_CONTAINER) $(VAULT_ADDR) $(VAULT_INIT_FILE) $(ENV_FILE)

clean-init:
	@docker compose -f ./docker/compose.yaml up -d vault --force-recreate
	@rm -f $(VAULT_INIT_FILE)
	@echo "🧹 Arquivo de inicialização apagado."
