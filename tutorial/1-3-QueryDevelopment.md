![DyDaQ](../images/dydaq.png) 

### Query Development
​
**Step 1. Getting Dynamic Query Instance**
​
Pass GraphQL SPQR provided ResolutionEnvironment(having client gql query details) to get dynamic query instance per request.
​
```java
DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
```

**Step 2 : Query Building**

For query building, DQG(dynamic query builder) provides below types of Query Builder : 
​
1. **SimpleQueryBuilder** : To be used when the result object is only a single object/entity ie.. without any inner object.Sample implementation : [SimpleQueryBuilderExample](../graphql-ddq-example/src/main/java/com/americanexpress/dydaq/graphql/ddq/dynamicquery/demoproj/resolver/SimpleQueryResolver.java)
2. **JoinQueryBuilder**: To be used when the result object is in nested form.Sample implementation : [JoinQueryBuilderExample](../graphql-ddq-example/src/main/java/com/americanexpress/dydaq/graphql/ddq/dynamicquery/demoproj/resolver/JoinQueryResolver.java)
3. **NativeQueryBuilder**: When you want to provide your own query which is difficult to be developed using above two query builders and complex in structure like using dense rank,partition etc.It can be used to create both simple query and joined query.Sample implementation : [NativeQueryBuilderExample](../graphql-ddq-example/src/main/java/com/americanexpress/dydaq/graphql/ddq/dynamicquery/demoproj/resolver/NativeQueryTestResolver.java)
​
Generate SQL query by passing the query builder instance to Dynamic Query Generator.
​
Eg using simple query builder: 
​
```java
SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                            .Builder()
                            .setGraphKey("rootObject")
                            .addWhereCondition("hospital_id", SqlCondition.equals)
                            .build();
String hospitalQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
```
**Step 3: Get Response by executing query and parsing it into response object:**
​
Use Spring JDBC template and result set executor to execute the query and parse the response into the response entity.
​
Result Set Executor example : 
```java
ResultSetExtractor<List<Hospital>> resultSetExtractorHosp =
            JdbcTemplateMapperFactory
            .newInstance()
            .newResultSetExtractor(Hospital.class);
```
​
**Executing Query  Using Spring JDBC template:**
​
```java
List<Hospital> hospitalDetails =  jdbcTemplate.query(hospitalQuery,new Object[] {1}, resultSetExtractorHosp);
```
​
***

**Complete code example :** 
```java 
@Component
public class SimpleQueryResolver {
    @Autowired
    private JdbcTemplate jdbcTemplate;
 
    ResultSetExtractor<List<Hospital>> resultSetExtractorHosp =
         JdbcTemplateMapperFactory
        .newInstance()
        .addKeys("hospital_id")
        .newResultSetExtractor(Hospital.class);
 
 
    @GraphQLQuery
    public List<Hospital> listHospitalById
        (@GraphQLEnvironment ResolutionEnvironment env,@GraphQLArgument(name = "HospitalList") List<Integer> hospitalList) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
            .Builder()
            .setGraphKey(HospitalMeta.GRAPH_LEVEL)
            .addWhereCondition(HospitalMeta.AllColumns.HOSPITAL_ID, SqlCondition.in(2))
            .build();
        String hospitalQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);//getSimpleQuery(simpleQueryBuilder);
        System.out.println("Hospital list By Id :: "+hospitalQuery);
        List<Hospital> hospitalDetails = jdbcTemplate
                                            .query(hospitalQuery,new Object[] {hospitalList.get(0),hospitalList.get(1)},    resultSetExtractorHosp);
        return hospitalDetails;
    }
}
```

***

### DyDaQ Major Classes Core Functions:

1.  **DynamicQueryGenerator Class**

This class takes the graphql query metadata object in the form of ResolutionEnvironment and provides methods to generate sql query dynamically based on the fields selected by the client.

**Methods:**

   - `getInstance(ResolutionEnvironment resEnv)`
      
      Takes instance of ResolutionEnvironment provided by SPQR and returns an instance of DynamicQueryGenerator

   - `getNativeQuery(NativeQueryBuilder queryBuilder)`

      Takes native query builder object and returns a sql query

   - `getSelectQuery(SimpleQueryBuilder queryBuilder)`
     
      Takes simple query builder object and returns a sql query
 
   - `getJoinQuery(JoinQueryBuilder queryBuilder)`
     
      Takes join query builder object and returns a sql query
      
   - `getGraphLevels()`
     
      Returns the graph levels under which the clients requested the fields.These levels are extracted from the graphql query itself.Based on the fields requested under graph level,developer can create query for specific graph/entity.It should be used with SimpleQueryBuilder to fetch data from database and construct a complex/nested response entity.


