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

package com.americanexpress.dydaq.graphql.ddq.dynamicquery.demoproj;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.EnableDynamicQuery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run this class class to start the application.More details can be found in README.md
 */
@SpringBootApplication
@EnableDynamicQuery
public class SampleProjectHospitalManagement {

    public static void main(String[] args) {
        SpringApplication.run(SampleProjectHospitalManagement.class, args);
    }

}
