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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.dto.base;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.DynamicJoinColumn;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@IdClass(value = DepartmentBase.PrimaryKeys.class)
public class DepartmentBase {

    @Id
    @DynamicJoinColumn
    @Column(name = "dept_id", nullable = false)
    private Integer deptId;
    @Id
    @DynamicJoinColumn
    @Column(name = "hospital_id", nullable = false)
    private Integer hospitalId;
    @Column(name = "dept_name", nullable = false)
    private String deptName;
    @DynamicJoinColumn
    @Column(name = "dept_head_id", nullable = true)
    private Integer deptHeadId;

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Integer getDeptHeadId() {
        return deptHeadId;
    }

    public void setDeptHeadId(Integer deptHeadId) {
        this.deptHeadId = deptHeadId;
    }

    @Data
    public static class PrimaryKeys implements Serializable {
        private Integer deptId;
        private Integer hospitalId;
    }
}