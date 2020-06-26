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

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

@SpringBootTest
public class GraphQLDDQPluginTest extends AbstractMojoTestCase {


    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testMojoLookup() throws Exception {
        System.out.println("Base Dir :: " + getBasedir());
        File testPom = new File(getBasedir(),
                "src/test/resources/pom.xml");

        GraphQLDDQPlugin mojo = (GraphQLDDQPlugin) lookupMojo("graphql-ddq-dquery", testPom);
        assertNotNull(mojo);
    }

    @Test
    public void testMojoExecute() throws Exception {
        System.out.println("Base Dir :: " + getBasedir());
        File testPom = new File(getBasedir(),
                "src/test/resources/pom.xml");
        MavenProject project = readMavenModel(testPom);
        GraphQLDDQPlugin mojo = (GraphQLDDQPlugin) lookupMojo("graphql-ddq-dquery", testPom);
        setPrivateField(mojo, "project", project, false);

        mojo.execute();
        verifyConstantFileGenerated(project);
        deleteOlderFileIfPresent(project);
    }

    private void deleteOlderFileIfPresent(MavenProject project) {
        File oldFile = new File(getBasedir(),
                "src/test/java/" + getDestinationPackage(project) + "/SurgeonMeta.java");
        oldFile.setWritable(true);
        oldFile.deleteOnExit();
    }

    private void verifyConstantFileGenerated(MavenProject project) throws IOException {
        String destinationPackage = getDestinationPackage(project);

        BufferedReader ref = new BufferedReader(new FileReader(getBasedir() + "/" +
                "src/test/resources/SurgeonMetaTestRef.txt"));

        BufferedReader generated = new BufferedReader(new FileReader(getBasedir() + "/" +
                "src/test/java/" + destinationPackage + "/SurgeonMeta.java"));

        Assert.assertTrue(verifyEqualityByLine(ref, generated));
    }

    /**
     * Verifying if two files are same by matching each line.
     * Utility methods like from IOUtils and FileUtils were not giving stable response
     */
    private boolean verifyEqualityByLine(BufferedReader ref, BufferedReader generated) throws IOException {
        boolean areEqual = true;

        int lineNum = 1;

        String refLine = ref.readLine();
        String generatedLine = generated.readLine();

        while (refLine != null || generatedLine != null) {
            if (refLine == null || generatedLine == null) {
                areEqual = false;
                break;
            } else if (!refLine.equalsIgnoreCase(generatedLine)) {
                areEqual = false;
                break;
            }

            refLine = ref.readLine();
            generatedLine = generated.readLine();

            lineNum++;
        }

        if (areEqual) {
            System.out.println("Two files have same content.");
        } else {
            System.out.println("Two files have different content. They differ at line " + lineNum);
            System.out.println("Referenced file has -------" + refLine + "------- and Generated class has -------" + generatedLine + "------- at line " + lineNum);
        }

        ref.close();

        generated.close();
        return areEqual;
    }

    private String getDestinationPackage(MavenProject project) {
        Xpp3Dom pluginConfiguration = (Xpp3Dom) project.getBuild().getPluginsAsMap().get("com.americanexpress.dydaq:graphql-ddq-plugin").getConfiguration();
        Xpp3Dom myConfig = pluginConfiguration.getChild("destinationPackage");
        return myConfig.getValue().replace(".", "/");
    }

    private void setPrivateField(Object objectInstance, String fieldName, Object fieldSetValue, boolean optional) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field declaredField = objectInstance.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(objectInstance, fieldSetValue);
        } catch (java.lang.NoSuchFieldException e) {
            if (!optional) {
                throw e;
            }
        }
    }


    private MavenProject readMavenModel(File testPom) {
        Model model;
        FileReader reader;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        MavenProject mavenProject = null;
        try {
            reader = new FileReader(testPom);

            model = mavenreader.read(reader);
            model.setPomFile(testPom);
            mavenProject = new MavenProject(model);
            model.getBuild().setSourceDirectory(getBasedir() + File.separator + "src/test/java");
            model.getBuild().setDirectory(getBasedir() + File.separator + "src/test/resources");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mavenProject;
    }

}