@import net.gradleutil.conf.template.*

@param EPackage javaClassTemplate
package ${ javaClassTemplate.name }

import net.gradleutil.conf.config.Config

import static net.gradleutil.conf.transform.json.JsonToSchema.getSchema
import static net.gradleutil.conf.transform.json.JsonToSchema.validate

import net.gradleutil.conf.json.JSONObject

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
@AnnotationCollector
@interface ToPrettyString { }
@interface Optional { }

class Block {
    private config

    void setConfig(Config fromConfigObject) {
        this.config = fromConfigObject
        this.metaClass.methods.each { method ->
            def keyName = method.name.replaceAll('^set', '').uncapitalize()
            def configValue = fromConfigObject.entrySet().find{it.key == keyName }?.value
            if (configValue) {
                def object = configValue.render()
                if (object) {
                    try{
                        if (method.signature.contains('Closure')) {
                            // only set base types
                        } else if (method.signature.contains('/String;')) {
                            method.invoke(this, object as String)
                        } else {
                            method.invoke(this, object)
                        }
                    } catch(IllegalArgumentException e){
                        throw new IllegalArgumentException("Could not call &#34;{method.signature} with &#34;{object.toString()} &#34;{object.class.simpleName}")
                    }
                }
            }
        }
    }

    void setConfigurer(Closure configurer) {
        configurer.rehydrate( this, this, this ).call()
    }

    Config getConfigObject() {
        this.configObject
    }

    String getJson() {
        configObject.root().render()
    }

    boolean validate(InputStream inputStream) {
        def jsonObject = new JSONObject(configObject.root().render())
        def validations = validate(getSchema(inputStream), jsonObject)
        if (validations.size()) {
            System.err.println jsonObject.toString()
            validations.each {
                System.err.println it.message
                def propertyPath = it.pointerToViolation.replaceAll('^#/?', '').replace('/', '.')
                System.err.println "&#34;{propertyPath}: &#34;{configObject.getPropertyByPath(propertyPath, false)}"
            }
            throw new IllegalArgumentException("Failed validations")
        }
        // pull in any defaults set via JSON schema validation
        //        configObject.merge(new ConfigObject(jsonObject.toMap()))
        true
    }

    @Override
    String toString() {
        configObject.root().render()
    }
}


@for (EClassifier configClass : javaClassTemplate.eClassifiers)
    @@template.groovydsl.GroovyDSLClass(eClass=(EClass)configClass)
@endfor
