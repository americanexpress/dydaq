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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj.constants;

/**
 * Constant file generated by graphql-ddq plugin.Format it before using
 * Should be used to fetch metadata for entity SurgeonSimple.
 * Inner class AllColumns contains all the columns of the entity and can be used get the column name
 * Inner class Keys contains all the primary key annotated with @Id
 * Inner class JoinColumns contains columns annotated with @DynamicJoinColumn
 */
public class SurgeonSimpleMeta {

    public static final String TABLE = "surgeon_tbl";
    public static final String GRAPH_LEVEL = "rootObject";
    private SurgeonSimpleMeta() {
    }

    public class AllColumns {
        public static final String SURGEON_ID = "surgeon_id";
        public static final String HOSPITAL_ID = "hospital_id";
        public static final String FULL_NAME = "full_name";
        public static final String DEPT_ID = "dept_id";
        public static final String CONTACT_NO = "contact_no";
        private AllColumns() {
        }
    }

    public class Keys {
        public static final String SURGEON_ID = "surgeon_id";

        private Keys() {
        }
    }

    public class JoinColumns {
        public static final String SURGEON_ID = "surgeon_id";
        public static final String HOSPITAL_ID = "hospital_id";
        public static final String DEPT_ID = "dept_id";
        public static final String CONTACT_NO = "contact_no";
        private JoinColumns() {
        }
    }
}
