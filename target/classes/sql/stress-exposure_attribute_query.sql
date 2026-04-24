-- SCEF GAI Feed: stress-exposure ATTRIBUTE
-- Source view/table: TODO replace with actual SCEF table name
-- Parameter: ? = cobDate (YYYYMMDD or YYYY-MM-DD depending on Oracle format)
SELECT
    se.contract_key                                  AS ATTRIBUTE_ADJUSTMENT_ID,
    se.request_id                                    AS RECORD_ID,
    'Stress Exposure'                                AS ATTRIBUTE_NAME,
    se.ORIGINAL_STRESS_IMPACT                        AS OLD_VALUE,
    se.STRESS_IMPACT_OVERRIDE                        AS NEW_VALUE,
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
    'USD'                                            AS TRANSACTION_CCY,
    '161534'                                         AS ORIGINAL_CSI,
    se.LV_CODE                                       AS LVID,
    'Posted'                                         AS ADJUSTMENT_POSTING_STATUS,
    se.UITID                                         AS UITID
FROM SCEF_STRESS_EXPOSURE se
WHERE TO_CHAR(se.COB_DATE, 'YYYYMMDD') = ?
  AND se.STRESS_IMPACT_OVERRIDE IS NOT NULL
ORDER BY se.request_id, se.contract_key
