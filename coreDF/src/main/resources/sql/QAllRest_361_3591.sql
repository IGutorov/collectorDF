set forceplan on
set nocount on

declare @Date      smalldatetime,
        @sFile     varchar(50),
        @BalanceA  varchar(10)

select @sFile = 'QAllRest_361_3591'

select @Date = convert(smalldatetime, '%1$s', 112),
       @BalanceA = convert(varchar(10), '%2$s')

select @Date = dateadd(dd, -1, @Date)

declare @BalanceID  DSIDENTIFIER

if isnull(@BalanceA, '') = 'false'
  select @BalanceID = 55015845 /* 55015845 || 2140*/
else
  select @BalanceID = 2140

create table #pResList
(
    SPID                     DSIDENTIFIER
    ,ResourceID               DSIDENTIFIER
    ,Date                     DSOPERDAY
    ,DepHash                  varchar(120)
    ,TurnDeb                  DSMONEY
    ,TurnCre                  DSMONEY
    ,TurnDebBs                DSMONEY
    ,TurnCreBs                DSMONEY
    ,Rest                     DSMONEY
    ,RestBs                   DSMONEY
    ,SecurityID               DSIDENTIFIER
    ,PortfolioID              DSIDENTIFIER
    ,FundID                   DSIDENTIFIER
    ,LastDate                 DSOPERDAY
    ,DepartmentID             DSIDENTIFIER
    ,RestFict                 DSMONEY
    ,TurnDebFict              DSMONEY
    ,TurnCreFict              DSMONEY
    ,BalanceID                DSIDENTIFIER
    ,InstitutionID            DSIDENTIFIER
)
create unique index XPKpResList on #pResList(SPID, ResourceID, Date, DepHash, RestBs)

exec au_AccList_Rest_
      @Date = @Date
    , @TurnCalc = 0
    , @Confirmed = 1
    , @InstitutionID = 2000
    , @NoCursor = 1
    , @CalcType = 1 --- исходящие остатки
    , @BalanceID = @BalanceID -- @BalanceAID --- Балансовая область учета
    , @RestoreResList = 0    ---перед расчетом таблица очищается

select convert(varbinary(20), stuff(rtrim(r.Brief), 9, 1, null)) as COMPARERESOURCENUMBER19,
       convert(numeric(15, 0), round(100 * case r.CharType when 2 then -p.Rest else p.Rest end, 0)) as AMOUNT,
       rtrim(r.Brief) as RESOURCENUMBER20,
       convert(varchar(11), stuff(rtrim(r.Brief), 9, 1, '_')) as MAINCRITERION,
       '' as SECONDCRITERION
  from #pResList p
 inner join tResource r (index XPKtResource)
         on r.ResourceID = p.ResourceID
        and substring(r.Brief, 10, 2) in ('01', '02', '03', '04', '05', '09', '10', '11', '12', '13', '14', '15', '16', '17', '21', '51', '90', '96')
        and (left(r.Brief, 5) in ('40817', '40820', '45815', '45915', '47423', '47427', '91312', '91414', '91604', '91704', '91802', '91803')
             or
             left(r.Brief, 4) = '4550'
            )
        and r.FundID = 2
 where p.SPID = @@spid
   and p.Rest <> 0
  order by 1
at isolation 0

