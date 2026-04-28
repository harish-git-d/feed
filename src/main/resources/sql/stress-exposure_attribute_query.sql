-- SCEF GAI Feed: stress-exposure ATTRIBUTE
-- Source: ADMCEF.V_SCEF_REQUEST_TABLEAU
-- Fields: A1-A32

SELECT
    TRIM('NA')                                                 AS EVENT_ID,           -- A1
    TRIM(t.request_id)                                         AS RECORD_ID,          -- A2
    TRIM(t.contract_key)                                       AS ATTRIBUTE_ADJUSTMENT_ID, -- A3
    'Stress Exposure'                                          AS ATTRIBUTE_NAME,     -- A4
    t.ORIGINAL_STRESS_IMPACT                                   AS OLD_VALUE,          -- A6
    t.STRESS_IMPACT_OVERRIDE                                   AS NEW_VALUE,          -- A7
    t.last_update_time                                         AS EFFECTIVE_DATE,     -- A8
    t.enter_time                                               AS POSTED_DATE,        -- A9
    TRIM(t.CREDIT_OFFICER)                                     AS CHECKER_SOEID,      -- A10
    TRIM(t.REQUEST_CREATOR)                                    AS MAKER_SOEID,        -- A11
    TRIM('NA')                                                 AS ACCOUNTING_METHODOLOGY, -- A12
    TRIM('NA')                                                 AS FDL_ACCOUNT,        -- A13
    TRIM('NA')                                                 AS GOC,                -- A14
    TRIM('NA')                                                 AS GL_ACCOUNT1,        -- A15
    TRIM('NA')                                                 AS GL_ACCOUNT2,        -- A16
    TRIM('NA')                                                 AS SUB_REASON_CODE,    -- A17
    TRIM('NA')                                                 AS RECURRENCE,         -- A18
    TRIM('NA')                                                 AS STANDARD_ACCOUNT,   -- A19
    TRIM('NA')                                                 AS FRS_BU,             -- A20
    TRIM('NA')                                                 AS BALANCE_TYPE,       -- A23
    TRIM('NA')                                                 AS CDE_FLAG,           -- A25
    TO_CHAR(se.COB_DATE, 'YYYYMMDD')                           AS COB_DATE,           -- A31
    TRIM('NA')                                                 AS REPORT_ID           -- A32
FROM ADMCEF.V_SCEF_REQUEST_TABLEAU se
WHERE se.STRESS_IMPACT_OVERRIDE IS NOT NULL
ORDER BY se.request_id, se.contract_key
