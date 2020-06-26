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
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.base.HospitalBase;

import javax.persistence.Table;

/**
 * Annotating with root entity as this object will be directly returned by a resolver
 * <p>
 * Purpose of this class is to allow GraphQL client to query the columns of this entity only
 */
@RootEntity
@Table(name = "hospital_tbl")
public class HospitalSimple extends HospitalBase {

}