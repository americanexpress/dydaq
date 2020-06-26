/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.service;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.ErrorConstants;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.ServiceConstants;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlAggregationType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.gqlrequestmeta.SimpleGraphObject;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.JoinQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.NativeQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.QueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.SimpleQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.exception.DynamicQueryException;
import org.apache.commons.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.util.Pair;

import java.beans.Introspector;
import java.util.*;

public class DynamicQueryGenerationHelper {

    private static final Logger LOGGER = LogManager.getLogger(DynamicQueryGenerationHelper.class);

    private SimpleGraphObject simpleGraphObject;

    public DynamicQueryGenerationHelper(SimpleGraphObject simpleGraphObject) {
        this.simpleGraphObject = simpleGraphObject;
    }

    /**
     * This method converts a NativeQueryBuiilder object into a sql query with the select columns obtained from {@link SimpleGraphObject#getGqlGraphReqFieldMap()}
     * specific to the entity for which the NativeQueryBuilderObject is created.
     */
    public String nativeQueryBuilder(NativeQueryBuilder queryBuilder) {
        StrSubstitutor sub = new StrSubstitutor(queryBuilder.getAliasToGraphMap());

        if (queryBuilder.getQueryTemplate() == null) {
            throw new DynamicQueryException(ErrorConstants.EMPTY_QUERY);
        }

        if (queryBuilder.getQueryTemplate().equals("")) {
            throw new DynamicQueryException(ErrorConstants.EMPTY_QUERY);
        }

        //Transforming Aggregate map in native form to dydaqJPA understandable format.eg ${alias}.{columnNmae} will be converted to graphKey.columnName
        Map<String, SqlAggregationType> aggregateColumnMap = transformAggSelectColMap(sub, queryBuilder.getAggregateSelectCols());

        //Verifying if all the columns which needs to be aggregated are actually valid SQL columns and are present in respective entity
        verifyAggregateSelectCols(aggregateColumnMap, simpleGraphObject.getRootEntity(), queryBuilder.getGraphKey());

        if (queryBuilder.getGraphKey() != null) {
            if (!queryBuilder.getQueryTemplate().equals("") && !queryBuilder.getGraphKey().equals("")) {
                String colSelects = selectColumnsFromSimpleGraphObject(queryBuilder.getGraphKey(), aggregateColumnMap);
                return ServiceConstants.SELECT_SNIPPET + colSelects + " " + queryBuilder.getQueryTemplate();
            }
        } else if (queryBuilder.getRootKey() != null) {
            StringBuilder colSelectSb = new StringBuilder();
            for (String tableAlias : queryBuilder.getAliasToGraphMap().keySet()) {
                String graphKey = queryBuilder.getAliasToGraphMap().get(tableAlias);
                //checking if GraphQL reuest has been made for the graphKeys mentioned then select column
                if (simpleGraphObject.getGqlGraphEntityMap().keySet().contains(graphKey)) {
                    colSelectSb.append(selectColumnsFromSimpleGraphObject(graphKey, queryBuilder.getRootKey(), aggregateColumnMap));
                }
            }
            //no columns selected
            if (colSelectSb.length() != 0) {
                String nativeQuery = sub.replace(queryBuilder.getQueryTemplate());
                return ServiceConstants.SELECT_SNIPPET + colSelectSb.substring(1) + " " + nativeQuery;
            }

        }

        return "";
    }

    /**
     * Converts aggregate column map with column names in the form of user query alias into Dynamic Query Builder graph key alias format
     * eg user can use {a}.sample_column_name in the aggregate column map's key.
     * {a} has alias set to sample_graph_key
     * then this function will convert the column in new map to - sample_graph_key.sample_column_name
     */
    private Map<String, SqlAggregationType> transformAggSelectColMap(StrSubstitutor sub, Map<String, SqlAggregationType> aggregateSelectCols) {

        if (aggregateSelectCols.size() > 0) {
            Map<String, SqlAggregationType> aggregateColMap = new HashMap<>();
            for (String nativeColNames : aggregateSelectCols.keySet()) {
                aggregateColMap.put(sub.replace(nativeColNames), aggregateSelectCols.get(nativeColNames));
            }
            return aggregateColMap;
        } else {
            return aggregateSelectCols;
        }
    }

