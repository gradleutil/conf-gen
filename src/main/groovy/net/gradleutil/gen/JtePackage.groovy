package net.gradleutil.gen


import gg.jte.TemplateEngine
import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import groovy.util.logging.Log
import net.gradleutil.conf.BeanConfigLoader
import net.gradleutil.conf.Loader
import net.gradleutil.conf.transform.groovy.SchemaToGroovyClass
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.gen.Generator

import java.nio.file.Path
import java.util.concurrent.TimeUnit

@Log(value = 'logger')
class JtePackage {


    static List<File> getUrls(ClassLoader classLoader){
        List<File> urls = []
        println "$classLoader"
        if(classLoader instanceof URLClassLoader){
            urls.addAll classLoader.getURLs().collect{new File(it.file) }
        }
        if (classLoader.parent) {
            urls.addAll getUrls(classLoader.parent)
        }
        urls
    }

    static void execute(Path sourceDirectory, Path targetDirectory, String packageName) {
        long start = System.nanoTime()
        targetDirectory.toFile().deleteDir()
        if (!targetDirectory.toFile().mkdirs()) {
            throw new RuntimeException("could not create target directory")
        }
        
        logger.info("Pre-compiling jte templates found in file://" + sourceDirectory.toAbsolutePath().toFile())
//        ConfUtil.copyFolder(sourceDirectory, tempDir.toPath())
        def mhf = sourceDirectory.toFile().listFiles().find { it.name.endsWith('.mhf') }
        assert mhf, "mhf file not found in ${sourceDirectory.toFile().listFiles()}"
        def modelName = mhf.name.replace('.mhf', '').capitalize()
        logger.info("using mhf file:///" + mhf.absolutePath)

        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(mhf, modelName)
        def modelFile = new File(sourceDirectory.toFile(), modelName + '.groovy')
        SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, modelName, modelFile)
        logger.info("groovy file:///" + modelFile.absolutePath)

        def jarPath = new File(targetDirectory.toFile(),"${modelName}.jar").toPath()
        
        def options = Generator.defaultOptions().compilePath(getUrls(this.classLoader))
        Generator.jar(sourceDirectory, jarPath, options)
        TemplateOutput output = new StringOutput()
        logger.info("created jar: ${jarPath}")
        TemplateEngine renderEngine = Generator.getTemplateEngine(jarPath)
        def loader = Generator.getLoader(jarPath)
        def config = Loader.load(mhf).getConfig('plugin')
        def funk = BeanConfigLoader.get(config, packageName + '.' + modelName, loader)
        renderEngine.render("build.gradle.jte", funk, output)
        //println(output)

        long end = System.nanoTime()
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start)
        logger.info("Successfully executed mhf file in " + duration + "s to " + targetDirectory)
    }

}
