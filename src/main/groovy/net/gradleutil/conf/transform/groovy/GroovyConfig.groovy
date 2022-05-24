package net.gradleutil.conf.transform.groovy

import net.gradleutil.conf.json.schema.Schema

class GroovyConfig {


    static illegalIfNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message)
        }
    }

    /**
     * Convert JSON schema to Groovy DSL.
     * @param schema
     * @param packageName
     * @return
     */
    static String toGroovyDsl(Schema schema, String schemaName, String packageName) {

        illegalIfNull(schema, "schema is required")
        StringBuilder groovy = new StringBuilder()
        groovy.append("package ${packageName}\n")
        //language=groovy
        groovy.append('''
            import net.gradleutil.conf.config.Config

            import static net.gradleutil.conf.transform.json.JsonToSchema.getSchema
            import static net.gradleutil.conf.transform.json.JsonToSchema.validate
            import net.gradleutil.conf.json.JSONObject

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
                                    throw new IllegalArgumentException("Could not call ${method.signature} with ${object.toString()} ${object.class.simpleName}")
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
                            System.err.println "${propertyPath}: ${configObject.getPropertyByPath(propertyPath, false)}"
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
        '''.stripIndent() + '\n\n')
        schema.unprocessedProperties['definitions'].each { name, info ->
            groovy.append("class ${name.capitalize()} extends Block {\n")
            info.properties.each { propName, propInfo ->
                if(!propInfo){
                    println 'no prop info'
                }
                def propInfoMap = propInfo as Map<String, Object>
                def type = propInfoMap?.type?.toString()?.capitalize() ?: propInfoMap['$ref'].toString().replace('#/definitions/', '').capitalize()
                if (propInfoMap['$ref']) {
                    groovy.append("""
                        ${type} ${propName}
                        ${type} ${propName}( @DelegatesTo( value = ${type}.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
                            ${propName} = new ${type}( configurer: configurer )
                            ${propName}
                        }
                        ${type} set${propName}( Config ${type.uncapitalize()}Config ) {
                            ${propName} = new ${type}( config: ${type.uncapitalize()}Config )
                        }
                    """.stripIndent().replaceAll(/(?im)^/, spaces(2)))
                } else {
                    def propListType = propName.toString().capitalize()
                    if (propInfoMap['patternProperties']) {
                        def itemType = propInfoMap['patternProperties']['^.*']['$ref']?.toString()?.replace('#/definitions/', '')
                        groovy.append("""
                                Map<String,${itemType}> ${propName} = [:]
                                ${itemType} ${itemType.uncapitalize()}( String name, @DelegatesTo( value = ${itemType}.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
                                    def item = new ${itemType}( configurer: configurer )
                                    ${propName}.put(name,item)
                                    item
                                }
                                ${itemType} get${itemType}(key) { 
                                    def item = ${propName}.find{it.key == key }?.value
                                    if(!item){
                                        throw new IllegalArgumentException("${itemType} '\${key}' not found in \${${propName}.keySet()}") 
                                    }
                                    item
                                }
                        """.stripIndent().replaceAll(/(?im)^/, spaces(2)))
                    } else if (type == 'Array') {
                        def itemType = propInfoMap?.items['$ref']?.toString()?.replace('#/definitions/', '')
                        groovy.append("""
                            List<${propListType}> ${propName} = [] as List<${propListType}>
                            ${propListType} ${propName}( @DelegatesTo( value = ${propListType}.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
                                new ${propListType}( configurer: configurer )
                            }
                            class ${propListType} extends Block {
                                void ${propListType.uncapitalize()}( @DelegatesTo( value = ${propListType}.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
                                    ${propName}.add( new ${propListType}( configurer: configurer ) )
                                }
                            }
                        """.stripIndent().replaceAll(/(?im)^/, spaces(2)))
                    } else {
                        groovy.append("${spaces(1)}${type} ${propName}\n")
                    }
                }

            }
            groovy.append('}\n\n')
        }
        def schemaRefName = schema.hasProperty('refValue') ? schema['refValue'].toString().replace('#/definitions/', '') : ''
        if(schemaRefName){
            def type = schemaRefName.capitalize()
            groovy.append("""
                class DSL extends Block {
                    ${type} ${type.uncapitalize()}
                    ${type} ${type.uncapitalize()}( @DelegatesTo( value = ${type}.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
                        ${type.uncapitalize()} = new ${type}( configurer: configurer )
                    }
                    ${type} set${type}( ConfigObject ${type.uncapitalize()}ConfigObject ) {
                        ${type.uncapitalize()} = new ${type}( configObject: ${type.uncapitalize()}ConfigObject )
                    }
                }
            """.stripIndent())
        }
        groovy.toString()
    }


    static String toGroovy(Map configObject, Integer indentLevel = 0) {
        StringBuilder groovy = new StringBuilder()
        configObject.each { object, value ->
            groovy.append(toGroovyLine(object, value, indentLevel))
        }
        groovy.toString()
    }

    static String gQuote(String value) {
        def qualifier = value.toString().contains('$') ? '"' : '\''
        return qualifier + value + qualifier
    }

    static String spaces(Integer indentLevel = 0) {
        indentLevel == 0 ? '' : String.format("%1\$" + (indentLevel * 2) + "s", "")
    }

    static String toGroovyLine(Object object, Object value, Integer indentLevel = 0) {
        StringBuilder groovy = new StringBuilder()
        def indent = spaces(indentLevel)
        if (object == '$schema' || object == 'configObject') {
            return ''
        }
        groovy.append(indent + object)
        switch (value) {
            case String:
                groovy.append(' = ' + gQuote(value as String) + '\n')
                break
            case Boolean:
            case Number:
                groovy.append(' = ' + value + '\n')
                break
            case ArrayList:
                def array = value
                if (array instanceof List<ConfigObject>) {
                    groovy.append(" = [\n${spaces(indentLevel + 1)}")
                    groovy.append(array.collect {
                        def item
                        def map = it as Map
                        if (map.keySet().size() == 1 && map.values().first() instanceof Map) {
                            item = toGroovy(map, indentLevel + 1).trim()
                        } else {
                            map.findAll { k, v -> v instanceof String }.each { k, v -> map[k] = gQuote(v as String) }
                            item = map.inspect().replace("\\'", "")
                        }
                        item
                    }.join(",\n${spaces(indentLevel + 1)}"))
                    groovy.append("\n${indent}]\n")
                }
                break
            case Map:
                groovy.append('{\n' + toGroovy(value as Map, indentLevel + 1) + "${indent}}\n")
                break
            default:
                groovy.append('{\n' + indent + value.toString() + '}\n')
        }
        groovy.toString()
    }

    /**
     * Simple project representation
     */
    static class SimpleProject {
        /**
         * @param dir
         * @return
         */
        static Object project(File dir = null) {
            def map = [:]
            // use a custom dir or default to the current parent dir
            File rootDir = dir ?: new File('.').absoluteFile.parentFile
            map.name = rootDir.name
            map.projectDir = rootDir
            map.rootDir = rootDir
            map.buildDir = new File(rootDir.path + './build')
            map.findProperty = { String name -> return map.find { it.key == name } }
            map.hasProperty = { String name -> return map.find { it.key == name } != null }
            map.file = { path ->
                if (path instanceof File) {
                    return path
                }
                return new File("${rootDir.path}/${path.replace(rootDir.path, '')}")
            }
            map.getRootDir = { Object[] args -> map.rootDir }
            map.getProjectDir = { Object[] args -> map.projectDir }
            map.getBuildDir = { Object[] args -> map.buildDir }
            map.rootProject = map
            map
        }
    }

}
