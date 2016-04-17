set forceplan on
set nocount on

declare @Date                smalldatetime,
        @sFile               varchar(50),
        @TransportTypeFilter int,
        @TypeFilter          varchar(10),
        @MBObjectTypeID_DAR  DSIDENTIFIER,
        @MBObjectTypeID_DARF DSIDENTIFIER

select @sFile = 'QDocs_361_3591'

select @Date = convert(smalldatetime, '%1$s', 112),
       @TypeFilter = convert(varchar(10), '%2$s')

-- фильтр отбора документов. 1 - только ПЭнгайн, 2 - только с SAK (DocAllRur), 3 - без фильтра
select @TransportTypeFilter = case @TypeFilter
                                when 'PE' then 1
                                when 'DocAllRur' then 2
                                else 3
                              end

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
                                       ), s.SAK, sf.SAK, '')) as EXTERNALID,
       -- атрибуты оборота использумые для сверки
       convert(varbinary(20), dt.DocNumber) as DOCNUMBER,
       isnull(left(rDeb.Brief, 5), 'Дебет') + ' - ' + isnull(left(rCre.Brief, 5), 'Кредит') as MAINCRITERION,
       ''    as SECONDCRITERION,
       convert(numeric(15, 0), round(100 * dt.FixQty, 0)) as AMOUNTDEB,
       convert(numeric(15, 0), round(100 * dt.Qty   , 0)) as AMOUNTCRE,
       isnull(rtrim(rDeb.Brief), '') as ACCOUNTDEB,
       isnull(rtrim(rCre.Brief), '') as ACCOUNTCRE,
       dt.InDateTime as INDATETIME,
       -- информационные атрибуты (для протокола сверки)
       -- convert(varchar(6), substring(rDeb.Brief, 6, 3) + substring(rCre.Brief, 6, 3)) as FundPair, -- для 3.5.9 валюты нектуальны
       dt.DealTransactID as DOCID,
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
   and substring(dt.NumberExt, 5, 1) = ' '
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