    /**
     * Used for multiple table join.It returns the database column selected for graph level with alias which
     * can directly be used in select query's select columns.
     *
     * @param graphKey     is the graph level/key/(reference variables pointing to another entity) for which scalar fields needs to be selected from database
     * @param graphRootKey is the root level or for select query,alias for the table on which remaining tables will be joined.
     * @return subset of SQL select column comma separated and there alias and can be directly used for selecting columns.
     */

    private String selectColumnsFromSimpleGraphObject(String graphKey, String graphRootKey, Map<String, SqlAggregationType> aggregateSelectCols) {

        Map<String, Set<String>> tableColMap = simpleGraphObject.getGqlGraphReqFieldMap();
        StringBuilder columnSelect = new StringBuilder();
        Set<String> gqlFieldNames = new HashSet<>();
        Map<String, String> graphEntityMap;
        if (graphKey.length() < graphRootKey.length() && !graphRootKey.equals(ServiceConstants.ROOT_GRAPH_LEVEL)) {
            graphEntityMap = EntityMetaDataProvider.GRAPH_ENTITY_MAP.get(simpleGraphObject.getRootEntity());
            throw new DynamicQueryException(String.format(ErrorConstants.GRAPH_KEY_NOT_CHILD_OF_ROOT, graphEntityMap.get(graphRootKey) + " having graph level " + graphRootKey, graphEntityMap.get(graphKey) + " having graph level " + graphKey + " ", graphEntityMap.get(graphRootKey)));
        }

        List<String> primaryColumnVarNames = null;
        //If Aggregation Columns Map is empty,then only add primary keys/dynamic join columns in the select columns list which can be used
        //later by using other query builders
        //If Aggregation Columns are added using the Query Builder,no Primary Keys DynamicJOinColumns will be added as it may lead to SQL
        // ineffective queries
        if (aggregateSelectCols.isEmpty()) {
            primaryColumnVarNames = getPkFkColumnVariableNames(simpleGraphObject.getGqlGraphEntityMap().get(graphKey));
        }
        if (primaryColumnVarNames != null) {
            gqlFieldNames.addAll(primaryColumnVarNames);
        }

        if (tableColMap.get(graphKey) != null) {
            gqlFieldNames.addAll(tableColMap.get(graphKey));
        }
        int graphRootKeyLen = (graphRootKey.equals(ServiceConstants.ROOT_GRAPH_LEVEL) ? 0 : graphKey.indexOf(graphRootKey) + graphRootKey.length() + 1);
        String columnAlias = (graphKey.equals(graphRootKey)) ? "" : graphKey.substring(graphRootKeyLen) + "_";
        for (String gqlField : gqlFieldNames) {
            String columnName = getColumnNameFromGraphKeyAndGQLField(graphKey, gqlField);

            if (aggregateSelectCols.containsKey(graphKey + "." + columnName)) {
                String aggregatedColumn = String.format(aggregateSelectCols.get(graphKey + "." + columnName).getAggregateType(), graphKey + "." + columnName);
                columnSelect = columnSelect.append(", " + aggregatedColumn).append(" as ")
                        .append(columnAlias).append(columnName);
            } else {
                columnSelect = columnSelect.append(", " + graphKey + ".").append(columnName).append(" as ")
                        .append(columnAlias).append(columnName);
            }
        }
        return (columnSelect.length() > 0 ? columnSelect.toString() : "");
    }

