# Payment Integration System

### Milestones
- [X] jwt extensions
- [X] reactive APIs
- [X] validation on requests
- [X] handle bulk payments on scale with Rabbitmq
- [X] establish pipelining for payment flows
- [ ] encryption on messages
- [ ] custom exception handling
- [ ] Rabbitmq dead letter queue handling
- [ ] Rabbitmq retry interceptor
- [ ] postgres database to persist
- [ ] r2dbc for non-blocking I/O with database
- [ ] idempotency key and APIs for all payments
- [X] circuit breakers on APIs
- [ ] rate limits on APIs
- [ ] API fallback handling
- [ ] grpc support

### Reference Documentation

* [Shopify: 10 Tips for Building Resilient Payment Systems](https://shopify.engineering/building-resilient-payment-systems)
* [Validation](https://www.konform.io)
* [Doordash: Functional Core, Imperative Shell – Using Structured Concurrency to Write Maintainable gRPC Endpoints in Kotlin](https://doordash.engineering/2022/07/26/functional-core-imperative-shell-using-structured-concurrency-to-write-maintainable-grpc-endpoints-in-kotlin/)


