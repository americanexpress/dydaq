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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants;

/**
 * These aggregation functions should be used when doing aggregation on graphql fields using the query builders(
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.JoinQueryBuilder},
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.SimpleQueryBuilder},
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.NativeQueryBuilder})
 */
public enum SqlAggregationType {


    AVG(" avg(%s) "),
    SUM(" sum(%s)"),
    COUNT(" count(%s) "),
    MAX(" max(%s) "),
    MIN(" min(%s) ");

    private String aggregateType;

    SqlAggregationType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

}
