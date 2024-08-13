package net.gradleutil.conf.transform

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader
import net.gradleutil.conf.util.ConfUtil
import spock.lang.Ignore

import static net.gradleutil.conf.Loader.loaderOptions


class TransformerTest extends AbstractTest {


    @Ignore
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
        def result = Transformer.transform(jsonSchema, packageName, 'JsonSchema', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "b rendered java"() {
        setup:
        def jteDir = new File('src/main/jte')
        def jsonSchema = getResourceText('json/Buildings.schema.json')
        def rootClassName = 'Buildings'
        def renderDir = new File(base, 'render').tap { it.mkdirs() }

        when:
        def options = Transformer.transformOptions()
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .convertToCamelCase(false)
                .packageName('Buildings')
                .jarJteDirectory(false)
                .toType(TransformOptions.Type.java)
                .rootClassName(rootClassName)
                .outputFile(renderDir)
        def ePackage = Transformer.transform(options)

        then:
        ePackage
        options.ePackage().eClassifiers.size() == 5
        renderDir.listFiles().each {
            println 'file://' + it.absolutePath
        }
    }

    def "b rendered jpa"() {
        setup:
        def jteDir = new File('src/main/jte')
        def jsonSchema = getResourceText('json/Buildings.schema.json')
        def rootClassName = 'Buildings'
        def renderDir = new File(base, 'render').tap { it.mkdirs() }

        when:
        def options = Transformer.transformOptions()
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .convertToCamelCase(false)
                .packageName('Buildings')
                .jarJteDirectory(false)
                .toType(TransformOptions.Type.jpa)
                .rootClassName(rootClassName)
                .outputFile(renderDir)
        def ePackage = Transformer.transform(options)

        then:
        ePackage
        options.ePackage().eClassifiers.size() == 5
        renderDir.listFiles().each {
            println 'file://' + it.absolutePath
        }
    }

    def "test beanToJson"() {
        setup:
        def jsonText = getResourceText('json/library.json')
        def conf = new File(base, 'config.conf').tap { text = jsonText }
        println "file:///${conf.absolutePath}"

        when:
        def config = Loader.load(conf)
        def library = Loader.create(config, Library, loaderOptions().silent(false))

        then:
        library.books.size() == 2
        def json = ConfUtil.beanToJson(library)
        json.startsWith('{"books":[{"ISBN":')

    }

    static class Library {
        String name
        List<Book> books
    }

    static class Book {
        String title
        String description
        Integer pages
        String ISBN
        List<Author> authors
    }

    static class Author {
        String firstName
        String lastName
    }

}
