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

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Table;
import java.util.LinkedList;
import java.util.List;

/**
 * defining it as root entity as this is also a return type from a resolver
 * if not we could have defined it as child entity
 * <p>
 * The graphql client will be able to query DocSpeciality entity object
 * when they navigate to this entity
 */
@RootEntity
@Table(name = "surgeon_tbl")
public class Surgeon extends SurgeonBase {

    /**
     * Adding association between Surgeon and DocSpeciality.This association will be used by
     * JoinQueryBuilder to join these two entities.
     */
    @JoinColumns(@JoinColumn(name = "surgeon_id", referencedColumnName = "surgeon_id"))
    private List<DocSpeciality> docSpeciality = new LinkedList<>();

    public List<DocSpeciality> getDocSpeciality() {
        return docSpeciality;
    }

    public void setDocSpeciality(List<DocSpeciality> docSpeciality) {
        this.docSpeciality = docSpeciality;
    }

}