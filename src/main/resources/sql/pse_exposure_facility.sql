-- PSE Exposure - Facility Query
SELECT TRIM('D') as H
     ,TRIM(t.adjustment_id) as ADJ_INVENTORY_ID
     ,TRIM(t.adjustment_id) as ADJUSTMENT_UPLOAD_ID
     ,TRIM('Risk User Adjustment - PSE Exposure - Facility') as ADJUSTMENT_TYPE
     ,to_char(cast(t.last_update_time as date),'MMDDYYYY') as EFFECTIVE_DATE
     ,to_char(cast(t.enter_time as date),'MMDDYYYY') as POSTED_DATE
     ,TRIM(t.CREDIT_OFFICER) as CHECKER_SOEID
     ,TRIM(t.REQUEST_CREATOR) as MAKER_SOEID
     ,TRIM('PSE') as ATTRIBUTE_NAME
     ,TRIM('ORIGINAL_PSE_DETAILS') as OLD_VALUE
     ,TRIM('CONCAT(fiscal_year,accounting_period)') as NEW_VALUE
     ,TRIM('161534') as ORIGINAL_CSI
     ,TRIM('Posted') as ADJUSTMENT_POSTING_STATUS
     ,TRIM('USD') as TRANSACTION_CCY
     ,TRIM('Manual') as ADJUSTMENT_METHOD
     ,TRIM('Risk User Adjustment - PSE Exposure - Facility') as REASON_CODE
     ,TRIM('NA') as SUB_REASON_CODE
     ,TRIM('Closed') as EVENT_STATUS
     ,TRIM('Recurring') as RECURRENCE
     ,TRIM('161534') as ADJUSTING_SYSTEM
     ,TRIM('Daily') as FREQUENCY
     ,TRIM(t.REQUEST_CREATOR) as REQUESTOR_SOEID
     ,TRIM('NA') as STANDARD_ACCOUNT
     ,TRIM('Undetermined') as FRS_BU
     ,TRIM('This is a gap as field cannot be populated for risk adjustments even in future') as BALANCE_TYPE
FROM AOMCRE.V_SCSE_REQUEST_TABLEAU t
WHERE t.request_type='Risk User Adjustment - PSE Exposure - Facility'
  AND t.status='Approved'
  AND t.enter_time >= TO_DATE(:cobDate, 'YYYYMMDD')
GROUP BY t.adjustment_id, t.last_update_time, t.enter_time,
         t.CREDIT_OFFICER, t.REQUEST_CREATOR