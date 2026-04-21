# Functional Verification Report

Generated on: 2026-04-21 09:22:03 -03:00

Base URL: `http://localhost:8080`

## Summary

- Passed: 11
- Failed: 0
- Run unit tests: True
- Started application: True

## Unit Test Suite

- Status: PASS
- Details: mvn test completed successfully.

## Results

### Case 1.1 - List all Transactions

- Status: PASS
- Details: rows=15

### Case 1.2 - Filter by Currency

- Status: PASS
- Details: rows=5 currencies=USD

### Case 2.1 - Multi-hop Join

- Status: PASS
- Details: rows=15 countries=CL,CO,US

### Case 3.1 - Nested Boolean Logic

- Status: PASS
- Details: SELECT t.TXN_ID, t.STATUS, t.AMOUNT FROM TRANSACTION t WHERE ((t.STATUS = :p1 AND t.AMOUNT > :p2) OR t.STATUS = :p3)

### Case 3.2 - Pagination and Sorting

- Status: PASS
- Details: rows=5 topAmount=11750000

### Case 4.1 - Unknown Field Error

- Status: PASS
- Details: status=400

### Case 5.1 - Metadata Entities

- Status: PASS
- Details: entities=instrument,transaction,party

### Case 5.2 - Metadata Comparators

- Status: PASS
- Details: string=7 number=10

### Case 5.3 - Query Validation

- Status: PASS
- Details: valid=True

### Case 5.4 - SQL Generation

- Status: PASS
- Details: SELECT t.TXN_ID FROM TRANSACTION t WHERE t.STATUS = :p1

### Case 5.5 - Data Execution

- Status: PASS
- Details: rows=9

