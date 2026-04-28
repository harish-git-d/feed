-- SCEF GAI Feed: pse-month-end ATTRIBUTE
-- Fields: A1-A32
-- TODO: confirm REQUEST_TYPE_DESC value

SELECT
    TRIM('NA')                                                 AS EVENT_ID,           -- A1
    r.REQUEST_ID                                               AS RECORD_ID,          -- A2
    r.ADJUSTMENT_ID                                            AS ATTRIBUTE_ADJUSTMENT_ID, -- A3
    'PSE_ME'                                             AS ATTRIBUTE_NAME,     -- A4
    TO_CHAR(r.CURRENT_OUTSTANDING_PSLE)                                                 AS OLD_VALUE,          -- A6
    TO_CHAR(r.CORRECT_OUTSTANDING_PSLE)                                                 AS NEW_VALUE,          -- A7
    TO_CHAR(CAST(r.LAST_UPDATE_TIME AS DATE), 'YYYYMMDD')      AS EFFECTIVE_DATE,     -- A8
    TO_CHAR(CAST(r.ENTER_TIME AS DATE), 'YYYYMMDD')            AS POSTED_DATE,        -- A9
    TRIM(r.CREDIT_OFFICER)                                     AS CHECKER_SOEID,      -- A10
    TRIM(r.REQUEST_CREATOR)                                    AS MAKER_SOEID,        -- A11
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
    TO_CHAR(CAST(r.COB_DATE AS DATE), 'YYYYMMDD')              AS COB_DATE,           -- A31
    TRIM('NA')                                                 AS REPORT_ID           -- A32
FROM ADMCEF.SCEF_REQUEST r
JOIN ADMCEF.SCEF_REQUEST_TYPE rt ON r.REQUEST_TYPE = rt.REQUEST_TYPE
WHERE rt.REQUEST_TYPE_DESC = 'TODO_CONFIRM'
  AND r.STATUS = 2
ORDER BY r.REQUEST_ID
