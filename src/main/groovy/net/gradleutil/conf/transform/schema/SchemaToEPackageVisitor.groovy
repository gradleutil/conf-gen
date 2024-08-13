package net.gradleutil.conf.transform.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.networknt.schema.JsonSchema
import net.gradleutil.conf.template.*
import net.gradleutil.conf.util.ConfUtil

class SchemaToEPackageVisitor {
    final EPackage ePackage
    final Boolean singularizeClassNames

    private Boolean silent = true
    private String ref
    private ObjectMapper OBJECT_MAPPER

    SchemaToEPackageVisitor(String packageName, String ref, String nsURI = 'http://emfjson/dynamic/model', String nsPrefix = '', Boolean singularizeClassNames = true) {
        ePackage = EPackage.eINSTANCE.createEPackage()
        ePackage.setNsURI(nsURI)
        ePackage.setName(packageName)
        ePackage.setNsPrefix(nsPrefix)
        OBJECT_MAPPER = new ObjectMapper()
        this.ref = ref
        this.singularizeClassNames = singularizeClassNames
    }

    SchemaToEPackageVisitor(String packageName, String ref, Boolean singularizeClassNames) {
        this(packageName, ref, null, null, singularizeClassNames)
    }

    Stack<ReferenceSchema> refStack = []
    Stack<EClass> eClassStack = []
    Stack<JsonNode> objectSchemaStack = []
    Stack<String> propertyNameStack = []

    Set<JsonNode> visited = []

    JsonSchema schema

    void visitSchema(JsonSchema schema) {}

    EClass definitionToEClass(String name, JsonNode node) {
        getOrCreateEClass(name, node)
    }

    JsonSchema ensureIsRefSchema(JsonSchema schema, String ref) {
        def node = (ObjectNode) schema.schemaNode
        ObjectNode defs
        if (node.has('definitions') || node.has('$defs')) {
            defs = node.has('$defs') ? node.get('$defs') : node.get('definitions')
        } else {
            node.set('$defs', OBJECT_MAPPER.createObjectNode())
            defs = node.get('$defs')
        }
        if (node.has('type') && node.has('properties')) {
            def object = OBJECT_MAPPER.createObjectNode()
            object.set('type', node.get('type'))
            object.set('properties', node.get('properties'))
            if (node.has('default')) {
                object.set('default', node.get('default'))
            }
            defs.set(ref, object)
            node.remove(['type', 'properties', 'default'])
        }
        schema
    }

    void visit(JsonSchema schema, String ref) {
        this.schema = ensureIsRefSchema(schema, ref)
        if (schema.getSchemaNode().has('definitions')) {
            schema.getSchemaNode().get('definitions').properties().each {
                definitionToEClass(it.key, it.value)
            }
            visited.add(schema)
        }
        if (schema.getSchemaNode().has('$defs')) {
            schema.getSchemaNode().get('$defs').properties().each {
                definitionToEClass(it.key, it.value)
            }
            visited.add(schema)
        }
        if (schema.getSchemaNode().has('type')) {
            String name = schema.schemaNode.findValuesAsText('title').first() ?: schema.schemaNode.get('properties').fieldNames().next()
            definitionToEClass(name, schema.getSchemaNode())
            visited.add(schema)
        }
        if (visited.size() == 0) {
            throw new Exception("dunno how to handle schema")
        }
    }

    void visit(JsonNode node) {
        if (!visited.contains(node)) {
            if (node.has('$ref')) {
                visitReferenceSchema(node)
            }
        }
    }

    void visitReferenceSchema(JsonSchema schema) {
        def eClass = getOrCreateEClass(schema)
        if (propertyNameStack.size() && eClassStack.size()) {
            EReference eRef = eReference(popPropertyNameStack(), eClass.name)
            eRef.upperBound = 1
            addStructuralFeature eRef
        }
        pushRefStack(schema)
        /*
        if (schema.referredSchema instanceof CombinedSchema) {
            if ((schema.referredSchema as CombinedSchema).criterion.toString() == 'allOf') {
                eClass.seteSuperTypes([getEClassName((schema.referredSchema as CombinedSchema).subschemas.first() as ReferenceSchema,singularizeClassNames)])
            }
        }
        */
        JsonNode refNode = schema.getRefSchemaNode(schema.schemaNode.get('$ref').textValue())
        visitObjectSchema(refNode)
        popRefStack()
    }

