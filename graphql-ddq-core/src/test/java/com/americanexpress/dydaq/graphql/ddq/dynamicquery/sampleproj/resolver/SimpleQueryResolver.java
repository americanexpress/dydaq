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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.resolver;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.DynamicQueryGenerator;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlAggregationType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlCondition;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlSubQueryCondition;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.SimpleQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.dto.Hospital;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.dto.HospitalCount;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.dto.Surgeon;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@GraphQLApi
public class SimpleQueryResolver {

    private static final Logger LOGGER = LogManager.getLogger(SimpleQueryResolver.class);


    private ResultSetExtractor<List<Hospital>> resultSetExtractorHosp =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .addKeys("hospital_id", "surgeon_surgeon_id")
                    .newResultSetExtractor(Hospital.class);
    private ResultSetExtractor<List<HospitalCount>> resultSetExtractorHospCount =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .newResultSetExtractor(HospitalCount.class);

    private ResultSetExtractor<List<Surgeon>> resultSetExtractorSurgeon =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .addKeys("surgeon_id", "docSpeciality_surgeon_id")
                    .newResultSetExtractor(Surgeon.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GraphQLQuery
    public List<Surgeon> listSurgeonWithLimit(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "Limit") int limit) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);

        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                .Builder()
                .setGraphKey("rootObject")
                .addLimitCondition()
                .build();

        String surgeonQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
        LOGGER.info("Surgeon Query :: {}", surgeonQuery);
        //Surgeon Query Generated:: select hospital_id, dept_id, full_name, surgeon_id, contact_no from surgeon_tbl limit ?
        return jdbcTemplate.query(surgeonQuery, new Object[]{limit}, resultSetExtractorSurgeon);
    }

    @GraphQLQuery
    public List<Surgeon> listSurgeonWithOrderByContactNo(@GraphQLEnvironment ResolutionEnvironment env) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);

        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                .Builder()
                .setGraphKey("rootObject")
                .addOrderByCondition("contact_no")
                .build();

        String surgeonQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
        LOGGER.info("Surgeon Query Order By Contact No :: {}", surgeonQuery);
        //Surgeon Query Order By Contact No Generated:: select hospital_id, dept_id, full_name, surgeon_id, contact_no from surgeon_tbl Order By contact_no
        return jdbcTemplate.query(surgeonQuery, resultSetExtractorSurgeon);
    }

    @GraphQLQuery
    public List<Surgeon> listSurgeonWithGroupBy(@GraphQLEnvironment ResolutionEnvironment env) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);

        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                .Builder()
                .setGraphKey("rootObject")
                .addOrderByCondition("contact_no")
                .orderbyAsc()
                .build();

        String surgeonQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
        LOGGER.info("Surgeon Query Order By Contact No :: {}", surgeonQuery);
        //Surgeon Query Order By Contact No Generated:: select hospital_id, dept_id, full_name, surgeon_id, contact_no from surgeon_tbl Group By contact_no,surgeon_id
        return jdbcTemplate.query(surgeonQuery, resultSetExtractorSurgeon);
    }

    @GraphQLQuery
    public Hospital hospitalWithMaxEmployee(@GraphQLEnvironment ResolutionEnvironment env) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        String hospWithMaxEmployee = "select hospital_id from hospital_tbl order by no_of_employees desc limit 1";
        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                .Builder()
                .setGraphKey("rootObject")
                .addWhereWithSubQuery("hospital_id", hospWithMaxEmployee, SqlSubQueryCondition.in)
                .build();

        String hospitalQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
        LOGGER.info("Hospital with Max employees :: {}", hospitalQuery);
        //Hospital with Max employees Generated:: select hospital_id, city, name, no_of_employees, contact_no from hospital_tbl where hospital_id in (select hospital_id from hospital_tbl order by no_of_employees desc limit 1)
        List<Hospital> hospitalDetails = jdbcTemplate.query(hospitalQuery, resultSetExtractorHosp);
        return !hospitalDetails.isEmpty() ? hospitalDetails.get(0) : null;
    }

    @GraphQLQuery
    public List<Hospital> listHospitalById(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "HospitalList") List<Integer> hospitalList) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                .Builder()
                .setGraphKey("rootObject")
                .addWhereCondition("hospital_id", SqlCondition.in(2))
                .build();

        String hospitalQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
        LOGGER.info("Hospital list By Id :: {}", hospitalQuery);
        //Hospital list By Id Generated:: select hospital_id, name from hospital_tbl where hospital_id  in (?,?)
        return jdbcTemplate.query(hospitalQuery, new Object[]{hospitalList.get(0), hospitalList.get(1)}, resultSetExtractorHosp);
    }

    @GraphQLQuery
    public List<HospitalCount> countHospitalByCity(@GraphQLEnvironment ResolutionEnvironment env) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder
                .Builder()
                .setGraphKey("rootObject")
                .addaggregateSelectCols(SqlAggregationType.COUNT, "hospital_id")
                .addGroupByCondition("city")
                .build();

        String hospitalQuery = dynamicQueryGenerator.getSelectQuery(simpleQueryBuilder);
        LOGGER.info("Hospital list By Id :: {}", hospitalQuery);
        //Hospital list By Id Generated:: select hospital_id, name from hospital_tbl where hospital_id  in (?,?)
        return jdbcTemplate.query(hospitalQuery, resultSetExtractorHospCount);
    }

}
