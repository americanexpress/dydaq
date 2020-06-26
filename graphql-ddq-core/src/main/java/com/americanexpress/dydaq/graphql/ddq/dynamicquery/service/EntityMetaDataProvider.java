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
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.DynamicJoinColumn;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.RootEntity;
import io.leangen.graphql.annotations.GraphQLQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class contains common methods which helps to implement dynamic query generation.To use this class location of
 * entity package must be set in application.yml file for the field ${sidh.entity-location}
 */

@Configuration
public class EntityMetaDataProvider {

    /**
     * Map holds Root Entity Name as key value as a Map which holds graph Key/level as key and Entity Name as value
     */
    protected static final Map<String, Map<String, String>> GRAPH_ENTITY_MAP = new HashMap<>();
    /**
     * A Map which holds Entity name,the GQL field name given using @GraphQLQuery(name = ?),
     * and the respective field name generated from its getter
     */
    protected static final Map<String, Map<String, String>> ENTITY_GQL_FIELD_TO_ENTITY_FIELD_MAP = new HashMap<>();
    /**
     * A Map which holds Entity class name as key,Value is another map with key as field
     * name and value as column name of the entity
     */
    protected static final Map<String, Map<String, String>> ENTITY_FIELD_COL_MAP = new HashMap<>();
    /**
     * A Map which holds Entity class name as key,Value is another map with key as db column name
     * name and value as field name of the entity
     */
    protected static final Map<String, Map<String, String>> ENTITY_COL_FIELD_MAP = new HashMap<>();
    /**
     * A Map which holds Entity class name as Key and primary key/foreign key field variable from entity name as list of values.
     */
    protected static final Map<String, List<String>> ENTITY_PK_FK_FIELD_MAP = new HashMap<>();
    /**
     * A Map which holds Entity class name as Key and primary key field variable from entity name as list of values.
     */
    protected static final Map<String, List<String>> ENTITY_PK_MAP = new HashMap<>();
    /**
     * A Map which holds ENtity Name as Key and its table name as value
     */
    protected static final Map<String, String> ENTITY_TABLE_MAP = new HashMap<>();
    /**
     * A Map which holds Entity class Name as Key and its value as a Map.
     * This value map in turn holds variable/graph name as key,Value is List of join columns names.These join column
     * names are the columns of the parent entity and child entity based on which join will happen.First value in the pair/tuple
     * is from parent entity and second value in the pair is column of the child table.
     */
    protected static final Map<String, Map<String, List<Pair<String, String>>>> TABLE_GRAPH_JOIN_COL_MAP = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(EntityMetaDataProvider.class);
    private static String entityLocation;
    @Autowired
    GraphEntityMapGenerator graphTblMapGenerator;

    @Value("${sidh.entity-location}")
    public void setEntityLocation(String entityLocation) {
        EntityMetaDataProvider.entityLocation = entityLocation;
    }

    /**
     * This method reads Entity classes by annotation {@link ChildEntity} and sets below class variables :
     * <p>
     * 1.{@link EntityMetaDataProvider#ENTITY_FIELD_COL_MAP}
     * 2.{@link EntityMetaDataProvider#ENTITY_PK_FK_FIELD_MAP}
     * 3.{@link EntityMetaDataProvider#ENTITY_TABLE_MAP}
     * 4.{@link EntityMetaDataProvider#TABLE_GRAPH_JOIN_COL_MAP}
     * 5.{@link EntityMetaDataProvider#ENTITY_GQL_FIELD_TO_ENTITY_FIELD_MAP}
     * 6.{@link EntityMetaDataProvider#ENTITY_COL_FIELD_MAP}
     * 7.{@linkEntityMetaDataProvider#graphEntityMap}
     *
     * @throws Exception
     */
    @PostConstruct
    public void getEntityMetaData() {

        Reflections reflections = new Reflections(entityLocation);
        Set<Class<? extends Object>> allClasses = reflections.getTypesAnnotatedWith(RootEntity.class);

        if (allClasses != null) allClasses.addAll(reflections.getTypesAnnotatedWith(ChildEntity.class));

        for (Class<?> entityClass : allClasses) {
            try {

                addEntityGqlFieldToEntityFieldMap(entityClass);

                addTableGraphJoinColMap(entityClass);

                Field[] fields = entityClass
                        .getSuperclass()
                        .getDeclaredFields();

                Map<String, String> fieldColumnMap = new HashMap<>();
                Map<String, String> columnFieldMap = new HashMap<>();
                List<String> pkFkFieldList = new ArrayList<>();
                List<String> pkFieldList = new ArrayList<>();
                for (Field field : fields) {
                    if (!field.isSynthetic() && !Modifier.isTransient(field.getModifiers())) {
                        columnFieldMap.put(field.getAnnotation(Column.class).name(), field.getName());
                        fieldColumnMap.put(field.getName(), field.getAnnotation(Column.class).name());
                        if (field.isAnnotationPresent(Id.class)) {
                            pkFieldList.add(field.getName());
                            pkFkFieldList.add(field.getName());
                        }
                        if (field.isAnnotationPresent(DynamicJoinColumn.class) && !pkFkFieldList.contains(field.getName())) {
                            pkFkFieldList.add(field.getName());
                        }
                    }
                }
                ENTITY_COL_FIELD_MAP.put(entityClass.getSimpleName(), columnFieldMap);
                ENTITY_FIELD_COL_MAP.put(entityClass.getSimpleName(), fieldColumnMap);
                ENTITY_PK_FK_FIELD_MAP.put(entityClass.getSimpleName(), pkFkFieldList);
                ENTITY_PK_MAP.put(entityClass.getSimpleName(), pkFieldList);
                ENTITY_TABLE_MAP.put(entityClass.getSimpleName(), entityClass.getAnnotation(Table.class).name());

            } catch (SecurityException e) {
                LOGGER.error("Exception while Creating metadata for GraphQL JPA : {}", e);
            }
        }

        Set<Class<? extends Object>> rootClasses = reflections.getTypesAnnotatedWith(RootEntity.class);

        for (Class<?> entityClass : rootClasses) {
            //Setting Root Entity as Key and Graph Entity Map(for nested graphs and entities inside it) as its value
            GRAPH_ENTITY_MAP.put(entityClass.getSimpleName(), graphTblMapGenerator.createGraphEntityMap(entityClass.getSimpleName(), "", entityClass, false));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Map<EntityName,Map<fieldName,CorrespondingColumnName>> entityFieldColumnMap : {}", ENTITY_FIELD_COL_MAP);
            LOGGER.debug("Map<EntityName,Map<ColumnName,CorrespondingfieldName>> entityColumnFieldMap : {}", ENTITY_COL_FIELD_MAP);
            LOGGER.debug("EntityPkFkFieldMap entityPkFkFieldMap : {}", ENTITY_PK_FK_FIELD_MAP);
            LOGGER.debug("EntityPkFieldMap entityPkMap : {}", ENTITY_PK_MAP);
            LOGGER.debug("Map<EntityName, EntityTableName> : {}", ENTITY_TABLE_MAP);
            LOGGER.debug("Map<EntityName,Map<GraphKey/Field Variables,List<Pair<joinColCurrentEntity,joinColumnReferencedEntity>>>> tableGraphJoinColMap : {}", TABLE_GRAPH_JOIN_COL_MAP);
            LOGGER.debug("Map<GraphKey,Table> graphEntityMap : {}", GRAPH_ENTITY_MAP);
        }


    }

