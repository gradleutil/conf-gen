package net.gradleutil.conf.transform

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader
import net.gradleutil.conf.LoaderTest
import net.gradleutil.conf.transform.groovy.SchemaToGroovyClass
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.conf.util.Inflector

class TransformerTest extends AbstractTest {

    def "json schema"() {
        setup:
        def jsonSchema = getResourceText('json/json-schema.json')
/*
        if (!jsonSchema.exists()) {
            jsonSchema.text = new URL('http://json-schema.org/draft-07/schema#').text
        }
*/


        when:
        def modelFile = new File(base + 'JsonSchema.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'JsonSchema', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "royalty schema"() {
        setup:
        def jsonSchema = getResourceText('json/royalty.schema.json')

        when:
        def modelFile = new File(base + 'Royalty.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'Royalty', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "ecore schema"() {
        setup:
        def jsonSchema = getResourceText('conf/schema/Ecore.schema.json')

        when:
        def modelFile = new File(base + 'Ecore.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'EPackage', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "produce singular"() {
        setup:
        def refName = 'Produce'
        def jsonSchema = GenUtil.confToReferenceSchemaJson(getResourceText('json/produce.json'), refName)
        def jsonSchemaFile = new File(base + 'produce.schema.json').tap{text = jsonSchema}
        println "file://${jsonSchemaFile.absolutePath}"

        when:
        def modelFile = new File(base + 'Produce.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'Produce', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "produce schema"() {
        setup:
        def jsonSchema = getResourceText('json/produce.schema.json')

        when:
        def modelFile = new File(base + 'Produce.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'Produce', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "minecraft schema"() {
        setup:
        def jsonSchema = getResourceText('json/multiple/MinecraftConfig.schema.json')

        when:
        def modelFile = new File(base + 'Minecraft.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'MinecraftConfig', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "test create all"() {
        setup:
        def inflector = Inflector.instance
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)

        when:
        def modelFile = new File(base + refName + '.groovy')
        println('json file:///' + data.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.defaultOptions().silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll { !it.directory && !it.name.endsWith('schema.json') }
    }


/*
    def "test dsl"() {
        setup:
        def configModelFile = new File('src/test/groovy/net/gradleutil/generated/JavaClass.groovy')
        def gen = new Gen()

        when:
        def result = net.gradleutil.generated.DSL.javaClass{
            name = 'fart'
        }

        then:
        result == true
    }
*/
}
