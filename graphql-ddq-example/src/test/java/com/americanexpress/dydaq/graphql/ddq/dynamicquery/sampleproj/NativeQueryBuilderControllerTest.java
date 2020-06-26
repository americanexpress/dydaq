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
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.resolver.NativeQueryTestResolver;
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
public class NativeQueryBuilderControllerTest {

    private static final Logger LOGGER = LogManager.getLogger(NativeQueryBuilderControllerTest.class);

    @Autowired
    private NativeQueryTestResolver service;

    @Test
    public void nativeQueryBuilderSimpleQueryTest() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ listSurgeon(Limit:1) {surgeonId fullName contactNo}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listSurgeon\":[{\"surgeonId\":4001,\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\"}]}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void nativeQueryBuilderJoinQueryTest1() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ listSurgeonByHospital(HospitalId:1001) {name surgeon {fullName contactNo}}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listSurgeonByHospital\":{\"name\":\"Hospital Name1\",\"surgeon\":[{\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\"},{\"fullName\":\"Human Name5\",\"contactNo\":\"9981110112\"},{\"fullName\":\"Human Name6\",\"contactNo\":\"7771110112\"}]}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void countSurgeonByHospital() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ countSurgeonByHospital {name surgeon {surgeonId}}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"countSurgeonByHospital\":[{\"name\":\"Hospital Name1\",\"surgeon\":[{\"surgeonId\":3}]},{\"name\":\"Hospital Name2\",\"surgeon\":[{\"surgeonId\":1}]},{\"name\":\"Hospital Name3\",\"surgeon\":[{\"surgeonId\":1}]},{\"name\":\"Hospital Name5\",\"surgeon\":[{\"surgeonId\":1}]}]}", gson.toJson(resultSpecification.get("data")));
        }
        Assertions.assertEquals(0, result.getErrors().size());
    }


    @Test
    public void nativeQueryBuilderJoinQueryTest2() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();
        GraphQL exe = GraphQL.newGraphQL(schema).build();
        Gson gson = new Gson();
        //	System.out.println(gson.toJson(exe.execute(IntrospectionQuery.INTROSPECTION_QUERY).toSpecification().get("data")));
        ExecutionResult result = exe.execute("{ listSurgeonsWithSpeciality(HospitalId:1001) {fullName contactNo docSpeciality{ speciality { speciality } }}}");
        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"listSurgeonsWithSpeciality\":[{\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\",\"docSpeciality\":[{\"speciality\":{\"speciality\":\"Anesthesiologists\"}},{\"speciality\":{\"speciality\":\"Endocrinologists\"}}]},{\"fullName\":\"Human Name6\",\"contactNo\":\"7771110112\",\"docSpeciality\":[{\"speciality\":{\"speciality\":\"Cardiologists\"}}]}]}", gson.toJson(resultSpecification.get("data")));
        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void nativeQueryBuilderWithsimpleAndJoinNativeQuery() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();
        GraphQL exe = GraphQL.newGraphQL(schema).build();
        Gson gson = new Gson();
        ExecutionResult result = exe.execute("{ fetchHospitalWithSurgeonSpeciality(HospitalId:1001) { name surgeon {fullName contactNo docSpeciality{ speciality { speciality } }}}}");
        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"fetchHospitalWithSurgeonSpeciality\":{\"name\":\"Hospital Name1\",\"surgeon\":[{\"fullName\":\"Human Name1\",\"contactNo\":\"8281110112\",\"docSpeciality\":[{\"speciality\":{\"speciality\":\"Anesthesiologists\"}},{\"speciality\":{\"speciality\":\"Endocrinologists\"}}]},{\"fullName\":\"Human Name6\",\"contactNo\":\"7771110112\",\"docSpeciality\":[{\"speciality\":{\"speciality\":\"Cardiologists\"}}]}]}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void nativeQueryBuilderWithsimpleAndJoinNativeQuery1() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();
        GraphQL exe = GraphQL.newGraphQL(schema).build();
        Gson gson = new Gson();
        ExecutionResult result = exe.execute("{ fetchHospitalWithSurgeonSpeciality(HospitalId:1001) { name }}");
        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"fetchHospitalWithSurgeonSpeciality\":{\"name\":\"Hospital Name1\"}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void nativeQueryBuilderWithsimpleAndJoinNativeQueryWithFewFields() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(service).generate();
        GraphQL exe = GraphQL.newGraphQL(schema).build();
        Gson gson = new Gson();
        ExecutionResult result = exe.execute("{ fetchHospitalWithSurgeonSpeciality(HospitalId:1001) { surgeon { docSpeciality{ speciality { speciality } }}}}");
        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"fetchHospitalWithSurgeonSpeciality\":{\"surgeon\":[{\"docSpeciality\":[{\"speciality\":{\"speciality\":\"Anesthesiologists\"}},{\"speciality\":{\"speciality\":\"Endocrinologists\"}}]},{\"docSpeciality\":[{\"speciality\":{\"speciality\":\"Cardiologists\"}}]}]}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

}
