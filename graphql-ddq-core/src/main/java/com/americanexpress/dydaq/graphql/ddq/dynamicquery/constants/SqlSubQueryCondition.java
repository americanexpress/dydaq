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
 * This class is used to add where or having condition by passing a sub query to evaluate with by using provided APIs of the Query Builders(
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.JoinQueryBuilder},
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.SimpleQueryBuilder})
 */
public class SqlSubQueryCondition {

    public static final SqlSubQueryCondition like;
    public static final SqlSubQueryCondition in;
    public static final SqlSubQueryCondition greaterThan;
    public static final SqlSubQueryCondition lessThan;
    public static final SqlSubQueryCondition greaterThanOrEqual;
    public static final SqlSubQueryCondition lessThanOrEqual;
    public static final SqlSubQueryCondition notEqual;
    public static final SqlSubQueryCondition equal;
    public static final SqlSubQueryCondition isNull;
    public static final SqlSubQueryCondition notNull;

    static {
        /**
         * Eg : SqlSubQueryCondition .like("select id from ... limit 1")
         * will add "like (select id from ...)" in the resulting SQL query
         * */
        like = new SqlSubQueryCondition(" like (%s)");
        /**
         * Eg : SqlSubQueryCondition .greaterThan("select id from ...")
         * will add "> (select id from ...)" in the resulting SQL query
         * */
        greaterThan = new SqlSubQueryCondition(" > (%s)");
        /**
         * Eg : SqlSubQueryCondition .lessThan("select id from ...")
         * will add "< (select id from ...)" in the resulting SQL query
         * */
        lessThan = new SqlSubQueryCondition(" < (%s)");
        /**
         * Eg : SqlSubQueryCondition .greaterThanOrEqual("select id from ...")
         * will add ">= (select id from ...)" in the resulting SQL query
         * */
        greaterThanOrEqual = new SqlSubQueryCondition(" >= (%s)");
        /**
         * Eg : SqlSubQueryCondition .lessThanOrEqual("select id from ...")
         * will add " <= (select id from ...)" in the resulting SQL query
         * */
        lessThanOrEqual = new SqlSubQueryCondition(" <= (%s)");

        /**
         * Eg : SqlSubQueryCondition .equal("select id from ...")
         * will add " = (select id from ...)" in the resulting SQL query
         * */
        notEqual = new SqlSubQueryCondition(" != (%s)");

        /**
         * Eg : SqlSubQueryCondition .notEqual("select id from ...")
         * will add " != (select id from ...)" in the resulting SQL query
         * */
        equal = new SqlSubQueryCondition(" = (%s)");

        /**
         Eg : SqlSubQueryCondition .in("select id from ...")
         * will add " in (select id from ...)" in the resulting SQL query
         * */
        in = new SqlSubQueryCondition(" in (%s)");

        /**
         * adds snippet " is null " into the resulting SQL query
         * */
        isNull = new SqlSubQueryCondition(" is null ");

        /**
         * adds snippet " is not null " into the resulting SQL query
         * */
        notNull = new SqlSubQueryCondition(" is not null ");
    }

    private String condition;

    private SqlSubQueryCondition(String condition) {
        this.condition = condition;
    }

    private SqlSubQueryCondition() {
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}
