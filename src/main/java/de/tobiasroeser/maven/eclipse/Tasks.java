package de.tobiasroeser.maven.eclipse;

import static de.tototec.utils.functional.FList.contains;
import static de.tototec.utils.functional.FList.flatten;
import static de.tototec.utils.functional.FList.foreach;
import static de.tototec.utils.functional.FList.map;
import static de.tototec.utils.functional.FList.mkString;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import de.tototec.utils.functional.Optional;

/**
 * Various worker methods to generate Eclipse project files.
 */
public class Tasks {

	private final File basedir;
	private final Optional<Log> log;

	public Tasks(final File basedir, Optional<Log> log) {
		this.basedir = basedir;
		this.log = log;
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
			if (contains(projectConfig.getDisabledBuilders(), b.getName())) {
				log.foreach(l -> l.debug("Builder [" + b.getName() + "] will be not added as it is disabled"));
			} else {
				printStream.println("\t\t<buildCommand>");
				printStream.println("\t\t\t<name>" + b.getName() + "</name>");
				printStream.println("\t\t\t<arguments>");
				printStream.println("\t\t\t</arguments>");
				printStream.println("\t\t</buildCommand>");
			}
		});
		printStream.println("\t</buildSpec>");

		printStream.println("\t<natures>");
		foreach(projectConfig.getNatures(), n -> {
			if (contains(projectConfig.getDisabledNatures(), n.getName())) {
				log.foreach(l -> l.debug("Nature [" + n.getName() + "] will be not added as it is disabled"));
			} else {
				printStream.println("\t\t<nature>" + n.getName() + "</nature>");
			}
		});
		printStream.println("\t</natures>");

		printStream.println("</projectDescription>");
	}

	public String relativePath(final String file) {
		return Util.relativePath(basedir, file);
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
						sourcesOptional, false));
		foreach(projectConfig.getResources(),
				s -> generateClasspathEntry(printStream, "src", s.getPath(),
						whenUndefined(buildOutput, outputDirectory),
						sourcesOptional, false, s.getIncludes(), s.getExcludes()));
		foreach(projectConfig.getTestSources(),
				s -> generateClasspathEntry(printStream, "src", s,
						whenUndefined(buildOutput, testOutputDirectory),
						sourcesOptional, true));
		foreach(projectConfig.getTestResources(),
				s -> generateClasspathEntry(printStream, "src", s.getPath(),
						whenUndefined(buildOutput, testOutputDirectory),
						sourcesOptional, true, s.getIncludes(), s.getExcludes()));

		// con
		foreach(projectConfig.getClasspathContainers(), cp -> {
			generateClasspathEntry(printStream, "con", cp, Optional.none(), false, false);
		});

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
			final boolean optional,
			final boolean test) {
		generateClasspathEntry(printStream, kind, path, outputPath, optional,
				test, Collections.emptyList(), Collections.emptyList());
	}

	protected void generateClasspathEntry(
			final PrintStream printStream,
			final String kind,
			final String path,
			final Optional<String> outputPath,
			final boolean optional,
			boolean test,
			final List<String> includes, final List<String> excludes) {

		
		String normalizedPath;
		if ("src".equals(kind)) {
			normalizedPath = relativePath(path);
		} else {
			normalizedPath = path;
		}
		
		boolean generated = (normalizedPath.startsWith("target/generated-"));
		log.foreach(l -> l.debug("ClasspathEntry: " + normalizedPath + (generated ? " (generated)" : "")));

		printStream.print("\t<classpathentry kind=\"" + kind + "\" path=\"" + normalizedPath + "\"");
		if (!includes.isEmpty()) {
			printStream.print(" including=\"");
			printStream.print(mkString(includes, "|"));
			printStream.print("\"");
		}
		if (!excludes.isEmpty()) {
			printStream.print(" excluding=\"");
			printStream.print(mkString(excludes, "|"));
			printStream.print("\"");
		}
		outputPath.foreach(p -> printStream.print(" output=\"" + relativePath(p) + "\""));
		printStream.println(">");
		printStream.println("\t\t<attributes>");
		if (optional) {
			printStream.println("\t\t\t<attribute name=\"optional\" value=\"true\"/>");
		}
		if(test) {
		    printStream.println("\t\t\t<attribute name=\"test\" value=\"true\"/>");
		}
		if(generated) {
		    printStream.println("\t\t\t<attribute name=\"ignore_optional_problems\" value=\"true\"/>");
		}
		printStream.println("\t\t\t<attribute name=\"maven.pomderived\" value=\"true\"/>");
		printStream.println("\t\t</attributes>");
		printStream.println("\t</classpathentry>");
	}

	public void generateSettingOrgEclipseJdtCorePrefs(final PrintStream printStream,
			final Optional<String> javaVersion) {
		printStream.println("eclipse.preferences.version=1");
		javaVersion.foreach(v -> {
			printStream.println("org.eclipse.jdt.core.compiler.codegen.targetPlatform=" + v);
			printStream.println("org.eclipse.jdt.core.compiler.compliance=" + v);
			printStream.println("org.eclipse.jdt.core.compiler.source=" + v);
		});
	}

	public void generateSettingOrgEclipseM2eCorePrefs(final PrintStream printStream,
			final List<String> activeProfiles) {
		printStream.println("activeProfiles=" + mkString(activeProfiles, ","));
		printStream.println("eclipse.preferences.version=1");
		printStream.println("resolveWorkspaceProjects=true");
		printStream.println("version=1");
	}

	public void generateSettingOrgEclipseCoreResourcesPrefs(final PrintStream printStream,
			final ProjectConfig projectConfig) {
		printStream.println("eclipse.preferences.version=1");
		projectConfig.getEncoding().foreach(encoding -> {
			foreach(flatten(Arrays.asList(
					projectConfig.getSources(),
					map(projectConfig.getResources(), r -> r.getPath()),
					projectConfig.getTestSources(),
					map(projectConfig.getTestResources(), r -> r.getPath()))),
					path -> printStream.println("encoding//" + relativePath(path) + "=" + encoding));
			printStream.println("encoding/<project>=" + encoding);
		});
	}

}
