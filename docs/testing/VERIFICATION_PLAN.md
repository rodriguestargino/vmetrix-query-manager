# Verification Plan — Query Manager POC Completion

This document outlines the strategy for verifying that the VMetrix Query Manager meets all technical challenge specifications.

## 1. Automated Test Verification
Run the full suite of automated tests to ensure all layers (Domain, Application, API) are functioning correctly.
- **JoinResolverTest**: Logic for transitive (multi-hop) joins.
- **FilterTreeEngineTest**: Logic for nested boolean conditions.
- **QueryAssemblerTest**: Logic for parameterized SQL generation.
- **QueryControllerIntegrationTest**: End-to-end API flows.

Execute with:
```bash
mvn test
```

## 2. API Functional Scenarios
Manually verify the endpoints using the [TEST_SUITE.md](./TEST_SUITE.md) JSON payloads.
1. **Metadata Discovery**: Ensure the data model is correctly exposed.
2. **Multi-hop Join Resolution**: Verify bridges (Transaction -> Instrument -> Issuer) are correctly identified.
3. **Complex Boolean Logic**: Verify nested AND/OR filter trees.
4. **Structured Error Handling**: Verify `GlobalExceptionHandler` and `ErrorResponse` formatting.
5. **Execution API**: Verify actual data retrieval from the H2 database.

## 3. Requirement Checklist
- [x] **Hexagonal Architecture**: Domain logic isolated from Spring/JPA.
- [x] **SQL-injection Safe**: All values are bound via parameters.
- [x] **Metadata-Driven**: No hardcoded entity logic in the execution engine.
- [x] **BFS Join Resolution**: Automatic discovery of bridge tables.
- [x] **Execution API**: `POST /api/query/execute` implemented and verified.
