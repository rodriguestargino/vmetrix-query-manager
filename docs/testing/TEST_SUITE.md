# API Test Suite — VMetrix Query Manager

Use these payloads to verify the functional requirements of the Query Manager POC.

## 1. Simple Scenarios

### Case 1.1: List all Transactions
**Endpoint**: `POST /api/query/execute`
```json
{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "txnType" },
    { "entity": "transaction", "field": "amount" },
    { "entity": "transaction", "field": "currency" },
    { "entity": "transaction", "field": "status" }
  ]
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/query/execute \
     -H "Content-Type: application/json" \
     -d '{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "txnType" },
    { "entity": "transaction", "field": "amount" },
    { "entity": "transaction", "field": "currency" },
    { "entity": "transaction", "field": "status" }
  ]
}'
```

### Case 1.2: Filter by Currency
**Endpoint**: `POST /api/query/execute`
```json
{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "amount" },
    { "entity": "transaction", "field": "currency" }
  ],
  "filters": {
    "entity": "transaction",
    "field": "currency",
    "comparator": "equals",
    "value": "USD"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/query/execute \
     -H "Content-Type: application/json" \
     -d '{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "amount" },
    { "entity": "transaction", "field": "currency" }
  ],
  "filters": {
    "entity": "transaction",
    "field": "currency",
    "comparator": "equals",
    "value": "USD"
  }
}'
```

---

## 2. Join Scenarios (Bridges)

### Case 2.1: Multi-hop Join (Transaction -> Instrument -> Issuer)
**Goal**: Verify the BFS Join Resolver finds the bridge table `INSTRUMENT` automatically.
**Endpoint**: `POST /api/query/execute`
```json
{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "issuer", "field": "partyName" },
    { "entity": "issuer", "field": "country" }
  ],
  "filters": {
    "entity": "issuer",
    "field": "isActive",
    "comparator": "equals",
    "value": 1
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/query/execute \
     -H "Content-Type: application/json" \
     -d '{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "issuer", "field": "partyName" },
    { "entity": "issuer", "field": "country" }
  ],
  "filters": {
    "entity": "issuer",
    "field": "isActive",
    "comparator": "equals",
    "value": 1
  }
}'
```

---

## 3. Complex Scenarios

### Case 3.1: Nested Boolean Logic (AND/OR)
**Goal**: Verify recursive Filter Tree Engine.
**Endpoint**: `POST /api/query/build` 
```json
{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "status" },
    { "entity": "transaction", "field": "amount" }
  ],
  "filters": {
    "operator": "OR",
    "conditions": [
      {
        "operator": "AND",
        "conditions": [
          { "entity": "transaction", "field": "status", "comparator": "equals", "value": "SETTLED" },
          { "entity": "transaction", "field": "amount", "comparator": "greaterThan", "value": 5000000 }
        ]
      },
      { "entity": "transaction", "field": "status", "comparator": "equals", "value": "PENDING" }
    ]
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/query/build \
     -H "Content-Type: application/json" \
     -d '{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "status" },
    { "entity": "transaction", "field": "amount" }
  ],
  "filters": {
    "operator": "OR",
    "conditions": [
      {
        "operator": "AND",
        "conditions": [
          { "entity": "transaction", "field": "status", "comparator": "equals", "value": "SETTLED" },
          { "entity": "transaction", "field": "amount", "comparator": "greaterThan", "value": 5000000 }
        ]
      },
      { "entity": "transaction", "field": "status", "comparator": "equals", "value": "PENDING" }
    ]
  }
}'
```

### Case 3.2: Pagination and Sorting
**Endpoint**: `POST /api/query/execute`
```json
{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "amount" }
  ],
  "sorting": [
    { "entity": "transaction", "field": "amount", "direction": "DESC" }
  ],
  "maxResults": 5
}
```

**cURL**:
```bash
curl -X POST http://localhost:8080/api/query/execute \
     -H "Content-Type: application/json" \
     -d '{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "amount" }
  ],
  "sorting": [
    { "entity": "transaction", "field": "amount", "direction": "DESC" }
  ],
  "maxResults": 5
}'
```

---

## 4. Error Scenarios

### Case 4.1: Unknown Field (Structured Error Response)
**Endpoint**: `POST /api/query/execute`
```json
{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "non_existent_field" }
  ]
}
```

**cURL**:
```bash
curl -i -X POST http://localhost:8080/api/query/execute \
     -H "Content-Type: application/json" \
     -d '{
  "select": [
    { "entity": "transaction", "field": "txnId" },
    { "entity": "transaction", "field": "non_existent_field" }
  ]
}'
```
**Expected Response**: `400 Bad Request` with structured JSON.

---

## 5. Endpoints Reference (Isolated Tests)

Use these minimal examples to verify each API endpoint in isolation.

### 5.1 Entities Metadata
**Endpoint**: `GET /api/metadata/entities`
```bash
curl -X GET http://localhost:8080/api/metadata/entities
```

### 5.2 Comparators Metadata
**Endpoint**: `GET /api/metadata/comparators`
```bash
curl -X GET http://localhost:8080/api/metadata/comparators
```

### 5.3 Query Validation
**Endpoint**: `POST /api/query/validate`
```bash
curl -X POST http://localhost:8080/api/query/validate \
     -H "Content-Type: application/json" \
     -d '{
  "select": [{ "entity": "transaction", "field": "txnId" }],
  "filters": { "entity": "transaction", "field": "status", "comparator": "equals", "value": "SETTLED" }
}'
```

### 5.4 SQL Generation (Build)
**Endpoint**: `POST /api/query/build`
```bash
curl -X POST http://localhost:8080/api/query/build \
     -H "Content-Type: application/json" \
     -d '{
  "select": [{ "entity": "transaction", "field": "txnId" }],
  "filters": { "entity": "transaction", "field": "status", "comparator": "equals", "value": "SETTLED" }
}'
```

### 5.5 Data Execution
**Endpoint**: `POST /api/query/execute`
```bash
curl -X POST http://localhost:8080/api/query/execute \
     -H "Content-Type: application/json" \
     -d '{
  "select": [{ "entity": "transaction", "field": "txnId" }],
  "filters": { "entity": "transaction", "field": "status", "comparator": "equals", "value": "SETTLED" }
}'
```
