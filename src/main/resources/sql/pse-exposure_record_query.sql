-- SCEF GAI Feed: pse-exposure RECORD
-- Fields: R1-R28
-- TODO: confirm REQUEST_TYPE_DESC value

SELECT
    r.REQUEST_ID                                               AS RECORD_ID,          -- R1
    TRIM('NA')                                                 AS RECORD_TYPE,        -- R2
    TRIM(r.CURRENCY_CD)                                        AS TRANSACTION_CCY,    -- R3
    '161534'                                                   AS ORIGINAL_CSI,       -- R4
    TRIM('NA')                                                 AS ORIGINAL_FEED_ID,   -- R5
    TRIM(r.LV_CODE)                                            AS LVID,               -- R6
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
    TRIM(r.UITID)                                              AS UITID,              -- R27
    TO_CHAR(CAST(r.COB_DATE AS DATE), 'YYYYMMDD')              AS COB_DATE            -- R28
FROM ADMCEF.SCEF_REQUEST r
JOIN ADMCEF.SCEF_REQUEST_TYPE rt ON r.REQUEST_TYPE = rt.REQUEST_TYPE
WHERE rt.REQUEST_TYPE_DESC = 'TODO_CONFIRM'
  AND r.STATUS = 2
ORDER BY r.REQUEST_ID
