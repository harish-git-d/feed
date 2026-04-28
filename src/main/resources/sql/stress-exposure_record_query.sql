-- SCEF GAI Feed: stress-exposure RECORD
-- Source: ADMCEF.V_SCEF_REQUEST_TABLEAU
-- Fields: R1-R28

SELECT
    TRIM(t.contract_key)                                       AS RECORD_ID,          -- R1
    TRIM('NA')                                                 AS RECORD_TYPE,        -- R2
    'USD'                                                      AS TRANSACTION_CCY,    -- R3
    '161534'                                                   AS ORIGINAL_CSI,       -- R4
    TRIM('NA')                                                 AS ORIGINAL_FEED_ID,   -- R5
    TRIM(t.uitid)                                              AS LVID,               -- R6
    'Posted'                                                   AS ADJUSTMENT_POSTING_STATUS, -- R7
    TRIM('NA')                                                 AS PRODUCT_ATTRIBUTE1, -- R8
    TRIM('NA')                                                 AS PRODUCT_ATTRIBUTE2, -- R9
    TRIM('NA')                                                 AS ESP,                -- R10
    TRIM('NA')                                                 AS EPT,                -- R11
    TRIM('NA')                                                 AS ONBALANCE_TRANSACTION_CCY, -- R16
    TRIM('NA')                                                 AS ONBALANCE_USD,      -- R17
    TRIM('NA')                                                 AS OFFBALANCE_TRANSACTION_CCY, -- R18
    TRIM('NA')                                                 AS OFFBALANCE_USD,     -- R19
    TRIM('NA')                                                 AS PNL_TRANSACTION_CCY, -- R20
    TRIM('NA')                                                 AS PNL_USD,            -- R21
    TRIM('NA')                                                 AS NOTIONAL_TRANSACTION_CCY, -- R22
    TRIM('NA')                                                 AS NOTIONAL_USD,       -- R23
    TRIM('NA')                                                 AS SUPPLEMENTAL_BALANCE_CCY, -- R24
    TRIM('NA')                                                 AS SUPPLEMENTAL_BALANCE_USD, -- R25
    TRIM('NA')                                                 AS UIPID,              -- R26
    TRIM(t.uitid)                                              AS UITID,              -- R27
    TO_CHAR(CAST(t.enter_time AS DATE), 'YYYYMMDD')            AS COB_DATE            -- R28
FROM ADMCEF.V_SCEF_REQUEST_TABLEAU t
WHERE t.request_type = 'NSE Override'
  AND t.status       = 'Approved'
ORDER BY TRIM(t.request_id), TRIM(t.contract_key)
