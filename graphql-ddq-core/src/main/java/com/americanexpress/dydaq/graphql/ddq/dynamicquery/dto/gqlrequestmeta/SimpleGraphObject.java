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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.gqlrequestmeta;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.service.ResolutionEnvToSimpleObjectConvertor;

import java.util.Map;
import java.util.Set;

/**
 * Holds GraphQL request in Simpler form with there Entity class name.
 * <p>
 * /**
 * Converting SPQR provided graphql query object into simple objects as below :
 * Map<String, Set<String>> tableColMap => level/graph of of field requested as
 * key,value as fields selected from that level/graph
 *
 * @param r is graphql spqr injected object
 * @return {@link SimpleGraphObject} having below fields :
 * <p>
 * Map<String, Set<String>> graphTableMap => level/graph of field requested as key,
 * value as set of fields requested under that level
 * <p>
 * Map<String, String> graphTableMap => level/graph of field requested as
 * key,value as Entity/table name where the level/graph
 * <p>
 * Eg :
 * <p>
 * Suppose below is the Entity Class structure :
 *
 * <pre>
 *
 * @ChildEntity
 * @Table(name="department")
 * public class Department{
 * 		private String deptName;
 * 		private String deptHead;
 * 		...setters
 * 		...getters
 * }
 *
 * @ChildEntity
 * @Table(name="surgeon")
 * public class Surgeon{
 * 		private String name;
 * 		Department department;
 *
 * 		...setters
 * 		...getters
 * }
 *
 * @RootEntity
 * @Table(name="hospital")
 * public class Hospital {
 *
 * 		private String hospName;
 * 		private [Surgeon] surgeon;
 *
 * 		...setters
 * 		...getters
 *
 *    }
 *
 * @GraphQLApi
 * public class Resolver{
 *        @Autowired hospitalRepo;//Suppose a service class with method getDoctorDetails returning Hospital with there doctor and department details
 *
 *        @GraphQLQuery
 *        public Hospital getDoctorDetailsRes(int hospId){
 * 			return hospitalRepo.getDoctorDetails(hospId);
 *        }
 *
 * }
 * </pre>
 * <p>
 * <p>
 * Below will be the GraphQL Query structure
 * <pre>
 *
 *    {
 * 		getDoctorDetailsRes(hospId : 1){
 * 			hospName
 * 			surgeon{
 * 				name
 * 				department{
 * 					deptName
 * 					deptHead
 *                }
 *            }
 *        }
 *    }
 *
 * </pre>
 * <p>
 * SimpleGraphObject will hold following tableColMap values as below when {@link ResolutionEnvToSimpleObjectConvertor#gqlRequestToSimpleGraphObject} is called.
 *
 * <pre>
 *
 *  rootEntity = Hospital
 *
 *  rootKey = rootObject
 *
 *  gqlGraphReqFieldMap
 *
 * [
 * 		rootObject=[hospName],
 * 		surgeon=[name],
 * 		surgeon_department=[deptName,deptHead]
 * ]
 *
 * In the above,key is the graph level/key name and value is list of fields selected
 *
 *  gqlGraphEntityMap
 *
 * [
 * 		rootObject=Hospital,
 * 		surgeon=Surgeon,
 * 		surgeon_department=Department
 * ]
 *
 * In the above,key is the graph level/key name and value is name of Entity
 *
 * </pre>
 * rootObject is the variable name for the root entity/final return type of resolver.
 * Also rootObject will be alias for Hospital,surgeon will be alias for Surgeon,surgeon_department will be alias for table fetching department of surgeon.
 */
public class SimpleGraphObject {

    Map<String, String> gqlGraphEntityMap;
    Map<String, Set<String>> gqlGraphReqFieldMap;
    String rootKey;
    String rootEntity;

    /**
     * RootEntity is the top level entity of the graphQL request,in other words the final return entity of the resolver.
     */
    public String getRootEntity() {
        return rootEntity;
    }

    public void setRootEntity(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    /**
     * RootKey is the top level entity of the graphQL request,in other words the final return object graph level of the resolver.
     * Its value is always set as "rootObject"
     */
    public String getRootKey() {
        return rootKey;
    }

    public void setRootKey(String rootKey) {
        this.rootKey = rootKey;
    }

    /**
     * Returns a Map of Graph level and the corresponding entity.For more details and example see the class level comment of this class
     */
    public Map<String, String> getGqlGraphEntityMap() {
        return gqlGraphEntityMap;
    }

    public void setGqlGraphEntityMap(Map<String, String> gqlGraphEntityMap) {
        this.gqlGraphEntityMap = gqlGraphEntityMap;
    }

    /**
     * Returns a Map of Graph level and correponding fields selected from the graph level.
     * For more details and example see the class level comment of this class
     */
    public Map<String, Set<String>> getGqlGraphReqFieldMap() {
        return gqlGraphReqFieldMap;
    }

    public void setGqlGraphReqFieldMap(Map<String, Set<String>> gqlGraphReqFieldMap) {
        this.gqlGraphReqFieldMap = gqlGraphReqFieldMap;
    }

}
