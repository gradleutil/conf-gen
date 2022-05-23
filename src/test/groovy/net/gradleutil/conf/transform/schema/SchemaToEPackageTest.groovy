package net.gradleutil.conf.transform.schema

import groovy.util.logging.Log
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.transform.groovy.SchemaToGroovyClass
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.gen.groovyclass.GroovyClassTemplate

import static net.gradleutil.conf.transform.json.JsonToSchema.getSchema
import static SchemaToEPackage.getEPackage

@Log
class SchemaToEPackageTest extends AbstractTest {

    def "simple array object"() {
        setup:
        def jsonSchema = getResourceText('json/booklist.schema.json')
        def rootClassName = 'Booklist'
        def convertToCamelCase = true

        when:
        def ePackage = getEPackage(getSchema(jsonSchema), rootClassName, packageName, convertToCamelCase)
        String source = GroovyClassTemplate.render( ePackage)
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 2
        ePackage.getEClassifiers()*.name.intersect(['Booklist', 'Book']).size() == 2

    }

    def "simple allOf"() {
        setup:
        def jsonSchema = getResourceText('json/AllOfTest.schema.json')
        def rootClassName = 'Booklist'

        when:
        def ePackage = getEPackage(getSchema(jsonSchema), rootClassName, packageName, true)
        String source = GroovyClassTemplate.render(ePackage)
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 2
        ePackage.getEClassifiers()*.name.intersect(['AllOfTest', 'EModelElement']).size() == 2

    }

    def "ecore"() {
        setup:
        def jsonSchema = new File('template/src/main/groovy/net/gradleutil/conf/schema/Ecore.schema.json')
        def rootClassName = 'Booklist'
        def convertToCamelCase = true

        when:
        def ePackage = getEPackage(getSchema(jsonSchema), rootClassName, packageName, convertToCamelCase)
        println "file://${jsonSchema.absolutePath}"
        String source = GroovyClassTemplate.render(ePackage)
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 20
//        ePackage.getEClassifiers()*.name.intersect(['Booklist','Books']).size() == 2

    }

    def "gradle plugin"() {
        setup:
        def confFile = getResourceText('mhf/gradleplugin/plugin.mhf')
        def rootClassName = 'Plugin'
        def jsonSchema = GenUtil.confToReferenceSchemaFile(confFile, rootClassName, new File(base, 'objectschema.json'))
        def convertToCamelCase = true
        println "file://${jsonSchema.absolutePath}"

        when:
        def ePackage = getEPackage(getSchema(jsonSchema), rootClassName, packageName, convertToCamelCase)
        def classNames = ['Plugin', 'Task', 'PluginProperty']

        then:
        ePackage.eClassifiers.size() == classNames.size()
        ePackage.eClassifiers*.name.intersect(classNames).size() == classNames.size()
        def modelFile = new File(base + rootClassName + '.groovy')
        println ePackage
        SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema.text, packageName, rootClassName, modelFile)
        println modelFile.text
    }

}
