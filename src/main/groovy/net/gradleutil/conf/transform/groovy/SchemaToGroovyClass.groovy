package net.gradleutil.conf.transform.groovy

import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.gen.groovyclass.GroovyClassTemplate

import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema

class SchemaToGroovyClass extends Transformer {

    static boolean schemaToSimpleGroovyClass(TransformOptions options) throws IOException {

        EPackage ePackage = SchemaToEPackage.getEPackage(getSchema(options.jsonSchema), options.rootClassName, options.packageName, options.convertToCamelCase)

        String source
        if (options.jteDirectory) {
            source = GroovyClassTemplate.render(options.jteDirectory, ePackage, options)
        } else {
            source = GroovyClassTemplate.render(ePackage, options)
        }

        FileWriter fileWriter = new FileWriter(options.outputFile)
        fileWriter.write(source)
        fileWriter.close()
        return true
    }

    static boolean schemaToSimpleGroovyClass(String jsonSchema, String packageName, String rootClassName, File outputFile) throws IOException {
        def options = defaultOptions().jsonSchema(jsonSchema).packageName(packageName).rootClassName(rootClassName).outputFile(outputFile)
        schemaToSimpleGroovyClass(options)
    }

    static List<File> schemaToSimpleGroovyClass(File schemaDirectory, File outputDirectory, String packageName) throws IOException {
        List<File> generatedFiles = []
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        schemaDirectory.listFiles().each {
            String jsonSchema = it.text
            String rootClassName = it.name.replace('.schema', '').replace('.json', '')
            def outputPackageDir = new File(outputDirectory.path + '/' + rootClassName.toLowerCase()).tap { it.mkdir() }
            def outputFile = new File(outputPackageDir, rootClassName + '.groovy')
            schemaToSimpleGroovyClass(jsonSchema, packageName, rootClassName, outputFile)
        }
        return generatedFiles
    }


}
