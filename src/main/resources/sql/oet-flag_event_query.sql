-- SCEF GAI Feed: oet-flag EVENT
-- Fields: E1, E2, E3, E4, E8, E9, E21, E22, E23
-- TODO: confirm REQUEST_TYPE_DESC value

SELECT DISTINCT
    r.REQUEST_ID                                               AS EVENT_ID,           -- E1
    'Manual'                                                   AS ADJUSTMENT_METHOD,  -- E2
    'Overwrite Missing or Inaccurate Data'                     AS ADJUSTMENT_TYPE,    -- E3
    'Risk User Adjustment - OET Flag - Trade'                                                AS REASON_CODE,        -- E4
    'Closed'                                                   AS EVENT_STATUS,       -- E8
    '161534'                                                   AS ADJUSTING_SYSTEM,   -- E9
    'Daily'                                                  AS FREQUENCY,          -- E21
    TRIM(r.REQUEST_CREATOR)                                    AS REQUESTOR_SOEID,    -- E22
    TO_CHAR(CAST(r.COB_DATE AS DATE), 'YYYYMMDD')              AS COB_DATE            -- E23
FROM ADMCEF.SCEF_REQUEST r
JOIN ADMCEF.SCEF_REQUEST_TYPE rt ON r.REQUEST_TYPE = rt.REQUEST_TYPE
WHERE rt.REQUEST_TYPE_DESC = 'TODO_CONFIRM'
  AND r.STATUS = 2
ORDER BY r.REQUEST_ID
