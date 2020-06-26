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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.resolver;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.DynamicQueryGenerator;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlAggregationType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants.HospitalMeta;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants.HospitalToSurgeonSimpleRelMeta;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants.SurgeonMeta;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants.SurgeonSimpleMeta;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.Hospital;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.HospitalToSurgeonSimpleRel;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.Surgeon;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.SurgeonSimple;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.NativeQueryBuilder;
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
public class NativeQueryTestResolver {

    private static final Logger LOGGER = LogManager.getLogger(NativeQueryTestResolver.class);


    private ResultSetExtractor<List<Hospital>> resultSetExtractorHosp =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .addKeys("hospital_id", "surgeon_surgeon_id")
                    .newResultSetExtractor(Hospital.class);

    private ResultSetExtractor<List<HospitalToSurgeonSimpleRel>> resultSetExtractorHospToSurgn =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .addKeys("hospital_id", "surgeon_surgeon_id")
                    .newResultSetExtractor(HospitalToSurgeonSimpleRel.class);

    private ResultSetExtractor<List<Surgeon>> resultSetExtractorSurgeon =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .addKeys("surgeon_id", "docSpeciality_surgeon_id")
                    .newResultSetExtractor(Surgeon.class);

    private ResultSetExtractor<List<SurgeonSimple>> resultSetExtractorSurgSimple =
            JdbcTemplateMapperFactory
                    .newInstance()
                    .newResultSetExtractor(SurgeonSimple.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GraphQLQuery
    public List<SurgeonSimple> listSurgeon(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "Limit") int limit) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        String surgeonQueryTemplate = " from surgeon_tbl limit ?";
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder
                .SimpleBuilder()
                .setGraphKey(SurgeonSimpleMeta.GRAPH_LEVEL)
                .setQueryTemplate(surgeonQueryTemplate)
                .build();


