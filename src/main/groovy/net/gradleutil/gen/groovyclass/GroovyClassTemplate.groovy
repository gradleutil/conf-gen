package net.gradleutil.gen.groovyclass

import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import groovy.util.logging.Log
import net.gradleutil.conf.template.EPackage
import net.gradleutil.gen.Generator

import java.nio.file.Path

@Log(value = "logger")
class GroovyClassTemplate {


    GroovyClassTemplate() {
    }

    static String render(Path zipPath, EPackage ePackage) {
        TemplateOutput output = new StringOutput()
        logger.info('loading ePackage ' + ePackage.name + ' from ' + zipPath.toString() )
        Generator.getTemplateEngine(zipPath).tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", ePackage, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static String render(EPackage ePackage) {
        TemplateOutput output = new StringOutput()
        logger.info('loading ePackage ' + ePackage.name )
        Generator.getTemplateEngine().tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", ePackage, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }
}