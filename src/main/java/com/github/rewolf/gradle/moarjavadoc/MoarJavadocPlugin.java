package com.github.rewolf.gradle.moarjavadoc;

import com.github.rewolf.gradle.moarjavadoc.tasks.Delomboker;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

/**
 * Plugin class for the Moar Javadoc Plugin
 *
 * @author rewolf
 */
public class MoarJavadocPlugin implements Plugin<Project> {
    public static final String DELOMBOK_TASK_NAME = "delombok";
    public static final String JAVADOC_CMD_SOURCE_OPTION = "source";
    public static final String JAVADOC_CMD_LINK_OPTION = "link";

    @Override
    public void apply(final Project project) {

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
            final Task delombokTask = configureDelombokTask(project, javaConvention);
            configureJavadoc(project, javaConvention, delombokTask);
        });
    }

    private Task configureDelombokTask(final Project project, final JavaPluginConvention javaConvention) {
        final SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        return project.getTasks().register(DELOMBOK_TASK_NAME, Delomboker.class, delombok -> {
            delombok.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            delombok.setDescription("Delomboks main Java source code into a 'delombokd' directory in the build directory");
            delombok.setDelombokFrom(main.getJava().getSrcDirs());
            delombok.setDelombokTo(project.getLayout().getBuildDirectory().dir("delombokd").get());
        }).get();
    }

    private void configureJavadoc(final Project project, final JavaPluginConvention javaConvention, final Task delombokTask) {
        project.getTasks().withType(Javadoc.class).configureEach(javadoc -> {
            final CoreJavadocOptions javadocOptions = (CoreJavadocOptions) javadoc.getOptions();
            linkJavadocByJavaVersion(javaConvention.getSourceCompatibility(), javadocOptions);

            javadoc.setSource(project.getLayout().getBuildDirectory().dir("delombokd").get());
            javadoc.dependsOn(delombokTask);
        });
    }

    /**
     * Use the provided JavaVersion to (1) Specify the expected source level of the Java code and (2) Link to the official
     * Java SE API Docs for the
     *
     * @param javaVersion    provided java version to sync with
     * @param javadocOptions the javadoc options to configure
     */
    private void linkJavadocByJavaVersion(final JavaVersion javaVersion, final CoreJavadocOptions javadocOptions) {
        final String majorVersion = javaVersion.getMajorVersion();
        String javaSeApiDocUrl;
        System.out.println(majorVersion);
        javadocOptions.addStringOption(JAVADOC_CMD_SOURCE_OPTION, majorVersion);

        if (Integer.parseInt(majorVersion) < 11) {
            javadocOptions.addStringOption(JAVADOC_CMD_LINK_OPTION, String.format("https://docs.oracle.com/javase/%s/docs/api", majorVersion));
        } else {
            javadocOptions.addStringOption(JAVADOC_CMD_LINK_OPTION, String.format("https://docs.oracle.com/en/java/javase/%s/docs/api", majorVersion));

        }

    }

}