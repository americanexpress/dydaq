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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlAggregationType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlCondition;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlJoinType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlSubQueryCondition;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class provides all the methods to be used for building query by joining multiple entities.The JoinQueryBuilder uses
 * associations mentioned on the entities with @JoinColumn annotations to join the the entities.
 */
public class JoinQueryBuilder extends QueryBuilder {

    private String rootObject;
    private Map<String, String> graphJoinType;

    public JoinQueryBuilder(@NonNull String rootObject, Map<String, String> graphJoinType, List<String> whereCondition, List<String> orderByCondition, List<String> groupByCondition, String limit, String offsetWithLimit, List<String> havingCondition, Map<String, SqlAggregationType> aggregateSelectCols) {
        this.rootObject = rootObject;
        this.graphJoinType = graphJoinType;
        this.whereCondition = whereCondition;
        this.orderByCondition = orderByCondition;
        this.groupByCondition = groupByCondition;
        this.havingCondition = havingCondition;
        this.limit = limit;
        this.offsetWithLimit = offsetWithLimit;
        this.aggregateSelectCols = aggregateSelectCols;
    }

    //Aggregate function implementations
    public static String avg(String columnName) {
        return "avg(%s" + columnName + ")";
    }

    public static String sum(String columnName) {
        return "sum(%s" + columnName + ")";
    }

    public static String count(String columnName) {
        return "count(%s" + columnName + ")";
    }

    public static String max(String columnName) {
        return "max(%s" + columnName + ")";
    }

    public static String min(String columnName) {
        return "min(%s" + columnName + ")";
    }

    @Override
    public List<String> getGroupByCondition() {
        return groupByCondition;
    }

    @Override
    public List<String> getOrderByCondition() {
        return orderByCondition;
    }

    public String getRootObject() {
        return rootObject;
    }

    public Map<String, String> getGraphJoinType() {
        return graphJoinType;
    }

    @Override
    public List<String> getWhereCondition() {
        return whereCondition;
    }

    public static class Builder {

        private String rootObject;
        private Map<String, String> graphJoinType = new LinkedHashMap<>();
        private List<String> whereCondition = new LinkedList<>();
        private List<String> orderByCondition = new LinkedList<>();
        private List<String> groupByCondition = new LinkedList<>();
        private String limit;
        private String offsetWithLimit;
        private List<String> havingCondition = new LinkedList<>();
        private Map<String, SqlAggregationType> aggregateSelectCols = new LinkedHashMap<>();

        public Builder() {
        }

        public List<String> getHavingCondition() {
            return havingCondition;
        }

        /**
         * Takes root key.Root Key is the GQL Graph level of the entity into which
         * you want to wrap your whole resultset of the Joined Query.
         */
        public Builder setRootKey(String rootObject) {
            this.rootObject = rootObject;
            return this;
        }

        /**
         * GraphKey are the graph levels in the GQL request payloads and also represents entities in
         * DynamicQuery.This method joins the input graphkey with the graphkey passed the in joinWithGraph
         * immediately above it.If no joinWithGraph is mentioned,the input graphlevel will be joined with
         * the rootKey above it.
         * <p>
         * Eg :
         * <p>
         * JoinQueryBuilder joinQueryBuider = new JoinQueryBuilder
         * .Builder()
         * .setRootKey(HospMeta.GRAPH_LEVEL)
         * .joinWithGraph(HospMeta.Surgeon.GRAPH_LEVEL, SqlJoinType.INNER_JOIN) //Entity with Graph level surgeon will be inner joined with entity with graph level HospMeta.GRAPH_LEVEL
         * .joinWithGraph(HospMeta.Surgeon.DocSpeciality.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)//entity with graph level HospMeta.Surgeon.DocSpeciality.GRAPH_LEVEL will be inner joined with entity with graph level HospMeta.Surgeon.GRAPH_LEVEL
         * <p>
         * *** For graph level generation from there entities like HospMeta.GRAPH_LEVEL,use meta class generator plugin of dynamic query.
         */
        public Builder joinWithGraph(String graphKey, SqlJoinType joinType) {
            graphJoinType.put(graphKey, joinType.getJoinType());
            return this;
        }

        /**
         * condition type can be "=",">" or "<"
         * Eg :for conditionType = ">" the addWhereCondition will be transformed to  {graphKey}.{columnName} > ?
         *
         * @param graphKey      is the graph key/level
         * @param columnName    is the column name which needs to be added in where clause
         * @param conditionType can be "=",">" or "<"
         *                      <p>
         *                      Eg : Builder().addWhereCondition("myGL","myColNm",SqlCondition.equal) will
         *                      add snippet "myGL.myColNm = ?" in where clause of resulting SQL query
         */
        public Builder addWhereCondition(String graphKey, String columnName, @NonNull SqlCondition conditionType) {
            if (graphKey == null) {
                graphKey = "";
            }

            whereCondition.add((!graphKey.trim().equals("") ? graphKey + "." : "") + columnName + conditionType.getCondition());
            return this;
        }

