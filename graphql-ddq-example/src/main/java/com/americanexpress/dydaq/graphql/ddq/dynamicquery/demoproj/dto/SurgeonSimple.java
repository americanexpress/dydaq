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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.RootEntity;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.base.SurgeonBase;

import javax.persistence.Table;

/**
 * defining it as root entity as this is also a return type from a resolver
 * if not we could have defined it as child entity
 * <p>
 * This class has no association with any other class.The graphQL client will
 * be able to query only the columns of this entity only when they navigate to this entity
 */
@RootEntity
@Table(name = "surgeon_tbl")
public class SurgeonSimple extends SurgeonBase {

}