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
 * This class is used to add where or having condition using provided APIs of the Query Builders(
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.JoinQueryBuilder},
 * {@link com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.SimpleQueryBuilder})
 */
public class SqlCondition {

    public static final SqlCondition like;
    public static final SqlCondition greaterThan;
    public static final SqlCondition lessThan;
    public static final SqlCondition greaterThanOrEqual;
    public static final SqlCondition lessThanOrEqual;
    public static final SqlCondition notEqual;
    public static final SqlCondition between;
    public static final SqlCondition equal;
    public static final SqlCondition isNull;
    public static final SqlCondition notNull;

    static {
        /**
         * adds snippet "like ?" into the resulting SQL query
         * */
        like = new SqlCondition(" like ?");
        /**
         * adds snippet "> ?" into the resulting SQL query
         * */
        greaterThan = new SqlCondition(" > ?");
        /**
         * adds snippet "< ?" into the resulting SQL query
         * */
        lessThan = new SqlCondition(" < ?");
        /**
         * adds snippet " >= ?" into the resulting SQL query
         * */
        greaterThanOrEqual = new SqlCondition(" >= ?");
        /**
         * adds snippet " <= ?" into the resulting SQL query
         * */
        lessThanOrEqual = new SqlCondition(" <= ?");
        /**
         * adds snippet " != ? " into the resulting SQL query
         * */
        notEqual = new SqlCondition(" != ?");
        /**
         * adds snippet " = ? " into the resulting SQL query
         */
        equal = new SqlCondition(" = ?");
        /**
         * adds snippet " between ? and ? " into the resulting SQL query
         * */
        between = new SqlCondition(" between ? and ? ");
        /**
         * adds snippet " is null " into the resulting SQL query
         * */
        isNull = new SqlCondition(" is null ");
        /**
         * adds snippet " is not null " into the resulting SQL query
         * */
        notNull = new SqlCondition(" is not null ");
    }

    private String condition;

    private SqlCondition(String condition) {
        this.condition = condition;
    }

    private SqlCondition() {
    }

    /**
     * adds noOfParams times "?" inside in clause.
     * <p>
     * Eg : SqlCondition.in(3) will add " in (?,?,?) " in the resulting SQL query
     */
    public static SqlCondition in(int noOfParams) {
        StringBuilder inParams = new StringBuilder(" in (");
        while (noOfParams > 0) {
            inParams.append(",?");
            noOfParams--;
        }
        inParams.deleteCharAt(5).append(")");
        return new SqlCondition(inParams.toString());
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}