    /**
     * It takes Aggregate columns added via Query builders and Graph Root Key.Using GraphRoot Key it determines the Root entity
     * and then fetches graphEntityMap.From GraphEntityMap it determines the EnittyFieldColumnMap and checks if the column added in the aggregate map
     * are present in entity field column map
     */
    private void verifyAggregateSelectCols(Map<String, SqlAggregationType> aggregateSelectCols, String graphRootEntity, String graphKey) {
        Map<String, String> graphEntityMap = EntityMetaDataProvider.GRAPH_ENTITY_MAP.get(graphRootEntity);
        for (String graphKeyWithAggColumnName : aggregateSelectCols.keySet()) {
            if (graphKeyWithAggColumnName.contains(".")) {
                String entityName = graphEntityMap.get(graphKeyWithAggColumnName.split("\\.")[0]);
                if (!EntityMetaDataProvider.ENTITY_COL_FIELD_MAP.get(entityName).containsKey(graphKeyWithAggColumnName.split("\\.")[1])) {
                    throw new DynamicQueryException(String.format(ErrorConstants.AGG_COLUMN_NOT_FOUND, graphKeyWithAggColumnName.split("\\.")[1], entityName));
                }
            } else {
                String entityName = graphEntityMap.get(graphKey);
                if (!EntityMetaDataProvider.ENTITY_COL_FIELD_MAP.get(entityName).containsKey(graphKeyWithAggColumnName)) {
                    throw new DynamicQueryException(String.format(ErrorConstants.AGG_COLUMN_NOT_FOUND, graphKeyWithAggColumnName, entityName));
                }
            }
        }
    }

    /**
     * Creates a SQL query whose input parameters(?) can be set using Spring jdbcTemplate/Java jdbc*.
     * The select columns in the SQL Query obtained from {@link SimpleGraphObject#getGqlGraphReqFieldMap()}
     * specific to the entity for which the the SimpleQueryBuilder is created
     *
     * @param queryBuilder instance of {@link SimpleQueryBuilder}
     * @return SQL query for the table having alias as graphKey name
     */
    public String getSelectQuery(SimpleQueryBuilder queryBuilder) {
        if (!queryBuilder.getGroupByCondition().isEmpty()) {
            //adding field names for groupby columns into SimpleGraphObject against graphKey specified in querybuilder.This field name will be added into the select query
            addColumnsInSimpleGraphObject(queryBuilder);
        }

        //Verifying if all the columns which needs to be aggregated are actually valid SQL columns and are present in respective entity
        verifyAggregateSelectCols(queryBuilder.getAggregateSelectCols(), simpleGraphObject.getRootEntity(), queryBuilder.getGraphKey());
        //gets select columns for the graphKey.
        String colSelects = selectColumnsFromSimpleGraphObject(queryBuilder.getGraphKey(), queryBuilder.getAggregateSelectCols());
        String fromTable = EntityMetaDataProvider.ENTITY_TABLE_MAP.get(simpleGraphObject.getGqlGraphEntityMap().get(queryBuilder.getGraphKey()));
        return buildQuery(queryBuilder, colSelects, fromTable);
    }

