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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.SampleProjectHospitalManagement;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.resolver.SimpleQueryResolver;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.helper.TestSchemaGenerator;
import com.google.gson.Gson;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        SampleProjectHospitalManagement.class
})
@PropertySource("classpath:application.yml")
public class SimpleQueryBuilderControllerTest {

    private static final Logger LOGGER = LogManager.getLogger(SimpleQueryBuilderControllerTest.class);

    @Autowired
    private SimpleQueryResolver resolver;

    @Test
    public void simpleQueryBuilderWithLimit() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ listSurgeonWithLimit(Limit:1) {surgeonId fullName contactNo}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listSurgeonWithLimit\":[{\"surgeonId\":4001,\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\"}]}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void simpleQueryBuilderWithOrderBy() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ listSurgeonWithOrderByContactNo {fullName contactNo}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listSurgeonWithOrderByContactNo\":[{\"fullName\":\"Human Name4\",\"contactNo\":\"1181110112\"},{\"fullName\":\"Human Name6\",\"contactNo\":\"7771110112\"},{\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\"},{\"fullName\":\"Human Name3\",\"contactNo\":\"8281551112\"},{\"fullName\":\"Human Name2\",\"contactNo\":\"9881110112\"},{\"fullName\":\"Human Name5\",\"contactNo\":\"9981110112\"}]}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void simpleQueryBuilderWithGroupBy() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ listSurgeonWithGroupBy {fullName contactNo}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listSurgeonWithGroupBy\":[{\"fullName\":\"Human Name4\",\"contactNo\":\"1181110112\"},{\"fullName\":\"Human Name6\",\"contactNo\":\"7771110112\"},{\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\"},{\"fullName\":\"Human Name3\",\"contactNo\":\"8281551112\"},{\"fullName\":\"Human Name2\",\"contactNo\":\"9881110112\"},{\"fullName\":\"Human Name5\",\"contactNo\":\"9981110112\"}]}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void simpleQueryBuilderWithMaxEmployeeHospital() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();
        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ hospitalWithMaxEmployee {hospitalId name noOfEmployees city contactNo}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"hospitalWithMaxEmployee\":{\"hospitalId\":1005,\"name\":\"Hospital Name5\",\"noOfEmployees\":300,\"city\":\"Bangalore\",\"contactNo\":\"8883155201\"}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void simpleQueryBuilderWithHospitalIn() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ listHospitalById(HospitalList:[1001,1002]) {name}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listHospitalById\":[{\"name\":\"Hospital Name1\"},{\"name\":\"Hospital Name2\"}]}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void simpleQueryBuilderCountHospitalByCity() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ countHospitalByCity{city hospitalCount}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"countHospitalByCity\":[{\"city\":\"Bangalore\",\"hospitalCount\":2},{\"city\":\"Hosmat\",\"hospitalCount\":1},{\"city\":\"Marathalli\",\"hospitalCount\":1},{\"city\":\"Munnekolala\",\"hospitalCount\":1}]}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

}
