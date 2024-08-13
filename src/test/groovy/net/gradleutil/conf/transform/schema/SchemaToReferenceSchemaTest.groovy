package net.gradleutil.conf.transform.schema

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil

class SchemaToReferenceSchemaTest extends AbstractTest {
    def "plugin schema"() {
        setup:
        def confFile = getResourceText('mhf/gradleplugin/plugin.mhf')
        def rootClassName = 'Plugin'
        def jsonRefSchema = GenUtil.confToReferenceSchemaFile(confFile, rootClassName, new File(base, 'refschema.json'))

        when:
        println "file://${jsonRefSchema.absolutePath}"

        then:
        jsonRefSchema.exists()
    }
    
}