    /**
     * Creates a joined SQL query whose parameters(?) can be set using Spring jdbc template/Java jdbc
     * The SQL query generated has select columns obtained from {@link SimpleGraphObject#gqlGraphReqFieldMap}
     * specific to the entities for which the JoinQueryBuilder object is created
     *
     * @param queryBuilder instance of {@link JoinQueryBuilder}
     * @return Complete SQL query with for the tables having alias as graphKeys
     */
    public String getJoinQuery(JoinQueryBuilder queryBuilder) {
        StringBuilder colSelects = new StringBuilder();
        String rootGraph = queryBuilder.getRootObject();
        String fromTables = "";

        //Verifying if all the columns which needs to be aggregated are actually valid SQL columns and are present in respective entity
        verifyAggregateSelectCols(queryBuilder.getAggregateSelectCols(), simpleGraphObject.getRootEntity(), "");

        Set<String> allKeySet = new HashSet<>();
        //inputGraphKeySet contins all the graph levels requested in the graphql request + graph level for column added in the where clause of JoinQueryBuilder
        Set<String> inputGraphKeySet = new HashSet<>();
        inputGraphKeySet.addAll(simpleGraphObject.getGqlGraphReqFieldMap().keySet());
        allKeySet.addAll(queryBuilder.getGraphJoinType().keySet());
        for (String whereCol : queryBuilder.getWhereCondition()) {
            if (whereCol.contains(".")) {
                //fetching Graph Key from WhereColumn
                String wherekey = whereCol.split("\\.")[0];
                allKeySet.add(wherekey);
                inputGraphKeySet.add(wherekey);
            }
        }
        //Validating parent graph key are present or not as table respective to the graph level will be joined with its parent
        for (String graphKey : queryBuilder.getGraphJoinType().keySet()) {
            boolean parentgraphPresent = false;
            while (!parentgraphPresent) {
                String parent = (graphKey.contains("_") ? graphKey.substring(0, graphKey.lastIndexOf('_')) : graphKey);
                if (!allKeySet.contains(parent) && !queryBuilder.getRootObject().equals(parent)) {
                    throw new DynamicQueryException(String.format(ErrorConstants.ADD_GRAPH_KEY, parent));
                } else {
                    parentgraphPresent = true;
                }
            }

        }

        if (!queryBuilder.getGroupByCondition().isEmpty()) {
            for (String columnWithTableAlias : queryBuilder.getGroupByCondition()) {
                String graphKey = (columnWithTableAlias.contains(".") ? columnWithTableAlias.split("\\.")[0] : "");
                String colName = (columnWithTableAlias.contains(".") ? columnWithTableAlias.split("\\.")[1] : columnWithTableAlias);
                addFieldNameForColumnInSGO(graphKey, colName);
            }
        }

        if (simpleGraphObject.getGqlGraphReqFieldMap().containsKey(rootGraph)) {
            colSelects.append(selectColumnsFromSimpleGraphObject(rootGraph, rootGraph, queryBuilder.getAggregateSelectCols()));
            String rootTable = simpleGraphObject.getGqlGraphEntityMap().get(rootGraph);
            String rootTableWithAlias = EntityMetaDataProvider.ENTITY_TABLE_MAP.get(rootTable) + " " + rootGraph;
            fromTables += rootTableWithAlias;
        } else if (simpleGraphObject.getGqlGraphEntityMap().containsKey(rootGraph)) {//fixing in case user has not queried fields from root object,it will not be in Request field map.Check in Graph Entity Map
            String entityName = simpleGraphObject.getGqlGraphEntityMap().get(rootGraph);
            String tableWithAlias = EntityMetaDataProvider.ENTITY_TABLE_MAP.get(entityName) + " " + rootGraph;
            fromTables += tableWithAlias;
            addPkFieldToSGOForGraphsWithoutFields(rootGraph, EntityMetaDataProvider.ENTITY_PK_MAP.get(entityName));

            colSelects.append(selectColumnsFromSimpleGraphObject(rootGraph, rootGraph, queryBuilder.getAggregateSelectCols()));

        }

        //Adding all the table for the graph key requested in graphQL request and also keys added in where condition in JoinQueryBuilder
        for (String graphKey : queryBuilder.getGraphJoinType().keySet()) {
            if (inputGraphKeySet.contains(graphKey)) {
                colSelects.append(selectColumnsFromSimpleGraphObject(graphKey, rootGraph, queryBuilder.getAggregateSelectCols()));
                boolean parentgraphPresent = false;

                while (!parentgraphPresent) {
                    String parent = (graphKey.contains("_") ? graphKey.substring(0, graphKey.lastIndexOf('_')) : graphKey);
                    if (!inputGraphKeySet.contains(parent) && !parent.equals(rootGraph)) {
                        fromTables = getFromTables(queryBuilder, fromTables, parent);
                    } else {
                        parentgraphPresent = true;
                    }
                }

                if (!graphKey.equals(rootGraph)) {
                    fromTables = getFromTables(queryBuilder, fromTables, graphKey);
                }

            }

        }
        return buildQuery(queryBuilder, colSelects.toString(), fromTables);
    }

    /**
     * This method needs to be used to add primary keys into the SimpleGraphObject for those graph for which no
     * fields have been selected.Adding PK is nescessary as it is used by ResultSetExecutor for setting object in
     * nested way with primary key for creating complete object.Check open source SimpleFlatMapper for role of primary keys in parsing
     * result set into pojos/entities
     */
    private void addPkFieldToSGOForGraphsWithoutFields(String graphKey, List<String> pkList) {
        String entityName = simpleGraphObject.getGqlGraphEntityMap().get(graphKey);
        for (String pkField : pkList) {
            addFieldNameForColumnInSGO(graphKey, EntityMetaDataProvider.ENTITY_FIELD_COL_MAP.get(entityName).get(pkField));
        }

    }