2. **NativeQueryBuilder Class**

It helps generate a SQL query by taking a native SQL query template.Native query builder can be built for a single table/entity or joining multiple tables and parsing it into nested response entity.Native query builder can be used in case you want to develop some complex query like using dense rank,partition,rank etc which is difficult implement using SimpleQueryBuilder/JoinQueryBuilder. 

NativeQueryBuilder provides two static builder classes, **SimpleBuilder** and **JoinQueryBuilder**

- **SimpleBuilder Class**

This static builder needs to be used when you want to the a query resultset to be wrapped into a single pojo/entity.

   - `setGraphKey(String graphKey)`
     
     Takes GQL graph level of the entity for which you are fetching the result.

   - `setQueryTemplate(String queryTemplate)`

     Takes a Query Template.Template is query without any select columns eg : "from my_table where ..."
     
   - `build()`
     
     Returns an instance of NativeQueryBuilder built using the graphkey and query template passed

- **JoinQueryBuilder Class**
  
This static builder class needs to be used when you want to the a query resultset to be parsed into a complex pojo/entity structure.

   - `setRootKey(String rootKey)`
     
     Takes root key.Root Key is the GQL Graph level of the object into which you want to wrap your whole resultset of the Joined Query.

   - `setQueryTemplate(String queryTemplate)`
     
     Takes a Query Template.Template is query without any select columns eg : " from my_table {a} inner join my_table2 {b}..."
     
   - `addTblAliasToGraphMap(String tableAlias,String graphKey)`
     
     Takes table alias name mentioned in the query template and respective graph level of the entity into which the table columns needs to be parsed.

   - `addaggregateSelectCols(SqlAggregationType aggregateFunc, String columnName)`

     Takes aggregate function of enum type SqlAggregationType and table column name on which aggregation needs to be done.Here the column name should be in form of {table_alias}.columnName where table_alias is alias of table used in native query template.

   - `build()`
     
     Returns an instance of NativeQueryBuilder built using the JoinQueryBuilder object
     
3. **SqlCondition class**

This class needs to be used specially with SimpleQueryBuilder and JoinQueryBuilder when constructing QueryBuilders. 

**Methods:**

   - `between`
     
     adds snippet " between ? and ? " into the resulting SQL query

   - `equal`
     
     adds snippet " = ? " into the resulting SQL query
     
   - `greaterThan`
      
      adds snippet "> ?" into the resulting SQL query`
      
   - `greaterThanOrEqual`

      adds snippet " >= ?" into the resulting SQL query`
      
   - `in(int noOfParams)`
      
      adds noOfParams times "?" inside in clause.
      
      Eg : SqlCondition.in(3) will add " in (?,?,?) " in the resulting SQL query
      
   - `isNull`
      
      adds snippet " is null " into the resulting SQL query
      
   - `lessThan`
      
      adds snippet "< ?" into the resulting SQL query
      
   - `lessThanOrEqual`
      
      adds snippet " <= ?" into the resulting SQL query
      
   - `like`
      
      adds snippet "like ?" into the resulting SQL query
      
   - `notEqual`
      
      adds snippet " != ? " into the resulting SQL query
      
   - `notNull`
      
      adds snippet " is not null " into the resulting SQL query
      
      
4. **SqlSubQueryCondition Class**
      
It has same method as SqlCondition except the conditions are evaluated against the Sub Query results passed.

**Methods:**

   - `like(String subQuery)`
      
      Eg : SqlSubQueryCondition .like("select id from ... limit 1") will add "like (select id from ...)" in the resulting SQL query

   - `greaterThan(String subQuery)`
      
      Eg : SqlSubQueryCondition .greaterThan("select id from ...") will add "> (select id from ...)" in the resulting SQL query
      
   - `lessThan(String subQuery)`
       
      Eg : SqlSubQueryCondition .lessThan("select id from ...") will add "< (select id from ...)" in the resulting SQL query   
   
   - `greaterThanOrEqual(String subQuery)`
      
      Eg : SqlSubQueryCondition .greaterThanOrEqual("select id from ...") will add ">= (select id from ...)" in the resulting SQL query

   - `lessThanOrEqual(String subQuery)`
      
      Eg : SqlSubQueryCondition .lessThanOrEqual("select id from ...") will add " <= (select id from ...)" in the resulting SQL query

   - `equal(String subQuery)`
      
      Eg : SqlSubQueryCondition .equal("select id from ...") will add " = (select id from ...)" in the resulting SQL query

   - `notEqual(String subQuery)`
      
      Eg : SqlSubQueryCondition .notEqual("select id from ...") will add " != (select id from ...)" in the resulting SQL query

   - `isNull`
      
      adds snippet " is null " into the resulting SQL query

   - `notNull`

      adds snippet " is not null " into the resulting SQL query
      
   - `in(String subQuery)`
      
      Eg : SqlSubQueryCondition .in("select id from ...") will add " in (select id from ...)" in the resulting SQL query
      
      
      