    void visitCombinedSchema(JsonNode schema) {
        if (schema.fieldNames().next() == 'allOf') {

        } else {
            // 'anyOf'
            if (schema.subschemas.every { it instanceof ObjectSchema }) {
                schema.subschemas.each {
                    def os = it as ObjectSchema
                    if (os.propertySchemas.size() == 1) {
                        os.propertySchemas.each { visit it.value }
                    } else {
                        println "don't know how to handle multiple properties, thought this was a type: ${os.propertySchemas*.getKey().join(',')}"
                    }
                }

            } else {
                if (schema.subschemas.size() == 2 && schema.subschemas.find { it instanceof NullSchema }) {
                    // we can only map some 'anyOf', for now just handling a type plus null
                    def subschema = schema.subschemas.find { !(it instanceof NullSchema) }
                    visit subschema
                    visited.remove(schema) // these are basically properties so always visit them
                } else {
                    def refSchema = SchemaToReferenceSchema.toReferenceSchema(schema, 'object', null)
                    refSchema.unprocessedProperties = schema.unprocessedProperties
                    //refStack.push(refSchema)
                    visit(refSchema)
                    //schema.subschemas.each {visit(it)}
                }
            }
        }
    }

    int requiredContains(String name) {
        objectSchemaStack.size() && objectSchemaStack.peek().get("required")?.textValue()?.contains(name) ? 1 : 0
    }

    void visitObjectSchema(JsonNode objectSchema) {
        def eClass = getOrCreateEClass(refStack.peek())
        pushEClassStack(eClass)
        objectSchemaStack.push objectSchema
        if (propertyNameStack.size() && eClassStack.size()) {
            EReference eRef = eReference(popPropertyNameStack(), eClass.name)
            eRef.upperBound = 1
            eRef.lowerBound = requiredContains(eRef.name)
            addStructuralFeature eRef
        }
        popEClassStack()
        objectSchemaStack.pop()
    }

    void visitPropertySchema(String properyName, JsonSchema schema) {
        pushPropertyNameStack(ident(properyName, false, false))
        visit(schema)
    }

