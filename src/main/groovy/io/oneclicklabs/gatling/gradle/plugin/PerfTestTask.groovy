package io.oneclicklabs.gatling.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by oneclicklabs.io on 12/21/16.
 */
public class PerfTestTask extends DefaultTask {
    @Input
    String warmUpURL
    @Input
    List<String> sysProperties = []
    @Input
    String simulationClass

    @Input
    String simulationClassesFolder

    @TaskAction
    def perfTest() {
        project.javaexec {
            main = "io.gatling.app.Gatling"
            classpath = project.configurations.performanceTest
            jvmArgs   project.perfTestPluginOptions.scala_jvmargs + ["-Dgatling.http.enableGA=false", "-Dgatling.core.directory.binaries=${simulationClassesFolder}",
                                                                     "-Dgatling.http.warmUpUrl=${warmUpURL}"] + sysProperties

            args = ["-rf", "${project.buildDir}/performance-results", "-s", "${simulationClass}"]
        }
    }
}

