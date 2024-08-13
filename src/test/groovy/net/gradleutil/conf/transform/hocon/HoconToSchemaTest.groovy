package net.gradleutil.conf.transform.hocon

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil

class HoconToSchemaTest extends AbstractTest {


    def "booklist"() {
        setup:
        def confFile = getResourceText('json/booklist.json')

        when:
        def jsonSchema = GenUtil.confToSchemaFile(confFile, 'booklist', new File(base, 'objectschema.json'))
        def originalSchema = getResourceText('json/booklist.objectschema.json')
        new File(base, 'objectschema.og.json').tap{
            text = originalSchema
            println "file://${it.absolutePath}"
        }
        println "file://${jsonSchema.absolutePath}"

        then:
        jsonSchema.text.trim() == originalSchema.trim()
    }


    def "veggie conf"() {
        setup:
        def confFile = getResourceText('json/produce.json')

        when:
        def jsonSchema = GenUtil.confToSchemaFile(confFile, 'Produce', new File(base, 'objectschema.json'))
        println "file://${jsonSchema.absolutePath}"

        then:
        jsonSchema.text.trim()
    }

    def "server conf"() {
        setup:
        def confFile = getResourceText('json/server.json')

        when:
        def jsonSchema = GenUtil.confToReferenceSchemaFile(confFile, 'server', new File(base, 'objectschema.json'))
        println "file://${jsonSchema.absolutePath}"

        then:
        jsonSchema.text.trim()
    }

}