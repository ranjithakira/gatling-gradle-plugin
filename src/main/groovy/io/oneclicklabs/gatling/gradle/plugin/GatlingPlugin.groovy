package io.oneclicklabs.gatling.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.scala.ScalaPlugin

/**
 * Created by oneclicklabs.io on 12/20/16.
 */
class GatlingPlugin implements Plugin<Project> {

    public static def GATLING_EXTENSION_NAME = 'performance'

    public static def GATLING_RUN_TASK_NAME = 'performanceTest'

    static String GATLING_TASK_NAME_PREFIX = "$GATLING_RUN_TASK_NAME-"

    private Project project

    void apply(Project project) {
        this.project = project

        project.pluginManager.apply ScalaPlugin

        def gatlingExt = project.extensions.create(GATLING_EXTENSION_NAME, PluginExtension, project)

        createConfiguration(gatlingExt)

        createGatlingTask(GATLING_RUN_TASK_NAME, gatlingExt)

        project.tasks.getByName("processGatlingResources").doLast(new LogbackTaskAction())

        project.tasks.addRule("Pattern: $GATLING_RUN_TASK_NAME-<SimulationClass>: Executes single Gatling simulation.") {
            String taskName ->
                if (taskName.startsWith(GATLING_TASK_NAME_PREFIX)) {
                    createGatlingTask(taskName, gatlingExt, [(taskName - GATLING_TASK_NAME_PREFIX)])
                }
        }
    }

    void createGatlingTask(String taskName, PluginExtension gatlingExt, Iterable<String> predefinedSimulations = null) {
        def task = project.tasks.create(name: taskName,
                dependsOn: project.tasks.gatlingClasses, type: PerformanceTestTask,
                description: "Execute Gatling simulation", group: "Gatling",
        ) as ConventionTask

        task.convention.plugins["gatling"] = gatlingExt
        task.conventionMapping["jvmArgs"] = { gatlingExt.jvmArgs }

        if (predefinedSimulations) {
            task.configure { simulations = predefinedSimulations }
        } else {
            task.conventionMapping["simulations"] = { gatlingExt.simulations }
        }
    }

    void createConfiguration(PluginExtension gatlingExt) {
        project.configurations {
            ['gatling', 'gatlingCompile', 'gatlingRuntime'].each() { confName ->
                create(confName) {
                    visible = false
                }
            }
            gatlingCompile.extendsFrom(gatling)
        }

        project.sourceSets {
            gatling {
                scala.srcDirs = [gatlingExt.simulationsDir()]
                resources.srcDirs = [gatlingExt.dataDir(),
                                     gatlingExt.bodiesDir(),
                                     gatlingExt.confDir()]
            }
        }

        project.dependencies {
            gatlingCompile 'org.scala-lang:scala-library:2.11.8'

            gatlingCompile project.sourceSets.main.output
            gatlingCompile project.sourceSets.test.output

            gatlingRuntime project.sourceSets.gatling.output
            gatlingRuntime project.sourceSets.gatling.output
        }

        project.afterEvaluate { Project p ->
            p.dependencies {
                gatling "io.gatling.highcharts:gatling-charts-highcharts:${p.extensions.getByType(PluginExtension).toolVersion}"
            }
        }
    }
}
