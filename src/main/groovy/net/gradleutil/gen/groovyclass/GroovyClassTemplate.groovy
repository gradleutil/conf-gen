package net.gradleutil.gen.groovyclass

import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import groovy.util.logging.Log
import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.gen.Generator

import java.nio.file.Path

@Log(value = "logger")
class GroovyClassTemplate {


    GroovyClassTemplate() {
    }

    static String render(File sourceDir, EPackage ePackage, TransformOptions options = Transformer.defaultOptions()) {
        TemplateOutput output = new StringOutput()
        logger.info('loading ePackage ' + ePackage.name + ' from ' + sourceDir.absoluteFile.toString() )
        def target = new File(sourceDir,'jte.jar').absoluteFile
        if(!sourceDir.exists()){
            throw new RuntimeException("Source dir $sourceDir.absolutePath does not exist")
        }
        if(!target.exists()){
            Generator.jar(sourceDir.toPath(), target.toPath())
        }
        def params = [ePackage:ePackage, options:options]
        Generator.getTemplateEngine(target.toPath()).tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", params, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static String render(Path zipPath, EPackage ePackage, TransformOptions options = Transformer.defaultOptions()) {
        TemplateOutput output = new StringOutput()
        logger.info('loading ePackage ' + ePackage.name + ' from ' + zipPath.toString() )
        def params = [ePackage:ePackage, options:options]
        Generator.getTemplateEngine(zipPath).tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", params, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static String render(EPackage ePackage, TransformOptions options = Transformer.defaultOptions()) {
        TemplateOutput output = new StringOutput()
        logger.info('loading ePackage ' + ePackage.name )
        def params = [ePackage:ePackage, options:options]
        Generator.getTemplateEngine().tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", params, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }
}