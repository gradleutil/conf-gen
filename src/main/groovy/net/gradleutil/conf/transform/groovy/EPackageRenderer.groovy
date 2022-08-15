package net.gradleutil.conf.transform.groovy

import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.template.EStructuralFeature
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
        options.renderParams.put('ePackage', ePackage)

        EPackageTemplate.render(options).each { fileName, fileContent ->
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

    static String featureToClassProp(EStructuralFeature prop, indent = 4) {
        String optional = (prop.lowerBound == 0) ? "@Optional" : ""
        StringBuilder sb = new StringBuilder()
        String lf = '\n' + (' ' * indent)
        if (prop.eType.equalsIgnoreCase("enum")) {
            String propCap = prop.name.substring(0, 1).toUpperCase() + prop.name.substring(1)
            sb.append(optional + lf)
            sb.append "${propCap} ${prop.name}" + lf
            sb.append "${prop.eType.toLowerCase()} ${propCap} ${prop.asEnum()}"
        } else if (prop.upperBound > 1 || prop.upperBound == -1) {
            sb.append optional + lf
            sb.append "List<${prop.eType}> ${prop.name} = "
            if (prop.defaultValue != null) {
                sb.append "${prop.defaultValue}"
            } else {
                sb.append "[]"
            }
            sb.append " as List<${prop.eType}>"
        } else {
            sb.append optional + lf
            sb.append "${prop.eType} ${prop.name}"
            if (prop.defaultValue != null) {
                switch (prop.eType) {
                case 'String':
                    sb.append " = \"${prop.defaultValue}\""
                    break
                case 'Boolean':
                    sb.append " = ${Boolean.toString(prop.defaultValue as Boolean)}"
                    break
                default:
                    sb.append " = ${prop.defaultValue}"
                }
            }
        }
        sb.toString()
    }

}
