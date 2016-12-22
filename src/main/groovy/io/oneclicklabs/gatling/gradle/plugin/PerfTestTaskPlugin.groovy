package io.oneclicklabs.gatling.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.scala.ScalaCompile;

/**
 * Created by oneclicklabs.io on 12/21/16.
 */
public class PerfTestTaskPlugin implements Plugin<Project> {

    @Override
    def void apply(Project target) {
        def perfTestConfigContainer = "performanceTest"
        target.apply(plugin: 'scala')
        setupConfigurationContainer(target, perfTestConfigContainer)
        setupGatlingDependenciesToContainer(target, perfTestConfigContainer)
        setupScalaCompilerOptions(target)
    }

    def void setupScalaCompilerOptions(Project target){
        target.extensions.perfTestPluginOptions =   PerfScalaCompileOptions
        target.tasks.withType(ScalaCompile)  {
            scalaCompileOptions.incrementalOptions.analysisFile = target.file("${target.buildDir}/incremental")
            scalaCompileOptions.useCompileDaemon = false
            scalaCompileOptions.useAnt = false
            scalaCompileOptions.forkOptions.jvmArgs = target.perfTestPluginOptions.scala_jvmargs
        }
    }

    def void setupConfigurationContainer(Project target, String containerName){
        target.configurations.create(containerName)
    }

    def void setupGatlingDependenciesToContainer(Project target, String containerName) {
        def gatlingVersion = "2.1.6"
        def libraries = [
                gatling : [
                        "org.scala-lang:scala-library:2.11.1",
                        "io.gatling:gatling-app:${gatlingVersion}",
                        "io.gatling:gatling-core:${gatlingVersion}",
                        "io.gatling:gatling-http:${gatlingVersion}",
                        "io.gatling.highcharts:gatling-charts-highcharts:${gatlingVersion}"
                ]
        ]
        libraries.gatling.each { librarySpec ->
            target.dependencies.add(containerName, librarySpec)
        }
        target.extensions.gatlingLibraries = libraries
    }
}
