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
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.resolver.JoinQueryResolver;
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
public class JoinQueryBuilderControllerTest {

    private static final Logger LOGGER = LogManager.getLogger(JoinQueryBuilderControllerTest.class);

    @Autowired
    private JoinQueryResolver resolver;

    @Test
    public void fetchHospitalWithSurgeonSpeciality() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ fetchHospitalWithSurgeonSpeciality(HospitalId:1001) {name surgeon { fullName docSpeciality { experience } }}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"fetchHospitalWithSurgeonSpeciality\":{\"name\":\"Hospital Name1\",\"surgeon\":[{\"fullName\":\"Human Name1\",\"docSpeciality\":[{\"experience\":\"10\"},{\"experience\":\"6\"}]},{\"fullName\":\"Human Name6\",\"docSpeciality\":[{\"experience\":\"20\"}]}]}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void fetchHospitalWithSurgeonSpecialitySkipRootObject() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ fetchHospitalWithSurgeonSpeciality(HospitalId:1001) { surgeon { fullName docSpeciality { experience } }}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"fetchHospitalWithSurgeonSpeciality\":{\"surgeon\":[{\"fullName\":\"Human Name1\",\"docSpeciality\":[{\"experience\":\"10\"},{\"experience\":\"6\"}]},{\"fullName\":\"Human Name6\",\"docSpeciality\":[{\"experience\":\"20\"}]}]}}", gson.toJson(resultSpecification.get("data")));

        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void fetchHospitalWithSurgeonSpecialityTwoQuerys() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ fetchHospitalWithSurgeonSpecialityTwoQuerys(HospitalId:1001) {name surgeon { fullName docSpeciality { experience } }}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"fetchHospitalWithSurgeonSpecialityTwoQuerys\":{\"name\":\"Hospital Name1\",\"surgeon\":[{\"fullName\":\"Human Name1\",\"docSpeciality\":[{\"experience\":\"10\"},{\"experience\":\"6\"}]},{\"fullName\":\"Human Name6\",\"docSpeciality\":[{\"experience\":\"20\"}]}]}}", gson.toJson(resultSpecification.get("data")));
        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

    @Test
    public void countSurgeonByHospital() {
        GraphQLSchema schema = new TestSchemaGenerator().withOperationsFromSingleton(resolver).generate();

        GraphQL exe = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = exe.execute("{ countSurgeonByHospitalJQB {name surgeon {surgeonId}}}");
        Gson gson = new Gson();

        if (null != result) {
            Map resultSpecification = result.toSpecification();
            LOGGER.debug("Returning execution result:{}", gson.toJson(resultSpecification.get("data")));
            Assertions.assertEquals("{\"countSurgeonByHospitalJQB\":[{\"name\":\"Hospital Name1\",\"surgeon\":[{\"surgeonId\":3}]},{\"name\":\"Hospital Name2\",\"surgeon\":[{\"surgeonId\":1}]},{\"name\":\"Hospital Name3\",\"surgeon\":[{\"surgeonId\":1}]},{\"name\":\"Hospital Name5\",\"surgeon\":[{\"surgeonId\":1}]}]}", gson.toJson(resultSpecification.get("data")));
        }
        Assertions.assertEquals(0, result.getErrors().size());
    }

}
