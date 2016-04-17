set forceplan on
set nocount on

declare @Date            smalldatetime,
        @ResDebBrAttrID  DSIDENTIFIER,
        @ResCreBrAttrID  DSIDENTIFIER

-- @sFile = 'DocsPE'

select @Date = convert(smalldatetime, '%1$s', 112)

select @ResDebBrAttrID = c.CntParamsID
  from tCntParams  c  (index XAK1tCntParams)
 where c.SysName = 'ResDebBr'
at isolation 0

select @ResCreBrAttrID = c.CntParamsID
  from tCntParams  c  (index XAK1tCntParams)
 where c.SysName = 'ResCreBr'
at isolation 0

select -- критерии идентификации документа: EXTERNALID - сверочный unique (проверить на клиенте)
       convert(varbinary(80), ib.paymentID) as EXTERNALID,
       -- атрибуты оборота использумые для сверки
       convert(varbinary(20), dt.DocNumber) as DOCNUMBER,
       ib.sourceSystem + ' - ' + ib.targetSystem as MAINCRITERION,
       ''                                   as SECONDCRITERION,
       rtrim(isnull((select max(eDeb.Value)
                       from tEntAttrValue  eDeb  (index XPKtEntAttrValue)
                      where eDeb.InterfaceType = 0
                        and eDeb.ObjectID = dt.DealTransactID
                        and eDeb.InstrumentID = dt.InstrumentID
                        and eDeb.AttributeID = @ResDebBrAttrID
                    ), rDeb.Brief))         as ACCOUNTDEB,
       rtrim(isnull((case i.InterfaceObjectID
                       when 66 then (select max(pi2.AccClient)
                                       from tPayInstruct pi2 (index XIE2tPayInstruct)
                                      where pi2.DealTransactID = dt.DealTransactID
                                        and pi2.Belong = 1
                                        and pi2.Direct = 0
                                    )
                       else (select max(eCre.Value)
                               from tEntAttrValue  eCre  (index XPKtEntAttrValue)
                              where eCre.InterfaceType = 0
                                and eCre.ObjectID = dt.DealTransactID
                                and eCre.InstrumentID = dt.InstrumentID
                                and eCre.AttributeID = @ResCreBrAttrID
                            )
                     end
                    ), rCre.Brief))         as ACCOUNTCRE, -- Для платёжных поручений из инструкции
       convert(numeric(15, 0), round(100 * dt.FixQty, 0)) as AMOUNTDEB,
       convert(numeric(15, 0), round(100 * dt.Qty   , 0)) as AMOUNTCRE,
       dt.InDateTime                        as INDATETIME,
       dt.DealTransactID                    as DOCID,
       dt.Comment                           as DOCCOMMENT,
       isnull(u.Brief, 'unknown')           as USERBRIEF,
       isnull(pu.Brief, 'unknown')          as BATCHBRIEF,
       isnull(i.Brief, 'unknown')           as FOBRIEF
  from ep_Hist_IB_RequestCreateDoc  ib  (index ind_plannedDate)
 inner join tDealTransact  dt  (index XPKtDealTransact)
         on dt.DealTransactID = ib.DealTransactID
        and dt.Confirmed = 1
 inner join tResource  rDeb  (index XPKtResource)
         on rDeb.ResourceID = dt.ResourceID
 inner join tResource  r  (index XPKtResource)
         on r.ResourceID = dt.ResourceID
 inner join tResource  rCre  (index XPKtResource)
         on rCre.ResourceID = dt.ResourcePsvID
  left join tPropertyUsr  pu  (index XPKtPropertyUsr)
         on pu.PropertyUsrID = dt.BatchID
  left join tUser  u  (index XPKtUser)
         on u.UserID = isnull(dt.UserID, dt.UserID)
  left join tInstrument  i  (index XPKtInstrument)
         on i.InstrumentID = dt.InstrumentID
 where ib.plannedDate = @Date
   and ib.RetCode = 0
   and ib.DealTransactID <> 0
union all
select convert(varbinary(80), ib.paymentID) as EXTERNALID,
       convert(varbinary(20), ib.DocNumber) as DOCNUMBER,
       ib.sourceSystem + ' - ' + ib.targetSystem as MAINCRITERION,
       ''                as SECONDCRITERION,
       ib.payerAccount   as ACCOUNTDEB,
       ib.payeeAccount   as ACCOUNTCRE,
       convert(numeric(15, 0), round(100 * ib.Amount, 0)) as AMOUNTDEB,
       convert(numeric(15, 0), round(100 * ib.Amount, 0)) as AMOUNTCRE,
       ib.InDateTime     as INDATETIME,
--       datediff(ss, '19700101', ib.InDateTime)  as INDATETIMEUTCNOZONE,
       0                 as DOCID,
       ib.paymentPurpose as DOCCOMMENT,
       ib.caller         as USERBRIEF,
       ib.Batch          as BATCHBRIEF,
       '*Внутрен'        as FOBRIEF
  from ep_Hist_IB_RequestCreateDoc  ib  (index ind_plannedDate)
 where ib.plannedDate = @Date
   and ib.RetCode = 802120 -- SKIP
   and ib.DealTransactID = 0
 order by 1, 2
at isolation 0
