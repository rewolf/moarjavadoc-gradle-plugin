package com.github.rewolf.gradle.moarjavadoc.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Optional;
import java.util.Set;

/**
 * Task for delomboking source
 */
public class Delomboker extends DefaultTask {
    private Set<File> delombokFrom;
    private Directory delombokTo;

    public void setDelombokFrom(Set<File> directories) {
        this.delombokFrom = directories;
    }

    @InputFiles
    public Set<File> getDelombokFrom() {
        return delombokFrom;
    }

    public void setDelombokTo(Directory directory) {
        this.delombokTo = directory;
    }

    @OutputDirectory
    public Directory getDelombokTo() {
        return delombokTo;
    }

    @TaskAction
    public void delombokSource() {
        final JavaVersion javaVersion = getProject().getConvention().getPlugin(JavaPluginConvention.class).getSourceCompatibility();
        delombokUsing(findLombokDependency(getProject()).get(), getProject());
    }

    private void delombokUsing(final File lombokJar, final Project project) {
        // TODO: configurable delombok directory
        // TODO: configurable source set
        project.javaexec(je -> {
                             je.classpath(project.files(lombokJar))
                               .args("delombok", "-d", delombokTo.getAsFile().getAbsolutePath(), "src/main/java");
                         }
        );
    }

    private Optional<File> findLombokDependency(final Project project) {
        Optional<File> lombokFile = Optional.empty();

        for (final Configuration configuration : project.getConfigurations()) {
            if (!configuration.isCanBeResolved())
                continue;
            lombokFile = configuration.resolve()
                                      .stream()
                                      .filter(file -> file.getName()
                                                          .matches("^lombok.+jar")
                                      )
                                      .findFirst();
            if (lombokFile.isPresent()) {
                break;
            }
        }
        return lombokFile;
    }
}
