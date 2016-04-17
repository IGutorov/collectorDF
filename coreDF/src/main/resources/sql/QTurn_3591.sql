set forceplan on
set nocount on

declare @Date                smalldatetime,
        @sFile               varchar(50),
        @MBObjectTypeID_DAR  DSIDENTIFIER,
        @MBObjectTypeID_DARF DSIDENTIFIER

select @sFile = 'QTurn_3591'

select @Date = convert(smalldatetime, '%1$s', 112)

select @MBObjectTypeID_DAR = ot.MBObjectTypeID
  from tMBObjectType  ot  (index XAK1tMBObjectType)
 where ot.Brief = 'DocAllRur'
at isolation read uncommitted

select @MBObjectTypeID_DARF = ot.MBObjectTypeID
  from tMBObjectType  ot  (index XAK1tMBObjectType)
 where ot.Brief = 'DOCALLRURFAST'
at isolation read uncommitted

select -- критерии идентификации движения по счёту (оборота)
       -- CompareResourceNumber19 + OpCharType + ExternalID = сверочный unique (проверить на клиенте)
       convert(tinyint, case op.CharType when 1 then 1 else 2 end) as TURNCHARTYPE, /* 1 - дебет, 2 - кредит */
       convert(varbinary(20), convert(varchar(20), stuff(rtrim(r.Brief), 9, 1, null))) as COMPARERESOURCENUMBER19,
       convert(varbinary(80), coalesce((select max(ib.paymentID)
                                          from ep_Hist_IB_RequestCreateDoc  ib  (index ind_DealTransactID)
                                         where ib.DealTransactID = nullif(op.DealTransactID, 0)
                                       ), s.SAK, sf.SAK, '')) as EXTERNALID,
       -- атрибуты оборота использумые для сверки
       convert(varchar(11), stuff(rtrim(r.Brief), 9, 1, '_')) as MAINCRITERION,
       case op.CharType
         when 1 then 'Дебет'
         else 'Кредит'
       end as SECONDCRITERION,
       convert(varbinary(25), op.Number) as DOCNUMBER,
       convert(numeric(15, 0), round(100 * op.Qty, 0)) as AMOUNT,
       -- информационные атрибуты (для протокола сверки)
       op.DealTransactID as DOCID,
       op.InDateTime as INDATETIME,
       rtrim(r.Brief) as RESOURCENUMBER20,
       rtrim(r2.Brief) as SECONDRESNUMBER,
       case op.CharType
         when 1 then op.Comment
         else op2.Comment
       end as DOCCOMMENT,
       isnull(u.Brief, 'unknown') as USERBRIEF,
       isnull(pu.Brief, 'unknown') as BATCHBRIEF,
       isnull(i.Brief, 'unknown') as FOBRIEF
  from tBranchBalance bb (index XAK1tBranchBalance) 
 inner join tOperPart  op  (index XIE8tOperPart)
         on op.BalanceID = bb.BalanceID
        and op.InstitutionID = 2000
        and op.OperDate = @Date
        and op.FundID = 2  -- Rub only ?
        and op.Confirmed = 1 -- факт
        and op.Qty <> 0
 inner join tResource  r  (index XPKtResource)
         on r.ResourceID = op.ResourceID
        and substring(r.Brief, 10, 2) in ('01', '02', '03', '04', '05', '09', '10', '11', '12', '13', '14', '15', '16', '17', '21', '51', '90', '96')
        and (left(r.Brief, 5) in ('40817', '40820', '45815', '45915', '47423', '47427', '91312', '91414', '91604', '91704', '91802', '91803')
             or
             left(r.Brief, 4) = '4550'
            )
 inner join tOperPart  op2  (index XPKtOperPart)
         on op2.OperationID = op.OperationID
        and op2.CharType = -op.CharType
 inner join tResource  r2  (index XPKtResource)
         on r2.ResourceID = op2.ResourceID
  left join tPropertyUsr  pu  (index XPKtPropertyUsr)
         on pu.PropertyUsrID = op.BatchID
  left join tDealTransact  dt  (index XPKtDealTransact)
         on dt.DealTransactID = op.DealTransactID
  left join tInstrument  i  (index XPKtInstrument)
         on i.InstrumentID = dt.InstrumentID
  left join tUser  u  (index XPKtUser)
         on u.UserID = isnull(dt.UserID, op.UserID)
  left join tMBSak  s  (index XPKtMBSak)
         on s.ObjectID = nullif(op.DealTransactID, 0)
        and s.MBObjectTypeID = @MBObjectTypeID_DAR
  left join tMBSak  sf  (index XPKtMBSak)
         on sf.ObjectID = nullif(op.DealTransactID, 0)
        and sf.MBObjectTypeID = @MBObjectTypeID_DARF
 where bb.InstitutionID = 2000
   and bb.BalanceID in (2140, 55015845) -- А + В
   and bb.AccountingType = 1
 order by 1, 2, 3, 4, 5
at isolation 0

/*
Turn DataSource
CompareResourceNumber19  varchar(20)
TurnCharType             tinyint
ExternalID               varchar(80)
DocNumber                varchar(20)
Amount                   numeric(15, 0)
ResourceNumber20         varchar(20)
DocID                    numeric(15, 0)
InDateTime               DateTime
*/
