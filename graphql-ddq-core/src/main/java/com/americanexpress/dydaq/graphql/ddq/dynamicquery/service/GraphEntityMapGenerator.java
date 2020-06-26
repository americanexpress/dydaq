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

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.ChildEntity;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.RootEntity;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants.ServiceConstants;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.exception.DynamicQueryException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a Map of graphLevel and associated entity based on the entiity associations created inside entity-location provided
 * in the application.yml of the implementer code
 */
@Component
public class GraphEntityMapGenerator {

    private static final Logger LOGGER = LogManager.getLogger(GraphEntityMapGenerator.class);
    Map<String, String> graphEntityMap = new HashMap<>();
    private String rootEntityName = "";

    /**
     * Creates Graph Level based on the reference names and then maps the graphl level the respective entity name which has the fields relevent to the graph Key
     *
     * @param graphName       is the referenced field names in the entity
     * @param parentGraphName is reference name of the input entity used by its parent class
     * @param entityClass     is the entity for which graph map is being created or passed in the input param
     * @param isRootEntitySet is true if rootEntity has been set while traversing a complex Entity.
     *                        Root Entity are top level entity of a complex entity or
     *                        entity returned by graphql resolver and annotated with @RootEntity
     * @returns a map of graphlevel and associated entity
     * <p>
     * Eg Output after all recursive calls for a Root Entity:
     * Map [{rootObject,E1}         //Root Entity is E1,graph level is set as "rootObject"
     * {refToE2,E2},           //reference name to E2 in E1 is refToE2,graph level is set as "refToE2"
     * {refToE2_refToE3,E3}    //reference name to E3 in E2 is refToE3,graph level is set as "refToE2_refToE3"
     * ...]
     * <p>
     * The reason behind setting such naming convention of these naming conventions are needed by SimpleFlatMapper
     * when for SQL query and setting the result set into the nested entity
     */
    public Map<String, String> createGraphEntityMap(String graphName, String parentGraphName, Class<?> entityClass, boolean isRootEntitySet) {

        if (!isRootEntitySet) {
            rootEntityName = entityClass.getSimpleName();
        }

        if (entityClass.isAnnotationPresent(ChildEntity.class) || entityClass.isAnnotationPresent(RootEntity.class)) {

            if (!entityClass.isAnnotationPresent(Table.class)) {
                throw new DynamicQueryException("@Table annotation with value not found for Dynamic Query Entity " + entityClass.getName());
            }

            String graphLevel = getGraphLevel(graphName, parentGraphName);
            graphName = graphLevel.equals(rootEntityName) ? ServiceConstants.ROOT_GRAPH_LEVEL : graphLevel;
            graphEntityMap.put(graphName, entityClass.getSimpleName());
            Field[] fields = entityClass.getDeclaredFields();// fields of root

            for (Field referenceVar : fields) {
                graphName = referenceVar.getName();

                Class<?> entityChildClass = getReferencedEntityObj(referenceVar);
                if (null != entityChildClass) {
                    if (entityChildClass.isAnnotationPresent(ChildEntity.class) || entityClass.isAnnotationPresent(RootEntity.class)) {
                        createGraphEntityMap(graphName, (graphLevel.equals(rootEntityName) ? "" : graphLevel), entityChildClass, true);// calling accountfact
                    } else {
                        LOGGER.warn("@DynamicQueryEntity annotation not found in class :: {}", entityChildClass.getName());
                    }
                }
            }
        }
        return graphEntityMap;

    }

    /**
     * Takes reference variable and returns the class object of referenced variable
     */
    private Class<?> getReferencedEntityObj(Field referenceVar) {
        String referenceEntityName;
        if (referenceVar.getType().equals(List.class)) {

            referenceEntityName = ((ParameterizedType) referenceVar.getGenericType())
                    .getActualTypeArguments()[0].getTypeName();

        } else {
            referenceEntityName = referenceVar.getType().getName();
        }

        Class<?> entityChildClass = null;
        try {
            entityChildClass = Class.forName(referenceEntityName);
        } catch (ClassNotFoundException e1) {
            LOGGER.error("Class Not Found {}", e1);
        }
        return entityChildClass;
    }

    private String getGraphLevel(String graphName, String parentGraphName) {
        String grahLevel;
        if (!parentGraphName.equals("")) {
            grahLevel = parentGraphName + "_" + graphName;
        } else {
            grahLevel = graphName;
        }
        return grahLevel;
    }
}
