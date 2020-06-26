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

package com.americanexpress.dydaq.graphql.ddq;

import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.ChildEntity;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.DynamicJoinColumn;
import com.americanexpress.dydaq.graphql.ddq.dynamicquery.annotations.RootEntity;
import com.google.common.base.CaseFormat;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @since 1.0
 */

@Mojo(name = "graphql-ddq-dquery", defaultPhase = LifecyclePhase.COMPILE)
public class GraphQLDDQPlugin extends AbstractMojo {


    private String rootEntityName = "";

    //give class path where root/main response entity exists
    @Parameter(property = "sourcePackage", required = true, readonly = true)
    private String sourcePackage;
    @Parameter(property = "sourceClassList", readonly = true)
    private String sourceClassList;
    //give the location where u want to create class
    @Parameter(property = "destinationPackage", required = true, readonly = true)
    private String destinationPackage;

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;


    protected static Reflections getReflection(String basePackages, URLClassLoader urlClassLoader) {
        final ConfigurationBuilder confBuilder = new ConfigurationBuilder();
        final FilterBuilder filterBuilder = new FilterBuilder();
        confBuilder.addUrls(ClasspathHelper.forPackage(basePackages, urlClassLoader));
        confBuilder.addUrls(ClasspathHelper.forPackage(basePackages + ".base", urlClassLoader));
        filterBuilder.include(FilterBuilder.prefix(basePackages));

        confBuilder.filterInputsBy(filterBuilder)
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
        confBuilder.addClassLoader(urlClassLoader);

        return new Reflections(confBuilder);
    }

    /**
     * Reads source location of the calling project and generates metdata class
     * in the destilation location
     */
    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("Entity Metadata Generation Starts ...");
        getLog().info("Reading entities from package :: " + sourcePackage);

        Reflections reflections;
        URLClassLoader childClassLoader;
        try {
            String appJar = project.getBuild().getDirectory() + File.separator + project.getBuild().getFinalName() + ".jar";
            getLog().info("Searching files in jar at url :: " + new File(appJar).toURI().toURL());
            childClassLoader = new URLClassLoader(new URL[]{new File(appJar).toURI().toURL()}, this.getClass().getClassLoader());

            reflections = getReflection(sourcePackage, childClassLoader);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to load project classes:", e);
        }

        reflections.getConfiguration().getUrls().forEach(url ->
                getLog().info(url.toString())
        );

        Set<Class<?>> allClasses = getClassesForMetaGeneration(reflections, childClassLoader);

