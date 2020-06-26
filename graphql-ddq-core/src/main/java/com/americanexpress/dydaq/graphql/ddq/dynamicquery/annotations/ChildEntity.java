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
 * @ChildEntity annotation needs to be added on the child entity or the entities to which other entity is referring which will be used to return
 * response to the graphql query.
 * It enables the the {@link EntityMetaDataProvider}
 * to scan these entities and store the metadata of the entity into there objects which can be used further at dynamic query
 * generation feature
 * <p>
 * In addition to this Table name needs to be passed using @Table annotation of ddq
 * <p>
 * For Example
 *
 * <pre class="code">
 *
 *    @Table(name="test_fact")
 *    @DynamicQueryEntity
 *    public class TestFactResponse extends TestFactBase{
 * 	...
 * </pre>
 * <p>
 * For the entity to get scanned,location of entity package must be provided in
 * the field ${sidh.entity-location} in application.yml
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChildEntity {
}
