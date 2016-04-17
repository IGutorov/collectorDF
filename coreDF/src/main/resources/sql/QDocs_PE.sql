-- @sFile = 'QDocs_PE'

select to_char(pay.PAYMENTID)                 as EXTERNALID,
       max(nvl(doc.DOCNUM, ' '))              as DOCNUMBER,
       max(nvl(pay.SOURCESYSTEM, 'unknown')  || ' - '  || nvl(pay.TARGETSYSTEM, 'unknown')) as MAINCRITERION,
       ''                                     as SECONDCRITERION,
       max(pay.PAYERACCOUNT)                  as ACCOUNTDEB,
       max(pay.PAYEEACCOUNT)                  as ACCOUNTCRE,
       cast(max(pay.AMOUNT) * 100 as integer) as AMOUNTDEB,
       cast(max(pay.AMOUNT) * 100 as integer) as AMOUNTCRE,
       max(pay.ESBSTATUSCHANGETIME)           as INDATETIME,
       '0'                                    as DOCID,
       max(rtrim(pay.PAYMENTPURPOSE))         as DOCCOMMENT,
       max(nvl(pay.USERLOGIN, ' '))           as USERBRIEF,
       max(nvl(doc.DOCBATCHCODE, ' '))        as BATCHBRIEF,
       case
         when max(substr(nvl(pay.TYPE, 'INT'), 1, 3)) = 'EXT'
           then '**ПлатПор'
         else '*Внутрен'
       end                                    as FOBRIEF
  from NF_PAYMENT.NF_PAYMENT  pay
  left join NF_PAYMENT.NF_PAYDOC doc
         on pay.PAYMENTID = doc.PAYMENTID
 where pay.ESBSTATUSCODE in (2000, 1715)
   and pay.PLANNEDDATE = to_date('%1$s', 'YYYYMMDD')
 group by pay.PAYMENTID
 order by EXTERNALID,
          DOCNUMBER