    void visitNumberSchema(NumberSchema numberSchema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'Number')
        if (numberSchema.requiresInteger()) {
            eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getEBigInteger())
        } else {
            eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getEBigDecimal())
        }
        addStructuralFeature eAttr
    }

    void visitEnumSchema(EnumSchema enumSchema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'enum')
        eAttr.setEType('enum')
        eAttr.valueList = enumSchema.possibleValuesAsList
        addStructuralFeature eAttr
    }

    private EAttribute eAttribute(String name, String eType) {
        EPackage.eINSTANCE.createEAttribute().tap {
            it.name = name; it.eType = eType
            it.lowerBound = requiredContains(name)
        }
    }

    private EReference eReference(String name, String eType) {
        //        println("adding ref: ${name} ${eType}")
        EPackage.eINSTANCE.createEReference().tap {
            it.name = name; it.eType = eType
            it.lowerBound = requiredContains(name)
        }
    }

    private static EDataType eBoolean() {
        EPackage.eINSTANCE.ecorePackage.getEBoolean()
    }

    private EClass getOrCreateEClass(String name, JsonNode node) {
        def eClassName = getEClassName(ePackage.name, name, singularizeClassNames)
        def existing = ePackage.getEClassifier(eClassName)
        if (existing) {
            return existing as EClass
        } else {
            def eClass = EPackage.eINSTANCE.createEClass()
            eClass.setName(eClassName)
            ePackage.getEClassifiers().add(eClass)
            if(node.has('enum')){
                eClass.seteSuperTypes(['enum'])
                EAttribute eAttr = eAttribute(name, 'enum')
                eAttr.valueList = node.get('enum').collect { it.textValue() }
                eClass.eStructuralFeatures.add(eAttr)
            }
            setStructuralFeatures(eClass, node)
            return eClass
        }
    }

    void setStructuralFeatures(EClass eClass, JsonNode node) {
        if (!node.has('properties')) {
            if (node.has('allOf')) {
        //        setStructuralFeatures(eClass, node.get('allOf'))
            }
            return
        }
        objectSchemaStack.push(node)
        def props = node.get('properties').properties()
        props.iterator().each { it ->
            String propName = ident(it.key, false, false)
            if (!eClass.eStructuralFeatures.find { it.name == propName }) {
                eClass.eStructuralFeatures.add(getAttribute(propName, node, it.value))
            } else {
                println("already processed: " + propName)
            }
        }

        objectSchemaStack.pop()
    }
    
    static String simpleJsonTypeToJavaType(String type){
        return type.capitalize()
    }

    EStructuralFeature getAttribute(String name, JsonNode parentNode, JsonNode node) {
        if (node.has('type')) {
            String type
            def typeNode = node.get('type')
            if (typeNode instanceof TextNode) {
                type = node.get('type').textValue()
            } else {
                type = typeNode.first().textValue()
            }
            if (type == 'array') {
                Boolean hasItems = node.has('items')
                if (hasItems && node.get('items').has('$ref')) {
                    String refPath = node.get('items').get('$ref').textValue()
                    String className = getEClassName(ePackage.name, refPath, singularizeClassNames)
                    EReference eRef = eReference(name, className)
                    eRef.upperBound = -1
                    eRef.lowerBound = 0
                    return eRef
                } else if(hasItems && node.get('items').has('type')) {
                    def itemType = node.get('items').get('type')
                    String itemTypeName = itemType.isArray() ? 'Object' : itemType.textValue()
                    EAttribute eAttr = eAttribute(name,  simpleJsonTypeToJavaType(itemTypeName))
                    eAttr.upperBound = -1
                    eAttr.lowerBound = 0
                    return eAttr
                } else {
                    EAttribute eAttr = eAttribute(name, 'String')
                    eAttr.upperBound = -1
                    eAttr.lowerBound = 0
                    return eAttr
                }
            } else if (type == 'string') {
                return eAttribute(name, 'String')
            } else if (type == 'number') {
                return eAttribute(name, 'BigDecimal')
            } else if (type == 'boolean') {
                return eAttribute(name, 'Boolean')
            } else if (type == 'null') {
                return eAttribute(name, 'String')
            } else if (type == 'integer') {
                return eAttribute(name, 'BigInteger')
            } else if (type == 'null') {
                return eAttribute(name, 'null')
            } else {
                throw new Exception('Unknown type: ' + type)
            }
        } else if (node.has('$ref')) {
            String eClassName = getEClassName(ePackage.name, node.get('$ref').textValue(), singularizeClassNames)
            return eReference(name, eClassName)
        } else if (node.has('enum')) {
            EAttribute eAttr = eAttribute(name, 'enum')
            eAttr.valueList = node.get('enum').collect { it.textValue() }
            return eAttr
        } else {
            throw new Exception('unknown node for name: ' + name)
        }
    }

    void addStructuralFeature(EStructuralFeature feature) {
        def features = eClassStack.peek().getEStructuralFeatures()
        if (!features.find { it.name == feature.name }) {
            features.add feature
        }
    }

    static String getEClassName(String packageName, String name, Boolean singularizeClassNames) {
        String className
        if(name.startsWith('#/definitions/')) {
            className = name.replace('#/definitions/', '')
        } else if(name.contains('#')) {
            def otherPackageName = name.split('#')[0].split('\\.').first()
            def strParts = packageName.split('\\.')
            className = strParts.take(strParts.size() - 1).join('.').toLowerCase() + '.' + otherPackageName + '.'
            className += name.split('#')[1].replace("/definitions/", "")
        } else {    
            className = name
        }
        if(className.contains('.')) {
            def strParts = className.split('\\.')
            className = strParts.take(strParts.size() - 1).join('.').toLowerCase() + '.' + 
                    ident(strParts.last(), true, singularizeClassNames)
            return className
        }
        return ident(className, true, singularizeClassNames)
    }

    static String ident(String string, Boolean upperCamel = false, Boolean singularizeClassNames = false) {
        ConfUtil.ident(string, true, upperCamel, singularizeClassNames)
    }

    void pushEClassStack(EClass eClass) {
        pushStack(eClassStack, eClass)
    }

    EClass popEClassStack() {
        popStack(eClassStack)
    }

    void pushRefStack(JsonSchema referenceSchema) {
        if (refStack.contains(referenceSchema)) {
            System.err.println('already added this reference schema!')
        } else {
            pushStack(refStack, referenceSchema)
        }
    }

    JsonSchema popRefStack() {
        if (refStack.size()) {
            popStack(refStack)
        } else {
            println 'WARN: refStack was empty'
        }
    }

    void pushPropertyNameStack(String name) {
        pushStack(propertyNameStack, name)
    }

    String popPropertyNameStack() {
        if (propertyNameStack.size()) {
            popStack(propertyNameStack)
        } else {
            println 'WARN: propertyNameStack was empty'
        }
    }


    void pushStack(Stack stack, Object object) {
        stack.push(object)
        printStackItem(true, object)
    }

    def <T> T popStack(Stack<T> stack) {
        def object = stack.pop()
        printStackItem(false, object)
        object as T
    }

    def indent = ''

    void printStackItem(Boolean isPush, Object object) {
        def line, pref
        if (isPush) {
            pref = indent + '->'
        } else {
            indent = indent.take(indent.length() - 2)
            pref = indent + '<-'
        }
        if (object instanceof ReferenceSchema) {
            line = pref + "R🔵 ️" + getEClassName(ePackage.name, object, singularizeClassNames) + "[${propertyNameStack.reverse().collect { '🔸' + it }.join(' ')}]"
        } else if (object instanceof EClass) {
            line = pref + "C🟢 " + object.name + ' [ ' + eClassStack.reverse().name.join(', ') + ']'
        } else {
            line = pref + "🔸 " + object + ' ' + "[${propertyNameStack.reverse().collect { '🔸' + it }.join(' ')}]"
        }
        if (!silent) {
            println(line)
            if (isPush) {
                indent += '  '
            }
        }
    }
}