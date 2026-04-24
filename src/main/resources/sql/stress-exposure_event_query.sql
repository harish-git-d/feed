-- SCEF GAI Feed: stress-exposure EVENT
-- One event row per unique request_id for the given COB date.
SELECT DISTINCT
    se.request_id       AS ATTRIBUTE_ADJUSTMENT_ID,
    se.request_id       AS RECORD_ID,
    se.COB_DATE         AS COB_DATE,
    se.CREDIT_OFFICER   AS CHECKER_SOEID,
    se.REQUEST_CREATOR  AS MAKER_SOEID,
    se.enter_time       AS POSTED_DATE,
    '161534'            AS ADJUSTING_SYSTEM,
    'Daily'             AS FREQUENCY,
    'Closed'            AS EVENT_STATUS,
    'Recurring'         AS RECURRENCE,
    'Manual'            AS ADJUSTMENT_METHOD,
    'Overwrite Missing or Inaccurate Data'           AS ADJUSTMENT_TYPE,
    'Risk User Adjustment - Stress Exposure - Trade' AS REASON_CODE
FROM SCEF_STRESS_EXPOSURE se
WHERE TO_CHAR(se.COB_DATE, 'YYYYMMDD') = ?
  AND se.STRESS_IMPACT_OVERRIDE IS NOT NULL
ORDER BY se.request_id
