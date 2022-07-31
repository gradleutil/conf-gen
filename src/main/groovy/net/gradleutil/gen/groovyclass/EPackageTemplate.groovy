package net.gradleutil.gen.groovyclass

import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import groovy.util.logging.Log
import net.gradleutil.conf.template.EClass
import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.gen.Generator

import java.nio.file.Path

@Log(value = "logger")
class EPackageTemplate {

    EPackageTemplate() {}

    static String jteJarRender(File target, TransformOptions options) {
        TemplateOutput output = new StringOutput()
        logger.info('Rendering ePackage ' + options.ePackage.name + ' with ' + options.jteRenderPath)
        Generator.getTemplateEngine(target.toPath())
                .tap { setTrimControlStructures(true) }
                .render(options.jteRenderPath, options.renderParams, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static Map<String, String> render(TransformOptions options = Transformer.defaultOptions()) {
        File jteDir = options.jteDirectory
        EPackage ePackage = options.ePackage
        assert ePackage
        logger.info('loading ePackage ' + ePackage.name + ' from ' + jteDir.absoluteFile.toString())
        def jteJar = new File(jteDir, 'jte.jar').absoluteFile
        if (!jteDir.exists()) {
            throw new RuntimeException("Source dir $jteDir.absolutePath does not exist")
        }
        if (!jteJar.exists()) {
            Generator.jar(jteDir.toPath(), jteJar.toPath())
        }
        Map<String, String> files = [:]

        jteDir.listFiles().each { file -> rend(file, jteJar, files, options) }
        files
    }

    static void rend(File file, File jteJar, Map<String, String> files, TransformOptions options) {
        if (file.isDirectory()) {
            if (file.name == 'eClass') {
                file.listFiles().each { eClassTemplate ->
                    options.jteRenderPath = file.name + File.separator + eClassTemplate.name
                    options.ePackage.eClassifiers.each {
                        options.renderParams.eClass = it
                        def path = it.name.toLowerCase() + File.separator + it.name + eClassTemplate.name.replace('.jte', '.groovy')
                        files.put path, jteJarRender(jteJar, options)
                    }
                }
            } else {
                file.listFiles().each {
                    rend(it, jteJar, files, options)
                }
            }
        } else if (file.name.endsWith('.jte')) {
            options.jteRenderPath = file.name
            files.put file.name.replace('.jte', '.groovy'), jteJarRender(jteJar, options)
        }
    }

}