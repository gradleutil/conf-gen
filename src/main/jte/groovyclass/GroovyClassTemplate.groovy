package net.gradleutil.gen.groovyclass

import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import net.gradleutil.conf.template.EPackage
import net.gradleutil.gen.Generator

import java.nio.file.Path

class GroovyClassTemplate {


    GroovyClassTemplate() {
    }

    static String render(Path zipPath, EPackage ePackage) {
        TemplateOutput output = new StringOutput()
        Generator.getTemplateEngine(zipPath).tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", ePackage, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static String render(EPackage ePackage) {
        TemplateOutput output = new StringOutput()
        Generator.getTemplateEngine().tap { setTrimControlStructures(true) }.render("groovyclass/GroovyGen.jte", ePackage, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }
}