package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.foreach;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;

import de.tototec.utils.functional.Optional;

public class Tasks {

	private final File basedir;

	public Tasks(final File basedir) {
		this.basedir = basedir;
	}

	public void generateProjectFile(
			final PrintStream printStream,
			final ProjectConfig projectConfig) {
		printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		printStream.println("<!-- Generated by eclipse-maven-plugin -->");
		printStream.println("<projectDescription>");
		printStream.println("\t<name>" + projectConfig.getName() + "</name>");
		printStream.println("\t<comment>" + projectConfig.getComment() + "</comment>");

		printStream.println("\t<projects>");
		printStream.println("\t</projects>");

		printStream.println("\t<buildSpec>");
		foreach(projectConfig.getBuilders(), b -> {
			printStream.println("\t\t<buildCommand>");
			printStream.println("\t\t\t<name>" + b + "</name>");
			printStream.println("\t\t\t<arguments>");
			printStream.println("\t\t\t</arguments>");
			printStream.println("\t\t</buildCommand>");
		});
		printStream.println("\t</buildSpec>");

		printStream.println("\t<natures>");
		foreach(projectConfig.getNatures(), n -> {
			printStream.println("\t\t<nature>" + n + "</nature>");
		});
		printStream.println("\t</natures>");

		printStream.println("</projectDescription>");
	}

	public String relativePath(final String file) {
		final URI basePath = basedir.toURI();
		final String relPath = basePath.relativize(new File(file).toURI()).getPath();
		if (relPath.length() > 1 && relPath.endsWith("/")) {
			return relPath.substring(0, relPath.length() - 1);
		} else {
			return relPath;
		}
	}

	public Optional<String> whenUndefined(final Optional<?> predicate, final String useWhenDefined) {
		if (predicate.isDefined())
			return Optional.none();
		else
			return Optional.some(useWhenDefined);
	}

	public void generateClasspathFileContent(final PrintStream printStream, final ProjectConfig projectConfig,
			final Optional<String> buildOutput, final String outputDirectory, final String testOutputDirectory,
			final boolean sourcesOptional) {

		printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		printStream.println("<!-- Generated by eclipse-maven-plugin -->");
		printStream.println("<classpath>");

		// src
		foreach(projectConfig.getSources(),
				s -> generateClasspathEntry(printStream, "src", s,
						whenUndefined(buildOutput, outputDirectory),
						sourcesOptional));
		foreach(projectConfig.getResources(),
				s -> generateClasspathEntry(printStream, "src", s,
						whenUndefined(buildOutput, outputDirectory),
						sourcesOptional));
		foreach(projectConfig.getTestSources(),
				s -> generateClasspathEntry(printStream, "src", s,
						whenUndefined(buildOutput, testOutputDirectory),
						sourcesOptional));
		foreach(projectConfig.getTestResources(),
				s -> generateClasspathEntry(printStream, "src", s,
						whenUndefined(buildOutput, testOutputDirectory),
						sourcesOptional));

		// con
		generateClasspathEntry(printStream, "con",
				"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8",
				Optional.none(), false);

		generateClasspathEntry(printStream, "con", "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER", Optional.none(),
				false);

		// output
		printStream.println("\t<classpathentry kind=\"output\" path=\""
				+ buildOutput.getOrElse(relativePath(outputDirectory)) + "\"/>");

		printStream.println("</classpath>");
	}

	protected void generateClasspathEntry(
			final PrintStream printStream,
			final String kind,
			final String path,
			final Optional<String> outputPath,
			final boolean optional) {
		printStream.print("\t<classpathentry kind=\"" + kind + "\" path=\"" + relativePath(path) + "\"");
		outputPath.foreach(p -> printStream.print(" output=\"" + relativePath(p) + "\""));
		printStream.println(">");
		printStream.println("\t\t<attributes>");
		if (optional) {
			printStream.println("\t\t\t<attribute name=\"optional\" value=\"true\"/>");
		}
		printStream.println("\t\t\t<attribute name=\"maven.pomderived\" value=\"true\"/>");
		printStream.println("\t\t</attributes>");
		printStream.println("\t</classpathentry>");
	}

}