5. **SimpleQueryBuilder Class**
      
This class needs to be used when you want to fetch data from a single table into a single entity and the query is simple in nature like having where,having,order by,between and aggregates. Multiple SimpleQueryBuilder can be used to construct a complex response object.With SimpleQueryBuilder we can lazily fire SQL queries ie..execute query for an entity only when the fields have been requested from then entity in the GQL request. 
      
**Methods:**

   - `Builder().setGraphKey(String graphKey)`
     
     Takes GQL graph level of the entity for which you are fetching the result.   
        
   - `Builder().addWhereCondition(String columnName,SqlCondition conditiontype)`
      
      Adds column passed into the where clause.where condition is defined by conditiontype parameter.
      
      Eg: addWhereCondition("colNm",SqlCondition.in(3)) will add "colNm in (?,?,?)" in where clause of the resulting SQL query  
      
   - `Builder().addWhereWithSubQuery(String columnName,String subQuery,@NonNull SqlSubQueryCondition conditiontype)`
      
      This method needs to be used when you want to evaluate with sub query in where clause against the column passed.The where condition is defined by conditionType parameter.
      
      Eg: addWhereWithSubQuery("id","select id from table a limit 1",SqlSubQueryCondition.equal) will add "id = (select id from table a limit 1)" in where clause   
      
   - `Builder().orWhere()`
      
      Adds or between two where condition  
      
   - `Builder().whereStartBracket(),whereCloseBracket()`
      
      To be used to put where conditions in brackets ie .. "(...)".
      
   - `Builder().addOrderByCondition(String columnName)`
     
     Adds passed column in order by clause
      
   - `Builder().addGroupByCondition(String columnName)`
     
     Adds passed column in group by clause
      
   - `Builder().addHavingCondition(String columnName,SqlCondition conditiontype)`
     
     Adds column in having clause with given condition type
     
     Eg: Builder().addHavingCondition("myColNm",SqlCondition.greaterThan) will add " myColNm > ?" in having clause in the resulting SQL query
      
   - `Builder().addHavingConditionWithSubQuery(String columnName,SqlSubQueryCondition sqlCondition,String subQuery)`
     
     This method needs to be used when you want to evaluate with sub query in having clause against the column passed.The where condition is defined by conditionType parameter.   
      
   - `Builder().orHaving()`
     
     Adds or between two having condition   
      
   - `Builder().havingStartBracket(),havingCloseBracket()`
     
     To be used to put having conditions in brackets ie .. "(...)".
     
   - `Builder().orderbyAsc()`
      
      Used after order by clause to order by Ascending
      
   - `Builder().orderbyDesc()`
      
      Used after order by clause to order by descending
   
   - `Builder().addLimitCondition()`
     
     Adds limit condition ie.. adds snippet " limit ?" into the resulting sql query
     
   - `Builder().addOffsetWithLimitCondition()`
      
      Adds both offset and limit condition ie.. adds snippet " limit ?,?" into the resulting sql query
      
   - `avg(String columnName)`
     
     Adds average function on the column name passed
     
   - `sum(String columnName)`
      
      Adds summation function on the column name passed
      
   - `count(String columnName)`
      
      Adds count function on the column name passed
      
   - `max(String columnName)`
      
      Adds min function on the column name passed
      
   - `min(String columnName)`
      
      Adds max function on the column name passed

   -  `addaggregateSelectCols(SqlAggregationType aggregateFunc, String columnName)`

      Takes aggregate function of enum type SqlAggregationType and table column name on which aggregation needs to be done.
      
6. **JoinQueryBuilder Class**
       
JoinQueryBuilder is to be used when the result you want to fetch a list of nested object.And the relation among the object have complex relations which you don't want to handle yourself using SimpleQueryBuilder.Simply saying,when you want to fetch list of a entity(say ContactDetails) which again holds reference to other entity(say its CreatorDetails entity),you can use JoinQueryBuilder. 
       
