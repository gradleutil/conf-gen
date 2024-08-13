package net.gradleutil.conf.transform.schema

import groovy.util.logging.Log
import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.template.EClass
import net.gradleutil.conf.template.EClassifier
import net.gradleutil.conf.transform.TransformOptions
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.gen.Template
import net.gradleutil.gen.groovyclass.GroovyClassTemplate

import static SchemaToEPackage.getEPackage
import static net.gradleutil.conf.json.schema.SchemaUtil.getSchema
import static net.gradleutil.conf.transform.Transformer.transformOptions
import static net.gradleutil.conf.transform.groovy.EPackageRenderer.schemaToEPackageRender
import static net.gradleutil.conf.transform.java.EPackageRenderer.schemaToJavaRender

@Log
class SchemaToEPackageTest extends AbstractTest {

    def "simple array object"() {
        setup:
        def jsonSchema = getResourceText('json/booklist.schema.json')
        def rootClassName = 'Booklist'
        def convertToCamelCase = true

        when:
        def ePackage = getEPackage(getSchema(jsonSchema,""), rootClassName, packageName, convertToCamelCase)
        String source = Template.render(transformOptions().ePackage(ePackage))
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 2
        ePackage.getEClassifiers()*.name.intersect(['Booklist', 'Book']).size() == 2

    }

    def "simple allOf"() {
        setup:
        def jsonSchema = getResourceText('json/AllOfTest.schema.json')
        def rootClassName = 'Booklist'
        println(jsonSchema)

        when:
        def ePackage = getEPackage(getSchema(jsonSchema,""), rootClassName, packageName, true)
        String source = GroovyClassTemplate.render(transformOptions().ePackage(ePackage))
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 2
        ePackage.getEClassifiers()*.name.intersect(['AllOfTest', 'EModelElement']).size() == 2

    }

    def "epackage allOf"() {
        setup:
        def jteDir = new File('src/testFixtures/resources/jte/epackage')
        def jsonSchema = getResourceText('json/AllOfTest.schema.json')
        def rootClassName = 'Booklist'

        when:
        def ePackage = getEPackage(getSchema(jsonSchema,""), rootClassName, packageName, true)
        def options = transformOptions()
                .ePackage(ePackage)
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .rootClassName(rootClassName)
                .outputFile(new File(base))
        def source = Template.render(options)
        println source

        then:
        ePackage.getEClassifiers().size() == 2
        ePackage.getEClassifiers()*.name.intersect(['AllOfTest', 'EModelElement']).size() == 2

    }

    def "epackage column order"() {
        setup:
        def jteDir = new File('src/testFixtures/resources/jte/epackage')
        def jsonSchema = getResourceText('json/Buildings.schema.json')
        def rootClassName = 'Buildings'

        when:
        def schema = getSchema(jsonSchema,"")
        def ePackage = getEPackage(schema, rootClassName, packageName, true)
        def options = transformOptions()
                .ePackage(ePackage)
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .rootClassName(rootClassName)
                .outputFile(new File(base))
        def source = Template.render(options)
        def feats = (ePackage.eClassifiers.get(0) as EClass) .eStructuralFeatures
        println source

        then:
        feats.get(0).name == 'name'
        feats.get(1).name == 'file'
        feats.get(2).name == 'buildingGroups'
        ePackage.eClassifiers.eachWithIndex { EClassifier entry, int i ->
        }
        ePackage.getEClassifiers().size() == 4

    }



    def "epackage rendered"() {
        setup:
        def jteDir = new File('src/testFixtures/resources/jte/epackage')
        def jsonSchema = getResourceText('json/AllOfTest.schema.json')
        def rootClassName = 'Booklist'
        def renderDir = new File(base, 'render').tap { it.mkdirs() }

        when:
        def options = transformOptions()
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .rootClassName(rootClassName)
                .outputFile(renderDir)
        def ePackage = schemaToEPackageRender(options)

        then:
        ePackage
    }

