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
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Marking this class as MappedSuperClass and can be extended by
 * multiple concrete entity implementations with different logics
 * Also as per implementation of DyDDQ,Columns should be kept in MappedSuperClass inside base folder inside dto
 */
@MappedSuperclass
public class SurgeonBase {

    @Id
    @DynamicJoinColumn
    @Column(name = "surgeon_id", nullable = false)
    private Integer surgeonId;
    @Column(name = "hospital_id", nullable = true)
    @DynamicJoinColumn
    private Integer hospitalId;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(name = "dept_id", nullable = true)
    @DynamicJoinColumn
    private Integer deptId;
    @DynamicJoinColumn
    @Column(name = "contact_no", nullable = true)
    private String contactNo;

    public Integer getSurgeonId() {
        return surgeonId;
    }

    public void setSurgeonId(Integer surgeonId) {
        this.surgeonId = surgeonId;
    }

    public Integer getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Integer hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

}