    /**
     * Creates a Map of GraphQL field to Entity Field Naames from the Entities MappedSuperClass.Map is added only when GraphQL
     * schema name is different from the getter name(by stripping get/is from getter)
     * Eg :
     * A getter getGQLFldName annotated with @GraphQLQuery("SampleName") may be present in GraphQL schema as SampleName
     * <p>
     * So the Map will hold Key value as {SampleName,GQLFldName}
     *
     * @param entityClass for which Map needs to be generated
     */
    private void addEntityGqlFieldToEntityFieldMap(Class<?> entityClass) {
        Method[] methods = entityClass.getSuperclass().getDeclaredMethods();
        Map<String, String> gqlFieldToEntityFieldMap = new HashMap<>();
        for (Method method : methods) {
            if (!method.isSynthetic() && !Modifier.isTransient(method.getModifiers())) {
                String fieldName;
                if (method.isAnnotationPresent(GraphQLQuery.class) && !method.getAnnotation(GraphQLQuery.class).name().equals("")) {
                    //if name is provided inside @GraphQLQuery annotation
                    //if the method starts with get /is,strip it and generate field name else keep getter name as field name
                    if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                        fieldName = Introspector.decapitalize(method.getName().substring(method.getName().startsWith("is") ? 2 : 3));
                    } else {
                        fieldName = method.getName();
                    }
                    //store GQL field name and respective entity field name
                    gqlFieldToEntityFieldMap.put(method.getAnnotation(GraphQLQuery.class).name(), fieldName);
                }
            }
        }
        ENTITY_GQL_FIELD_TO_ENTITY_FIELD_MAP.put(entityClass.getSimpleName(), gqlFieldToEntityFieldMap);
    }

    /**
     * Creates  Map<EntityName,List<Pair<JoinColumnName,ReferenceJoinColumnName>> from EntityClass
     * annotated with @DynamicQueryEntity.
     *
     * @param entityClass
     */
    private void addTableGraphJoinColMap(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        Map<String, List<Pair<String, String>>> graphKeyJoinColsMap = new HashMap<>();
        for (Field field : fields) {
            if (!field.isSynthetic() && !Modifier.isTransient(field.getModifiers())) {
                List<Pair<String, String>> joinColList = new ArrayList<>();
                if (field.isAnnotationPresent(JoinColumns.class)) {
                    for (JoinColumn joinCol : field.getAnnotation(JoinColumns.class).value()) {
                        Pair<String, String> joinCols = Pair.of(joinCol.name(), joinCol.referencedColumnName());
                        joinColList.add(joinCols);
                    }
                } else if (field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinCol = field.getAnnotation(JoinColumn.class);
                    Pair<String, String> joinCols = Pair.of(joinCol.name(), joinCol.referencedColumnName());
                    joinColList.add(joinCols);
                }
                if (!joinColList.isEmpty()) {
                    graphKeyJoinColsMap.put(field.getName(), joinColList);
                }
            }
        }
        if (graphKeyJoinColsMap.size() > 0) {
            TABLE_GRAPH_JOIN_COL_MAP.put(entityClass.getSimpleName(), graphKeyJoinColsMap);
        }

    }

}
