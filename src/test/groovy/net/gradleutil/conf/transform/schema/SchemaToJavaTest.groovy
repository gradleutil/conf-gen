package net.gradleutil.conf.transform.schema

import groovy.util.logging.Log
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.transform.Transformer

import static net.gradleutil.conf.transform.java.EPackageRenderer.schemaToJavaRender

@Log
class SchemaToJavaTest extends AbstractTest {

    def "epackage rendered java"() {
        setup:
        def jteDir = new File('src/testFixtures/resources/jte/epackage')
        def jsonSchema = getResourceText('json/AllOfTest.schema.json')
        def rootClassName = 'Booklist'
        def renderDir = new File(base, 'render').tap { it.mkdirs() }

        when:
        def options = Transformer.transformOptions()
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .rootClassName(rootClassName)
                .outputFile(renderDir)
        def ePackage = schemaToJavaRender(options)

        then:
        ePackage
        renderDir.listFiles().each {
            println 'file://' + it.absolutePath
        }
    }

}