        String surgeonQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilder);
        LOGGER.info("Surgeon Query :: {},", surgeonQuery);
        //Surgeon Query :: select  hospital_id, dept_id, full_name, surgeon_id, contact_no  from surgeon_tbl limit ?
        return jdbcTemplate.query(surgeonQuery, new Object[]{limit}, resultSetExtractorSurgSimple);
    }

    @GraphQLQuery
    public HospitalToSurgeonSimpleRel listSurgeonByHospital(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "HospitalId") int hospitalId) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        String surgeonQueryTemplate = " from hospital_tbl ${a} inner join surgeon_tbl ${b} on ${a}.hospital_id = ${b}.hospital_id where ${a}.hospital_id = ?";
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder
                .JoinQueryBuilder()
                .setRootKey(HospitalToSurgeonSimpleRelMeta.GRAPH_LEVEL)
                .setQueryTemplate(surgeonQueryTemplate)
                .addTblAliasToGraphMap("a", HospitalToSurgeonSimpleRelMeta.GRAPH_LEVEL)
                .addTblAliasToGraphMap("b", HospitalToSurgeonSimpleRelMeta.Surgeon.GRAPH_LEVEL)
                .build();


        String surgeonQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilder);
        LOGGER.info("Surgeon Query 2 :: {}", surgeonQuery);
        //Surgeon Query 2 :: select  rootObject.hospital_id as hospital_id, rootObject.name as name, surgeon.full_name as surgeon_full_name, surgeon.surgeon_id as surgeon_surgeon_id, surgeon.contact_no as surgeon_contact_no  from hospital_tbl rootObject inner join surgeon_tbl surgeon on rootObject.hospital_id = surgeon.hospital_id where rootObject.hospital_id = ?
        List<HospitalToSurgeonSimpleRel> hopitalDetails = jdbcTemplate.query(surgeonQuery, new Object[]{hospitalId}, resultSetExtractorHospToSurgn);
        return !hopitalDetails.isEmpty() ? hopitalDetails.get(0) : null;
    }

    @GraphQLQuery
    public List<HospitalToSurgeonSimpleRel> countSurgeonByHospital(@GraphQLEnvironment ResolutionEnvironment env) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        String surgeonQueryTemplate = " from hospital_tbl ${a} inner join surgeon_tbl ${b} on ${a}.hospital_id = ${b}.hospital_id group by ${a}.hospital_id ";
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder
                .JoinQueryBuilder()
                .setRootKey(HospitalMeta.GRAPH_LEVEL)
                .setQueryTemplate(surgeonQueryTemplate)
                .addaggregateSelectCols(SqlAggregationType.COUNT, "${b}.surgeon_id")
                .addTblAliasToGraphMap("a", HospitalToSurgeonSimpleRelMeta.GRAPH_LEVEL)
                .addTblAliasToGraphMap("b", HospitalToSurgeonSimpleRelMeta.Surgeon.GRAPH_LEVEL)
                .build();


        String surgeonCountByHospitalQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilder);
        LOGGER.info("SurgeonCountByHospitalQuery Query :: {}", surgeonCountByHospitalQuery);
        //Surgeon Query 2 :: select  rootObject.hospital_id as hospital_id, rootObject.name as name, surgeon.full_name as surgeon_full_name, surgeon.surgeon_id as surgeon_surgeon_id, surgeon.contact_no as surgeon_contact_no  from hospital_tbl rootObject inner join surgeon_tbl surgeon on rootObject.hospital_id = surgeon.hospital_id where rootObject.hospital_id = ?
        return jdbcTemplate.query(surgeonCountByHospitalQuery, resultSetExtractorHospToSurgn);
    }

    @GraphQLQuery
    public List<Surgeon> listSurgeonsWithSpeciality(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "HospitalId") int hospitalId) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);
        String surgeonQueryTemplate = " from surgeon_tbl ${a} inner join doc_speciality_tbl ${b} on ${a}.surgeon_id = ${b}.surgeon_id inner join speciality_tbl ${c} on ${b}.speciality_id = ${c}.speciality_id where ${a}.hospital_id = ? order by ${b}.surgeon_id";
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder
                .JoinQueryBuilder()
                .setRootKey(SurgeonMeta.GRAPH_LEVEL)
                .setQueryTemplate(surgeonQueryTemplate)
                .addTblAliasToGraphMap("a", SurgeonMeta.GRAPH_LEVEL)
                .addTblAliasToGraphMap("b", SurgeonMeta.DocSpeciality.GRAPH_LEVEL)
                .addTblAliasToGraphMap("c", SurgeonMeta.DocSpeciality.Speciality.GRAPH_LEVEL)
                .build();


        String surgeonQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilder);
        LOGGER.info("Surgeon Query 3 :: {}", surgeonQuery);
        //Surgeon Query 3 :: select  rootObject.full_name as full_name, rootObject.surgeon_id as surgeon_id, rootObject.contact_no as contact_no, docSpeciality.id as docSpeciality_id, docSpeciality_speciality.speciality as docSpeciality_speciality_speciality, docSpeciality_speciality.speciality_id as docSpeciality_speciality_speciality_id  from surgeon_tbl rootObject inner join doc_speciality_tbl docSpeciality on rootObject.surgeon_id = docSpeciality.surgeon_id inner join speciality_tbl docSpeciality_speciality on docSpeciality.speciality_id = docSpeciality_speciality.speciality_id where rootObject.hospital_id = ? order by docSpeciality.surgeon_id
        return jdbcTemplate.query(surgeonQuery, new Object[]{hospitalId}, resultSetExtractorSurgeon);
    }

    @GraphQLQuery
    public Hospital fetchHospitalWithSurgeonSpeciality(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "HospitalId") int hospitalId) {

        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);

        String hospitalQueryTemplate = " from hospital_tbl where hospital_id = ?";
        NativeQueryBuilder nativeQueryBuilderHosp = new NativeQueryBuilder
                .SimpleBuilder()
                .setGraphKey(HospitalMeta.GRAPH_LEVEL)
                .setQueryTemplate(hospitalQueryTemplate)
                .build();


        String hospitalQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilderHosp);
        LOGGER.info("Hospital Query 4 :: {}", hospitalQuery);
        List<Hospital> hopitalDetails = jdbcTemplate.query(hospitalQuery, new Object[]{hospitalId}, resultSetExtractorHosp);
        Hospital hospitalDet = !hopitalDetails.isEmpty() ? hopitalDetails.get(0) : null;


        if (null != hospitalDet) {
            String surgeonQueryTemplate = " from surgeon_tbl ${a} inner join doc_speciality_tbl ${b} on ${a}.surgeon_id = ${b}.surgeon_id inner join speciality_tbl ${c} on ${b}.speciality_id = ${c}.speciality_id where ${a}.hospital_id = ? order by ${b}.surgeon_id";
            NativeQueryBuilder nativeQueryBuilderSurg = new NativeQueryBuilder
                    .JoinQueryBuilder()
                    .setRootKey(HospitalMeta.Surgeon.GRAPH_LEVEL)
                    .setQueryTemplate(surgeonQueryTemplate)
                    .addTblAliasToGraphMap("a", HospitalMeta.Surgeon.GRAPH_LEVEL)
                    .addTblAliasToGraphMap("b", HospitalMeta.Surgeon.DocSpeciality.GRAPH_LEVEL)
                    .addTblAliasToGraphMap("c", HospitalMeta.Surgeon.DocSpeciality.Speciality.GRAPH_LEVEL)
                    .build();


            String surgeonQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilderSurg);
            LOGGER.info("Surgeon Query 4 :: {}", surgeonQuery);
            //Surgeon Query 4 :: select  surgeon.full_name as full_name, surgeon.surgeon_id as surgeon_id, surgeon.contact_no as contact_no, surgeon_docSpeciality.id as docSpeciality_id, surgeon_docSpeciality_speciality.speciality as docSpeciality_speciality_speciality, surgeon_docSpeciality_speciality.speciality_id as docSpeciality_speciality_speciality_id  from surgeon_tbl surgeon inner join doc_speciality_tbl surgeon_docSpeciality on surgeon.surgeon_id = surgeon_docSpeciality.surgeon_id inner join speciality_tbl surgeon_docSpeciality_speciality on surgeon_docSpeciality.speciality_id = surgeon_docSpeciality_speciality.speciality_id where surgeon.hospital_id = ? order by surgeon_docSpeciality.surgeon_id
            List<Surgeon> surgeonDetails;
            if (!surgeonQuery.equals("")) {
                surgeonDetails = jdbcTemplate.query(surgeonQuery, new Object[]{hospitalId}, resultSetExtractorSurgeon);
                hospitalDet.setSurgeon(surgeonDetails);
            }
        }
        return hospitalDet;
    }

}
