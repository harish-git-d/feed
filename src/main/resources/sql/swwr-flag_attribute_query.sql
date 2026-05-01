-- SCEF GAI Feed: swwr-flag ATTRIBUTE — Full field list A1-A33
-- TODO: confirm REQUEST_TYPE_DESC

SELECT
    TRIM('NA')                                                 AS EVENT_ID,              -- A1
    r.REQUEST_ID                                               AS RECORD_ID,             -- A2
    r.ADJUSTMENT_ID                                            AS ATTRIBUTE_ADJUSTMENT_ID, -- A3
    'SWWR'                                             AS ATTRIBUTE_NAME,        -- A4
    TRIM('NA')                                                 AS ATTRIBUTE_DESCRIPTION, -- A5
    TRIM(r.ORIGINAL_FLAG)                                                 AS OLD_VALUE,             -- A6
    TRIM(r.SWWR_FLAG)                                                 AS NEW_VALUE,             -- A7
    r.LAST_UPDATE_TIME      AS EFFECTIVE_DATE,        -- A8
    r.ENTER_TIME            AS POSTED_DATE,           -- A9
    TRIM(r.CREDIT_OFFICER)                                     AS CHECKER_SOEID,         -- A10
    TRIM(r.REQUEST_CREATOR)                                    AS MAKER_SOEID,           -- A11
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
    TRIM(r.COMMENTS)                                           AS ADJUSTMENT_COMMENT,    -- A24
    TRIM('NA')                                                 AS CDE_FLAG,              -- A25
    TRIM('NA')                                                 AS UDF1,                  -- A26
    TRIM('NA')                                                 AS UDF2,                  -- A27
    TRIM('NA')                                                 AS UDF3,                  -- A28
    TRIM('NA')                                                 AS UDF4,                  -- A29
    TRIM('NA')                                                 AS UDF5,                  -- A30
    r.COB_DATE              AS COB_DATE,              -- A31
    TRIM('NA')                                                 AS REPORT_ID,             -- A32
    'Posted'                                                   AS ATTRIBUTE_POSTING_STATUS -- A33
FROM ADMCEF.SCEF_REQUEST r
JOIN ADMCEF.SCEF_REQUEST_TYPE rt ON r.REQUEST_TYPE = rt.REQUEST_TYPE
WHERE rt.REQUEST_TYPE_DESC = 'TODO_CONFIRM'
  AND r.STATUS = 2
ORDER BY r.REQUEST_ID
