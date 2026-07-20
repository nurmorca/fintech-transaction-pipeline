### fintech transaction pipeline
 
an event-driven transaction processing system. instead of just updating a balance column, it will run transactions through a pipeline: validation, fraud check, ledger update, notification. building to work through problems real payment systems deal with such as duplicate requests, partial failures, money movements that can't just be overwritten.
 
stack: rabbitmq, postgresql, docker compose, java + spring boot, flyway 

is this vibe coded: not really. it is vibe-product-owned by claude though. 

### status

- full projecct initialization. docker compose: rabbitmq (management ui) + postgres 
- producer api `POST /transactions` with idempotency key handling implemented.
- fraud check consumer done. 
- ledger consumer (double-entry) added 
- compensating transactions (say a transaction flagged as fraud after ledger entries were created, with this they are reversed)
- notification consumer (with webhook delivery! cool.) 
- audit trail (persisted state-transition log)
