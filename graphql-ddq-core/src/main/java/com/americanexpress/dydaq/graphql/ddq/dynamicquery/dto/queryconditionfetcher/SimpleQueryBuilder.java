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
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlSubQueryCondition;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.gqlrequestmeta.SimpleGraphObject;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is a builder object.It needs to be used when a SQL native query is to be created on a single entity.
 * The builder takes various parameters like,columns on which aggregation needs to be done,
 * columns in where clause,having clause like clause etc...
 * Instance of SimpleQueryBuilder needs to be passed to DynamicQueryGenerator.
 */
public class SimpleQueryBuilder extends QueryBuilder {

    private String graphKey;

    public SimpleQueryBuilder(@NonNull String graphKey, List<String> whereCondition, List<String> orderByCondition, List<String> groupByCondition, String limit, String offsetWithLimit, List<String> havingCondition, Map<String, SqlAggregationType> aggregateSelectCols) {
        this.graphKey = graphKey;
        this.whereCondition = whereCondition;
        this.orderByCondition = orderByCondition;
        this.groupByCondition = groupByCondition;
        this.offsetWithLimit = offsetWithLimit;
        this.havingCondition = havingCondition;
        this.limit = limit;
        this.aggregateSelectCols = aggregateSelectCols;
    }

    /**
     * Adds average function on the column name passed
     */
    public static String avg(String columnName) {
        return "avg(" + columnName + ")";
    }

    /**
     * Adds summation function on the column name passed
     */
    public static String sum(String columnName) {
        return "sum(" + columnName + ")";
    }

    /**
     * Adds count function on the column name passed
     */
    public static String count(String columnName) {
        return "count(" + columnName + ")";
    }

    /**
     * Adds min function on the column name passed
     */
    public static String max(String columnName) {
        return "max(" + columnName + ")";
    }

    /**
     * Adds max function on the column name passed
     */
    public static String min(String columnName) {
        return "min(" + columnName + ")";
    }

    public String getGraphKey() {
        return graphKey;
    }

    public static class Builder {

        private String graphKey;
        private List<String> whereCondition = new LinkedList<>();
        private List<String> orderByCondition = new LinkedList<>();
        private List<String> groupByCondition = new LinkedList<>();
        private String limit;
        private String offsetWithLimit;
        private List<String> havingCondition = new LinkedList<>();
        private Map<String, SqlAggregationType> aggregateSelectCols = new LinkedHashMap<>();

        public Builder() {
        }

        /**
         * @param graphKey is obtained from {@link SimpleGraphObject} and is used for aliasing the table by the DynamicQuery framework.
         */
        public Builder setGraphKey(String graphKey) {
            this.graphKey = graphKey;
            return this;
        }

        /**
         * @param columnName    which needs to be added in where clause
         * @param conditionType of type {@link SqlCondition} is to passed sql condition like in,=,<,> etc...
         *                      Eg :for conditionType {@link SqlCondition#equal} the addWhereCondition will be transformed to  {columnName} > ?
         */
        public Builder addWhereCondition(String columnName, SqlCondition conditionType) {
            whereCondition.add(columnName + " " + conditionType.getCondition());
            return this;
        }

        /**
         * @param columnName    is the column name which needs to be added in where clause
         * @param subQuery      is the native sql query which you want to add as a part of sub query,
         * @param conditiontype of type {@link SqlCondition} is to passed sql condition like in,=,<,> etc...
         *                      <p>
         *                      then internally the query will be parsed into <strong> where {columnName} = (staticQuery)</strong>
         */
        public Builder addWhereWithSubQuery(String columnName, String subQuery, @NonNull SqlSubQueryCondition conditiontype) {
            whereCondition.add(columnName + String.format(conditiontype.getCondition(), subQuery));
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
         * Adds passed column in order by clause
         */
        public Builder addOrderByCondition(String columnName) {
            orderByCondition.add(columnName);
            return this;
        }

        /**
         * Adds passed column in group by clause
         */
        public Builder addGroupByCondition(String columnName) {
            groupByCondition.add(columnName);
            return this;
        }

        /**
         * Eg :for conditionType = ">" the addWhereCondition will be transformed to  {graphKey}.{columnName} > ?
         *
         * @param columnName    is the column name which needs to be added in where clause
         * @param conditionType is used to pass having condition like  "=",">" or "<"
         */
        public Builder addHavingCondition(String columnName, SqlCondition conditionType) {
            havingCondition.add(columnName + " " + conditionType.getCondition());
            return this;
        }

        /**
         * addHavingConditionWithSubQuery takes sub query and adds it in where clause.By default conditions are and separated
         *
         * @param columnName   is the column name which needs to be added as a part of where clause
         * @param sqlCondition is object of type SqlSubQueryCondition to provide the condition (eg in,=,!=etc)
         * @param subQuery     is the subquery which needs to be used for  evaluation
         *                     <p>
         *                     Eg: for columnName = "account_id",sqlCondition= {@link SqlSubQueryCondition#in},subQuery = "select * from mytbl",
         *                     subquery generated will have "account_id in (select * from mytbl)" in the having clause
         */
        public Builder addHavingConditionWithSubQuery(String columnName, SqlSubQueryCondition sqlCondition, String subQuery) {
            if (graphKey == null) {
                graphKey = "";
            }
            havingCondition.add(columnName + String.format(sqlCondition.getCondition(), subQuery));
            return this;
        }

        /**
         * Adds or between two having condition
         */
        public Builder orHaving() {
            havingCondition.add("or");
            return this;
        }

        /**
         * To be used to put having conditions in brackets ie .. "(...)".
         */
        public Builder havingStartBracket() {
            havingCondition.add("(");
            return this;
        }

        /**
         * To be used to put having conditions in brackets ie .. "(...)".
         */
        public Builder havingCloseBracket() {
            havingCondition.add(")");
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

        /**
         * Takes aggregate function of enum type @link {@link SqlAggregationType} and table column name on
         * which aggregation needs to be done.
         */
        public Builder addaggregateSelectCols(SqlAggregationType aggregateFunc, String columnName) {
            this.aggregateSelectCols.put(columnName, aggregateFunc);
            return this;
        }

        /**
         * internally this method will result in the sql snippet <strong> limit ? </string>
         */
        public Builder addLimitCondition() {
            this.limit = "?";
            return this;
        }

        /**
         * This method will add limit with offset in the sql snippet.
         * it internally results in the sql snippet <strong> limit ?,? </strong>
         */
        public Builder addOffsetWithLimitCondition() {
            this.offsetWithLimit = "?,?";
            return this;
        }

        /**
         * used to build the query after all the conditions have been added
         */
        public SimpleQueryBuilder build() {
            return new SimpleQueryBuilder(graphKey, whereCondition, orderByCondition, groupByCondition, limit, offsetWithLimit, havingCondition, aggregateSelectCols);
        }

    }

}
