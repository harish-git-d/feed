-- SCEF GAI Feed: stress-exposure RECORD
-- One record row per trade (contract_key) for the given COB date.
SELECT
    se.request_id                                    AS ATTRIBUTE_ADJUSTMENT_ID,
    se.contract_key                                  AS RECORD_ID,
    'Stress Exposure'                                AS ATTRIBUTE_NAME,
    se.last_update_time                              AS EFFECTIVE_DATE,
    se.enter_time                                    AS POSTED_DATE,
    se.CREDIT_OFFICER                                AS CHECKER_SOEID,
    se.REQUEST_CREATOR                               AS MAKER_SOEID,
    'Manual'                                         AS ADJUSTMENT_METHOD,
    'Overwrite Missing or Inaccurate Data'           AS ADJUSTMENT_TYPE,
    'Risk User Adjustment - Stress Exposure - Trade' AS REASON_CODE,
    '161534'                                         AS ADJUSTING_SYSTEM,
    'Daily'                                          AS FREQUENCY,
    se.REQUEST_CREATOR                               AS REQUESTOR_SOEID,
    se.COB_DATE                                      AS COB_DATE,
    se.LV_CODE                                       AS LVID,
    se.UITID                                         AS UITID,
    '161534'                                         AS ORIGINAL_CSI,
    'USD'                                            AS TRANSACTION_CCY,
    'Posted'                                         AS ADJUSTMENT_POSTING_STATUS
FROM SCEF_STRESS_EXPOSURE se
WHERE TO_CHAR(se.COB_DATE, 'YYYYMMDD') = ?
  AND se.STRESS_IMPACT_OVERRIDE IS NOT NULL
ORDER BY se.request_id, se.contract_key