        if (!allClasses.isEmpty()) {
            getLog().info("Processing Entity Count:: " + allClasses.size());
        } else {
            getLog().warn("No Root Entity found!");
        }
        for (Class<?> entityClass : allClasses) {

            rootEntityName = entityClass.getSimpleName() + "Meta";

            if (entityClass.isAnnotationPresent(ChildEntity.class) || entityClass.isAnnotationPresent(RootEntity.class)) {

                String entityMetaLoc = project.getBuild().getSourceDirectory() + File.separator + destinationPackage.replace(".", File.separator) + File.separator + rootEntityName + ".java";

                getLog().info("Generating entity meta at:" + entityMetaLoc);
                try (FileWriter aWriter = new FileWriter(entityMetaLoc, false)) {
                    aWriter.write("package " + destinationPackage + ";" + "\n");
                    createEntityUtilClass(childClassLoader, rootEntityName, "", entityClass, aWriter);
                    aWriter.write("}\n");
                    aWriter.flush();
                } catch (Exception e) {
                    throw new MojoExecutionException("Failed to run DGF Maven query", e);
                }

                getLog().info("Entity Meta Location Completed::  " + entityMetaLoc);

            }

        }
    }

    /**
     * Returns classes for which metadata needs to be generated.These classes are either root entity annotated classes
     * or if specially sourceClass list is provided,then the specified class metadata will be generated
     */
    private Set<Class<?>> getClassesForMetaGeneration(Reflections reflections, URLClassLoader childClassLoader) {
        Set<Class<? extends Object>> allClasses = new HashSet<>();

        if (null == sourceClassList || sourceClassList.equals("")) {
            allClasses = reflections.getTypesAnnotatedWith(RootEntity.class);
        } else {
            try {
                if (sourceClassList.contains(",")) {
                    for (String inputClassName : sourceClassList.split(",")) {
                        Class<?> classObj = getClassObj(sourcePackage, inputClassName, childClassLoader);
                        if (null != classObj) {
                            allClasses.add(classObj);
                        }
                    }
                } else {
                    Class<?> classObj = getClassObj(sourcePackage, sourceClassList, childClassLoader);
                    if (null != classObj) {
                        allClasses.add(classObj);
                    }
                }
            } catch (ClassNotFoundException e) {
                getLog().error("Class Not Found at location " + sourcePackage, e);
            }
        }
        return allClasses;
    }

    /**
     * Returns class object based on class name passed and source package passed
     */
    private Class<? extends Object> getClassObj(String sourcePackage, String inputClassName, URLClassLoader childClassLoader) throws ClassNotFoundException {
        String className = (inputClassName.contains(".") ? inputClassName.split("\\.")[0] : inputClassName);
        return Class.forName(sourcePackage + "." + className, true, childClassLoader);
    }

    /**
     * Reads class via reflection and writes graphlevel,table name,DynamicJOinColumn Names in the metaclass
     */
    public void createEntityUtilClass(URLClassLoader classLoader, String graphName, String parentGraphName, Class<?> entityClass, FileWriter aWriter) throws Exception {

        if (entityClass.isAnnotationPresent(ChildEntity.class) || entityClass.isAnnotationPresent(RootEntity.class)) {

            if (!entityClass.isAnnotationPresent(Table.class)) {
                throw new Exception("@Table annotation with value not found for Dynamic Query Entity " + entityClass.getName());
            }

            String graphLevel = getGraphLevel(graphName, parentGraphName);

            //Writing entity level info like entity meta class name,graph level and table name
            writeEntityLevelInfo(graphName, entityClass, aWriter, graphLevel);

            //Creating Key and Join column class from base class
            createSupportingClasses(entityClass, aWriter);

            Field[] fields = entityClass.getDeclaredFields();// fields of root

            for (Field referenceVar : fields) {

                graphName = referenceVar.getName();

                Class<?> entityChildClass = getReferencedEntityClassObj(classLoader, referenceVar);

                if (null != entityChildClass) {
                    if (entityChildClass.isAnnotationPresent(ChildEntity.class) || entityClass.isAnnotationPresent(RootEntity.class)) {
                        createEntityUtilClass(classLoader, graphName, (graphLevel.equals(rootEntityName) ? "" : graphLevel), entityChildClass, aWriter);// calling accountfact
                        aWriter.write("}\n");
                    } else {
                        getLog().warn("@ChildEntity/@RootEntity annotation not found in class :: " + entityChildClass.getName());
                    }
                }
            }
        }

    }

    private void writeEntityLevelInfo(String graphName, Class<?> entityClass, FileWriter aWriter, String graphLevel) throws IOException {
        aWriter.write("\npublic class " + graphName.substring(0, 1).toUpperCase() + graphName.substring(1) + "{\n");
        aWriter.write("\nprivate " + graphName.substring(0, 1).toUpperCase() + graphName.substring(1) + "(){}\n");
        aWriter.write("\tpublic static final String TABLE = \"" + entityClass.getAnnotation(Table.class).name()
                + "\";\n");
        aWriter.write("\tpublic static final String GRAPH_LEVEL = \""
                + (graphLevel.equals(rootEntityName) ? "rootObject" : graphLevel) + "\";\n");
    }

    private String getGraphLevel(String graphName, String parentGraphName) {
        String grahLevel;
        if (!parentGraphName.equals("")) {
            grahLevel = parentGraphName + "_" + graphName;
        } else {
            grahLevel = graphName;
        }
        return grahLevel;
    }

    /**
     * Creates Key and JoinColumn Classes from the base class
     */
    private void createSupportingClasses(Class<?> entityClass, FileWriter aWriter) throws IOException {
        Field[] columnfields = entityClass.getSuperclass().getDeclaredFields();

        List<Field> keyColumns = new LinkedList<>();
        List<Field> joinColumns = new LinkedList<>();
        List<Field> allColumns = new LinkedList<>();

        for (Field f : columnfields) {
            if (f.isAnnotationPresent(Column.class)) {
                allColumns.add(f);
            }
            if (f.isAnnotationPresent(Id.class)) {
                keyColumns.add(f);
            }

            if (f.isAnnotationPresent(DynamicJoinColumn.class)) {
                joinColumns.add(f);
            }
        }

        if (!allColumns.isEmpty()) {
            createAllColumnsClass(allColumns, aWriter);
        }

        if (!keyColumns.isEmpty()) {
            createInnerKeyClass(keyColumns, aWriter);
        }

        if (!keyColumns.isEmpty()) {
            if (!joinColumns.isEmpty()) {
                createJoinColumnClass(joinColumns, aWriter);
            } else {
                getLog().warn("@DynamicJoinColumn annotation not found in any field :: " + entityClass.getName());
            }
        }
    }

    /**
     * Returns class type object of Reference variable passed
     */
    private Class<?> getReferencedEntityClassObj(URLClassLoader classLoader, Field referenceVar) {
        String referenceEntityName;
        if (referenceVar.getType().equals(List.class)) {

            referenceEntityName = ((ParameterizedType) referenceVar.getGenericType())
                    .getActualTypeArguments()[0].getTypeName();

        } else {
            referenceEntityName = referenceVar.getType().getName();
        }

        Class<?> entityChildClass = null;
        try {
            entityChildClass = Class.forName(referenceEntityName, true, classLoader);
        } catch (ClassNotFoundException e1) {
            getLog().error("Class Not Found : {}", e1);
        }
        return entityChildClass;
    }

    /**
     * Creates class containing all columns
     */
    private void createAllColumnsClass(List<Field> allColumns, FileWriter aWriter) throws IOException {
        aWriter.write("\tpublic  class AllColumns {\n");
        aWriter.write("\tprivate AllColumns(){}\n");
        for (Field joinColumn : allColumns) {
            String fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, joinColumn.getName());
            aWriter.write("\tpublic static final String " + fieldName + " = " + "\"" + joinColumn.getAnnotation(Column.class).name() + "\";\n");

        }
        aWriter.write("}\n");
    }

    /**
     * Creates class containing DynamicJoin columns
     */
    private void createJoinColumnClass(List<Field> joinColumns, FileWriter aWriter) throws IOException {
        aWriter.write("\tpublic  class JoinColumns {\n");
        aWriter.write("\tprivate JoinColumns(){}\n");
        for (Field joinColumn : joinColumns) {
            String fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, joinColumn.getName());
            aWriter.write("\tpublic static final String " + fieldName + " = " + "\"" + joinColumn.getAnnotation(Column.class).name() + "\";\n");

        }
        aWriter.write("}\n");

    }

    /**
     * Creates inner class containing primary keys
     */
    private void createInnerKeyClass(List<Field> keyColumns, FileWriter aWriter) throws IOException {
        aWriter.write("\tpublic class Keys {\n");
        aWriter.write("\tprivate Keys(){}\n");
        for (Field key : keyColumns) {
            String fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key.getName());
            aWriter.write("\tpublic static final String " + fieldName + " = " + "\"" + key.getAnnotation(Column.class).name() + "\";\n");

        }
        aWriter.write("}\n");

    }
}
