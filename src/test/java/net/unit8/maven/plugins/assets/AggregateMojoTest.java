package net.unit8.maven.plugins.assets;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.test.plugin.RepositoryTool;
import org.junit.Test;

import java.io.File;

public class AggregateMojoTest extends AbstractMojoTestCase {

	@Test
	public void test() throws Exception {
        File pom = getTestFile("src/test/resources/pom.xml");

        assertNotNull(pom);
        assertTrue(pom.exists());

        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();
        RepositoryTool repositoryTool = lookup(RepositoryTool.class);
        File localRepo = repositoryTool.findLocalRepositoryDirectory();
        buildingRequest.setLocalRepository(repositoryTool.createLocalArtifactRepositoryInstance(localRepo));
        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
        MavenProject project = projectBuilder.build(pom, buildingRequest).getProject();
		AggregateMojo mojo = (AggregateMojo) lookupConfiguredMojo(project, "aggregate");
        assertNotNull(mojo);

		mojo.execute();
	}

}
