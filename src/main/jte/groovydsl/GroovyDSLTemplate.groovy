package net.gradleutil.gen.groovydsl


import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import net.gradleutil.conf.template.ECore
import net.gradleutil.conf.template.EPackage
import net.gradleutil.gen.Generator

import java.nio.file.Path

class GroovyDSLTemplate {


    GroovyDSLTemplate() {
    }

    static String render(EPackage ePackage) {
        TemplateOutput output = new StringOutput()
        Generator.getTemplateEngine().tap { setTrimControlStructures(true) }.render("groovydsl/GroovyDSL.jte", ePackage, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }
}