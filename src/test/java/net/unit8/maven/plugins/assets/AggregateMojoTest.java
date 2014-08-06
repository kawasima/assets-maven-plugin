package net.unit8.maven.plugins.assets;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.test.plugin.RepositoryTool;
import org.apache.maven.shared.test.plugin.TestToolsException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AggregateMojoTest extends AbstractMojoTestCase {

    protected MavenProject createProject(File pom) throws ComponentLookupException, TestToolsException, ProjectBuildingException {
        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();
        RepositoryTool repositoryTool = lookup(RepositoryTool.class);
        File localRepo = repositoryTool.findLocalRepositoryDirectory();
        buildingRequest.setLocalRepository(repositoryTool.createLocalArtifactRepositoryInstance(localRepo));
        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
        return projectBuilder.build(pom, buildingRequest).getProject();
    }

    public void testNoMinify() throws Exception {
        File pom = getTestFile("src/test/resources/pom-1.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        MavenProject project = createProject(pom);
        AggregateMojo mojo = (AggregateMojo) lookupConfiguredMojo(project, "aggregate");
        assertNotNull(mojo);

        mojo.execute();

        Path aggregatedJs = Paths.get("target/js/action-1.7.js");
        assertTrue(Files.exists(aggregatedJs));
    }

    public void testMinify() throws Exception {
        File pom = getTestFile("src/test/resources/pom-2.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        MavenProject project = createProject(pom);
        AggregateMojo mojo = (AggregateMojo) lookupConfiguredMojo(project, "aggregate");
        assertNotNull(mojo);

        mojo.execute();
        Path aggregatedJs = Paths.get("target/js/action-2.js");
        assertTrue(Files.exists(aggregatedJs));
    }

    public void testAuto() throws Exception {
        File pom = getTestFile("src/test/resources/pom-3.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        MavenProject project = createProject(pom);
        final AggregateMojo mojo = (AggregateMojo) lookupConfiguredMojo(project, "aggregate");
        assertNotNull(mojo);

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mojo.execute();
                } catch (Exception ex) {
                    fail();
                }
            }
        });

        Path aggregatedJs = Paths.get("target/js/action-3.js");
        Thread.sleep(1000);
        assertTrue(Files.exists(aggregatedJs));

        long modifiedTime = System.currentTimeMillis();
        Files.setLastModifiedTime(Paths.get("src/test/resources/js/a1.js"),
                FileTime.fromMillis(modifiedTime));

        //Thread.sleep(2000);
        //assertTrue(Files.getLastModifiedTime(aggregatedJs).to(TimeUnit.MILLISECONDS) > modifiedTime);
        service.shutdown();
    }
}
