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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.dto.base;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.DynamicJoinColumn;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A base class for showing aggregation.Here city with bumber of hospital selected using query builder's aggregation features
 * Also as per implementation of DyDDQ,Columns should be kept in MappedSuperClass inside base folder inside dto
 */
@MappedSuperclass
public class HospitalCountBase {

    /**
     * Renaming the field as hospitalCount instead of hospitalId as aggregation will be done on this field and this field will ve visible to client as hospitalCount
     */
    @DynamicJoinColumn
    @Column(name = "hospital_id", nullable = false)
    private Integer hospitalCount;
    @Column(name = "city", nullable = true)
    private String city;

    public Integer getHospitalCount() {
        return hospitalCount;
    }

    public void setHospitalCount(Integer hospitalId) {
        this.hospitalCount = hospitalId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}