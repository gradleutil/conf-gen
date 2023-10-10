package net.gradleutil.conf

import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil

class ConfigSchemaTest extends AbstractTest {

    def "json config to schema"() {
        setup:
        def configFile = getResourceText('json/royalty.json')
        def config = Loader.resolveStringWithSystem(configFile)
        assert config

        when:
        def modelFile = new File(base + 'Royalty.schema.json')
        def dataFile = new File(base + 'Royalty.json')
        dataFile.text = ConfUtil.configToJson(config)
        GenUtil.confToSchemaFile(configFile, modelFile)
        println "file://${modelFile.absolutePath}"
        println "file://${dataFile.absolutePath}"

        then:
        modelFile.exists()
    }


    def "json booklist to schema"() {
        setup:
        def configFile = getResourceText('json/booklist.json')
        def config = Loader.resolveWithSystem(configFile)
        assert config

        when:
        def modelFile = new File(base + 'Booklist.schema.json')
        def dataFile = new File(base + 'Booklist.json')
        dataFile.text = ConfUtil.configToJson(config)
        modelFile.text = GenUtil.confToReferenceSchemaJson(configFile, 'Booklist')
        println "file://${modelFile.absolutePath}"
        println "file://${dataFile.absolutePath}"

        then:
        modelFile.exists()
    }

    def "produce json to schema"() {
        setup:
        def configFile = getResourceText('json/produce.json')
        def config = Loader.resolveWithSystem(configFile)
        assert config

        when:
        def modelFile = new File(base + 'Produce.schema.json')
        def dataFile = new File(base + 'Produce.json')
        dataFile.text = ConfUtil.configToJson(config)
        GenUtil.confToSchemaFile(configFile, modelFile)
        println "file://${modelFile.absolutePath}"
        println "file://${dataFile.absolutePath}"

        then:
        modelFile.exists()
    }

    def "gradle plugin mht to schema"() {
        setup:
        def configFile = getResourceText('mhf/gradleplugin/plugin.mhf')
        def config = Loader.resolveWithSystem(configFile)
        assert config
        println config

        when:
        def modelFile = new File(base + 'GradlePlugin.schema.json')
        def dataFile = new File(base + 'GradlePlugin.json')
        dataFile.text = ConfUtil.configToJson(config)
        modelFile.text = GenUtil.confToReferenceSchemaJson(configFile, 'plugin')
        println "file://${modelFile.absolutePath}"
        println "file://${dataFile.absolutePath}"

        then:
        modelFile.exists()
    }

    def "one string conf to schema"() {
        setup:
        def configFile = getResourceText('conf/onestring.conf')
        def config = Loader.resolveWithSystem(configFile)
        assert config

        when:
        def modelFile = new File(base + 'One.schema.json')
        def dataFile = new File(base + 'One.json')
        dataFile.text = ConfUtil.configToJson(config)
        GenUtil.confToSchemaFile(configFile, modelFile)
        println "file://${modelFile.absolutePath}"
        println "file://${dataFile.absolutePath}"

        then:
        modelFile.exists()
    }

    def "produce configFile to schema json"() {
        setup:
        def jsonSchema = GenUtil.confToSchemaJson(getResourceText('json/produce.json'))
        println(jsonSchema)

        when:
        def modelFile = new File(base + 'Produce.groovy')
        def result = Transformer.transform(jsonSchema, packageName, 'Produce', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }


}
