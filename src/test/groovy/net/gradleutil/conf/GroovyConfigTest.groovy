package net.gradleutil.conf

import net.gradleutil.conf.transform.groovy.GroovyConfig
import net.gradleutil.conf.transform.json.JsonToSchema
import groovy.util.logging.Log
import net.gradleutil.conf.dna.common.text.Inflector

@Log
class GroovyConfigTest extends AbstractTest {

    def "minecraft schema to groovyDSL"() {
        setup:
        def jsonSchema = getResourceText('json/multiple/MinecraftConfig.schema.json')

        when:
        def modelFile = new File(base + 'Minecraft.groovy')
        def result = GroovyConfig.toGroovyDsl(JsonToSchema.getSchema(jsonSchema, true), 'MinecraftConfig', packageName)
        modelFile.text = result
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "schema to groovyDSL"() {
        setup:
        def jsonSchemaDir = File.createTempDir()
        extractFiles('json/', jsonSchemaDir)
        def jsonSchemas = new File(jsonSchemaDir, 'json').listFiles().findAll { it.name.endsWith('schema.json') }
        def modelFiles = []
        def inflector = new Inflector()
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)

        when:
        jsonSchemas.each { jsonSchema ->
            def name = inflector.upperCamelCase(jsonSchema.name.replace('.schema.json', ''), '. -_'.chars)
            def modelFile = new File(base + "/${name.toLowerCase()}/" + name + '.groovy')
            def pack = packageName + '.' + name.toLowerCase()
            def result = GroovyConfig.toGroovyDsl(JsonToSchema.getSchema(jsonSchema.text, true, name), name, pack)
            modelFile.parentFile.mkdirs()
            modelFile.text = result
            println "file://${modelFile.absolutePath}"
            println "file://${jsonSchema.absolutePath}"
            try {
                gcl.parseClass(modelFile).classLoader.loadClass(pack + '.' + name)
            } catch (Exception e) {
                log.severe(modelFile.path + " failed " + e.message.take(200))
            }
            println ""
            modelFiles.add(modelFile)
        }

        then:
        jsonSchemas.size() == modelFiles.size()
    }


}
