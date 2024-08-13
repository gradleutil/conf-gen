package net.gradleutil.conf.transform

import com.networknt.schema.JsonSchema
import net.gradleutil.conf.json.JSONArray
import net.gradleutil.conf.json.JSONObject
import net.gradleutil.conf.json.schema.SchemaUtil
import net.gradleutil.conf.template.EPackage
import net.gradleutil.gen.Template

import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema
import static net.gradleutil.conf.transform.schema.SchemaToEPackage.getEPackage

class Transformer {

    Transformer() {}

    static TransformOptions transformOptions() {
        new TransformOptions()
                .convertToCamelCase(true)
                .basePath("")
                .jteRenderPath('groovyclass/GroovyGen.jte')
                .singleFile(false)
                .toType(TransformOptions.Type.groovy)
    }

    static boolean transform(TransformOptions options) throws IOException {

        options.renderParams = options.renderParams ?: [options: options]

        JsonSchema schema = getSchema(options.jsonSchema, options.basePath ?: "")
        schema.walk(null, false)
        EPackage ePackage = getEPackage(schema, options.rootClassName, options.packageName, options.convertToCamelCase)
        options.ePackage = ePackage

        if (options.outputFile?.isDirectory()) {
            String extension = options.toType == TransformOptions.Type.groovy ? '.groovy' : '.java'
            if (options.toType == TransformOptions.Type.java) {
                options.jteRenderPath('javaclass/EJavaClass.jte')
            } else if (options.toType == TransformOptions.Type.jpa) {
                options.jteRenderPath('jpa/JPAClass.jte')
            } else if (options.toType == TransformOptions.Type.cli) {
                options.jteRenderPath('cli/ECLIClass.jte')
            } else {
                options.jteRenderPath('groovyclass/EGroovyClass.jte')
            }
            ePackage.eClassifiers.each {
                // skip any external package refs as they will be rendered in their own file
                if (!it.name.contains("\\.")) {
                    def txt = Template.render(it, options)
                    new File(options.outputFile.absolutePath + '/' + it.name + extension).text = txt
                } else {
                    println "Skipping ${it.name}"
                }
            }
        } else {
            String source
            source = Template.render(options)
            FileWriter fileWriter = new FileWriter(options.outputFile)
            fileWriter.write(source)
            fileWriter.close()
        }
        return true
    }

    static boolean transform(String jsonSchema, String packageName, String rootClassName, String basePath, File outputFile, TransformOptions.Type toType = TransformOptions.Type.java, Boolean isSingleFile = true) throws IOException {
        def options = transformOptions()
                .jsonSchema(jsonSchema)
                .packageName(packageName)
                .rootClassName(rootClassName)
                .basePath(basePath)
                .toType(toType)
                .singleFile(isSingleFile)
                .outputFile(outputFile)
        transform(options)
    }

    static List<File> transform(File schemaDirectory, File outputDirectory, String packageName) throws IOException {
        List<File> generatedFiles = []
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        schemaDirectory.listFiles().each {
            String jsonSchema = it.text
            String rootClassName = it.name.replace('.schema', '').replace('.json', '')
            def outputPackageDir = new File(outputDirectory.path + '/' + rootClassName.toLowerCase()).tap { it.mkdir() }
            def outputFile = new File(outputPackageDir, rootClassName + '.groovy')
            transform(jsonSchema, packageName, rootClassName, schemaDirectory.absolutePath, outputFile)
        }
        return generatedFiles
    }

    static void editor(File path, JsonSchema schema, String json) {
        path.text = SchemaUtil.editor(schema.toString(), json)
    }

    static Map<String, Object> toMap(JSONObject jsonObject, List<String> definitionTypes = []) {
        Map<String, Object> map = new HashMap<String, Object>()
        Iterator<String> keys = jsonObject.keys()
        def key, value
        while (keys.hasNext()) {
            key = keys.next()
            value = jsonObject.get(key)
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value, definitionTypes)
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value, definitionTypes)
            } else if (value instanceof String && value.startsWith('#') && value.length() > 1) {
                def definition = value.toString().replace('#/definitions/', '')
                if (!definitionTypes.find { it == definition }) {
                    def closeMatch = definitionTypes.findAll { it.contains(definition) }.sort { it.length() }.find()
                    if (closeMatch) {
                        value = "#/definitions/" + closeMatch
                    } else {
                        key = 'type'
                        value = 'object'
                    }
                }
            }
            map.put(key, value)
        }
        return map
    }

    static List<Object> toList(JSONArray array, List<String> definitionTypes = []) {
        List<Object> list = new ArrayList<Object>()
        def value
        for (int i = 0; i < array.length(); i++) {
            value = array.get(i)
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value, definitionTypes)
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value, definitionTypes)
            }
            list.add(value)
        }
        return list
    }


}
