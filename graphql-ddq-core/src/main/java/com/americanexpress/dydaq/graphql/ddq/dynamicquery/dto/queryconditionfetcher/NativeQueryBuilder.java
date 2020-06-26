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

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.ErrorConstants;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlAggregationType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.gqlrequestmeta.SimpleGraphObject;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.exception.DynamicQueryException;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a builder object.It needs to be used when a SQL native query is to be created on a single/multiple entity.
 * The builder takes various parameters like native query snippet ,columns on which aggregation needs to be done,
 * columns alias in then native query to graph level mappings,rootKey in case of building join query,graphKey in case of native query
 * Instance of SimpleQueryBuilder needs to be passed to DynamicQueryGenerator.
 */
public class NativeQueryBuilder {

    private String graphKey;
    private String queryTemplate;
    private String rootKey;
    private Map<String, String> aliasToGraphMap;
    private Map<String, SqlAggregationType> aggregateSelectCols;

    //NativeQueryBuilder instance for SimpleQuery(ie.. query on a single entity)
    public NativeQueryBuilder(@NonNull String graphKey, String queryTemplate, Map<String, SqlAggregationType> aggregateSelectCols) {
        this.graphKey = graphKey;
        this.queryTemplate = queryTemplate;
        this.aggregateSelectCols = aggregateSelectCols;
    }

    //NativeQueryBuilder instance for SimpleQuery(ie.. query on a multiple entity)
    public NativeQueryBuilder(String rootKey, String queryTemplate, Map<String, SqlAggregationType> aggregateSelectCols, Map<String, String> aliasToGraphMap) {
        this.rootKey = rootKey;
        this.queryTemplate = queryTemplate;
        this.aliasToGraphMap = aliasToGraphMap;
        this.aggregateSelectCols = aggregateSelectCols;
    }

    public String getGraphKey() {
        return graphKey;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    public String getRootKey() {
        return rootKey;
    }

    public Map<String, String> getAliasToGraphMap() {
        return aliasToGraphMap;
    }

    public Map<String, SqlAggregationType> getAggregateSelectCols() {
        return aggregateSelectCols;
    }

    public static class SimpleBuilder {

        private String graphKey;
        private String queryTemplate;
        private Map<String, SqlAggregationType> aggregateSelectCols = new LinkedHashMap<>();

        public SimpleBuilder() {
        }

        /**
         * @param graphKey is obtained from {@link SimpleGraphObject} and is used for aliasing the table by the DynamicQuery framework.
         */
        public SimpleBuilder setGraphKey(String graphKey) {
            this.graphKey = graphKey;
            return this;
        }

        /**
         * Takes a Query Template.Template is query without any select columns eg : "from my_table where ..."
         */
        public SimpleBuilder setQueryTemplate(String queryTemplate) {
            this.queryTemplate = queryTemplate;
            return this;
        }

        /**
         * Takes aggregate function of enum type @link {@link SqlAggregationType} and table column name on
         * which aggregation needs to be done.
         */
        public SimpleBuilder addaggregateSelectCols(SqlAggregationType aggregateFunc, String columnName) {
            this.aggregateSelectCols.put(columnName, aggregateFunc);
            return this;
        }

        /**
         * Returns an instance of NativeQueryBuilder built using the graphkey and query template passed
         */
        public NativeQueryBuilder build() {

            if (graphKey == null) {
                throw new DynamicQueryException(ErrorConstants.GRAPH_KEY_NULL_NSQB);
            }

            if (queryTemplate == null) {
                throw new DynamicQueryException(ErrorConstants.QUERY_TEMPLATE_NULL_NJQB);
            }
            return new NativeQueryBuilder(graphKey, queryTemplate, aggregateSelectCols);
        }

    }

    public static class JoinQueryBuilder {

        private String rootKey;
        private String queryTemplate;
        private Map<String, String> aliasToGraphMap = new LinkedHashMap<>();
        private Map<String, SqlAggregationType> aggregateSelectCols = new LinkedHashMap<>();

        public JoinQueryBuilder() {
        }

        /**
         * @param rootKey is obtained from {@link SimpleGraphObject} and is used for aliasing the table by the DynamicQuery framework.
         */
        public JoinQueryBuilder setRootKey(String rootKey) {
            this.rootKey = rootKey;
            return this;
        }

        public JoinQueryBuilder addTblAliasToGraphMap(String tableAlias, String graphKey) {
            if (!tableAlias.equals(graphKey)) {
                this.aliasToGraphMap.put(tableAlias, graphKey);
            }
            return this;
        }

        public JoinQueryBuilder setQueryTemplate(String queryTemplate) {
            this.queryTemplate = queryTemplate;
            return this;
        }

        /**
         * Takes aggregate function of enum type @link {@link SqlAggregationType} and table column name on
         * which aggregation needs to be done.Here the column name should be in form of {table_alias}.columnName
         * where table_alias is alias of table used in native query template.
         */
        public JoinQueryBuilder addaggregateSelectCols(SqlAggregationType aggregateFunc, String columnName) {
            this.aggregateSelectCols.put(columnName, aggregateFunc);
            return this;
        }

        /**
         * Returns an instance of NativeQueryBuilder built using the JoinQueryBuilder object
         */
        public NativeQueryBuilder build() {
            if (rootKey == null) {
                throw new DynamicQueryException(ErrorConstants.ROOT_KEY_NULL_NJQB);
            }

            if (queryTemplate == null) {
                throw new DynamicQueryException(ErrorConstants.QUERY_TEMPLATE_NULL_NJQB);
            }

            return new NativeQueryBuilder(rootKey, queryTemplate, aggregateSelectCols, aliasToGraphMap);
        }

    }


}
