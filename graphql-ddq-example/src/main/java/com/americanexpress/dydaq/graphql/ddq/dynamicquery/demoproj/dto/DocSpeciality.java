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

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.ChildEntity;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.base.DocSpecialityBase;

import javax.persistence.Table;

/**
 * Annotating it with @ChildEntity as it is child of {@link Surgeon} class
 */
@ChildEntity
@Table(name = "doc_speciality_tbl")
public class DocSpeciality extends DocSpecialityBase {

    /**
     * No association among DocSpeciality and Speciality is defined as we are not joining DocSpeciality
     * with Speciality using JoinQueryBuilder.In our examples we will be using SimpleQueryBuilder or native
     * Query builder to fetch value of Speciality
     */
    private Speciality speciality;

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

}