        /**
         * @param graphKey   is the graph key/level
         * @param columnName is the column name which needs to be added in where clause
         * @param subQuery   is the native sql query which you want to add as a part of sub query,
         *                   <p>
         *                   then internally the query will be parsed into <strong> where {graphKey}.{columnName} in (staticQuery)</strong>
         */
        public Builder addWhereWithSubQuery(@NonNull String graphKey, String columnName, String subQuery,
                                            @NonNull SqlSubQueryCondition sqlCondition) {

            whereCondition.add((!graphKey.trim().equals("") ? graphKey + "." : "") + columnName
                    + String.format(sqlCondition.getCondition(), subQuery));
            return this;
        }

        /**
         * To be used to put where conditions in brackets ie .. "(...)".
         */
        public Builder whereStartBracket() {
            whereCondition.add("(");
            return this;
        }

        /**
         * To be used to put where conditions in brackets ie .. "(...)".
         */
        public Builder whereCloseBracket() {
            whereCondition.add(")");
            return this;
        }

        /**
         * Adds or between two where condition
         */
        public Builder orWhere() {
            whereCondition.add("or");
            return this;
        }

        /**
         * Adds passed column in order by clause
         * <p>
         * Eg : Builder().addOrderByCondition("myGL","myColNm",SqlCondition.equal) will
         * add snippet " order by myGL.myColNm" in where clause of resulting SQL query
         */
        public Builder addOrderByCondition(String graphKey, String columnName) {
            if (graphKey == null) {
                graphKey = "";
            }
            orderByCondition.add(String.format(columnName, (!graphKey.trim().equals("") ? graphKey + "." : "")));
            return this;
        }

        /**
         * Used after order by clause to order by Ascending
         */
        public Builder orderbyAsc() {
            orderByCondition.add("asc");
            return this;
        }

        /**
         * Used after order by clause to order by descending
         */
        public Builder orderbyDesc() {
            orderByCondition.add("desc");
            return this;
        }

        public Builder addGroupByCondition(String graphKey, String columnName) {
            if (graphKey == null) {
                graphKey = "";
            }
            groupByCondition.add((!graphKey.trim().equals("") ? graphKey + "." : "") + columnName);
            return this;
        }

        /**
         * addHavingCondition takes condition type for where clause.By default conditions are and separated
         * <p>
         * Eg: for graphKey = "root".columnName = "accountId",conditiontype = {@link SqlCondition#greaterThanOrEqual}
         * snippet formed will be "root.accountId >= ?"
         *
         * @param graphKey   is the graph key/level
         * @param columnName is the column name which needs to be added in where clause
         */
        public Builder addHavingCondition(String graphKey, String columnName, SqlCondition conditiontype) {
            if (graphKey == null) {
                graphKey = "";
            }
            havingCondition.add((!graphKey.trim().equals("") ? graphKey + "." : "") + columnName + " " + conditiontype.getCondition());
            return this;
        }

        /**
         * addHavingConditionWithSubQuery takes sub query and adds it in where clause.By default conditions are and separated
         *
         * @param graphKey     is the graph key/level
         * @param columnName   is the column name which needs to be added as a part of where clause
         * @param sqlCondition is object of type {@link SqlSubQueryCondition} to provide the condition (eg in,=,!=etc)
         * @param subQuery     is the subquery which needs to be used for  evaluation
         *                     <p>
         *                     Eg: for graphKey= "root",columnName = "account_id",sqlCondition= {@link SqlSubQueryCondition#in},subQuery = "select * from mytbl",
         *                     subquery generated will have "root.account_id in (select * from mytbl)" in the having clause
         */
        public Builder addHavingConditionWithSubQuery(String graphKey, String columnName, SqlSubQueryCondition sqlCondition, String subQuery) {
            if (graphKey == null) {
                graphKey = "";
            }
            havingCondition.add((!graphKey.trim().equals("") ? graphKey + "." : "") + columnName + String.format(sqlCondition.getCondition(), subQuery));
            return this;
        }

        /**
         * for adding or condition in between two having conditions
         */
        public Builder orHaving() {
            havingCondition.add("or");
            return this;
        }

        public Builder havingStartBracket() {
            havingCondition.add("(");
            return this;
        }

        public Builder havingCloseBracket() {
            havingCondition.add(")");
            return this;
        }

        /**
         * internally this method will result in the sql snippet <strong> limit ? </string>
         */
        public Builder setLimit(int limit) {
            this.limit = String.valueOf(limit);
            return this;
        }

        /**
         * internally this method will result in the sql snippet <strong> limit ?,? </string>
         */
        public Builder addOffsetWithLimitCondition() {
            this.offsetWithLimit = "?,?";
            return this;
        }

        public Builder addaggregateSelectCols(SqlAggregationType aggregateFunc, String graphKey, String columnName) {
            this.aggregateSelectCols.put((!graphKey.trim().equals("") ? graphKey + "." : "") + columnName, aggregateFunc);
            return this;
        }

        /**
         * used to build the query after all the conditions have been added
         */
        public JoinQueryBuilder build() {
            return new JoinQueryBuilder(rootObject, graphJoinType, whereCondition, orderByCondition, groupByCondition, limit, offsetWithLimit, havingCondition, aggregateSelectCols);
        }

    }

}
