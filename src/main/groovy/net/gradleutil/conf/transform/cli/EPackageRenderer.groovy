package net.gradleutil.conf.transform.cli

import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.gen.Template

import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema

class EPackageRenderer extends Transformer {

    static boolean schemaToCLIRender(TransformOptions options) throws IOException {

        EPackage ePackage = SchemaToEPackage.getEPackage(getSchema(options.jsonSchema,""), options.rootClassName, options.packageName, options.convertToCamelCase)
        options.ePackage = ePackage
        options.renderParams.put('ePackage', ePackage)
        options.toType(TransformOptions.Type.cli)

        Template.renderEpackage(options).each { fileName, fileContent ->
            File file = new File(options.outputFile.path + File.separator + fileName)
            file.parentFile.mkdirs()
            FileWriter fileWriter = new FileWriter(file)
            fileWriter.write(fileContent)
            fileWriter.close()
        }

        return true
    }

    static boolean schemaToCLIRender(String jsonSchema, String packageName, String rootClassName, File outputFile) throws IOException {
        def options = transformOptions().jsonSchema(jsonSchema).packageName(packageName).rootClassName(rootClassName).outputFile(outputFile)
        schemaToCLIRender(options)
    }

    static List<File> schemaToCLIRender(File schemaDirectory, File outputDirectory, String packageName) throws IOException {
        List<File> generatedFiles = []
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        if(!schemaDirectory.exists()) {
            throw new FileNotFoundException("Schema directory does not exist: " + schemaDirectory.path)
        }
        schemaDirectory.listFiles().each {
            String jsonSchema = it.text
            String rootClassName = it.name.replace('.schema', '').replace('.json', '')
            def outputPackageDir = new File(outputDirectory.path + '/' + rootClassName.toLowerCase()).tap { it.mkdir() }
            def outputFile = new File(outputPackageDir, rootClassName + '.java')
            schemaToCLIRender(jsonSchema, packageName, rootClassName, outputFile)
        }
        return generatedFiles
    }
    

}
