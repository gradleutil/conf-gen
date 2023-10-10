package net.gradleutil.conf.transform.groovy


import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.Loader
import net.gradleutil.conf.LoaderTest
import net.gradleutil.conf.json.schema.SchemaUtil
import net.gradleutil.conf.template.EClass
import net.gradleutil.conf.template.EStructuralFeature
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.transform.schema.SchemaToEPackage
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.conf.util.Inflector

import javax.tools.*

import static net.gradleutil.conf.transform.TransformOptions.Type.java

class GroovyTransformerTest extends AbstractTest {

    def "royalty schema"() {
        setup:
        def jsonSchema = getResourceText('json/royalty.schema.json')

        when:
        def modelFile = new File(base + 'Royalty.groovy')
        def result = Transformer.transform(jsonSchema, packageName, 'Royalty', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "royalty schema java"() {
        setup:
        def jsonSchema = getResourceText('json/royalty.schema.json')

        when:
        def modelFile = new File(base + 'Royalty.java')
        def result = Transformer.transform(jsonSchema, packageName, 'Royalty', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "multiple schema"() {
        setup:
        def jsonSchemaDir = File.createTempDir()
        extractFiles('json/multiple', jsonSchemaDir)

        when:
        def modelFile = new File(base + '/json/multiple')
        Transformer.transform(new File(jsonSchemaDir, 'json/multiple'), modelFile, packageName)
        println "file://${modelFile.absolutePath}"

        then:
        modelFile.listFiles().size() > 0
    }

    def "ref schema"() {
        setup:
        def jsonSchema = GroovyTransformerTest.classLoader.getResource('json/ref.schema.json')

        when:
        def modelFile = new File(base + 'Family.groovy')
        def result = Transformer.transform(jsonSchema.text, packageName, 'Ref', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "ecore schema"() {
        setup:
        def jsonSchema = getResourceText('conf/schema/Ecore.schema.json')

        when:
        def modelFile = new File(base + 'Ecore.groovy')
        def result = Transformer.transform(jsonSchema, packageName, 'EPackage', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "produce singular"() {
        setup:
        def refName = 'Produce'
        extractFiles('json/produce.json', baseDir)

        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(new File(base + 'json/produce.json'), refName)
        def jsonSchemaFile = new File(base + 'produce.schema.json').tap { text = jsonSchema }
        println "file://${jsonSchemaFile.absolutePath}"

        when:
        def modelFile = new File(base + 'Produce.groovy')
        def result = Transformer.transform(jsonSchema, packageName, 'Produce', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "produce schema"() {
        setup:
        def jsonSchema = getResourceText('json/produce.schema.json')

        when:
        def modelFile = new File(base + 'Produce.groovy')
        def result = Transformer.transform(jsonSchema, packageName, 'Produce', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result
    }

    def "booklist schema"() {
        setup:
        def data = new File('src/testFixtures/resources/json/booklist.json')
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)

        when:
        def modelFile = new File(base + refName + '.groovy')
        def schemaFile = new File(base + refName + '.schema.json').tap { it.text = jsonSchema }
        println('json file:///' + data.absolutePath)
        println('schema file:///' + schemaFile.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        Transformer.transform(jsonSchema, packageName, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()
    }

    def "minecraft schema load"() {
        setup:
        def jsonSchema = getResourceText('json/MCConfig.schema.json')

        when:
        def lib = SchemaToEPackage.getEPackage(SchemaUtil.getSchema(jsonSchema), "MCConfig", "net.gradle", false)
        def mod = (lib.eClassifiers.find { it.name == 'ModArtifact' } as EClass).eStructuralFeatures.find { it.name == 'minecraft' }

        then:
        mod != null
        (mod as EStructuralFeature).eType == 'Minecraft'
    }

    def "curseforge schema load"() {
        setup:
        def jsonSchema = getResourceText('json/CurseForgeModQuery.schema.json')
        def refName = 'CurseForgeModQuery'

        when:
        def modelFile = new File(base + refName + '.groovy')
        Transformer.transform(jsonSchema, packageName, refName.capitalize(), modelFile)
        println('parsing file:///' + modelFile.absolutePath)
        def lib = SchemaToEPackage.getEPackage(SchemaUtil.getSchema(jsonSchema), "CurseForgeModQuery", "net.gradle", false)
        def mod = (lib.eClassifiers.find { it.name == 'SortableGameVersion' } as EClass).eStructuralFeatures.find { it.name == 'gameVersionTypeId' }

        then:
        mod != null
        (mod as EStructuralFeature).eType == 'Long'
    }

    def "test create curseforge"() {
        setup:
        def inflector = new Inflector()
        def data = new File('src/testFixtures/resources/json/CurseForgeModQuery.json')
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)

        when:
        def modelFile = new File(base + refName + '.groovy')
        println('json file:///' + data.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        Transformer.transform(jsonSchema, packageName, refName.capitalize(), modelFile)
        println 'file://' + modelFile.absolutePath
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()

    }


    def "minecraft create"() {
        setup:
        def data = new File('src/testFixtures/resources/json/MinecraftConfig.json')
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def jsonSchema = getResourceText('json/MinecraftConfig.schema.json')

        when:
        def modelFile = new File(base + '/' + refName.toLowerCase() + '/' + refName + '.groovy')
        println('json file:///' + data.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        def pkg = packageName + '.' + refName.toLowerCase()
        modelFile.parentFile.mkdir()
        Transformer.transform(jsonSchema, pkg, refName.capitalize(), modelFile)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(pkg + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.loaderOptions().classLoader(gcl).silent(false).allowUnresolved(false).useSystemProperties(false))

        then:
        println funk.toString()
        //println ConfUtil.configToJsonObject(funk as Config).toString()
    }


    def "test create all"() {
        setup:
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def schemaFile = data.parentFile.listFiles().find{it.name.replace('.json','') == data.name.replace('.json','') + '.schema' }
        def jsonSchema
        if(schemaFile){
            jsonSchema = schemaFile.text
        } else {
            jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)
        }

        when:
        def modelFile = new File(base + refName + '.groovy')
        println('json file:///' + data.absolutePath)
        println('parsing file:///' + modelFile.absolutePath)
        Transformer.transform(jsonSchema, packageName, refName.capitalize(), modelFile)
        println modelFile.absolutePath
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)
        def modelClass = gcl.parseClass(modelFile).classLoader.loadClass(packageName + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll { !it.directory && !it.name.endsWith('schema.json') }
    }

    def "test create all java"() {
        setup:
        def inflector = new Inflector()
        def refName = inflector.upperCamelCase(data.name.replace('.json', ''), '-_ '.chars)
        def schemaFile = data.parentFile.listFiles().find{it.name.replace('.json','') == data.name.replace('.json','') + '.schema' }
        def jsonSchema
        if(schemaFile){
            jsonSchema = schemaFile.text
        } else {
            jsonSchema = GenUtil.configFileToReferenceSchemaJson(data, refName)
        }
        
        when:
        def modelFile = new File(base + refName).tap { it.mkdir() }
        println('json file:///' + data.absolutePath)
        def pkg = packageName + '.' + refName.capitalize()
        println pkg
        Transformer.transform(jsonSchema, pkg, refName.capitalize(), modelFile, java)
        def gcl = new GroovyClassLoader(LoaderTest.classLoader)

        compileTarget(modelFile)

        gcl.addClasspath(modelFile.absolutePath)
        modelFile.listFiles().findAll { it.name.endsWith('class') }.each {
            byte[] classData = it.bytes
            def name = it.name.replace('.class', '')
            def classn = pkg + '.' + name
            println classn
            gcl.defineClass(classn, classData)
        }
        modelFile.listFiles().findAll { it.name.endsWith('java') }.each {
            println('parsing file:///' + it.absolutePath)
            gcl.parseClass(it)
        }

        def modelClass = gcl.loadClass(pkg + '.' + refName.capitalize())
        def funk = Loader.create(data.text, modelClass, Loader.loaderOptions().classLoader(gcl).silent(false).allowUnresolved(true).useSystemProperties(false))

        then:
        funk
        println funk.toString()

        where:
        data << new File('src/testFixtures/resources/json/').listFiles().findAll { !it.directory && !it.name.endsWith('schema.json') }
    }

    protected void compileTarget(File targetDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector()
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)
        Collection allFiles = targetDir.listFiles().findAll { it.name.endsWith('.java') }.collect()
        println '-------------------------------------------------'
        println '--------  COMPILE                ----------------'
        println '-------------------------------------------------'

        List<String> optionList = new ArrayList<String>()
        allFiles.each {
            println("Compiling file://" + it.absolutePath)
        }

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(allFiles)
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits)

        boolean status = task.call()
        if (!status) {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                printf("Error on line %s in %s", diagnostic.getLineNumber(), diagnostic)
            }
        }
        fileManager.close()
    }

    def "test dsl"() {
        setup:
        def jsonSchema = getResourceText('json/booklist.schema.json')

        when:
        def modelFile = new File(base + 'BooklistDSL.groovy')
        def result = SchemaToGroovyDSL.schemaToGroovyDSL(jsonSchema, packageName, 'Booklist', modelFile)
        println "file://${modelFile.absolutePath}"

        then:
        result

    }

    /*
        def "test dsl"() {
            setup:
            def configModelFile = new File('src/test/groovy/net/gradleutil/generated/JavaClass.groovy')
            def gen = new Gen()

            when:
            def result = net.gradleutil.generated.DSL.javaClass{
                name = 'fart'
            }

            then:
            result == true
        }
    */
}
