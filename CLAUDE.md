# CLAUDE.md — vmetrix-query-manager

## What This Project Is
Proof of Concept of a metadata-driven SQL query engine for VMetrix,
a SaaS investment management platform.

The engine receives a high-level query specification (columns + filters)
via REST API and generates parameterized SQL automatically, resolving
JOINs between tables based on relationship metadata stored in the database.

**Key constraint:** adding a new entity or field requires ZERO Java code 
changes. Everything comes from metadata configuration.

---

## Stack
- Java 17
- Spring Boot 2.7.18
- H2 (Oracle compatibility mode)
- Maven
- Lombok
- MapStruct
- JUnit 5 + Mockito + MockMvc
- Springdoc OpenAPI 1.7.0

---

## Architecture — Hexagonal (Ports & Adapters)

### Package Structure

```
com.vmetrix.querymanager
├── api/                → Adapters IN (controllers, DTOs, mappers)
│   ├── controller/         HTTP layer only, no business logic
│   ├── dto/request/        Incoming request shapes
│   ├── dto/response/       Outgoing response shapes
│   └── mapper/             DTO ↔ domain mapping (MapStruct)
│
├── application/        → Use cases, orchestration
│   ├── service/            QueryService, MetadataService
│   └── port/               Application-level interfaces
│
├── domain/             → Core business logic, NO framework deps
│   ├── model/              EntityMetadata, FieldMetadata,
│   │                       RelationshipMetadata, FilterNode,
│   │                       JoinNode, QueryResult
│   ├── port/               MetadataRepository (interface only)
│   └── engine/             SQL generation engine
│       ├── filter/             FilterTree, ComparatorStrategy impls
│       ├── join/               JoinResolver, JoinClauseBuilder
│       └── builder/            SelectClauseBuilder, QueryAssembler
│
├── infrastructure/     → Adapters OUT (JPA, DB access)
│   ├── persistence/
│   │   ├── entity/             JPA entities (MetaTableEntity, etc.)
│   │   └── repository/        Spring Data repos (internal only)
│   └── metadata/           JpaMetadataRepository (implements port)
│
└── shared/             → Cross-cutting concerns
    ├── exception/          Custom exceptions
    └── constant/           Shared constants
```

### Layer Dependency Rules — NEVER VIOLATE

```
api            → can import application, domain, shared
application    → can import domain, shared
domain         → can import shared ONLY (no Spring, no JPA)
infrastructure → can import domain, shared
shared         → no imports from other layers
```

---

## Data Model
Three business tables with these relationships:
- TRANSACTION → INSTRUMENT via INSTRUMENT_ID
- TRANSACTION → PARTY (counterparty) via COUNTERPARTY_ID → PARTY_ID
- INSTRUMENT  → PARTY (issuer) via ISSUER_ID → PARTY_ID

Metadata tables store the model definition at runtime:
- META_TABLE        → entity definitions
- META_COLUMN       → field definitions per entity
- META_RELATIONSHIP → JOIN relationship definitions

---

## Active Design Patterns

### Composite — Filter Tree
Problem: Nested AND/OR filter groups of arbitrary depth
Solution: FilterNode interface implemented by:
FilterGroup → composite node (AND/OR + list of FilterNode)
FilterCondition → leaf node (entity, field, comparator, value)
Where: domain/engine/filter/

### Strategy — Comparator Mapping
Problem: 12 different comparators each producing different SQL
Solution: ComparatorStrategy interface with one impl per comparator
EqualStrategy, GreaterThanStrategy, InStrategy,
BetweenStrategy, IsNullStrategy, LikeStrategy, etc.
Factory: ComparatorStrategyFactory resolves strategy by name
Where: domain/engine/filter/

### Builder — SQL Clause Construction
Problem: SELECT, FROM, JOIN, WHERE, ORDER BY built independently
Solution: Separate builder per clause, QueryAssembler orchestrates
SelectClauseBuilder
FromClauseBuilder
OrderByClauseBuilder
QueryAssembler (orchestrator)
Where: domain/engine/builder/

### Repository (Port/Adapter) — Metadata Access
Problem: Domain needs metadata without depending on JPA/Spring
Solution: MetadataRepository interface in domain/port/
JpaMetadataRepository implements it in infrastructure/
Where: domain/port/ + infrastructure/metadata/

