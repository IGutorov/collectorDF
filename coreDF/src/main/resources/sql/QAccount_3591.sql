set forceplan on
set nocount on

declare @DateStart           smalldatetime,
        @DateEnd             smalldatetime,
        @sFile               varchar(50),
        @Account             char(35),
        @ResourceID          DSIDENTIFIER

select @sFile = 'Account361'

select @DateStart = convert(datetime, '%1$s', 112),
       @DateEnd = convert(datetime, '%2$s', 112),
       @Account = convert(char(35), '%3$s')

if @DateStart > @DateEnd
   select @DateStart = @DateEnd -- ?? 2 client ??

select @ResourceID = r.ResourceID
  from tResource  r  (index XAK1tResource)
 where r.Brief = @Account
   and r.BalanceID in (55015845, 2140)
at isolation 0

select -- критерии идентификации движения по счёту (оборота)
       -- CompareResourceNumber19 + OpCharType + ExternalID = сверочный unique (проверить на клиенте)
       convert(tinyint, case op.CharType when 1 then 1 else 2 end) as TurnCharType, /* 1 - дебет, 2 - кредит */
       convert(varbinary(20), convert(varchar(20), op.OperDate, 12)) as CompareResourceNumber19,
       convert(varbinary(80), coalesce((select max(ib.paymentID)
                                          from ep_Hist_IB_RequestCreateDoc  ib  (index ind_DealTransactID)
                                         where ib.DealTransactID = nullif(op.DealTransactID, 0)
                                       ), s.SAK, sf.SAK, '')) as ExternalID,
       -- сверяемые атрибуты оборота
       convert(varbinary(20), op.Number) as DocNumber,
       convert(numeric(15, 0), round(100 * op.Qty, 0)) as Amount,
       -- информационные атрибуты (для протокола сверки)
       @Account as ResourceNumber20,
       op2.Comment as Comment, -- DocComment ??
       isnull(u.Brief, 'unknown') as USERBRIEF
  from tOperPart  op  (index XIE8tOperPart)
 inner join tOperPart  op2  (index XPKtOperPart)
         on op2.OperationID = op.OperationID
        and op2.CharType = 1
 inner join tResource  r2  (index XPKtResource)
         on r2.ResourceID = op2.ResourceID
  left join tMBSak  s  (index XPKtMBSak)
         on s.ObjectID = nullif(op.DealTransactID, 0)
        and s.MBObjectTypeID = @MBObjectTypeID_DAR
  left join tMBSak  sf  (index XPKtMBSak)
         on sf.ObjectID = nullif(op.DealTransactID, 0)
        and sf.MBObjectTypeID = @MBObjectTypeID_DARF
 where op.ResourceID = @ResourceID
   and op.OperDate between @DateStart and @DateEnd
   and op.InstitutionID = 2000
--   and op.FundID = 2
   and op.Confirmed = 1 -- факт
   and op.Qty <> 0
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

