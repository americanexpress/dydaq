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

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlCondition;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlSubQueryCondition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JoinQueryBuilderTest {

    @Test
    public void testAvgFunction() {
        Assertions.assertEquals("avg(%ssampleColName)", JoinQueryBuilder.avg("sampleColName"));
    }

    @Test
    public void testSumFunction() {
        Assertions.assertEquals("sum(%ssampleColName)", JoinQueryBuilder.sum("sampleColName"));
    }

    @Test
    public void testCountFunction() {
        Assertions.assertEquals("count(%ssampleColName)", JoinQueryBuilder.count("sampleColName"));
    }

    @Test
    public void testMaxFunction() {
        Assertions.assertEquals("max(%ssampleColName)", JoinQueryBuilder.max("sampleColName"));
    }

    @Test
    public void testMinFunction() {
        Assertions.assertEquals("min(%ssampleColName)", JoinQueryBuilder.min("sampleColName"));
    }

    @Test
    public void testOrWhere() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .orWhere()
                .build();

        Assertions.assertEquals("or", jqb.getWhereCondition().get(0));
    }

    @Test
    public void testWhereBracketStarts() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .whereStartBracket()
                .whereCloseBracket()
                .build();

        Assertions.assertEquals("()", jqb.getWhereCondition().get(0) + jqb.getWhereCondition().get(1));
    }

    @Test
    public void testOrHaving() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .orHaving()
                .build();

        Assertions.assertEquals("or", jqb.getHavingCondition().get(0));
    }

    @Test
    public void testHavingBracket() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .havingStartBracket()
                .havingCloseBracket()
                .build();

        Assertions.assertEquals("()", jqb.getHavingCondition().get(0) + jqb.getHavingCondition().get(1));
    }

    @Test
    public void testOrderByDesc() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .orderbyDesc()
                .build();

        Assertions.assertEquals("desc", jqb.getOrderByCondition().get(0));
    }

    @Test
    public void testAddOffsetWithLimitCondition() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .addOffsetWithLimitCondition()
                .build();

        Assertions.assertEquals("?,?", jqb.getOffsetWithLimit());
    }

    @Test
    public void testAddWhereWithSubQuery() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .addWhereWithSubQuery("sampleGraphKey", "sampleColumnName", "select saample from sample_tbl", SqlSubQueryCondition.in)
                .build();

        Assertions.assertEquals("sampleGraphKey.sampleColumnName in (select saample from sample_tbl)", jqb.getWhereCondition().get(0));
    }

    @Test
    public void testAddHavingCondition() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .addHavingCondition("sampleGraphKey", "sampleColumnName", SqlCondition.equal)
                .build();

        Assertions.assertEquals("sampleGraphKey.sampleColumnName  = ?", jqb.getHavingCondition().get(0));
    }

    @Test
    public void testAddHavingConditionWithSubQuery() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .addHavingConditionWithSubQuery("sampleGraphKey", "sampleColumnName", SqlSubQueryCondition.in, "select saample from sample_tbl")
                .build();

        Assertions.assertEquals("sampleGraphKey.sampleColumnName in (select saample from sample_tbl)", jqb.getHavingCondition().get(0));
    }

    @Test
    public void testSetLimit() {
        JoinQueryBuilder jqb = new JoinQueryBuilder
                .Builder()
                .setLimit(10)
                .build();

        Assertions.assertEquals("10", jqb.getLimit());
    }

}
