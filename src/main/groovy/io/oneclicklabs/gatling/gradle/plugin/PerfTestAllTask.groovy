package io.oneclicklabs.gatling.gradle.plugin

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by oneclicklabs.io on 12/21/16.
 */
public class PerfTestAllTask extends DefaultTask {
    @Input
    String warmUpURL
    @Input
    List<String> sysProperties = []
    @Input
    List<String> simulationClasses = []

    @Input
    String simulationClassesFolder

    @TaskAction
    def perfTestAll() {

        /*
        We need to verify that the list of classes to run match
        the class names in the performanceTest folder.  If they do
        NOT match, (eg "junktest" is passed into the simulationClasses
        array) the Gatling IO plugin will spin in an infinite loop.

        The code below checks the .class file names and ensures that
        they match with at least the name of the files in the folder.
        Unfortunately, without inspecting the class itself, there is
        no easy way to make sure that the classes actually match.

         */
        println "performanceTest folder: ${simulationClassesFolder}"
        List<String> fileList = new ArrayList<>()
        FileType
        def dir = new File(simulationClassesFolder)
        dir.eachFileRecurse(FileType.FILES) { file ->
            String fileName = file.name
            if (fileName.endsWith(".class")) {
                fileName = fileName.substring(0, fileName.length() - 6)
            }
            fileList.add(fileName)
            println "Class from performanceTest folder: ${fileName}"
        }
        simulationClasses.each { simulationClass ->
            //println "File supplied to run: ${simulationClass}"
            // Remove the package name.
            def simulationClassName = simulationClass
            if (simulationClass.contains(".")) {
                simulationClassName = simulationClass.substring(simulationClass.lastIndexOf(".") + 1)
            }
            // println "Base file name ${simulationClassName}"

            boolean missing = true
            for (String fileName : fileList) {
                //println "  Matching against class file ${fileName}"
                if (simulationClassName.equals(fileName)) {
                    missing = false
                }
            }
            if (missing) {
                println "ERROR: The file specified in the build.gradle [${simulationClass}] " +
                        "was not found in the performance test folder [${simulationClassesFolder}]"
                throw new GradleException("The file specified in the build.gradle [${simulationClass}] " +
                        "was not found in the performance test folder [${simulationClassesFolder}]")
            }
        }


        simulationClasses.each { simulationClass ->
            project.javaexec {
                main = "io.gatling.app.Gatling"
                classpath = project.configurations.performanceTest
                jvmArgs project.perfTestPluginOptions.scala_jvmargs + ["-Dgatling.http.enableGA=false", "-Dgatling.core.directory.binaries=${simulationClassesFolder}",
                                                                       "-Dgatling.http.warmUpUrl=${warmUpURL}"] + sysProperties

                args = ["-rf", "${project.buildDir}/performance-results", "-s", "${simulationClass}"]
            }
        }
    }
}
