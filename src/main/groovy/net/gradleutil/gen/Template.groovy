package net.gradleutil.gen

import gg.jte.TemplateEngine
import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import groovy.util.logging.Log
import net.gradleutil.conf.template.EClassifier
import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer

@Log(value = "logger")
class Template {

    static String render(TransformOptions options) {
        TemplateOutput output = new StringOutput()
        def params = options.renderParams
        options.renderParams.put('options', options)
        getTemplateEngine(options)
                .render(options.jteRenderPath, params, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static String render(EClassifier eClassifier, TransformOptions options) {
        TemplateOutput output = new StringOutput()
        def params = [eClass:eClassifier] << options.renderParams
        getTemplateEngine(options).render(options.jteRenderPath, params, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static TemplateEngine getTemplateEngine(TransformOptions options) {
        TemplateEngine engine
        if (options.jteDirectory) {
            engine = Generator.getTemplateEngine(Generator.getJar(options.ePackage, options.jteDirectory).toPath())
        } else {
            engine = Generator.getTemplateEngine()
        }
        engine.setTrimControlStructures(true)
        return engine
    }

    static String jteJarRender(File target, TransformOptions options) {
        TemplateOutput output = new StringOutput()
        logger.info('Rendering ePackage ' + options.ePackage.name + ' with ' + options.jteRenderPath)
        Generator.getTemplateEngine(target.toPath())
                .tap { setTrimControlStructures(true) }
                .render(options.jteRenderPath, options.renderParams, output)
        output.toString().replaceAll(/\n\s+\n/, '\n\n')
    }

    static Map<String, String> renderEpackage(TransformOptions options = Transformer.transformOptions()) {
        File jteDir = options.jteDirectory
        EPackage ePackage = options.ePackage
        assert ePackage
        logger.info('loading ePackage ' + ePackage.name + ' from ' + jteDir.absoluteFile.toString())
        Map<String, String> files = [:]
        File jteJar = Generator.getJar(ePackage, jteDir)

        jteDir.listFiles().each { file -> rend(file, jteJar, files, options) }
        files
    }

    static void rend(File file, File jteJar, Map<String, String> files, TransformOptions options) {
        String ext = options.toType == TransformOptions.Type.groovy ? '.groovy' : '.java'
        if (file.isDirectory()) {
            if (file.name == 'eClass') {
                file.listFiles().each { eClassTemplate ->
                    options.jteRenderPath = file.name + File.separator + eClassTemplate.name
                    options.ePackage.eClassifiers.each {
                        options.renderParams.eClass = it
                        def path = it.name.toLowerCase() + File.separator + it.name + eClassTemplate.name.replace('.jte', ext)
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
            files.put file.name.replace('.jte', ext), jteJarRender(jteJar, options)
        }
    }


}
