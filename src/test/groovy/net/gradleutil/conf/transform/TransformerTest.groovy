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


}
