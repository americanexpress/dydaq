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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.service;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.DynamicQueryGenerator;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.ServiceConstants;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.dto.gqlrequestmeta.SimpleGraphObject;
import graphql.schema.*;
import io.leangen.graphql.execution.ResolutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Converts ResolutionEnvironment object injected by SPQR library into object of SimpleGraphObject.
 * Injects the SimpleGraphObject obtained into DynamicQueryGenerator and finally returns instance of DynamicQueryGenerator.
 */
public class ResolutionEnvToSimpleObjectConvertor {

    private static final Logger LOGGER = LogManager.getLogger(ResolutionEnvToSimpleObjectConvertor.class);

    private ResolutionEnvToSimpleObjectConvertor() {
    }

    /**
     * Takes Resolution Environment injected from GraphQL SPQR,creates SimpleGraphObject and injects it to a DynamicQueryGenerator instance
     * using it and returns the DynamicQueryGenerator instance.
     * <p>
     * It creates a SimplegraphObject by parsing the complex GraphQL request object in ResolutionEnvironment into below Simple Maps :
     * 1.  gqlGraphEntityMap : A Map with graph level and Corresponding Entity
     * 2.  gqlGraphReqFieldMap : A Map with Graph Level and set of fields selected from that Graph Level
     * 3.  RootEntity Name : Name of the top level entity or the final return type of the resolver.
     * Please check {@link SimpleGraphObject} class documentation for more details
     * <p>
     * Override this method to create SimpleGraphObject if you are upgrading SPQR version.
     */
    public static DynamicQueryGenerator gqlRequestToSimpleGraphObject(ResolutionEnvironment resEnv) {

        SimpleGraphObject simpleGraphObject = new SimpleGraphObject();

        // Fetching graphql query in object form
        Map<String, GraphQLFieldDefinition> graphFieldDetails = resEnv.dataFetchingEnvironment.getSelectionSet()
                .getDefinitions();
        Set<String> graphKeys = graphFieldDetails.keySet();
        Map<String, String> gqlGraphEntityMap = new HashMap<>();
        Map<String, Set<String>> gqlGraphReqFieldMap = new LinkedHashMap<>();

        // separately fetching rootObject is not available in the selection set
        String rootObjectEntityName;

        //For scenarios where response type of the resolver is Single object
        if (null != resEnv.dataFetchingEnvironment.getFieldType().getName()) {
            rootObjectEntityName = resEnv.dataFetchingEnvironment.getFieldType().getName();
        } else {
            //for scenarios where resolver is returning object of list type
            rootObjectEntityName = resEnv.dataFetchingEnvironment.getFieldType().getChildren().get(0).getName();
        }

        gqlGraphEntityMap.put(ServiceConstants.ROOT_GRAPH_LEVEL, rootObjectEntityName);

        graphKeys.forEach(key -> {
            Class<? extends GraphQLOutputType> objType = graphFieldDetails.get(key).getType().getClass();
            // Root Entity and their fields are being added ,separate implementation if the
            // root object itself is list
            if (!key.contains("/") && objType.equals(GraphQLScalarType.class)) {
                if (gqlGraphReqFieldMap.get(ServiceConstants.ROOT_GRAPH_LEVEL) != null) {
                    gqlGraphReqFieldMap.get(ServiceConstants.ROOT_GRAPH_LEVEL).add(key);
                } else {
                    Set<String> columnList = new HashSet<>();
                    columnList.add(key);
                    gqlGraphReqFieldMap.put(ServiceConstants.ROOT_GRAPH_LEVEL, columnList);
                }
            } else if (objType.equals(GraphQLObjectType.class)) {// Complex object level/graph added other than root
                String fieldPathByClass = graphFieldDetails.get(key).getType().getName();

                gqlGraphEntityMap.put(key.replace("/", "_"), fieldPathByClass);

            } else if (objType.equals(GraphQLList.class)) {// For List object level graph added
                String fieldPathByClass = graphFieldDetails.get(key).getType().toString().replace("[", "").replace("]", "");
                gqlGraphEntityMap.put(key.replace("/", "_"), fieldPathByClass);

            } else {// for fields inside complex object ie GraphQLObjectType or GraphQLList

                String fieldLocationGraph = key.substring(0, key.lastIndexOf('/')).replace("/", "_");
                addParentGraphObjectIfNotExists(fieldLocationGraph, gqlGraphReqFieldMap);
                if (gqlGraphReqFieldMap.get(fieldLocationGraph) != null) {
                    gqlGraphReqFieldMap.get(fieldLocationGraph).add(graphFieldDetails.get(key).getName());
                } else {
                    Set<String> columnList = new HashSet<>();
                    columnList.add(graphFieldDetails.get(key).getName());
                    gqlGraphReqFieldMap.put(fieldLocationGraph, columnList);
                }
            }

        });
        simpleGraphObject.setRootKey(ServiceConstants.ROOT_GRAPH_LEVEL);
        simpleGraphObject.setRootEntity(rootObjectEntityName);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Root Key :: " + ServiceConstants.ROOT_GRAPH_LEVEL);
            LOGGER.debug("Root Entity Name :: " + rootObjectEntityName);
            LOGGER.debug("GraphTable :: " + gqlGraphEntityMap);
            LOGGER.debug("gqlGraphReqFieldMap :: " + gqlGraphReqFieldMap);
        }

        simpleGraphObject.setGqlGraphEntityMap(gqlGraphEntityMap);
        simpleGraphObject.setGqlGraphReqFieldMap(gqlGraphReqFieldMap);

        return new DynamicQueryGenerator(simpleGraphObject);
    }

    private static Map<String, Set<String>> addParentGraphObjectIfNotExists(String fieldLocationGraph, Map<String, Set<String>> gqlGraphReqFieldMap) {

        if (!fieldLocationGraph.contains("_")) {
            return gqlGraphReqFieldMap;
        }

        String parentObjectGraph = fieldLocationGraph.substring(0, fieldLocationGraph.lastIndexOf('_'));

        if (gqlGraphReqFieldMap.containsKey(parentObjectGraph)) {
            return gqlGraphReqFieldMap;
        } else {
            gqlGraphReqFieldMap.put(parentObjectGraph, new HashSet<>());
        }

        return addParentGraphObjectIfNotExists(parentObjectGraph, gqlGraphReqFieldMap);

    }

}
