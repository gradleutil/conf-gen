package net.gradleutil.conf.transform.groovy

import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.gen.groovyclass.EPackageTemplate
import net.gradleutil.gen.groovyclass.GroovyClassTemplate

import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema

class EPackageRenderer extends Transformer {

    static boolean schemaToEPackageRender(TransformOptions options) throws IOException {

        EPackage ePackage = SchemaToEPackage.getEPackage(getSchema(options.jsonSchema), options.rootClassName, options.packageName, options.convertToCamelCase)
        options.ePackage = ePackage
        options.renderParams.put('ePackage',ePackage)

        EPackageTemplate.render(options).each {fileName, fileContent ->
            File file = new File(options.outputFile.path + File.separator + fileName)
            file.parentFile.mkdirs()
            FileWriter fileWriter = new FileWriter(file)
            fileWriter.write(fileContent)
            fileWriter.close()
        }

        return true
    }

    static boolean schemaToEPackageRender(String jsonSchema, String packageName, String rootClassName, File outputFile) throws IOException {
        def options = defaultOptions().jsonSchema(jsonSchema).packageName(packageName).rootClassName(rootClassName).outputFile(outputFile)
        schemaToEPackageRender(options)
    }

    static List<File> schemaToEPackageRender(File schemaDirectory, File outputDirectory, String packageName) throws IOException {
        List<File> generatedFiles = []
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        schemaDirectory.listFiles().each {
            String jsonSchema = it.text
            String rootClassName = it.name.replace('.schema', '').replace('.json', '')
            def outputPackageDir = new File(outputDirectory.path + '/' + rootClassName.toLowerCase()).tap { it.mkdir() }
            def outputFile = new File(outputPackageDir, rootClassName + '.groovy')
            schemaToEPackageRender(jsonSchema, packageName, rootClassName, outputFile)
        }
        return generatedFiles
    }


}
