package org.codehaus.mojo.webstart;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Packages a jnlp application.
 * <p/>
 * The plugin tries to not re-sign/re-pack if the dependent jar hasn't changed.
 * As a consequence, if one modifies the pom jnlp config or a keystore, one should clean before rebuilding.
 * <p/>This mojo forks a build lifecycle and won't install the zip packages in your local repository.
 * You probably want to use the jnlp-inline instead.
 * <p/>
 * For more informations about how to choose the matching mojo see http://mojo.codehaus.org/webstart/webstart-maven-plugin/usage.html#Choices
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
@Mojo(name = "jnlp-inf", defaultPhase = LifecyclePhase.COMPILE, requiresProject = true, inheritByDefault = true,
	requiresDependencyResolution = ResolutionScope.RUNTIME, aggregator = true)
@Execute(phase = LifecyclePhase.COMPILE)
public class JnlpInfMojo
	extends AbstractJnlpMojo {
	private static final String INF_FOLDER = "JNLP-INF";

	private static final String APPLICATION_TEMPLATE = "APPLICATION_TEMPLATE.JNLP";

	// ----------------------------------------------------------------------
	// Mojo Parameters
	// ----------------------------------------------------------------------

	/**
	 * Get the executed project from the forked lifecycle.
	 */
	@Parameter(defaultValue = "${executedProject}", required = true, readonly = true)
	private MavenProject executedProject;

	// ----------------------------------------------------------------------
	// AbstractBaseJnlpMojo implementatio
	// ----------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public MavenProject getProject() {
		return executedProject;
	}

	@Override
	protected String getCodebase() {
		return "*";
	}


	@Override
	public void execute() throws MojoExecutionException {
		makeArchive = false;
		super.execute();
	}

	@Override
	protected void processDependencies() throws MojoExecutionException {
		artifactWithMainClass = getProject().getArtifact(); // We haven't built the artifact jar yet.
		packagedJnlpArtifacts.add(artifactWithMainClass);
		super.processDependencies();
	}

	@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
	@Override
	protected void generateJnlpFile(File outputDirectory, String outputFile) throws MojoExecutionException {

		outputDirectory = new File(getProject().getBuild().getOutputDirectory(), INF_FOLDER);
		outputDirectory.mkdirs();

		super.generateJnlpFile(outputDirectory, APPLICATION_TEMPLATE);

		try{
			for (File file: getWorkDirectory().listFiles()) {
				file.delete();
			}
		}
		catch(NullPointerException ignored){
			// If it fails, it fails - It will be captured later by the jnlp process.
		}
	}

	@Override
	protected void signOrRenameJars() throws MojoExecutionException {
	}
}

