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

public class SimpleQueryBuilderTest {

    @Test
    public void testAvgFunction() {
        Assertions.assertEquals("avg(sampleColName)", SimpleQueryBuilder.avg("sampleColName"));
    }

    @Test
    public void testSumFunction() {
        Assertions.assertEquals("sum(sampleColName)", SimpleQueryBuilder.sum("sampleColName"));
    }

    @Test
    public void testCountFunction() {
        Assertions.assertEquals("count(sampleColName)", SimpleQueryBuilder.count("sampleColName"));
    }

    @Test
    public void testMaxFunction() {
        Assertions.assertEquals("max(sampleColName)", SimpleQueryBuilder.max("sampleColName"));
    }

    @Test
    public void testMinFunction() {
        Assertions.assertEquals("min(sampleColName)", SimpleQueryBuilder.min("sampleColName"));
    }

    @Test
    public void testOrWhere() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .orWhere()
                .build();

        Assertions.assertEquals("or", sqb.getWhereCondition().get(0));
    }

    @Test
    public void testWhereBracketStarts() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .whereStartBracket()
                .whereCloseBracket()
                .build();

        Assertions.assertEquals("()", sqb.getWhereCondition().get(0) + sqb.getWhereCondition().get(1));
    }

    @Test
    public void testHavingCondition() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .addHavingCondition("sampleColumnName", SqlCondition.between)
                .build();

        Assertions.assertEquals("sampleColumnName  between ? and ? ", sqb.getHavingCondition().get(0));
    }

    @Test
    public void testAddHavingConditionWithSubQuery() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .addHavingConditionWithSubQuery("sampleCOlumnName", SqlSubQueryCondition.equal, "Select a from a_tbl")
                .build();

        Assertions.assertEquals("sampleCOlumnName = (Select a from a_tbl)", sqb.getHavingCondition().get(0));
    }

    @Test
    public void testOrHaving() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .orHaving()
                .build();

        Assertions.assertEquals("or", sqb.getHavingCondition().get(0));
    }

    @Test
    public void testHavingBracket() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .havingStartBracket()
                .havingCloseBracket()
                .build();

        Assertions.assertEquals("()", sqb.getHavingCondition().get(0) + sqb.getHavingCondition().get(1));
    }

    @Test
    public void testOrderByDesc() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .orderbyDesc()
                .build();

        Assertions.assertEquals("desc", sqb.getOrderByCondition().get(0));
    }

    @Test
    public void testAddOffsetWithLimitCondition() {
        SimpleQueryBuilder sqb = new SimpleQueryBuilder
                .Builder()
                .addOffsetWithLimitCondition()
                .build();

        Assertions.assertEquals("?,?", sqb.getOffsetWithLimit());
    }

}
