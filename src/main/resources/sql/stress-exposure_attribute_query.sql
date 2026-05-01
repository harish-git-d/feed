-- SCEF GAI Feed: stress-exposure ATTRIBUTE
-- Source: ADMCEF.V_SCEF_REQUEST_TABLEAU
-- Full field list A1-A33

SELECT
    TRIM('NA')                                                 AS EVENT_ID,              -- A1
    TRIM(se.request_id)                                        AS RECORD_ID,             -- A2
    TRIM(se.contract_key)                                      AS ATTRIBUTE_ADJUSTMENT_ID, -- A3
    'Stress Exposure'                                          AS ATTRIBUTE_NAME,        -- A4
    'Stress Impact is increase in MTM under a stress condition' AS ATTRIBUTE_DESCRIPTION, -- A5
    se.ORIGINAL_STRESS_IMPACT                                  AS OLD_VALUE,             -- A6
    se.STRESS_IMPACT_OVERRIDE                                  AS NEW_VALUE,             -- A7
    se.last_update_time     AS EFFECTIVE_DATE,        -- A8
    se.enter_time           AS POSTED_DATE,           -- A9
    TRIM(se.CREDIT_OFFICER)                                    AS CHECKER_SOEID,         -- A10
    TRIM(se.REQUEST_CREATOR)                                   AS MAKER_SOEID,           -- A11
    TRIM('NA')                                                 AS ACCOUNTING_METHODOLOGY,-- A12
    TRIM('NA')                                                 AS FDL_ACCOUNT,           -- A13
    TRIM('NA')                                                 AS GOC,                   -- A14
    TRIM('NA')                                                 AS GL_ACCOUNT1,           -- A15
    TRIM('NA')                                                 AS GL_ACCOUNT2,           -- A16
    TRIM('NA')                                                 AS SUB_REASON_CODE,       -- A17
    TRIM('NA')                                                 AS RECURRENCE,            -- A18
    TRIM('NA')                                                 AS STANDARD_ACCOUNT,      -- A19
    TRIM('NA')                                                 AS FRS_BU,                -- A20
    TRIM('NA')                                                 AS AFFILIATE_BU,          -- A21
    TRIM('NA')                                                 AS REVERSAL_FLAG,         -- A22
    TRIM('NA')                                                 AS BALANCE_TYPE,          -- A23
    TRIM(se.comments)                                          AS ADJUSTMENT_COMMENT,    -- A24
    TRIM('NA')                                                 AS CDE_FLAG,              -- A25
    TRIM('NA')                                                 AS UDF1,                  -- A26
    TRIM('NA')                                                 AS UDF2,                  -- A27
    TRIM('NA')                                                 AS UDF3,                  -- A28
    TRIM('NA')                                                 AS UDF4,                  -- A29
    TRIM('NA')                                                 AS UDF5,                  -- A30
    se.COB_DATE             AS COB_DATE,              -- A31
    TRIM('NA')                                                 AS REPORT_ID,             -- A32
    'Posted'                                                   AS ATTRIBUTE_POSTING_STATUS -- A33
FROM ADMCEF.V_SCEF_REQUEST_TABLEAU se
WHERE TO_CHAR(se.COB_DATE, 'YYYYMMDD') = ?
  AND se.STRESS_IMPACT_OVERRIDE IS NOT NULL
ORDER BY se.request_id, se.contract_key