### Factory — Strategy Resolution
Problem: Resolve correct ComparatorStrategy at runtime by name
Solution: ComparatorStrategyFactory holds Map<String, Strategy>
Where: domain/engine/filter/

---

## Strict Rules — NEVER VIOLATE
1. NO hardcoded table names in Java
→ All physical names come from FieldMetadata / EntityMetadata

2. NO hardcoded column names in Java
→ All physical names come from metadata lookup

3. NO SQL value concatenation
→ Filter values ALWAYS as named bind parameters (:p1, :p2)
→ Only column/table names (from trusted metadata) in SQL string

4. NO Spring annotations in domain layer
→ domain/ has zero @Component, @Service, @Repository, @Autowired

5. NO JPA imports in domain layer
→ domain/ has zero javax.persistence or jakarta.persistence imports

6. NO business logic in controllers
→ api/controller/ only: parse request → call service → return response

7. NO Spring Data interfaces leaking into domain
→ JpaRepository only in infrastructure/persistence/repository/

8. Tests written BEFORE implementation (TDD)
→ RED → GREEN → REFACTOR, always

9. Validation collects ALL errors before returning
   → QueryValidator NEVER fails fast on first error
   → Returns complete error list in ValidationResponse with HTTP 400

10. maxResults must be positive if provided
    → Validated before SQL generation
    → Generates FETCH FIRST N ROWS ONLY clause when present

---

## Naming Conventions

Classes:
Controllers: QueryController, MetadataController
Services: QueryService, MetadataService
Repositories: MetadataRepository (port), JpaMetadataRepository (impl)
Mappers: QueryRequestMapper, MetadataMapper
Builders: SelectClauseBuilder, QueryAssembler
Resolvers: JoinResolver, BfsJoinResolver
Strategies: ComparatorStrategy, EqualStrategy, InStrategy
Factories: ComparatorStrategyFactory
Domain models: EntityMetadata, FieldMetadata, JoinNode, QueryResult
JPA entities: MetaTableEntity, MetaColumnEntity
DTOs: QueryRequest, QueryBuildResponse, SelectField
Exceptions: UnknownEntityException, InvalidComparatorException

Fields:
DTOs and domain → camelCase (txnDate, partyName, assetClass)
SQL and metadata → SNAKE_CASE (TXN_DATE, PARTY_NAME, ASSET_CLASS)
Mapping is done via metadata lookup, NOT string manipulation

Test methods:
should_[expected behavior]when[condition]
Example: should_return_empty_joins_when_only_base_entity_requested

---

## Metadata-Driven Design

### How Logical Names Map to Physical Names
Request field: "txnDate" (camelCase, from API consumer)
Lookup: MetadataService.findField("transaction", "txnDate")
Returns: FieldMetadata { physicalName: "TXN_DATE", ... }
Used in SQL: t.TXN_DATE

### How Entity Aliases Map to Tables
Request entity: "counterparty" (relation alias, not table name)
Lookup: MetadataService.findEntityByAlias("counterparty")
Returns: EntityMetadata { physicalTable: "PARTY", alias: "cp" }
JOIN resolved: LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID

### How JOINs Are Resolved
Engine receives: entities [transaction, instrument, counterparty]
JoinResolver: BFS graph traversal over RelationshipMetadata
Result: [JoinNode(transaction→instrument),
JoinNode(transaction→party/counterparty)]
No hardcoding: all table/column names from metadata

---

## API Endpoints
POST /api/query/build      → generate SQL from query spec
POST /api/query/validate   → validate spec, return structured errors
POST /api/query/execute    → generate + run SQL, return actual rows (bonus)
GET  /api/metadata/entities    → list all entities with fields
GET  /api/metadata/comparators → list comparators by data type
POST /api/metadata/reload      → clear and reload metadata cache (bonus)

---

## Request / Response Contract

### POST /api/query/build request shape
```json
{
  "select": [
    { "entity": "transaction", "field": "txnDate" },
    { "entity": "counterparty", "field": "partyName", "alias": "counterpartyName" }
  ],
  "filters": {
    "operator": "AND",
    "conditions": [
      { "entity": "transaction", "field": "status",
        "comparator": "equals", "value": "SETTLED" },
      { "operator": "OR", "conditions": [ ... ] }
    ]
  },
  "sorting": [{ "entity": "transaction", "field": "txnDate", "direction": "desc" }],
  "maxResults": 500
}
```
### POST /api/query/build response shape