    /**
     * It is used when value of the column selects
     * belongs to a single table/entity.
     *
     * @throws DynamicQueryException
     */
    private String selectColumnsFromSimpleGraphObject(String graphKey, Map<String, SqlAggregationType> aggregateSelectCols) {

        Map<String, Set<String>> tableColMap = simpleGraphObject.getGqlGraphReqFieldMap();
        StringBuilder columnSelect = new StringBuilder();

        List<String> primaryColumnVarNames = null;
        //If Aggregation Columns Map is empty,then only add primary keys/dynamic join columns in the select columns list which can be used
        //later by using other query builders
        //If Aggregation Columns are added using the Query Builder,no Primary Keys DynamicJOinColumns will be added as it may lead to SQL
        // ineffective queries
        if (aggregateSelectCols.isEmpty()) {
            primaryColumnVarNames = getPkFkColumnVariableNames(simpleGraphObject.getGqlGraphEntityMap().get(graphKey));
        }
        Set<String> gqlFields = new HashSet<>();
        if (null != tableColMap.get(graphKey)) {
            gqlFields.addAll(tableColMap.get(graphKey));
        }
        if (null != primaryColumnVarNames) {
            gqlFields.addAll(primaryColumnVarNames);
        }
        for (String gqlField : gqlFields) {

            String columnName = getColumnNameFromGraphKeyAndGQLField(graphKey, gqlField);
            if (aggregateSelectCols.containsKey(columnName)) {
                String aggregatedColumn = String.format(aggregateSelectCols.get(columnName).getAggregateType(), columnName);
                columnSelect = columnSelect.append(", ").append(aggregatedColumn + " as " + columnName);
            } else {
                columnSelect = columnSelect.append(", ").append(columnName);
            }
        }
        return columnSelect.substring(1);
    }

    /**
     * @param graphKey is the graph level/key/(reference variables pointing to another entity) for which scalar fields needs to be selected from database
     * @param gqlfield is the field of the graphKey whose respective column name needs to be selected from database.
     * @return database column name
     */
    public String getColumnNameFromGraphKeyAndGQLField(String graphKey, String gqlfield) throws DynamicQueryException {

        String columnName = getColumnName(simpleGraphObject.getGqlGraphEntityMap().get(graphKey), gqlfield);

        if (columnName == null) {
            //sometimes SPQR adds field name as getter name.So if the GQL is a getter name of the entity,fetching the actual field name from the entity getter
            if (gqlfield.startsWith("get") || gqlfield.startsWith("is")) {
                String fieldName = Introspector.decapitalize(gqlfield.substring(gqlfield.startsWith("is") ? 2 : 3));
                columnName = getColumnName(simpleGraphObject.getGqlGraphEntityMap().get(graphKey), fieldName);
            } else {
                //in case the developer is giving a name using @GraphQLQuery(name = ?) for giving name to a field,checking the field name for such custom names
                String fieldName = EntityMetaDataProvider.ENTITY_GQL_FIELD_TO_ENTITY_FIELD_MAP.get(simpleGraphObject.getGqlGraphEntityMap().get(graphKey)).get(gqlfield);
                if (fieldName != null) {
                    columnName = getColumnName(simpleGraphObject.getGqlGraphEntityMap().get(graphKey), fieldName);
                }
            }
            if (columnName == null) {
                throw new DynamicQueryException(String.format(ErrorConstants.FIELD_NOT_FOUND, gqlfield, (gqlfield.substring(0, 1).toUpperCase() + gqlfield.substring(1)), simpleGraphObject.getGqlGraphEntityMap().get(graphKey)));
            }
        }
        return columnName;
    }

