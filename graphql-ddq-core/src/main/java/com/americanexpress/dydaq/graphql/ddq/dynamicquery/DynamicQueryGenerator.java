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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.gqlrequestmeta.SimpleGraphObject;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.JoinQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.NativeQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.queryconditionfetcher.SimpleQueryBuilder;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.service.DynamicQueryGenerationHelper;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.service.ResolutionEnvToSimpleObjectConvertor;
import io.leangen.graphql.execution.ResolutionEnvironment;
import org.springframework.context.annotation.ComponentScan;

import java.util.Set;

/**
 * This class needs to be initialized with instance of SimpleGraphObject as Constructor parameter.
 * This class uses this instance of SimpleGraphObject and EntityMetadataProvider to construct sql query.
 * SimpleGraphObject holds metadata about the GraphQL request and EntityMetadataProvider holds metadata about entities ie..table names and respective entity,field name and respective column names
 */
@ComponentScan({"com.americanexpress.dydaq.graphql.ddq.dynamicquery"})
public class DynamicQueryGenerator {

    private DynamicQueryGenerationHelper queryGenerationHelper = null;
    private SimpleGraphObject simpleGraphObject = null;

    public DynamicQueryGenerator(SimpleGraphObject simpleGraphObject) {
        this.simpleGraphObject = simpleGraphObject;
        queryGenerationHelper = new DynamicQueryGenerationHelper(simpleGraphObject);
    }

    /**
     * Instantiates {@link DynamicQueryGenerator} with {@link ResolutionEnvironment} object
     *
     * @param resEnv injected by SPQR into GraphQL query resolver using @GraphQLEnvironment ResolutionEnvironment env
     */
    public static DynamicQueryGenerator getInstance(ResolutionEnvironment resEnv) {
        return ResolutionEnvToSimpleObjectConvertor.gqlRequestToSimpleGraphObject(resEnv);
    }

    /**
     * Returns graph levels/object references present in the graphql request.
     * <p>
     * Below will be the Sample GraphQL Query structure
     * <pre>
     *    {
     * 	  getDoctorDetailsRes(hospId : 1){
     * 	    hospName 	# fields directly at root are part of <strong>rootObject</strong> graph level,here fields hospName(scalar type) and surgeon(object type)
     * 		  surgeon{	# fields under of surgeon are part of <strong>surgeon</strong> graph level,here fields name(scalar type) and department(object type)
     * 		    name
     * 			department{ # fields under department are part of <strong>surgeon_department</strong> graph level,here scalar fields deptName and deptHead
     * 			  deptName
     * 			  deptHead
     *            }
     *          }
     *        }
     *    }
     * </pre>
     * <p>
     * GraphLevel set will have following graph key [rootObject,surgeon,surgeon_department]
     *
     * @return set of graph levels/keys
     */
    public Set<String> getGraphLevels() {
        return simpleGraphObject.getGqlGraphReqFieldMap().keySet();
    }


    /**
     * Generate a SQL Native query using native Query builder object
     *
     * @param queryBuilder instance of {@link NativeQueryBuilder}
     * @return SQL Query
     */
    public String getNativeQuery(NativeQueryBuilder queryBuilder) {
        return queryGenerationHelper.nativeQueryBuilder(queryBuilder);
    }


    /**
     * Creates a SQL query whose parameters(?) can be set using Spring jdbc template/Java jdbc*
     *
     * @param queryBuilder instance of {@link SimpleQueryBuilder}
     * @return SQL query for the table having alias as graphKey name
     */
    public String getSelectQuery(SimpleQueryBuilder queryBuilder) {
        return queryGenerationHelper.getSelectQuery(queryBuilder);
    }

    /**
     * Creates a joined SQL query whose parameters(?) can be set using Spring jdbc template/Java jdbc
     *
     * @param queryBuilder instance of {@link JoinQueryBuilder}
     * @return Complete SQL query with for the tables having alias as graphKeys
     */
    public String getJoinQuery(JoinQueryBuilder queryBuilder) {
        return queryGenerationHelper.getJoinQuery(queryBuilder);
    }


}
