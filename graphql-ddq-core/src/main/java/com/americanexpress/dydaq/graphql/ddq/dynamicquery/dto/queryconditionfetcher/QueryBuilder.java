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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A super class extended by SimpleQueryBuilder and JoinQueryBuilder and have the below
 * defined common methods
 */
public class QueryBuilder {

    protected List<String> whereCondition = new LinkedList<>();
    protected List<String> orderByCondition = new LinkedList<>();
    protected List<String> groupByCondition = new LinkedList<>();
    protected String limit = "";
    protected String offsetWithLimit = "";
    protected Map<String, SqlAggregationType> aggregateSelectCols = new HashMap<>();

    protected List<String> havingCondition = new LinkedList<>();

    public List<String> getHavingCondition() {
        return havingCondition;
    }

    public String getOffsetWithLimit() {
        return offsetWithLimit;
    }

    public String getLimit() {
        return limit;
    }

    public List<String> getGroupByCondition() {
        return groupByCondition;
    }

    public List<String> getOrderByCondition() {
        return orderByCondition;
    }

    public List<String> getWhereCondition() {
        return whereCondition;
    }

    public Map<String, SqlAggregationType> getAggregateSelectCols() {
        return aggregateSelectCols;
    }

}