    /**
     * @param className ie.. ClassName/Entity name as input
     * @return primary key/foreign key field variable from entity name provided as list of values
     */
    private List<String> getPkFkColumnVariableNames(String className) {
        return EntityMetaDataProvider.ENTITY_PK_FK_FIELD_MAP.get(className);
    }

    /**
     * @param className ie.. ClassName/Entity name as input
     * @param fieldName as graphQL field name
     * @return column name in annotation @Column for the field name given as input in the Entity passed
     */
    private String getColumnName(String className, String fieldName) {
        String colName;
        try {
            colName = EntityMetaDataProvider.ENTITY_FIELD_COL_MAP.get(className).get(fieldName);
        } catch (Exception nex) {
            throw new DynamicQueryException(String.format(ErrorConstants.ENTITY_NOT_READ, className));
        }
        return colName;
    }

    public String buildQuery(QueryBuilder queryBuilder, String colSelects, String fromTable) {

        String finalQuery = "";
        String whereCondition;
        StringBuilder groupByCondition = new StringBuilder();
        String havingCondition;
        String orderByCondition;
        String limitCondition = "";

        whereCondition = buildWhereCondition(queryBuilder);

        for (String groupBy : queryBuilder.getGroupByCondition()) {
            groupByCondition.append(",").append(groupBy);
        }

        havingCondition = buildHavingCondition(queryBuilder);

        orderByCondition = buildOrderByCondition(queryBuilder);

        if (queryBuilder.getLimit() != null && !queryBuilder.getLimit().equals("")) {
            limitCondition = queryBuilder.getLimit();
        }

        if (queryBuilder.getOffsetWithLimit() != null && !queryBuilder.getOffsetWithLimit().equals("")) {
            limitCondition = queryBuilder.getOffsetWithLimit();
        }

        if (!colSelects.equals("")) {
            finalQuery = ServiceConstants.SELECT_SNIPPET + colSelects.substring(1) + " from "
                    + fromTable
                    + (!whereCondition.equals("") ? " where " + whereCondition + " " : "")
                    + (!groupByCondition.toString().equals("") ? " Group By " + groupByCondition.substring(1) + " " : "")
                    + (!havingCondition.equals("") ? " having " + havingCondition + " " : "")
                    + (!orderByCondition.equals("") ? " Order By " + orderByCondition.substring(1) + " " : "")
                    + (!limitCondition.equals("") ? " limit " + limitCondition + " " : "");
        }
        LOGGER.debug(finalQuery);
        return finalQuery;
    }

    private String buildOrderByCondition(QueryBuilder queryBuilder) {
        StringBuilder orderByCondition = new StringBuilder();
        for (String orderBy : queryBuilder.getOrderByCondition()) {
            if (orderBy.equals("asc") || orderBy.equals("desc")) {
                orderByCondition.append(" ").append(orderBy);
            } else {
                orderByCondition.append(",").append(orderBy);
            }

        }
        return orderByCondition.toString();
    }

    private String buildHavingCondition(QueryBuilder queryBuilder) {
        int havingCondCount = 0;
        StringBuilder havingCondition = new StringBuilder();
        for (String havingCond : queryBuilder.getHavingCondition()) {

            havingCondCount++;

            if (havingCondCount != queryBuilder.getHavingCondition().size()) {
                if (!havingCond.equals("or") && !havingCond.equals("(") && !havingCond.equals(")")) {
                    String nextCondition = queryBuilder.getHavingCondition().get(havingCondCount);
                    havingCondition.append(havingCond).append((!nextCondition.equals("or") && nextCondition.equals("(") && nextCondition.equals(")") ? " and " : ""));
                } else {
                    havingCondition.append(" ").append(havingCond).append(" ");
                }
            } else {
                havingCondition.append(havingCond);
            }
        }
        return havingCondition.toString();
    }