    def "epackage referenced"() {
        setup:
        def jteDir = new File('src/testFixtures/resources/jte/epackage')
        def schemaDir = new File('src/testFixtures/resources/json/bundle/')
        def renderDir = new File(base, 'render').tap { it.mkdirs() }
        setup:

        when:


        schemaDir.listFiles().each { jsonSchema ->
            def name = jsonSchema.name.replace('.schema', '').replace('.json', '')
            def packagePrefix = jsonSchema.path.replace(schemaDir.path, '')
                    .replace(File.separator + jsonSchema.name, '')
                    .replace(File.separator, '.').toLowerCase()

            def modelSourceDir = new File(renderDir, packagePrefix.replace('.', File.separator) + File.separator + name.toLowerCase()).tap { mkdirs() }
            def fullPackageName = "${packageName}${packagePrefix ?: ''}.${name.toLowerCase()}"
            
            def options = transformOptions()
                    //.jteDirectory(jteDir)
                    .basePath(jsonSchema.parentFile.path)
                    .jsonSchema(jsonSchema.text)
                    .packageName(fullPackageName)
                    .toType(TransformOptions.Type.java)
                    .rootClassName(name)
                    .outputFile(modelSourceDir)
            Transformer.transform(options)
        }

        then:
        renderDir.listFiles().each {
            println 'file://' + it.absolutePath
        }
        //        ePackage.getEClassifiers()*.name.intersect(['Booklist','Books']).size() == 2

    }

    def "epackage rendered java"() {
        setup:
        def jteDir = new File('src/testFixtures/resources/jte/epackage')
        def jsonSchema = getResourceText('json/AllOfTest.schema.json')
        def rootClassName = 'Booklist'
        def renderDir = new File(base, 'render').tap { it.mkdirs() }

        when:
        def options = transformOptions()
                .jteDirectory(jteDir)
                .jsonSchema(jsonSchema)
                .rootClassName(rootClassName)
                .outputFile(renderDir)
        def ePackage = schemaToJavaRender(options)

        then:
        ePackage
        renderDir.listFiles().each {
            println 'file://' + it.absolutePath
        }
    }


    def "produce"() {
        setup:
        def jsonSchema = new File('src/testFixtures/resources/json/produce.schema.json')
        def rootClassName = 'Produce'
        def convertToCamelCase = true

        when:
        def ePackage = getEPackage(getSchema(jsonSchema,""), rootClassName, packageName, convertToCamelCase)
        println "file://${jsonSchema.absolutePath}"
        String source = Template.render(transformOptions().ePackage(ePackage))
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 2
        //        ePackage.getEClassifiers()*.name.intersect(['Booklist','Books']).size() == 2

    }

    def "recursive"() {
        setup:
        def jsonSchema = new File('src/testFixtures/resources/json/includes/recursive.schema.json')
        def rootClassName = 'Project'
        def convertToCamelCase = true

        when:
        def ePackage = getEPackage(getSchema(jsonSchema,""), rootClassName, packageName, convertToCamelCase)
        println "file://${jsonSchema.absolutePath}"
        String source = Template.render(transformOptions().ePackage(ePackage))
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 1

    }


    def "ecore"() {
        setup:
        def jsonSchema = new File('template/src/main/groovy/net/gradleutil/conf/schema/Ecore.schema.json')
        def rootClassName = 'Booklist'
        def convertToCamelCase = true

        when:
        def ePackage = getEPackage(getSchema(jsonSchema,""), rootClassName, packageName, convertToCamelCase)
        println "file://${jsonSchema.absolutePath}"
        String source = Template.render(transformOptions().ePackage(ePackage))
        def modelFile = new File(base + rootClassName + '.groovy').tap { text = source }
        println "file://${modelFile.absolutePath}"

        then:
        ePackage.getEClassifiers().size() == 20
        //        ePackage.getEClassifiers()*.name.intersect(['Booklist','Books']).size() == 2

    }

    def "gradle plugin"() {
        setup:
        def confFile = getResourceText('mhf/gradleplugin/plugin.mhf')
        def rootClassName = 'Plugin'
        new File(base, 'objectschema.json').delete()
        def jsonSchema = GenUtil.confToReferenceSchemaFile(confFile, rootClassName, new File(base, 'objectschema.json'))
        def convertToCamelCase = true
        println "file://${jsonSchema.absolutePath}"

        when:
        def sc = getSchema(jsonSchema,"")
        def ePackage = getEPackage(sc, rootClassName, packageName, convertToCamelCase)
        def classNames = ['Plugin', 'Task', 'PluginProperty']

        then:
        ePackage.eClassifiers.size() == classNames.size()
        ePackage.eClassifiers*.name.intersect(classNames).size() == classNames.size()
        def modelFile = new File(base + rootClassName + '.groovy')
        println ePackage
        Transformer.transform(jsonSchema.text, packageName, rootClassName,"", modelFile)
        println modelFile.text
    }

}
