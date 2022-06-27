package net.gradleutil.gen;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import groovy.util.logging.Log;

@Log(value = "logger")
class Template {

    static TemplateEngine getTemplateEngine(Generator.GeneratorOptions options) {
        return TemplateEngine.createPrecompiled(Generator.getJarPath(Generator.class), ContentType.Plain, options.getClassLoader(), options.getPackageName());
    }


}
