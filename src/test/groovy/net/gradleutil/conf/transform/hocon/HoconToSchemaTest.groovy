package net.gradleutil.conf.transform.hocon

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil

class HoconToSchemaTest extends AbstractTest {


    def "booklist"() {
        setup:
        def confFile = getResourceText('json/booklist.json')

        when:
        def jsonSchema = GenUtil.confToSchemaFile(confFile, new File(base, 'objectschema.json'))
        println "file://${jsonSchema.absolutePath}"

        then:
        jsonSchema.text.trim() == getResourceText('json/booklist.objectschema.json').trim()
    }


    def "veggie conf"() {
        setup:
        def confFile = getResourceText('json/produce.json')

        when:
        def jsonSchema = GenUtil.confToSchemaFile(confFile, new File(base, 'objectschema.json'))
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