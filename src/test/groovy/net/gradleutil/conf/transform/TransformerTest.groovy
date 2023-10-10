package net.gradleutil.conf.transform

import net.gradleutil.conf.AbstractTest


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
        def result = Transformer.transform(jsonSchema, packageName, 'JsonSchema', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }


}
