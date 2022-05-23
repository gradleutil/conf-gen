package net.gradleutil.conf


import net.gradleutil.conf.transform.groovy.SchemaToGroovyClass
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil

class BeanConfigLoaderTest extends AbstractTest {

    def "test create manytyped"() {
        setup:
        def refName = 'Manytyped'
        def confFile = getResourceText('conf/manytyped.conf')
        def schemaFile = new File(base + 'Manytyped.schema.json')
        GenUtil.confToReferenceSchemaFile(confFile, refName, schemaFile)
        println('file:///' + schemaFile.absolutePath)

        when:
        def modelFile = new File(base + 'Manytyped.groovy')
        println('file:///' + modelFile.absolutePath)
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(schemaFile.text, packageName, 'Manytyped', modelFile)

        then:
        result

        when:
        def gcl = new GroovyClassLoader(BeanConfigLoaderTest.classLoader)
        def classLoader = gcl.parseClass(modelFile).classLoader
        def funk = BeanConfigLoader.get(Loader.load(confFile).getConfig('manytyped'), packageName + '.Manytyped', classLoader)
        println funk
        def method = funk.invokeMethod('getTasks', null)

        then:
        method instanceof List
        def list = method as List
        list.toString() == '[Task(name:some task, type:SomeTask)]'
        def task = list.get(0)
        task.toString() == 'Task(name:some task, type:SomeTask)'
    }


    def "test create produce"() {
        setup:
        def refName = 'Produce'
        def confFile = getResourceText('json/produce.json')
        def schemaFile = new File(base + 'Produce.schema.json')
        GenUtil.confToReferenceSchemaFile(confFile, refName, schemaFile)
        println('file:///' + schemaFile.absolutePath)

        when:
        def modelFile = new File(base + 'Produce.groovy')
        println('file:///' + modelFile.absolutePath)
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(schemaFile.text, packageName, 'Produce', modelFile)

        then:
        result

        when:
        def gcl = new GroovyClassLoader(BeanConfigLoaderTest.classLoader)
        def classLoader = gcl.parseClass(modelFile).classLoader
        def funk = BeanConfigLoader.get(Loader.load(confFile), packageName + '.Produce',  classLoader)
        println funk
        def method = funk.invokeMethod('getVegetables', null)

        then:
        method instanceof List
        def list = method as List
        list.toString() == '[Vegetable(veggieName:potato, veggieLike:true), Vegetable(veggieName:broccoli, veggieLike:false)]'
    }


    def "test create plugin"() {
        setup:
        def refName = 'Plugin'
        def confFile = getResourceText('mhf/gradleplugin/plugin.mhf')
        def jsonSchema = GenUtil.confToReferenceSchemaJson(confFile, refName)
        def schemaFile = new File(base + 'Plugin.schema.json').tap { text = jsonSchema }
        println('file:///' + schemaFile.absolutePath)

        when:
        def modelFile = new File(base + 'Plugin.groovy')
        println('file:///' + modelFile.absolutePath)
        def result = SchemaToGroovyClass.schemaToSimpleGroovyClass(jsonSchema, packageName, refName, modelFile)

        then:
        result

        when:
        def gcl = new GroovyClassLoader(BeanConfigLoaderTest.classLoader)
        def classLoader = gcl.parseClass(modelFile).classLoader
        def funk = BeanConfigLoader.get(Loader.load(confFile).getConfig('plugin'), packageName + '.' + refName, classLoader)
        println funk
        //def funk = Loader.create(confFile.text, modelClass)
        def method = funk.invokeMethod('getTasks', null)

        then:
        method instanceof List
        def list = method as List
        list.toString() == '[Task(name:mytask, type:MyTask)]'
        def task = list.get(0)
        task.toString() == 'Task(name:mytask, type:MyTask)'
    }

    

}