```json
{
  "sql": "SELECT t.TXN_DATE, ...",
  "parameters": { "p1": "SETTLED", "p2": 1000000 },
  "resolvedTables": ["TRANSACTION", "INSTRUMENT", "PARTY"],
  "resolvedJoins": ["LEFT JOIN INSTRUMENT i ON t.INSTRUMENT_ID = i.INSTRUMENT_ID"],
  "metadata": { "columnCount": 6, "filterCount": 4, "generatedAt": "..." }
}
```
### Comparators by Data Type
string:    equals, notEquals, like, in, notIn, isNull, isNotNull
number:    equals, notEquals, greaterThan, lessThan, greaterOrEqual,
           lessOrEqual, between, in, isNull, isNotNull
date:      equals, notEquals, greaterThan, lessThan, greaterOrEqual,
           lessOrEqual, between, isNull, isNotNull
timestamp: equals, notEquals, greaterThan, lessThan, greaterOrEqual,
           lessOrEqual, isNull, isNotNull

---

## Current Session Context
Active story:    [PASTE STORY ID HERE]
Story goal:      [PASTE ONE-LINE GOAL HERE]
Acceptance criteria: [PASTE AC HERE]
Current branch:  [PASTE BRANCH NAME HERE]
AI mode:         [GENERATE | ASSIST | REVIEW | AVOID]
Last completed:  [PASTE LAST COMPLETED STORY HERE]

## Known Decisions Already Made

- Metadata stored in DB tables (not YAML/JSON)
  Reason: true runtime dynamism, no file parsing, SQL queryable

- H2 in Oracle compatibility mode
  Reason: DDL uses Oracle types (NUMBER, VARCHAR2, TIMESTAMP)
  Config: MODE=Oracle;DB_CLOSE_DELAY=-1

- MapStruct for DTO mapping (not manual mappers)
  Reason: compile-time safe, no reflection, less boilerplate

- JUnit 5 + Mockito + MockMvc for testing
  Reason: standard Spring Boot test stack, no extra config

- Schema initialized via schema.sql + data.sql
  Config: spring.sql.init.mode=always

- Springdoc OpenAPI 1.7.0 for Swagger UI
  Reason: compatible with Spring Boot 2.7.x

- Root entity for all queries: TRANSACTION
  Reason: all relationships originate from or pass through TRANSACTION

- Hexagonal architecture strictly enforced
  Reason: domain layer has zero Spring/JPA imports; testable in isolation

- BFS used in JoinResolver (not DFS)
  Reason: shortest JOIN path first; deterministic output for same input

- ComparatorStrategy per comparator (not switch/if-else)
  Reason: Open/Closed principle; new comparator = new class, zero existing changes

- Root entity for queries is always TRANSACTION when present
  Reason: all FK relationships originate from or pass through TRANSACTION

- FETCH FIRST N ROWS ONLY for pagination (Oracle-compatible H2 syntax)
  Reason: MODE=Oracle makes this valid; consistent with Oracle target dialect

- SELECT * never generated
  Reason: only explicitly requested fields appear in SELECT clause

## Metadata Schema (DDL Reference)

