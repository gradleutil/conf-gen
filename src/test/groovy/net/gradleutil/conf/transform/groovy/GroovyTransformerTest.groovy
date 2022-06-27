package net.gradleutil.conf.transform.groovy

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader
import net.gradleutil.conf.LoaderTest
import net.gradleutil.conf.json.schema.SchemaUtil
import net.gradleutil.conf.template.EClass
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.conf.util.Inflector
import net.gradleutil.conf.util.GenUtil

class GroovyTransformerTest extends AbstractTest {

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

    def "multiple schema"() {
        setup:
        def jsonSchemaDir = File.createTempDir()
        extractFiles('json/multiple', jsonSchemaDir)

        when:
        def modelFile = new File(base + '/json/multiple')
        SchemaToGroovyClass.schemaToSimpleGroovyClass(new File(jsonSchemaDir, 'json/multiple'), modelFile, packageName)
        println "file://${modelFile.absolutePath}"

        then:
        modelFile.listFiles().size() > 0
    }

    def "ref schema"() {
        setup:
        def jsonSchema = GroovyTransformerTest.classLoader.getResource('json/ref.schema.json')

        when:
        def modelFile = new File(base + 'Family.groovy')
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema.text, packageName, 'Ref', modelFile)
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
        extractFiles('json/produce.json', baseDir)

        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(new File(base + 'json/produce.json'), refName)
        def jsonSchemaFile = new File(base + 'produce.schema.json').tap { text = jsonSchema }
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

    def "booklist schema"() {
        setup:
        def data = new File('src/testFixtures/resources/json/booklist.json')
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)

        when:
        def modelFile = new File(base + refName + '.groovy')
        def schemaFile = new File(base + refName + '.schema.json').tap { it.text = jsonSchema }
        println('json file:///' + data.absolutePath)
        println('schema file:///' + schemaFile.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.defaultOptions().silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()
    }

    def "minecraft schema load"() {
        setup:
        def jsonSchema = getResourceText('json/MCConfig.schema.json')

        when:
        def modelFile = new File(base + 'MinecraftConfig.groovy')
        SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, 'MinecraftConfig', modelFile)
        println "file://${modelFile.absolutePath}"

        def lib = SchemaToEPackage.getEPackage(SchemaUtil.getSchema(jsonSchema), "MCConfig", "net.gradle", false)
        def mod = (lib.eClassifiers.find { it.name == 'ModArtifact' } as EClass).eStructuralFeatures.find { it.name == 'minecraftVersion' }
        def mcv = (lib.eClassifiers.find { it.name == 'MinecraftConfig' } as EClass).eStructuralFeatures.find { it.name == 'minecraftVersion' }

        then:
        mod != null
        mcv == null
    }

    def "test create all"() {
        setup:
        def inflector = new Inflector()
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


    def "test dsl"() {
        setup:
        def jsonSchema = getResourceText('json/booklist.schema.json')

        when:
        def modelFile = new File(base + 'BooklistDSL.groovy')
        def result = SchemaToGroovyDSL.schemaToGroovyDSL(jsonSchema, packageName, 'Booklist', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result

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
