package net.gradleutil.conf.transform.schema

import groovy.util.logging.Log
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader
import net.gradleutil.conf.ConfLoaderTest
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.conf.util.Inflector

import static net.gradleutil.conf.transform.TransformOptions.Type.cli

@Log
class SchemaToCliTest extends AbstractTest {

    def "epackage rendered cli"() {
        setup:
        def jsonSchema = getResourceText('json/booklist.objectschema.json')
        def rootClassName = 'Booklist'
        def renderDir = new File(base, 'render').tap { it.mkdirs() }

        when:
        def options = Transformer.transformOptions()
                .jsonSchema(jsonSchema)
                .toType(cli)
                .packageName(packageName)
                .rootClassName(rootClassName)
                .outputFile(renderDir)
        Transformer.transform(options)


        then:
        renderDir.listFiles().each {
            println 'file://' + it.absolutePath
        }
    }


    def "test create all cli"() {
        setup:
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def schemaFile = data.parentFile.listFiles().find { it.name.replace('.json', '') == data.name.replace('.json', '') + '.schema' }
        def jsonSchema
        if (schemaFile) {
            jsonSchema = schemaFile.text
        } else {
            jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)
        }

        when:
        def modelFile = new File(base + refName).tap { it.mkdir() }
        println('json file:///' + data.absolutePath)
        def pkg = packageName + '.' + refName.capitalize()
        println pkg
        Transformer.transform(jsonSchema, pkg, refName.capitalize(), "src/testFixtures/resources/json/", modelFile, cli)
        def gcl = new GroovyClassLoader(ConfLoaderTest.classLoader)

        compileTarget(modelFile)

        gcl.addClasspath(modelFile.absolutePath)
        modelFile.listFiles().findAll { it.name.endsWith('class') }.each {
            byte[] classData = it.bytes
            def name = it.name.replace('.class', '')
            def classn = pkg + '.' + name
            println classn
            gcl.defineClass(classn, classData)
        }
        modelFile.listFiles().findAll { it.name.endsWith('java') }.each {
            println('parsing file:///' + it.absolutePath)
            gcl.parseClass(it)
        }

        def modelClass = gcl.loadClass(pkg + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll { !it.directory && !it.name.endsWith('schema.json') }
    }

}
