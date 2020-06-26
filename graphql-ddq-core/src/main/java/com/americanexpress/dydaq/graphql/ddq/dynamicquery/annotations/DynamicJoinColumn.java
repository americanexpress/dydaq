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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.service.EntityMetaDataProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DynamicJoinColumn needs to be added in the JPA entity foreign key fields when dynamic query
 * feature needs to be implemented.
 * Based on this annotation the column will be eagerly fetched from database at run time
 * so that other queries which are dependent on the annotated columns value can use the value.
 * DynamicJoinColumns are not eagerly fetched when using aggregation feature of the QueryBuilder as
 * selecting non relevant columns while doing aggregation can lead to wrong query result
 * <p>
 * It enables the the {@link EntityMetaDataProvider }
 * to scan these entities and collects the fields which must be fetched
 * from so that join does not fail on these columns
 * <p>
 * For example
 *
 * <pre class="code">
 *
 *    @MappedSuperclass
 *    public class Test {
 *
 *    @Id
 *    @DynamicJoinColumn
 *    @Column(name = "id")
 * 	private String Id;
 * 	...
 *
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DynamicJoinColumn {
}
