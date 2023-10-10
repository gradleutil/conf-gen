package net.gradleutil.conf

import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.Inflector
import net.gradleutil.conf.util.GenUtil

import static net.gradleutil.conf.Loader.loaderOptions

class LoaderTest extends AbstractTest {

    def "test create all"() {
        setup:
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def schemaFile = data.parentFile.listFiles().find{it.name.replace('.json','') == data.name.replace('.json','') + '.schema' }
        def jsonSchema
        if(schemaFile){
            jsonSchema = schemaFile.text
            println('schema file:///' + schemaFile.absolutePath)
        } else {
            jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)
        }

        when:
        def modelFile = new File(base + refName + '.groovy')
        println('json file:///' + data.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        Loader.invalidateCaches()
        Transformer.transform(jsonSchema, packageName, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true))

        then:
        funk

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll { !it.directory && !it.name.endsWith('schema.json') }
    }

    def "test create mc"() {
        setup:
        def resourceName = 'json/royalty.json'
        def data = getResourceText(resourceName)
        def refName = 'Royalty'
        def jsonSchema = new File('src/testFixtures/resources/json/royalty.schema.json').text

        when:
        def modelFile = new File(base + '/' + refName.toLowerCase() + '/' + refName + '.groovy')
        println('parsing file:///' + modelFile.absolutePath)
        def pkg = packageName + '.' + refName.toLowerCase()
        modelFile.parentFile.mkdir()
        Loader.invalidateCaches()
        Transformer.transform(jsonSchema, pkg, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(pkg + '.' + refName.capitalize())
        def funk = Loader.create(data, modelClass, loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true))

        then:
        funk

    }

    def "test create rename parameters"() {
        setup:
        def resourceName = 'json/invalidfield.json'
        def data = getResourceText(resourceName)
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase('invalidfield', '-_ '.chars)
        def jsonSchema = GenUtil.confToReferenceSchemaJson(data, refName)

        when:
        def schemaFile = new File(base, 'invalidfields.schema.json').tap { text = jsonSchema }
        def modelFile = new File(base + refName + '.groovy')
        println('json file:///' + resourceName )
        println('schema file:///' + schemaFile.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        Loader.invalidateCaches()
        Transformer.transform(jsonSchema, packageName, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data, modelClass, loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true))

        then:
        funk
    }

    def "test groovy class from all schema"() {
        setup:
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.schema', '').replace('.json', ''), '-_ '.chars)

        when:
        def modelFile = new File(base + refName + '.groovy')
        println('schema file:///' + data.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        Transformer.transform(data.text, packageName, refName.capitalize(), modelFile)

        then:
        modelFile.exists()

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll {
            it.name.endsWith('schema.json')
        }
    }

    def "test override"() {
        setup:
        def conf = new File(base, 'config.conf').tap {
            text = """
        {
            "car": {
                "engine": {
                    "type": "big",
                    "brand": "sterling"
                },
                "doors": {
                    "number": 4
                }
            }
        }
        """
        }
        def confOverride = new File(base, 'config.override.conf').tap {
            text = """
        {
            "car": {
                "engine": {
                    "type": "small",
                }
            }
        }
        """
        }

        when:
        def config = Loader.loadWithOverride(conf, confOverride)

        then:
        config.root().unwrapped().car.engine.type == 'small'
        config.root().unwrapped().car.doors.number == 4
    }

    def "test system props or nots"() {
        setup:
        def config
        def conf = new File(base, 'config.conf').tap { text = 'one=1\ntwo=2\nthree=3\n' }

        when:
        config = Loader.load(loaderOptions().conf(conf).useSystemProperties(false).silent(false))

        then:
        config.root().unwrapped().java == null

        when:
        config = Loader.load(loaderOptions().conf(conf).useSystemProperties(true))
        println ConfUtil.configToJson(config)

        then:
        config.root().unwrapped().java != null

    }


    def "test reference"() {
        setup:
        def config
        def ref = new File(base, 'reference.conf').tap {
            text = """
        {
            "car": {
                "engine": {
                    "type": "big",
                    "brand": "sterling",
                    "dir": \${user.dir}
                },
                "doors": {
                    "number": 4
                }
            }
        }
        """
        }
        def conf = new File(base, 'config.conf').tap {
            text = """
        {
            "car": {
                "engine": {
                    "type": "small"
                }
            }
        }
        """
        }
        def confOverride = new File(base, 'config.override.conf').tap {
            text = """
        {
            "car": {
                "engine": {
                    "brand": "wellbuilt"
                }
            }
        }
        """
        }

        when:
        System.setProperty('car.doors.number', '2')
        Loader.invalidateCaches()
        config = Loader.load(loaderOptions().conf(conf).useSystemProperties(true).reference(ref).confOverride(confOverride).silent(false))
        println ConfUtil.configToJson(config)


        then:
        config.root().unwrapped().car.engine.dir != 'user.dir'
        config.root().unwrapped().car.engine.brand == 'wellbuilt'
        config.root().unwrapped().car.engine.type == 'small'
        System.getProperty('car.doors.number') == '2'
        config.root().unwrapped().car.doors.number == '2'

        then:
        config.root().unwrapped().java != null
        System.clearProperty('car.doors.number')

    }

}
