### fintech transaction pipeline
 
(it will be) an event-driven transaction processing system. instead of just updating a balance column, it will run transactions through a pipeline: validation, fraud check, ledger update, notification. building to work through problems real payment systems deal with such as duplicate requests, partial failures, money movements that can't just be overwritten.
 
stack: rabbitmq, postgresql, docker compose, java + spring boot, flyway 

### status

- full projecct initialization. docker compose: rabbitmq (management ui) + postgres 
- producer api `POST /transactions` with idempotency key handling implemented.
