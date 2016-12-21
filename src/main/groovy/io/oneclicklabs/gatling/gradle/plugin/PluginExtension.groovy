package io.oneclicklabs.gatling.gradle.plugin

import org.gradle.api.Project
import java.nio.file.Paths

/**
 * Created by oneclicklabs.io on 12/20/16.
 */
class PluginExtension {

    def toolVersion = '2.2.2'

    def jvmArgs = [
            '-server',
            '-XX:+UseThreadPriorities',
            '-XX:ThreadPriorityPolicy=42',
            '-Xms512M',
            '-Xmx512M',
            '-Xmn100M',
            '-XX:+HeapDumpOnOutOfMemoryError',
            '-XX:+AggressiveOpts',
            '-XX:+OptimizeStringConcat',
            '-XX:+UseFastAccessorMethods',
            '-XX:+UseParNewGC',
            '-XX:+UseConcMarkSweepGC',
            '-XX:+CMSParallelRemarkEnabled',
            '-Djava.net.preferIPv4Stack=true',
            '-Djava.net.preferIPv6Addresses=false'
    ]

    def simulations = {
        include "**/*Simulation.scala"
    }

    String logLevel = "WARN"

    private final boolean isGatlingLayout

    private final Project project

    PluginExtension(Project project) {
        this.project = project
        this.isGatlingLayout = project.file("src/performanceTest/simulations").exists()
    }

    String simulationsDir() {
        "src/performanceTest/${isGatlingLayout ? "simulations" : "scala"}"
    }

    String dataDir() {
        "src/performanceTest${isGatlingLayout ? "" : "/resources"}/data"
    }

    String bodiesDir() {
        "src/performanceTest${isGatlingLayout ? "" : "/resources"}/bodies"
    }

    String confDir() {
        "src/performanceTest${isGatlingLayout ? "" : "/resources"}/conf"
    }

    Iterable<String> resolveSimulations(Closure simulationFilter = getSimulations()) {
        def p = this.project
        project.sourceSets.gatling.allScala.matching(simulationFilter).collect { File simu ->
            Paths.get(p.file(this.simulationsDir()).toURI()).relativize(Paths.get(simu.toURI())).join(".") - ".scala"
        }
    }

}