```sql
CREATE TABLE META_ENTITY (
    ENTITY_ID      NUMBER PRIMARY KEY,
    ENTITY_NAME    VARCHAR2(50)  NOT NULL UNIQUE,  -- logical: 'transaction'
    PHYSICAL_TABLE VARCHAR2(50)  NOT NULL,          -- physical: 'TRANSACTION'
    DEFAULT_ALIAS  VARCHAR2(10)  NOT NULL,          -- SQL alias: 't'
    DESCRIPTION    VARCHAR2(200)
);

CREATE TABLE META_COLUMN (
    COLUMN_ID         NUMBER PRIMARY KEY,
    ENTITY_ID         NUMBER NOT NULL REFERENCES META_ENTITY(ENTITY_ID),
    LOGICAL_NAME      VARCHAR2(50) NOT NULL,   -- camelCase: 'txnDate'
    PHYSICAL_NAME     VARCHAR2(50) NOT NULL,   -- SNAKE_CASE: 'TXN_DATE'
    DATA_TYPE         VARCHAR2(20) NOT NULL,   -- 'string'|'number'|'date'|'timestamp'
    IS_PK             NUMBER(1) DEFAULT 0,
    IS_FK             NUMBER(1) DEFAULT 0,
    FK_TARGET_ENTITY  VARCHAR2(50),
    FK_TARGET_COLUMN  VARCHAR2(50),
    IS_FILTERABLE     NUMBER(1) DEFAULT 1,
    IS_SELECTABLE     NUMBER(1) DEFAULT 1
);

CREATE TABLE META_RELATIONSHIP (
    REL_ID         NUMBER PRIMARY KEY,
    SOURCE_ENTITY  VARCHAR2(50) NOT NULL,   -- 'transaction'
    SOURCE_COLUMN  VARCHAR2(50) NOT NULL,   -- 'instrumentId'
    TARGET_ENTITY  VARCHAR2(50) NOT NULL,   -- 'instrument'
    TARGET_COLUMN  VARCHAR2(50) NOT NULL,   -- 'instrumentId'
    JOIN_TYPE      VARCHAR2(10) NOT NULL,   -- 'LEFT'
    RELATION_ALIAS VARCHAR2(50) NOT NULL    -- 'instrument'|'counterparty'|'issuer'
);

CREATE TABLE META_COMPARATOR_TYPE (
    DATA_TYPE  VARCHAR2(20) NOT NULL,
    COMPARATOR VARCHAR2(30) NOT NULL,
    PRIMARY KEY (DATA_TYPE, COMPARATOR)
);
```

## Testing Requirements

### Unit Tests (JUnit 5 + Mockito)
- `FilterProcessorTest` — nested AND/OR groups, all comparator types, empty filters
- `JoinResolverTest` — single entity (no join), two entities, three entities,
  counterparty vs issuer disambiguation
- `ComparatorStrategyFactoryTest` — all valid mappings, unknown comparator throws
- `QueryValidatorTest` — unknown entity, non-filterable field, type mismatch,
  between with wrong value count, full error list returned (not just first)

### Integration Tests (Spring Boot Test + MockMvc)
- `QueryBuildIntegrationTest` — full happy path from HTTP request to SQL response
- `QueryValidationIntegrationTest` — invalid request returns 400 with full error list
- `MetadataIntegrationTest` — GET /api/metadata/entities returns all 3 entities

## 4. Add Commit Strategy section

Add this new section before **AI Interaction Rules**:

## Commit Strategy

Each commit must be atomic and reflect a single logical step:

1.  `chore: project scaffold, dependencies, H2 config`
2.  `feat: DDL schema for data tables and metadata tables`
3.  `feat: seed data — PARTY, INSTRUMENT, TRANSACTION, metadata rows`
4.  `feat: metadata domain model and repository port`
5.  `feat: JpaMetadataRepository and MetadataService with cache`
6.  `feat: NameMapper and ComparatorStrategyFactory`
7.  `feat: QueryValidator — collect all errors before returning`
8.  `feat: FilterProcessor — recursive Composite tree to SQL`
9.  `feat: JoinResolver — BFS automatic JOIN path resolution`
10. `feat: SqlBuilder / QueryAssembler — final SQL construction`
11. `feat: QueryEngine — orchestrates validate → resolve → build`
12. `feat: QueryController and MetadataController`
13. `feat: GlobalExceptionHandler`
14. `test: unit tests for engine components`
15. `test: integration tests for API endpoints`
16. `feat(bonus): execute endpoint with NamedParameterJdbcTemplate`
17. `feat(bonus): metadata reload endpoint`
18. `docs: README with setup, design decisions, AI usage`

## AI Interaction Rules
1. ALWAYS read this file before generating any code
2. NEVER change architecture decisions defined here
3. NEVER introduce patterns not listed in Active Design Patterns
4. ALWAYS flag if a request would violate a Strict Rule
5. ALWAYS generate tests before implementation when in ASSIST mode
6. NEVER generate SQL with concatenated values
7. ASK before adding any new dependency to pom.xml

