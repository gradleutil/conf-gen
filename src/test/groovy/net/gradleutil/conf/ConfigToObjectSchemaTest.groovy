package net.gradleutil.conf

import static net.gradleutil.conf.util.GenUtil.confToReferenceSchemaJson
import static net.gradleutil.conf.util.GenUtil.confToSchemaJson
import static net.gradleutil.conf.util.GenUtil.configFileToReferenceSchemaJson
import static net.gradleutil.conf.util.GenUtil.configFileToSchemaJson

class ConfigToObjectSchemaTest extends AbstractTest {

    def "test create all"() {
        setup:
        def objName = data.name.replace('.json', '')
        def jsonSchema = configFileToSchemaJson(data)
        def jsonRefSchema = configFileToReferenceSchemaJson(data, objName)

        when:
        def schemaFile = new File(base + objName + '.schema.json')
        schemaFile.text = jsonSchema.toString()
        def refSchemaFile = new File(base + objName + '.ref.schema.json')
        refSchemaFile.text = jsonRefSchema.toString()
        println('json file:///' + data.absolutePath)
        println('ref file:///' + refSchemaFile.absolutePath)
        println('parsing file:///' + schemaFile.absolutePath)
        println()

        then:
        true

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll {
            !it.directory && !it.name.endsWith('schema.json')
        }.tap { addAll(new File('src/testFixtures/resources/conf/').listFiles()) }
    }

    def "test manytyped to reference"() {
        setup:
        def objName = 'manytyped'
        def data = getResourceText("conf/${objName}.conf")
        def jsonSchema = confToSchemaJson(data, 'manytyped')
        def jsonRefSchema = confToReferenceSchemaJson(data, objName)

        when:
        def schemaFile = new File(base + objName + '.schema.json')
        schemaFile.text = jsonSchema.toString()
        def refSchemaFile = new File(base + objName + '.ref.schema.json')
        refSchemaFile.text = jsonRefSchema.toString()
        println('json file:///' + objName)
        println('ref file:///' + refSchemaFile.absolutePath)
        println('parsing file:///' + schemaFile.absolutePath)
        println()

        then:
        true

    }

    def "test propertyFile to reference"() {
        setup:
        def objName = 'conf'
        def data = getResourceText("propertyfile/${objName}.properties")
        def jsonSchema = confToSchemaJson(data, objName)
        def jsonRefSchema = confToReferenceSchemaJson(data, objName)

        when:
        def schemaFile = new File(base + objName + '.schema.json')
        schemaFile.text = jsonSchema.toString()
        def refSchemaFile = new File(base + objName + '.ref.schema.json')
        refSchemaFile.text = jsonRefSchema.toString()
        println('json file:///' + objName)
        println('ref file:///' + refSchemaFile.absolutePath)
        println('parsing file:///' + schemaFile.absolutePath)
        println()

        then:
        true

    }

    def "test booklist to reference"() {
        setup:
        def objName = 'booklist'
        def data = getResourceText("json/${objName}.json")
        def jsonSchema = confToSchemaJson(data, objName)
        def jsonRefSchema = confToReferenceSchemaJson(data, objName)

        when:
        def schemaFile = new File(base + objName + '.schema.json')
        schemaFile.text = jsonSchema.toString()
        def refSchemaFile = new File(base + objName + '.ref.schema.json')
        refSchemaFile.text = jsonRefSchema.toString()
        println('json file:///' + objName)
        println('ref file:///' + refSchemaFile.absolutePath)
        println('parsing file:///' + schemaFile.absolutePath)
        println()

        then:
        true

    }

}
