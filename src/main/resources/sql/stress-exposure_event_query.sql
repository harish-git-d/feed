-- SCEF GAI Feed: stress-exposure EVENT
-- Source: ADMCEF.V_SCEF_REQUEST_TABLEAU
-- Fields: E1, E2, E3, E4, E8, E9, E21, E22, E23

SELECT DISTINCT
    TRIM(t.request_id)                                         AS EVENT_ID,           -- E1
    'Manual'                                                   AS ADJUSTMENT_METHOD,  -- E2
    'Overwrite Missing or Inaccurate Data'                     AS ADJUSTMENT_TYPE,    -- E3
    'Risk User Adjustment - Stress Exposure - Trade'           AS REASON_CODE,        -- E4
    'Closed'                                                   AS EVENT_STATUS,       -- E8
    '161534'                                                   AS ADJUSTING_SYSTEM,   -- E9
    'Daily'                                                    AS FREQUENCY,          -- E21
    TRIM(t.REQUEST_CREATOR)                                    AS REQUESTOR_SOEID,    -- E22
    TO_CHAR(CAST(t.enter_time AS DATE), 'YYYYMMDD')            AS COB_DATE            -- E23
FROM ADMCEF.V_SCEF_REQUEST_TABLEAU t
WHERE t.request_type = 'NSE Override'
  AND t.status       = 'Approved'
ORDER BY TRIM(t.request_id)
