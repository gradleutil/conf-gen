package net.gradleutil.conf.transform.java

import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.template.EStructuralFeature
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.gen.Template

import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema

class EPackageRenderer extends Transformer {

    static boolean schemaToJavaRender(TransformOptions options) throws IOException {

        EPackage ePackage = SchemaToEPackage.getEPackage(getSchema(options.jsonSchema), options.rootClassName, options.packageName, options.convertToCamelCase)
        options.ePackage = ePackage
        options.renderParams.put('ePackage', ePackage)
        options.toType(TransformOptions.Type.java)

        Template.renderEpackage(options).each { fileName, fileContent ->
            File file = new File(options.outputFile.path + File.separator + fileName)
            file.parentFile.mkdirs()
            FileWriter fileWriter = new FileWriter(file)
            fileWriter.write(fileContent)
            fileWriter.close()
        }

        return true
    }

    static boolean schemaToJavaRender(String jsonSchema, String packageName, String rootClassName, File outputFile) throws IOException {
        def options = transformOptions().jsonSchema(jsonSchema).packageName(packageName).rootClassName(rootClassName).outputFile(outputFile)
        schemaToJavaRender(options)
    }

    static List<File> schemaToJavaRender(File schemaDirectory, File outputDirectory, String packageName) throws IOException {
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
            schemaToJavaRender(jsonSchema, packageName, rootClassName, outputFile)
        }
        return generatedFiles
    }
    
    static String javaType(EStructuralFeature prop, indent = 4) {
        StringBuilder sb = new StringBuilder()
        String lf = '\n' + (' ' * indent)
        if (prop.eType.equalsIgnoreCase("enum")) {
            String enumClass = prop.name.substring(0, 1).toUpperCase() + prop.name.substring(1)
            sb.append("${enumClass} ${prop.name};" + lf)
            sb.append "public ${prop.eType.toLowerCase()} ${enumClass} ${prop.asEnum()}"
        } else if (prop.eType.equalsIgnoreCase("BigInteger")) {
            sb.append("java.math.BigInteger ${prop.name}")
        } else if (prop.upperBound > 1 || prop.upperBound == -1) {
            sb.append("List<${prop.eType}> ${prop.name}")
            if (prop.defaultValue != null) {
                sb.append "${prop.defaultValue}"
            } else {
                sb.append ""
            }
        } else {
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

    static String javaSetter(String className, EStructuralFeature prop, indent = 4) {
        StringBuilder sb = new StringBuilder()
        String lf = '\n' + (' ' * indent)
        if (prop.eType.equalsIgnoreCase("enum")) {
            String propCap = prop.name.substring(0, 1).toUpperCase() + prop.name.substring(1)
            sb.append("${className} ${prop.name}(${propCap} ${prop.name}){ this.${prop.name} = ${prop.name}; return this; }")
        } else if (prop.eType.equalsIgnoreCase("BigInteger")) {
            sb.append("${className} ${prop.name}(java.math.${prop.eType} ${prop.name}){ this.${prop.name} = ${prop.name}; return this; }")
        } else if (prop.upperBound > 1 || prop.upperBound == -1) {
            sb.append("${className} ${prop.name}(List<${prop.eType}> ${prop.name}){ this.${prop.name} = ${prop.name}; return this; }")
        } else {
            sb.append("${className} ${prop.name}(${prop.eType} ${prop.name}){ this.${prop.name} = ${prop.name}; return this; }")
        }
        sb.toString() + lf
    }


}
