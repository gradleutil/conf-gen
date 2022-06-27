package net.gradleutil.conf.transform.groovy


import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.gen.groovydsl.GroovyDSLTemplate

import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema

class SchemaToGroovyDSL extends Transformer {

    static boolean schemaToGroovyDSL(TransformOptions options) throws IOException {

        EPackage ePackage = SchemaToEPackage.getEPackage(getSchema(options.jsonSchema), options.rootClassName, options.packageName, options.convertToCamelCase)
        String source = GroovyDSLTemplate.render(ePackage)

        FileWriter fileWriter = new FileWriter(options.outputFile)
        fileWriter.write(source.replace('&#34;','$'))
        fileWriter.close()
        return true
    }

    static boolean schemaToGroovyDSL(String jsonSchema, String packageName, String rootClassName, File outputFile) throws IOException {
        def options = defaultOptions().jsonSchema(jsonSchema).packageName(packageName).rootClassName(rootClassName).outputFile(outputFile)
        schemaToGroovyDSL(options)
    }

}
