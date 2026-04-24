-- OET Flag Query - Risk Reduction Approval for OET trades
select TRIM('D') as H
     ,TRIM(t.request_id) as ADJ_INVENTORY_ID
     ,TRIM(t.request_id) as ADJUSTMENT_UPLOAD_ID
     ,TRIM('Risk User Adjustment - OET Flag - Trade') as ADJUSTMENT_TYPE
     ,TRIM('') as ADJUSTMENT_EFFORT
     ,TRIM('') as REVERSAL_FLAG
     ,TRIM('') as IVID
     ,TRIM('NA') as FRS_BU
     ,to_char(cast(t.last_update_time as date),'MMDDYYYY') as effective_date
     ,to_char(cast(t.enter_time as date),'MMDDYYYY') as posted_date
     ,TRIM('') as reversal_book_date
     ,TRIM('Posted') as adjustment_posting_status
     ,sum(1) record_count
     ,sum(0) JOURNAL_COUNT
     ,sum(0) CONTRACT_COUNT
     ,sum(0) RECORD_COUNT_REJECT
     ,sum(1) RECORD_COUNT_VALID
     ,TRIM('NA') as FDL_ACCOUNT
     ,TRIM('') as GAAP_TYPE
     ,TRIM('NA') as GL_ACCOUNT1
     ,TRIM('NA') as GL_ACCOUNT2
     ,TRIM('NA') as STANDARD_ACCOUNT
     ,TRIM('OET_FLAG') as BALANCE_TYPE
     ,TRIM('NA') as AFFILIATE_BU
     ,TRIM('Indicative') as IND_BAL_FLAG
     ,TRIM('NA') as GOC
     ,TRIM(t.gfcid) as customer_id
     ,TRIM('') enterprise_product
     ,TRIM('NO_FILE') as feed_id
     ,TRIM('161534') as original_csi
     ,TRIM('') as original_feed_id
     ,TRIM(t.JUSTIFICATION) as JOURNAL_COMMENT
     ,TRIM('WS') WS_RT_INDICATOR
     ,TRIM(t.CREDIT_OFFICER) as CHECKER_SOEID
     ,TRIM(t.REQUEST_CREATOR) as MAKER_SOEID
     ,TRIM('') as ABS_Adjustment_AMOUNT
     ,TRIM('') as Adjustment_AMOUNT
     ,TRIM('') as DEBIT_CREDIT
     ,TRIM('') as FUNCTIONAL_AMOUNT
     ,TRIM('USD') TRANSACTION_CCY
     ,TRIM('') as transactional_amount
     ,TRIM('OET_FLAG') as ATTRIBUTE_NAME
     ,TRIM('Function to be used to request approval for OET trades with "non-professional counterparties to benefit PSE/FSLB tenor reduction and PSB amount. (Non-professional: DGPP client classification is not DEALER or MARKET PARTICIPANT)') as ATTRIBUTE_DESCRIPTION
     ,TRIM(t.JUSTIFICATION) as JOURNAL_UPLOAD_DESCRIPTION
     ,TRIM('') as DETAIL_ATTRIBUTE1
     ,TRIM('') as DETAIL_ATTRIBUTE2
     ,TRIM('') as DETAIL_ATTRIBUTE3
     ,TRIM('') as DETAIL_ATTRIBUTE4
     ,TRIM('') as DETAIL_ATTRIBUTE5
     ,TRIM('') as DETAIL_ATTRIBUTE6
     ,TRIM('') as DETAIL_ATTRIBUTE7
     ,TRIM('') as DETAIL_ATTRIBUTE8
     ,TRIM('') as DETAIL_ATTRIBUTE9
     ,TRIM('') as DETAIL_ATTRIBUTE10
     ,TRIM('NA') as FIT_CODE
     ,TRIM('NA') as PRODUCT_ATTRIBUTE1
     ,TRIM('NA') as PRODUCT_ATTRIBUTE2
     ,TRIM(t.uitid) as UITID
     ,TRIM('') as UITID
     ,TRIM('') as GFBN_ID
     ,TRIM('') as SOURCE_SYSTEM_TRANSACTION_ID
     ,TRIM('') as SOURCE_SYSTEM_CSI_ID
     ,TRIM('') as FIRM_ACCOUNT_MNEMONIC
     ,TRIM('') as INSTRUMENT_CUSIP_ID
     ,TRIM('') as QUANTITY
     ,TRIM('Replacement') as DETAIL_ATTRIBUTE11
     ,TRIM('') as DETAIL_ATTRIBUTE12
     ,TRIM('') as DETAIL_ATTRIBUTE13
     ,TRIM('') as DETAIL_ATTRIBUTE14
from AOMCRE.V_SCSE_REQUEST_TABLEAU t
WHERE t.request_type='Risk reduction Approval for OET trades' and t.status='Approved'
  and t.enter_time &gt;= TO_DATE(#{ibd}, 'MMDDYYYY')
group by
    TRIM(t.uitid)
    ,TRIM(t.request_id)
    ,to_char(cast(t.last_update_time as date),'MMDDYYYY')
    ,to_char(cast(t.enter_time as date),'MMDDYYYY')
    ,TRIM(t.gfcid)
    ,TRIM(t.CREDIT_OFFICER)
    ,TRIM(t.REQUEST_CREATOR)
    ,TRIM(t.JUSTIFICATION)