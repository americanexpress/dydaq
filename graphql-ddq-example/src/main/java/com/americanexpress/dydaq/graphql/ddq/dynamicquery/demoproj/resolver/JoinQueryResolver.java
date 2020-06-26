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
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlCondition;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.SqlJoinType;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants.HospitalMeta;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants.HospitalToSurgeonSimpleRelMeta;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.Hospital;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.HospitalToSurgeonSimpleRel;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.Surgeon;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.JoinQueryBuilder;
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
public class JoinQueryResolver {

    private static final Logger LOGGER = LogManager.getLogger(JoinQueryResolver.class);


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
                    .addKeys("surgeon_id", "docSpeciality_surgeon_id")//or annotate the primary keys in the entity base with @Key annotation of SimpleFlatMapper
                    .newResultSetExtractor(Surgeon.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GraphQLQuery
    public Hospital fetchHospitalWithSurgeonSpeciality(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "HospitalId") int hospitalId) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);

        JoinQueryBuilder joinQueryBuider = new JoinQueryBuilder
                .Builder()
                .setRootKey(HospitalMeta.GRAPH_LEVEL)
                .joinWithGraph(HospitalMeta.Surgeon.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)
                .joinWithGraph(HospitalMeta.Surgeon.DocSpeciality.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)
                .joinWithGraph(HospitalMeta.Surgeon.DocSpeciality.Speciality.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)
                .addWhereCondition(HospitalMeta.GRAPH_LEVEL, HospitalMeta.AllColumns.HOSPITAL_ID, SqlCondition.equal)
                .build();

        String hospitalQuery = dynamicQueryGenerator.getJoinQuery(joinQueryBuider);
        LOGGER.info("Hospital Query :: {}", hospitalQuery);
        //Hospital Query Generated:: select  rootObject.hospital_id as hospital_id, rootObject.name as name, surgeon.full_name as surgeon_full_name, surgeon.surgeon_id as surgeon_surgeon_id, surgeon_docSpeciality.id as surgeon_docSpeciality_id, surgeon_docSpeciality.experience as surgeon_docSpeciality_experience from hospital_tbl rootObject   inner join  surgeon_tbl surgeon on  rootObject.hospital_id = surgeon.hospital_id    inner join  doc_speciality_tbl surgeon_docSpeciality on  surgeon.surgeon_id = surgeon_docSpeciality.surgeon_id  where rootObject.hospital_id = ?
        List<Hospital> hopitalDetails = jdbcTemplate.query(hospitalQuery, new Object[]{hospitalId}, resultSetExtractorHosp);

        return !hopitalDetails.isEmpty() ? hopitalDetails.get(0) : null;
    }

    @GraphQLQuery
    public Hospital fetchHospitalWithSurgeonSpecialityTwoQuerys(@GraphQLEnvironment ResolutionEnvironment env, @GraphQLArgument(name = "HospitalId") int hospitalId) {

        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);

        String hospitalQueryTemplate = " from hospital_tbl where hospital_id = ?";
        NativeQueryBuilder nativeQueryBuilderHosp = new NativeQueryBuilder
                .SimpleBuilder()
                .setGraphKey(HospitalMeta.GRAPH_LEVEL)
                .setQueryTemplate(hospitalQueryTemplate)
                .build();


        String hospitalQuery = dynamicQueryGenerator.getNativeQuery(nativeQueryBuilderHosp);
        LOGGER.info("Hospital Query 2 :: {},", hospitalQuery);
        //Hospital Query 2 Generated:: select  hospital_id, name  from hospital_tbl where hospital_id = ?
        List<Hospital> hopitalDetails = jdbcTemplate.query(hospitalQuery, new Object[]{hospitalId}, resultSetExtractorHosp);

        Hospital hospitalDet = !hopitalDetails.isEmpty() ? hopitalDetails.get(0) : null;

        if (hospitalDet != null) {
            JoinQueryBuilder joinQueryBuider = new JoinQueryBuilder
                    .Builder()
                    .setRootKey(HospitalMeta.Surgeon.GRAPH_LEVEL)
                    .joinWithGraph(HospitalMeta.Surgeon.DocSpeciality.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)
                    .joinWithGraph(HospitalMeta.Surgeon.DocSpeciality.Speciality.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)
                    .addWhereCondition(HospitalMeta.Surgeon.GRAPH_LEVEL, HospitalMeta.Surgeon.AllColumns.HOSPITAL_ID, SqlCondition.equal)
                    .addOrderByCondition(HospitalMeta.Surgeon.GRAPH_LEVEL, HospitalMeta.Surgeon.AllColumns.SURGEON_ID)
                    .build();

            String hospitalQueryPart2 = dynamicQueryGenerator.getJoinQuery(joinQueryBuider);
            LOGGER.info("Hospital Query Part 2 :: {}", hospitalQuery);
            //Hospital Query Part 2 Generated:: select  hospital_id, name  from hospital_tbl where hospital_id = ?
            List<Surgeon> surgeonDet = jdbcTemplate.query(hospitalQueryPart2, new Object[]{hospitalId}, resultSetExtractorSurgeon);
            hospitalDet.setSurgeon(surgeonDet);
        }

        return hospitalDet;
    }

    @GraphQLQuery
    public List<HospitalToSurgeonSimpleRel> countSurgeonByHospitalJQB(@GraphQLEnvironment ResolutionEnvironment env) {
        DynamicQueryGenerator dynamicQueryGenerator = DynamicQueryGenerator.getInstance(env);


        JoinQueryBuilder joinQueryBuider = new JoinQueryBuilder
                .Builder()
                .setRootKey(HospitalMeta.GRAPH_LEVEL)
                .addaggregateSelectCols(SqlAggregationType.COUNT, HospitalToSurgeonSimpleRelMeta.Surgeon.GRAPH_LEVEL, HospitalToSurgeonSimpleRelMeta.Surgeon.JoinColumns.SURGEON_ID)
                .joinWithGraph(HospitalToSurgeonSimpleRelMeta.Surgeon.GRAPH_LEVEL, SqlJoinType.INNER_JOIN)
                .addGroupByCondition(HospitalToSurgeonSimpleRelMeta.Surgeon.GRAPH_LEVEL, HospitalToSurgeonSimpleRelMeta.Surgeon.AllColumns.HOSPITAL_ID)
                .build();

        String hospitalQuery = dynamicQueryGenerator.getJoinQuery(joinQueryBuider);
        LOGGER.info("Hospital Query :: {}", hospitalQuery);
        //Hospital Query Generated:: select  rootObject.hospital_id as hospital_id, rootObject.name as name, surgeon.full_name as surgeon_full_name, surgeon.surgeon_id as surgeon_surgeon_id, surgeon_docSpeciality.id as surgeon_docSpeciality_id, surgeon_docSpeciality.experience as surgeon_docSpeciality_experience from hospital_tbl rootObject   inner join  surgeon_tbl surgeon on  rootObject.hospital_id = surgeon.hospital_id    inner join  doc_speciality_tbl surgeon_docSpeciality on  surgeon.surgeon_id = surgeon_docSpeciality.surgeon_id  where rootObject.hospital_id = ?

        return jdbcTemplate.query(hospitalQuery, resultSetExtractorHospToSurgn);
    }

}
