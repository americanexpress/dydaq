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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.constants;

public class ErrorConstants {

    public static final String EMPTY_QUERY = "Query Cannot be empty for native query building";
    public static final String ADD_GRAPH_KEY = "Add graph level %s in join conditions of JoinQueryBuilder to complete the associations";
    public static final String FIELD_NOT_FOUND = "No field found with name %s found for getter get%s() in entity %s.The field name shown in GraphQL is derived from the getters in the entity class,so consider renaming the getter method";
    public static final String GRAPH_KEY_NOT_CHILD_OF_ROOT = "Root entity has been set as %s in JoinQueryBuilder.%s is not a child of %s";
    public static final String ASSOCIATION_NOT_DEFINED = "Associations not defined between entity %s and %s with graph level/variable name %s.Add associations using @JoinColumns as per JPA standards.";
    public static final String ROOT_KEY_NULL_NJQB = "Root Key Cannot be null for building join query using Native Query Builder.";
    public static final String QUERY_TEMPLATE_NULL_NJQB = "Query template cannot be null for building query using Native Query Builder";
    public static final String GRAPH_KEY_NULL_NSQB = "Graph Key Cannot be null for Simple Query/Single entity using Native Query Builder.";
    public static final String ENTITY_NOT_READ = "Entity %s is not detected by DynamicQueryGenerator!Consider below points for debugging:"
            + "\n1. Make Sure you have added @RootEntity/@ChildEntity for this entity."
            + "\n2. Entity Base classes are available at location mentioned in application-{env}.yml file attribute ${sidh.entity-location}."
            + "\n3. SpringBoot main class is annotated with @EnableDynamicQuery.";
    public static final String AGG_COLUMN_NOT_FOUND = "Column %s added for aggregation not found.Please check entity %s if the column is missing";

    private ErrorConstants() {
    }

}
