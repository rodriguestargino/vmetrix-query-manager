-- =============================================================================
-- VMetrix Query Manager — Schema Definition
-- H2 Database (Oracle Compatibility Mode)
-- =============================================================================

-- BUSINESS TABLES
-- =============================================================================

DROP TABLE IF EXISTS TRANSACTION;
DROP TABLE IF EXISTS INSTRUMENT;
DROP TABLE IF EXISTS PARTY;

-- METADATA TABLES
-- =============================================================================

DROP TABLE IF EXISTS META_RELATIONSHIP;
DROP TABLE IF EXISTS META_COLUMN;
DROP TABLE IF EXISTS META_TABLE;

-- =============================================================================

CREATE TABLE PARTY (
    PARTY_ID       NUMBER(19)    PRIMARY KEY,
    PARTY_NAME     VARCHAR2(200) NOT NULL,
    PARTY_TYPE     VARCHAR2(30)  NOT NULL, -- COUNTERPARTY, ISSUER, BROKER, CUSTODIAN
    TAX_ID         VARCHAR2(30),
    COUNTRY        VARCHAR2(3),
    SECTOR         VARCHAR2(50),
    RATING         VARCHAR2(10),
    IS_ACTIVE      NUMBER(1)     NOT NULL -- 1 = active, 0 = inactive
);

CREATE TABLE INSTRUMENT (
    INSTRUMENT_ID   NUMBER(19)    PRIMARY KEY,
    TICKER          VARCHAR2(20),
    INSTRUMENT_NAME VARCHAR2(200) NOT NULL,
    INSTRUMENT_TYPE VARCHAR2(30)  NOT NULL, -- BOND, EQUITY, FUND, DERIVATIVE, DEPOSIT
    ASSET_CLASS     VARCHAR2(30)  NOT NULL, -- FIXED_INCOME, EQUITY, ALTERNATIVES
    ISSUER_ID       NUMBER(19)    REFERENCES PARTY(PARTY_ID),
    CURRENCY        VARCHAR2(3)   NOT NULL,
    MATURITY_DATE   DATE,
    COUPON_RATE     NUMBER(8,4),
    NOMINAL_VALUE   NUMBER(18,2),
    IS_ACTIVE       NUMBER(1)     NOT NULL -- 1 = active, 0 = inactive
);

CREATE TABLE TRANSACTION (
    TXN_ID           NUMBER(19)      PRIMARY KEY,
    TXN_DATE         DATE            NOT NULL,
    TXN_TYPE         VARCHAR2(20)    NOT NULL, -- BUY, SELL, MATURITY, COUPON, DIVIDEND
    QUANTITY         NUMBER(18,6),
    PRICE            NUMBER(18,8),
    AMOUNT           NUMBER(18,2)    NOT NULL,
    CURRENCY         VARCHAR2(3)     NOT NULL,
    STATUS           VARCHAR2(20)    NOT NULL, -- PENDING, APPROVED, SETTLED, CANCELLED
    SETTLEMENT_DATE  DATE,
    PORTFOLIO_ID     NUMBER(19)      NOT NULL,
    INSTRUMENT_ID    NUMBER(19)      REFERENCES INSTRUMENT(INSTRUMENT_ID),
    COUNTERPARTY_ID  NUMBER(19)      REFERENCES PARTY(PARTY_ID),
    CREATED_AT       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- METADATA TABLES
-- =============================================================================

CREATE TABLE META_ENTITY (
    ENTITY_ID      NUMBER(19)    PRIMARY KEY,
    ENTITY_NAME    VARCHAR2(50)  NOT NULL UNIQUE, -- used in API requests (transaction, instrument, party)
    PHYSICAL_TABLE VARCHAR2(50)  NOT NULL,        -- physical table name in DB (TRANSACTION, INSTRUMENT, PARTY)
    DEFAULT_ALIAS  VARCHAR2(10)  NOT NULL,        -- SQL alias used in generated queries (t, i, p)
    DESCRIPTION    VARCHAR2(200)
);

CREATE TABLE META_COLUMN (
    COLUMN_ID         NUMBER(19)    PRIMARY KEY,
    ENTITY_ID         NUMBER(19)    NOT NULL REFERENCES META_ENTITY(ENTITY_ID),
    LOGICAL_NAME      VARCHAR2(50)  NOT NULL, -- camelCase name used in API (txnDate, partyName)
    PHYSICAL_NAME     VARCHAR2(50)  NOT NULL, -- SNAKE_CASE column name in DB (TXN_DATE, PARTY_NAME)
    DATA_TYPE         VARCHAR2(20)  NOT NULL, -- string, number, date, timestamp
    IS_PK             NUMBER(1)     NOT NULL, -- 1 = primary key, 0 = not
    IS_FK             NUMBER(1)     NOT NULL, -- 1 = foreign key, 0 = not
    FK_TARGET_ENTITY  VARCHAR2(50),           -- Name of the target entity (not table)
    FK_TARGET_COLUMN  VARCHAR2(50),           -- Name of the target logical column
    IS_FILTERABLE     NUMBER(1)     NOT NULL, -- 1 = can be used in WHERE clause
    IS_SELECTABLE     NUMBER(1)     NOT NULL  -- 1 = can be used in SELECT clause
);

CREATE TABLE META_RELATIONSHIP (
    REL_ID              NUMBER(19)    PRIMARY KEY,
    SOURCE_ENTITY_ID    NUMBER(19)    NOT NULL REFERENCES META_ENTITY(ENTITY_ID),
    SOURCE_COLUMN       VARCHAR2(50)  NOT NULL, -- FK column on source table
    TARGET_ENTITY_ID    NUMBER(19)    NOT NULL REFERENCES META_ENTITY(ENTITY_ID),
    TARGET_COLUMN       VARCHAR2(50)  NOT NULL, -- PK column on target table
    JOIN_TYPE           VARCHAR2(20)  NOT NULL, -- LEFT JOIN, INNER JOIN
    RELATION_ALIAS      VARCHAR2(50)  NOT NULL  -- Alias used in API requests
);
