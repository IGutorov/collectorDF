declare @Date                smalldatetime,
        @sFile               varchar(50),
        @TransportTypeFilter int,
        @MBObjectTypeID_DAR  DSIDENTIFIER,
        @MBObjectTypeID_DARF DSIDENTIFIER

set forceplan on

select @sFile = 'Docs361'

select @Date = convert(smalldatetime, '%1$s', 112),
       @TransportTypeFilter = convert(int, %2$s) -- фильтр отбора документов. 1 - только ПЭнгайн, 2 - только с SAK (DocAllRur), 3 - без фильтра

if isnull(@TransportTypeFilter, 0) not in (1, 2, 3) 
  select @TransportTypeFilter = 3

select @MBObjectTypeID_DAR = ot.MBObjectTypeID
  from tMBObjectType  ot  (index XAK1tMBObjectType)
 where ot.Brief = 'DocAllRur'
at isolation read uncommitted

select @MBObjectTypeID_DARF = ot.MBObjectTypeID
  from tMBObjectType  ot  (index XAK1tMBObjectType)
 where ot.Brief = 'DOCALLRURFAST'
at isolation read uncommitted

select -- критерии идентификации документа
       -- ExternalID (+ DocNumber, если ExternalID не задан) = сверочный unique (проверить на клиенте)
       convert(varbinary(80), coalesce((select max(ib.paymentID)
                                          from ep_Hist_IB_RequestCreateDoc  ib  (index ind_DealTransactID)
                                         where ib.DealTransactID = dt.DealTransactID
                                       ), s.SAK, sf.SAK, '')) as ExternalID,
       -- сверяемые атрибуты оборота
       dt.DocNumber as DocNumber,
       convert(numeric(15, 0), round(100 * dt.FixQty, 0)) as AmountDeb,
       convert(numeric(15, 0), round(100 * dt.Qty   , 0)) as AmountCre,
       isnull(rtrim(rDeb.Brief), '') as AccountDeb,
       isnull(rtrim(rCre.Brief), '') as AccountCre,
       -- информационные атрибуты (для протокола сверки)
       -- convert(varchar(6), substring(rDeb.Brief, 6, 3) + substring(rCre.Brief, 6, 3)) as FundPair, -- для 3.5.9 валюты нектуальны
       dt.DealTransactID as DOCID,
       dt.InDateTime as INDATETIME,
       dt.Comment as DOCCOMMENT,
       isnull(u.Brief, 'unknown') as USERBRIEF,
       isnull(pu.Brief, 'unknown') as BATCHBRIEF,
       isnull(i.Brief, 'unknown') as FOBrief
  from tDealTransact  dt  (index XIE8tDealTransact)
  left join tResource  rDeb  (index XPKtResource)
         on rDeb.ResourceID = dt.ResourceID
        and rDeb.InstitutionID = 2000
        and rDeb.FundID = 2
        and rDeb.BalanceID in (2140, 55015845) -- А и В сидели на трубе
        and substring(rDeb.Brief, 10, 2) in ('01', '02', '03', '04', '05', '09', '10', '11', '12', '13', '14', '15', '16', '17', '21', '51', '90', '96')
        and (left(rDeb.Brief, 5) in ('40817', '40820', '45815', '45915', '47423', '47427', '91312', '91414', '91604', '91704', '91802', '91803')
             or
             left(rDeb.Brief, 4) = '4550'
            )
  left join tResource  rCre  (index XPKtResource)
         on rCre.ResourceID = dt.ResourcePsvID
        and rCre.InstitutionID = 2000
        and rCre.FundID = 2
        and rCre.BalanceID in (2140, 55015845) -- А и В сидели на трубе
        and substring(rCre.Brief, 10, 2) in ('01', '02', '03', '04', '05', '09', '10', '11', '12', '13', '14', '15', '16', '17', '21', '51', '90', '96')
        and (left(rCre.Brief, 5) in ('40817', '40820', '45815', '45915', '47423', '47427', '91312', '91414', '91604', '91704', '91802', '91803')
             or
             left(rCre.Brief, 4) = '4550'
            )
  left join tMBSak  s  (index XPKtMBSak)
         on s.ObjectID = dt.DealTransactID
        and s.MBObjectTypeID = @MBObjectTypeID_DAR
  left join tMBSak  sf  (index XPKtMBSak)
         on sf.ObjectID = dt.DealTransactID
        and sf.MBObjectTypeID = @MBObjectTypeID_DARF
  left join tPropertyUsr  pu  (index XPKtPropertyUsr)
         on pu.PropertyUsrID = dt.BatchID
  left join tUser  u  (index XPKtUser)
         on u.UserID = isnull(dt.UserID, dt.UserID)
  left join tInstrument  i  (index XPKtInstrument)
         on i.InstrumentID = dt.InstrumentID
 where dt.TransactType = 5
   and dt.Date = @Date
   and dt.Confirmed = 1
   and (rDeb.ResourceID is not null
        or
        rCre.ResourceID is not null
       )
   and 'true'  = case 
                   when @TransportTypeFilter = 3 then 'true'
                   when @TransportTypeFilter = 2 and (s.SAK is not null or sf.SAK is not null) then 'true'
                   when exists(select 1
                                 from ep_Hist_IB_RequestCreateDoc  ib  (index ind_DealTransactID)
                                where ib.DealTransactID = dt.DealTransactID
                                  and isnull(ib.paymentID, '') <> ''
                                  and @TransportTypeFilter = 1
                            ) then 'true'
                   else 'false'
                 end
 order by 1, 2
at isolation 0

/*
checktest: 40_2 <-> 48_2 (20150323)
*/

/*
-- сортировка по критерию 1 и 2
1) ExternalID: SAK/PaymentID
2) DocNumber : Номер документа. Если 1) - пусто он идентификатор, а если ещё и не уникален - ошибка.
-- сверяемые атрибуты
3) AmountDeb : сумма по дебету
4) AmountCre : сумма по кредиту
5) AccountDeb : счёт по дебету, если не сверяемый, то пустая строка
6) AccountCre : счёт по кредиту, если не сверяемый, то пустая строка
-- информационные атрибуты
-- 7) FundPair
8) DocID
9) InDateTime : время создания
10) DocComment
11) UserBrief
12) BatchBrief
13) FOBrief
*/


/*
182 000 (DocAllRur) from 886 000 (tDealTransact)
time: 75 sec (100 percents cash hits)
statistics:
Table: ep_Hist_IB_RequestCreateDoc scan count 182563, logical reads: (regular=9920 apf=0 total=9920), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tDealTransact scan count 1, logical reads: (regular=834579 apf=0 total=834579), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tResource scan count 885754, logical reads: (regular=3346985 apf=0 total=3346985), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tResource scan count 885754, logical reads: (regular=3432495 apf=0 total=3432495), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tMBSAK scan count 182564, logical reads: (regular=880990 apf=0 total=880990), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tMBSAK scan count 182564, logical reads: (regular=12 apf=0 total=12), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tPropertyUsr scan count 182563, logical reads: (regular=247310 apf=0 total=247310), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tUser scan count 182563, logical reads: (regular=225745 apf=0 total=225745), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: tInstrument scan count 182563, logical reads: (regular=190667 apf=0 total=190667), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: ep_Hist_IB_RequestCreateDoc scan count 0, logical reads: (regular=0 apf=0 total=0), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Table: Worktable1  scan count 0, logical reads: (regular=199943 apf=0 total=199943), physical reads: (regular=0 apf=0 total=0), apf IOs used=0
Total actual I/O cost for this command: 18737292.
*/