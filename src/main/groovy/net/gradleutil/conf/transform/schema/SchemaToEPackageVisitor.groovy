package net.gradleutil.conf.transform.schema

import net.gradleutil.conf.json.schema.GeneratorVisitor
import net.gradleutil.conf.json.schema.*
import net.gradleutil.conf.template.*
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.conf.util.GenUtil

class SchemaToEPackageVisitor extends GeneratorVisitor {
    final EPackage ePackage
    final Boolean singularizeClassNames

    SchemaToEPackageVisitor(String packageName, String nsURI = 'http://emfjson/dynamic/model', String nsPrefix = '', Boolean singularizeClassNames = true) {
        ePackage = EPackage.eINSTANCE.createEPackage()
        ePackage.setNsURI(nsURI)
        ePackage.setName(packageName)
        ePackage.setNsPrefix(nsPrefix)
        this.singularizeClassNames = singularizeClassNames
    }

    Stack<ReferenceSchema> refStack = []
    Stack<EClass> eClassStack = []
    Stack<String> propertyNameStack = []

    List<Schema> visited = []

    @Override
    void visitSchema(Schema schema) {
    }

    @Override
    void visit(Schema schema) {
        Boolean isReference
        isReference = ['ArraySchema','ReferenceSchema', 'CombinedSchema'].contains(schema.class.simpleName )
        if (!visited.contains(schema) && isReference) {
            visited.add(schema)
            super.visit(schema)
        }
        if (!visited.contains(schema)) {
            super.visit(schema)
        }
    }

    @Override
    void visitReferenceSchema(ReferenceSchema schema) {
        def eClass = getOrCreateEClass(schema)
        pushRefStack(schema)
        if (schema.referredSchema instanceof CombinedSchema) {
            if ((schema.referredSchema as CombinedSchema).criterion.toString() == 'allOf') {
                eClass.seteSuperTypes([getEClassName((schema.referredSchema as CombinedSchema).subschemas.first() as ReferenceSchema)])
            }
        }
        if (propertyNameStack.size() && eClassStack.size()) {
            EReference eRef = eReference(popPropertyNameStack(), eClass.name)
            eRef.upperBound = 1
            addStructuralFeature eRef
        }
        super.visitReferenceSchema(schema)
        popRefStack()
    }

    @Override
    void visitCombinedSchema(CombinedSchema schema) {
        if (schema.criterion.toString() == 'allOf') {
            schema.subschemas.each { visit(it) }
        } else {
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
                def refSchema = SchemaToReferenceSchema.toReferenceSchema(schema, 'object', null)
                refSchema.unprocessedProperties = schema.unprocessedProperties
                //refStack.push(refSchema)
                visit(refSchema)
                //schema.subschemas.each {visit(it)}
            }
        }
    }


    @Override
    void visitObjectSchema(ObjectSchema objectSchema) {
        def eClass = getOrCreateEClass(refStack.peek() as ReferenceSchema)
        if (propertyNameStack.size() && eClassStack.size()) {
            EReference eRef = eReference(popPropertyNameStack(), eClass.name)
            eRef.upperBound = 1
            addStructuralFeature eRef
        }
        eClassStack.push eClass
        super.visitObjectSchema(objectSchema)
        eClassStack.pop()
    }

    @Override
    void visitPropertySchema(String properyName, Schema schema) {
        pushPropertyNameStack(ident(properyName, false, false))
        visit(schema)
    }

    @Override
    void visitArraySchema(ArraySchema arraySchema) {
        def propertyName = popPropertyNameStack()
        def itemSchema = arraySchema.allItemSchema ?: arraySchema.itemSchemas.first()
        if (itemSchema instanceof ReferenceSchema) {
            EReference eRef = eReference(propertyName, getEClassName(itemSchema))
            eRef.upperBound = -1
            addStructuralFeature eRef
            visit(arraySchema.allItemSchema ?: arraySchema.itemSchemas.first())
        } else {
            EAttribute eAttr = eAttribute(propertyName, 'String')
            eAttr.upperBound = -1
            eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getEString())
            addStructuralFeature eAttr
        }
    }

    @Override
    void visitBooleanSchema(BooleanSchema schema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'Boolean')
        eAttr.setEType(eBoolean())
        if (schema instanceof TrueSchema) {
            eAttr.defaultValue = true
        }
        addStructuralFeature eAttr
    }

    @Override
    void visitNullSchema(NullSchema nullSchema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'null')
        eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getEString())
        addStructuralFeature eAttr
    }

    @Override
    void visitStringSchema(StringSchema stringSchema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'String')
        eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getEString())
        addStructuralFeature eAttr
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'Number')
        if (numberSchema.requiresInteger()) {
            eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getEBigInteger())
        } else {
            eAttr.setEType(EPackage.eINSTANCE.ecorePackage.getELong())
        }
        addStructuralFeature eAttr
    }

    @Override
    void visitEnumSchema(EnumSchema enumSchema) {
        def propertyName = popPropertyNameStack()
        EAttribute eAttr = eAttribute(propertyName, 'enum')
        eAttr.setEType('enum')
        addStructuralFeature eAttr
    }

    private static EAttribute eAttribute(String name, String eType) {
        EPackage.eINSTANCE.createEAttribute().tap { it.name = name; it.eType = eType }
    }

    private static EReference eReference(String name, String eType) {
        EPackage.eINSTANCE.createEReference().tap { it.name = name; it.eType = eType }
    }

    private static EDataType eBoolean() {
        EPackage.eINSTANCE.ecorePackage.getEBoolean()
    }


    private EClass getOrCreateEClass(ReferenceSchema referenceSchema) {
        def eClassName = getEClassName(referenceSchema)
        def existing = ePackage.getEClassifier(eClassName)
        if (existing) {
            return existing as EClass
        } else {
            def eClass = EPackage.eINSTANCE.createEClass()
            eClass.setName(eClassName)
            ePackage.getEClassifiers().add(eClass)
            return eClass
        }
    }

    void addStructuralFeature(EStructuralFeature feature) {
        def features = eClassStack.peek().getEStructuralFeatures()
        if (!features.find { it.name == feature.name }) {
            features.add feature
        }
    }

    static String getEClassName(ReferenceSchema schema) {
        def className = schema.referredSchema.schemaLocation.replace('#/definitions/', '')
        assert className, "No class name defined in schema for ${schema}"
        ident(className, true, true)
    }

    static String ident(String string, Boolean upperCamel = false, Boolean singularizeClassNames = false) {
        ConfUtil.ident(string, true, upperCamel, singularizeClassNames)
    }

    void pushRefStack(ReferenceSchema referenceSchema) {
        //println "+ refStack:" + getEClassName(referenceSchema)
        refStack.push referenceSchema
    }

    ReferenceSchema popRefStack() {
        //println "- refStack:" + getEClassName(refStack.peek())
        if(refStack.size()){
            refStack.pop()
        } else {
            println 'WARN: refStack was empty'
        }
    }

    void pushPropertyNameStack(String name) {
        //println "+ propNameStack: " + getEClassName(refStack.peek()) + ': ' + name
        propertyNameStack.push name
    }

    String popPropertyNameStack() {
        //println "- propNameStack: " + getEClassName(refStack.peek()) + ': ' + propertyNameStack.peek()
        if(propertyNameStack.size()){
            propertyNameStack.pop()
        } else {
            println 'WARN: propertyNameStack was empty'
        }
    }

}