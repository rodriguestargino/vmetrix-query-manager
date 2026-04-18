# vmetrix-query-manager

> **Proof of Concept** — Metadata-driven SQL query engine for [VMetrix](https://vmetrix.com), a SaaS investment management platform.

Receives a high-level query specification (columns + filters) via REST API and generates **parameterized SQL** automatically, resolving JOINs between tables based on **relationship metadata stored in the database**.

### Key Constraint

> Adding a new entity or field requires **zero Java code changes**.  
> Everything is driven by metadata configuration.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Data Model](#data-model)
- [How the Metadata Engine Works](#how-the-metadata-engine-works)
- [API Reference](#api-reference)
- [Design Patterns](#design-patterns)
- [Design Decisions](#design-decisions)
- [Getting Started](#getting-started)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 2.7.18 |
| Database | H2 (Oracle compatibility mode) |
| Build | Maven |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| API Docs | Springdoc OpenAPI 1.7.0 (Swagger UI) |
| Testing | JUnit 5 · Mockito · MockMvc |

---

## Architecture

The project follows **Hexagonal Architecture (Ports & Adapters)** to keep the domain completely framework-agnostic.

```
com.vmetrix.querymanager
├── api/                    ← Adapters IN (controllers, DTOs, mappers)
│   ├── controller/             HTTP layer — no business logic
│   ├── dto/request/            Incoming request shapes
│   ├── dto/response/           Outgoing response shapes
│   └── mapper/                 DTO ↔ Domain mapping (MapStruct)
│
├── application/            ← Use cases & orchestration
│   ├── service/                QueryService, MetadataService
│   └── port/                   Application-level interfaces
│
├── domain/                 ← Core business logic — NO framework deps
│   ├── model/                  EntityMetadata, FieldMetadata,
│   │                           RelationshipMetadata, FilterNode,
│   │                           JoinNode, QueryResult
│   ├── port/                   MetadataRepository (interface only)
│   └── engine/                 SQL generation engine
│       ├── filter/                 FilterTree, ComparatorStrategy impls
│       ├── join/                   JoinResolver, JoinClauseBuilder
│       └── builder/                SelectClauseBuilder, QueryAssembler
│
├── infrastructure/         ← Adapters OUT (JPA, DB access)
│   ├── persistence/
│   │   ├── entity/                 JPA entities (MetaTableEntity, etc.)
│   │   └── repository/            Spring Data repos (internal only)
│   └── metadata/               JpaMetadataRepository (implements port)
│
└── shared/                 ← Cross-cutting concerns
    ├── exception/              Custom exceptions
    └── constant/               Shared constants
```

### Layer Dependency Rules

```
api           → application, domain, shared
application   → domain, shared
domain        → shared ONLY  (no Spring, no JPA)
infrastructure→ domain, shared
shared        → no imports from other layers
```

---

## Data Model

### Business Tables

Three core business tables connected through foreign keys:

```
TRANSACTION ──── INSTRUMENT_ID ────→ INSTRUMENT
TRANSACTION ──── COUNTERPARTY_ID ──→ PARTY (as counterparty)
INSTRUMENT  ──── ISSUER_ID ────────→ PARTY (as issuer)
```

### Metadata Tables

The engine's runtime model definition — **no code changes needed to add new entities or fields**:

| Table | Purpose |
|---|---|
| `META_TABLE` | Entity definitions (logical name → physical table) |
| `META_COLUMN` | Field definitions per entity (logical name → physical column) |
| `META_RELATIONSHIP` | JOIN relationship definitions between entities |

Schema is initialized via `schema.sql` + `data.sql` with `spring.sql.init.mode=always`.

---

## How the Metadata Engine Works

### 1. Logical → Physical Name Resolution

```
API request field: "txnDate"  (camelCase)
        ↓
MetadataService.findField("transaction", "txnDate")
        ↓
FieldMetadata { physicalName: "TXN_DATE", ... }
        ↓
SQL output:  t.TXN_DATE
```

### 2. Entity Alias Resolution

```
Request entity: "counterparty"  (relation alias, not a table name)
        ↓
MetadataService.findEntityByAlias("counterparty")
        ↓
EntityMetadata { physicalTable: "PARTY", alias: "cp" }
        ↓
SQL:  LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID
```

### 3. Automatic JOIN Resolution

```
Entities requested:  [transaction, instrument, counterparty]
        ↓
JoinResolver: BFS graph traversal over RelationshipMetadata
        ↓
Result: [ JoinNode(transaction → instrument),
          JoinNode(transaction → party/counterparty) ]
```

All table and column names come from metadata — **never hardcoded**.

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/query/build` | Generate parameterized SQL from a query specification |
| `POST` | `/api/query/validate` | Validate a query spec and return structured errors |
| `GET` | `/api/metadata/entities` | List all entities with their fields |
| `GET` | `/api/metadata/comparators` | List available comparators by data type |

> Swagger UI available at `/swagger-ui.html` when the application is running.

### POST `/api/query/build` — Request

```json
{
  "select": [
    { "entity": "transaction", "field": "txnDate" },
    { "entity": "counterparty", "field": "partyName", "alias": "counterpartyName" }
  ],
  "filters": {
    "operator": "AND",
    "conditions": [
      {
        "entity": "transaction",
        "field": "status",
        "comparator": "equals",
        "value": "SETTLED"
      },
      {
        "operator": "OR",
        "conditions": [ "..." ]
      }
    ]
  },
  "sorting": [
    { "entity": "transaction", "field": "txnDate", "direction": "desc" }
  ],
  "maxResults": 500
}
```

### POST `/api/query/build` — Response

```json
{
  "sql": "SELECT t.TXN_DATE, cp.PARTY_NAME AS counterpartyName ...",
  "parameters": { "p1": "SETTLED", "p2": 1000000 },
  "resolvedTables": ["TRANSACTION", "INSTRUMENT", "PARTY"],
  "resolvedJoins": [
    "LEFT JOIN INSTRUMENT i ON t.INSTRUMENT_ID = i.INSTRUMENT_ID"
  ],
  "metadata": {
    "columnCount": 6,
    "filterCount": 4,
    "generatedAt": "2026-04-18T00:00:00Z"
  }
}
```

### Supported Comparators by Data Type

| Type | Comparators |
|---|---|
| `string` | `equals`, `notEquals`, `like`, `in`, `notIn`, `isNull`, `isNotNull` |
| `number` | `equals`, `notEquals`, `greaterThan`, `lessThan`, `greaterOrEqual`, `lessOrEqual`, `between`, `in`, `isNull`, `isNotNull` |
| `date` | `equals`, `notEquals`, `greaterThan`, `lessThan`, `greaterOrEqual`, `lessOrEqual`, `between`, `isNull`, `isNotNull` |
| `timestamp` | `equals`, `notEquals`, `greaterThan`, `lessThan`, `greaterOrEqual`, `lessOrEqual`, `isNull`, `isNotNull` |

---

## Design Patterns

### Composite — Filter Tree

Nested AND/OR filter groups of arbitrary depth.

- `FilterNode` — interface
- `FilterGroup` — composite node (operator + children)
- `FilterCondition` — leaf node (entity, field, comparator, value)

### Strategy — Comparator Mapping

Each comparator produces different SQL. One implementation per comparator resolved at runtime via `ComparatorStrategyFactory`.

| Strategy | SQL Fragment |
|---|---|
| `EqualStrategy` | `column = :p1` |
| `InStrategy` | `column IN (:p1, :p2)` |
| `BetweenStrategy` | `column BETWEEN :p1 AND :p2` |
| `LikeStrategy` | `column LIKE :p1` |
| `IsNullStrategy` | `column IS NULL` |
| ... | ... |

### Builder — SQL Clause Construction

Each SQL clause built independently, then orchestrated by `QueryAssembler`:

- `SelectClauseBuilder` — SELECT projection
- `FromClauseBuilder` — FROM + JOINs
- `OrderByClauseBuilder` — ORDER BY
- `QueryAssembler` — final SQL assembly

### Repository (Port/Adapter)

`MetadataRepository` interface in `domain/port/`, implemented by `JpaMetadataRepository` in `infrastructure/`.

---

## Design Decisions

| Decision | Rationale |
|---|---|
| Metadata stored in DB tables (not YAML/JSON) | True runtime dynamism, no file parsing, SQL-queryable |
| H2 in Oracle compatibility mode | DDL uses Oracle types (`NUMBER`, `VARCHAR2`, `TIMESTAMP`); config: `MODE=Oracle;DB_CLOSE_DELAY=-1` |
| MapStruct for DTO mapping | Compile-time safe, no reflection, less boilerplate |
| JUnit 5 + Mockito + MockMvc | Standard Spring Boot test stack, zero extra config |
| Schema via `schema.sql` + `data.sql` | `spring.sql.init.mode=always` |
| Springdoc OpenAPI 1.7.0 | Compatible with Spring Boot 2.7.x |
| Root entity: `TRANSACTION` | All relationships originate from or pass through TRANSACTION |
| Parameterized SQL only | Filter values always as named bind params (`:p1`, `:p2`) — **no concatenation** |
| TDD workflow | RED → GREEN → REFACTOR, always |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Run

```bash
./mvnw spring-boot:run
```

### Test

```bash
./mvnw test
```

### Swagger UI

Once running, open: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Status

🟡 **Proof of Concept** — This project validates the metadata-driven query engine approach. Not intended for production use without further hardening.

---

## License

Proprietary — VMetrix © 2026
