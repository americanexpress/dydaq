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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.dto;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.RootEntity;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.sampleproj.dto.base.HospitalBase;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Table;
import java.util.LinkedList;
import java.util.List;

@RootEntity
@Table(name = "hospital_tbl")
public class Hospital extends HospitalBase {

    @JoinColumns(@JoinColumn(name = "hospital_id", referencedColumnName = "hospital_id"))
    private List<Surgeon> surgeon = new LinkedList<Surgeon>();

    public List<Surgeon> getSurgeon() {
        return surgeon;
    }

    public void setSurgeon(List<Surgeon> surgeon) {
        this.surgeon = surgeon;
    }

}