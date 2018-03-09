package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.distinct;
import static de.tototec.utils.functional.FList.mkString;
import static de.tototec.utils.functional.FList.take;

import java.util.Collections;
import java.util.List;

import de.tototec.utils.functional.Optional;

/**
 * Project configuration data used to generate Eclipse project files.
 */
public class ProjectConfig {

	private final String name;
	private final String comment;
	private final List<String> sources;
	private final List<String> testSources;
	private final List<String> resources;
	private final List<String> testResources;
	private final List<Builder> builders;
	private final List<Nature> natures;
	private Optional<String> javaVersion;

	public ProjectConfig() {
		this("", "",
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				Optional.lift(System.getProperty("java.version")).map(v -> javaVersion(v)));
	}

	public ProjectConfig(
			final String name,
			final String comment,
			final List<String> sources,
			final List<String> testSources,
			final List<String> resources,
			final List<String> testResources,
			final List<Builder> builders,
			final List<Nature> natures,
			final Optional<String> javaVersion) {
		this.name = name;
		this.comment = comment;
		this.sources = distinct(sources);
		this.testSources = distinct(testSources);
		this.resources = distinct(resources);
		this.testResources = distinct(testResources);
		this.builders = distinct(builders);
		this.natures = distinct(natures);
		this.javaVersion = javaVersion;
	}

	public static String javaVersion(final String javaVersion) {
		return mkString(take(javaVersion.split("[.]"), 2), ".");
	}

	public String getName() {
		return name;
	}

	public ProjectConfig withName(final String name) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public String getComment() {
		return comment;
	}

	public ProjectConfig withComment(final String comment) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getSources() {
		return sources;
	}

	public ProjectConfig withSources(final List<String> sources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getTestSources() {
		return testSources;
	}

	public ProjectConfig withTestSources(final List<String> testSources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getResources() {
		return resources;
	}

	public ProjectConfig withResources(final List<String> resources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<String> getTestResources() {
		return testResources;
	}

	public ProjectConfig withTestResources(final List<String> testResources) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<Builder> getBuilders() {
		return builders;
	}

	public ProjectConfig withBuilders(final List<Builder> builders) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public List<Nature> getNatures() {
		return natures;
	}

	public ProjectConfig withNatures(final List<Nature> natures) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	public Optional<String> getJavaVersion() {
		return javaVersion;
	}

	public ProjectConfig withJavaVersion(final Optional<String> javaVersion) {
		return new ProjectConfig(name, comment, sources, testSources, resources, testResources, builders, natures,
				javaVersion);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " {" +
				"\n  name: " + name +
				"\n  comment: " + comment +
				"\n  sources: " + sources +
				"\n  resources: " + resources +
				"\n  testSources: " + testSources +
				"\n  testResources: " + testResources +
				"\n  builders: " + builders +
				"\n  natures: " + natures +
				"\n  javaVersion: " + javaVersion +
				"\n}";
	}
}