    private String buildWhereCondition(QueryBuilder queryBuilder) {
        int whereCondCount = 0;
        StringBuilder whereCondition = new StringBuilder();
        for (String whereCond : queryBuilder.getWhereCondition()) {

            whereCondCount++;

            if (whereCondCount != queryBuilder.getWhereCondition().size()) {
                if (!whereCond.equals("or") && !whereCond.equals("(") && !whereCond.equals(")")) {
                    String nextCondition = queryBuilder.getWhereCondition().get(whereCondCount);
                    whereCondition.append(whereCond).append((!nextCondition.equals("or") && !nextCondition.equals("(") && !nextCondition.equals(")") ? " and " : ""));
                } else {
                    whereCondition.append(" ").append(whereCond).append(" ");
                }
            } else {
                whereCondition.append(whereCond);
            }
        }
        return whereCondition.toString();
    }


    /**
     * Appends groupBy columns into against the graphKey in the {@link SimpleGraphObject}
     * Takes instance of SimpleGraphObject and SimpleQueryBuilder and
     */
    public void addColumnsInSimpleGraphObject(SimpleQueryBuilder queryBuilder) {
        for (String columnName : queryBuilder.getGroupByCondition()) {
            addFieldNameForColumnInSGO(queryBuilder.getGraphKey(), columnName);
        }

    }

    /**
     * Appends table column name passed (also must be present in JPA entity) against the graphKey in the {@link SimpleGraphObject} which is used later to select column for table with alias as graphKey.
     *
     * @param graphKey   is the graph level/table alias which is key in the map returned by {@link SimpleGraphObject#getGqlGraphReqFieldMap()}
     * @param columnName table column name which needs to be added into the table with alias as graphKey
     */
    private void addFieldNameForColumnInSGO(String graphKey, String columnName) {
        String entityName = simpleGraphObject.getGqlGraphEntityMap().get(graphKey);
        Map<String, String> fieldColMap = EntityMetaDataProvider.ENTITY_FIELD_COL_MAP.get(entityName);
        for (String fieldName : fieldColMap.keySet()) {
            if (fieldColMap.get(fieldName).contains(columnName)) {
                EntityMetaDataProvider.ENTITY_COL_FIELD_MAP.get(entityName);
                if (null == simpleGraphObject.getGqlGraphReqFieldMap().get(graphKey)) {
                    simpleGraphObject.getGqlGraphReqFieldMap().put(graphKey, new HashSet<>());
                }
                simpleGraphObject.getGqlGraphReqFieldMap().get(graphKey).add(EntityMetaDataProvider.ENTITY_COL_FIELD_MAP.get(entityName).get(columnName));
            }
        }
    }

    private String getFromTables(JoinQueryBuilder queryBuilder, String fromTables, String graphKey) {
        String rootEntity = simpleGraphObject.getRootEntity();
        String entityName = EntityMetaDataProvider.GRAPH_ENTITY_MAP.get(rootEntity).get(graphKey);
        String tableWithAlias = EntityMetaDataProvider.ENTITY_TABLE_MAP.get(entityName) + " " + graphKey;
        String parentTableAlias = (graphKey.contains("_") ? graphKey.substring(0, graphKey.lastIndexOf('_')) : ServiceConstants.ROOT_GRAPH_LEVEL);
        String childTableReference = graphKey.substring(graphKey.lastIndexOf('_') + 1);
        String parentTable = simpleGraphObject.getGqlGraphEntityMap().get(parentTableAlias);
        String joinCondition = "";
        try {
            for (Pair joinCols : EntityMetaDataProvider.TABLE_GRAPH_JOIN_COL_MAP.get(parentTable).get(childTableReference)) {
                joinCondition = " and " + parentTableAlias + "." + joinCols.getFirst() + " = " + graphKey + "." + joinCols.getSecond();
            }
        } catch (NullPointerException nex) {
            Map<String, String> graphEntityMap = EntityMetaDataProvider.GRAPH_ENTITY_MAP.get(rootEntity);
            throw new DynamicQueryException(String.format(ErrorConstants.ASSOCIATION_NOT_DEFINED, graphEntityMap.get(parentTableAlias), graphEntityMap.get(graphKey), childTableReference));
        }

        fromTables += "  " + queryBuilder.getGraphJoinType().get(graphKey) + " " + tableWithAlias + " on " + joinCondition.substring(4) + " ";
        return fromTables;
    }

}