Even though JoinQueryBuilder helps you avoid lot of boiler plate code,a single joined query is formed and is executed.Hence even if you have not queried some of the entities from GQL end,the join query will have these tables.

**Methods:**

   - `Builder().setRootKey(String rootObject)`

     Takes root key.Root Key is the GQL Graph level of the entity into which you want to wrap your whole resultset of the Joined Query.

   - `Builder().joinWithGraph(String graphKey,SqlJoinType joinType)`
     
     GraphKey are the graph levels in the GQL request payloads and also represents entities in DynamicQuery.This method joins the input graphkey with the graphkey passed the in joinWithGraph immediately above it.If no joinWithGraph is mentioned,the input graphlevel will be joined with the rootKey above it.
     
     Eg :
     ```yaml
         JoinQueryBuilder joinQueryBuider = new JoinQueryBuilder
          .Builder()
          .setRootKey(HospMeta.GRAPH_LEVEL)
          .joinWithGraph(HospMeta.Surgeon.GRAPH_LEVEL, SqlJoinType.INNER_JOIN) //Entity with Graph level surgeon will be inner joined with entity with graph level HospMeta.GRAPH_LEVEL
          .joinWithGraph(HospMeta.Surgeon.DocSpeciality.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)//entity with graph level HospMeta.Surgeon.DocSpeciality.GRAPH_LEVEL will be inner joined with entity with graph level HospMeta.Surgeon.GRAPH_LEVEL
     ```
      For graph level generation from there entities like HospMeta.GRAPH_LEVEL,use meta class generator plugin of dynamic query.
    
   - `Builder().addWhereCondition(String graphKey,String columnName,@NonNull SqlCondition conditiontype)`

      Generates column name using the graphkey and column name and adds the snippet where clause into resulting SQL query.
   
       Eg : Builder().addWhereCondition("myGL","myColNm",SqlCondition.equal) will add snippet "myGL.myColNm = ?" in where clause of resulting SQL query

   - `Builder().addWhereWithSubQuery(@NonNull String graphKey, String columnName, String subQuery, @NonNull SqlSubQueryCondition sqlCondition)`
     
     This method needs to be used when you want to evaluate with sub query in where clause against the column passed.The where condition is defined by conditionType parameter.
     
     Eg: addWhereWithSubQuery("myGL,""id","select id from table a limit 1",SqlSubQueryCondition.equal) will add "myGL.id = (select id from table a limit 1)" in where clause of resulting SQL query.
     

   - `Builder().orHaving()`
     
     Adds or between two having condition

   - `Builder().havingStartBracket(),havingCloseBracket()`
     
     To be used to put having conditions in brackets ie .. "(...)".


   - `Builder().orWhere()`

     Adds or between two where condition
     
   - `Builder().whereStartBracket(),whereCloseBracket()`
     
     To be used to put where conditions in brackets ie .. "(...)".
     
   - `Builder().addHavingCondition(String graphKey,String columnName,SqlCondition conditiontype)`
      
      Adds column in having clause with given condition type
      
   - `Builder().addHavingConditionWithSubQuery(String graphKey,String columnName,SqlSubQueryCondition sqlCondition,String subQuery)`
      
      This method needs to be used when you want to evaluate with sub query in having clause against the column passed.The where condition is defined by conditionType parameter.
      
   - `Builder().orderbyAsc()`
     
     Used after order by clause to order by Ascending
     
   - `Builder().orderbyDesc()`
     
     Used after order by clause to order by descending
     
   - `Builder().addLimitCondition()`
     
     Adds limit condition ie.. adds snippet " limit ?" into the resulting sql query
        
   - `Builder().addOffsetWithLimitCondition()`
     
     Adds both offset and limit condition ie.. adds snippet " limit ?,?" into the resulting sql query
     
   - `avg(String columnName)`
     
     Adds average function on the column name passed
     
   - `sum(String columnName)`
     
     Adds summation function on the column name passed
     
   - `count(String columnName)`
     
     Adds count function on the column name passed
     
   - `max(String columnName)`
     
     Adds min function on the column name passed
     
   - `min(String columnName)`
     
     Adds max function on the column name passed

   - `addaggregateSelectCols(SqlAggregationType aggregateFunc, String graphKey, String columnName)`

     Takes aggregate function of enum type SqlAggregationType,GraphKey of the field for which aggregation needs to be done and table column name on which aggregation needs to be done.Here the column name should be in form of {table_alias}.columnName where table_alias is alias of table used in native query template.