package net.gradleutil.conf.transform.schema

import net.gradleutil.conf.json.schema.ArraySchema
import net.gradleutil.conf.json.schema.CombinedSchema
import net.gradleutil.conf.json.schema.GeneratorVisitor
import net.gradleutil.conf.json.schema.ObjectSchema
import net.gradleutil.conf.json.schema.ReferenceSchema
import net.gradleutil.conf.json.schema.Schema
import net.gradleutil.conf.json.schema.SchemaLocation
import net.gradleutil.conf.json.JSONObject

class SchemaToReferenceSchema {

    static ReferenceSchema schemaToReferenceSchema(Schema schema, String ref, String schemaProperty = 'http://json-schema.org/draft-07/schema#') {
        def referenceBuilder = ReferenceSchema.builder().tap { refValue("#/definitions/${ref}") }
        referenceBuilder.unprocessedProperties.put('$schema', schemaProperty)
        def referenceSchema = toReferenceSchema(referenceBuilder, schema, ref)
        setUnprocessedPropertyDefinitions(referenceSchema)
        referenceSchema
    }


    static void setUnprocessedPropertyDefinitions(Schema referenceSchema){
        def  definitions =   [:] as Map<String, Object>
        def visitor = new GeneratorVisitor(){
            @Override
            void visit(Schema schema) {
                if(schema instanceof ReferenceSchema){
                    definitions.put(getRef(schema),new JSONObject(schema.referredSchema?.toString()))
                }
                super.visit(schema)
            }
        }
        visitor.visit(referenceSchema)
        referenceSchema.unprocessedProperties.definitions = definitions
    }


    @SuppressWarnings('GroovyAccessibility')
    static String getRef(ReferenceSchema schema) {
        def ref = schema.refValue.toString().split('/')?.last()
        return ref
    }

    static ReferenceSchema toReferenceSchema(Schema.Builder<ReferenceSchema> referenceBuilder, Schema schema, String ref) {
        getReferenceSchema(referenceBuilder, schema , ref)
    }

    static ReferenceSchema toReferenceSchema(Schema schema, String ref, String schemaProperty = null) {
        def referenceBuilder = ReferenceSchema.builder().tap { refValue("#/definitions/${ref}") }
        getReferenceSchema(referenceBuilder, schema , ref)
    }

    static ReferenceSchema getReferenceSchema(Schema.Builder<ReferenceSchema> referenceBuilder, Schema schema, String ref) {
        def referenceSchema
        if (schema instanceof ObjectSchema) {
            referenceSchema = buildReferenceSchema(ref, referenceBuilder, schema as ObjectSchema)
        } else if (schema instanceof CombinedSchema) {
            referenceSchema = buildReferenceSchema(referenceBuilder, schema as CombinedSchema)
        } else {
            throw new RuntimeException("Ahhhhhhhhhhhhhhhhhhhhhh ${schema.class}")
        }
        referenceSchema
    }

    static ReferenceSchema buildReferenceSchema(String ref, Schema.Builder<ReferenceSchema> referenceBuilder, ObjectSchema sourceObjectSchema, Boolean toSingularNames = false) {
        def objectBuilder = ObjectSchema.builder()
        objectBuilder.title(ref)
        objectBuilder.schemaLocation(SchemaLocation.parseURI('#/definitions/' + ref))

        sourceObjectSchema.propertySchemas.each { String key, Schema propertySchema ->
            if (propertySchema instanceof ObjectSchema) {
                def refSchema = toReferenceSchema(propertySchema as ObjectSchema, key)
                objectBuilder.addPropertySchema(key, refSchema)
            } else if (propertySchema instanceof ArraySchema) {
                def firstItem = propertySchema.allItemSchema ?: propertySchema.itemSchemas.first()
                if(firstItem instanceof ObjectSchema){
                    def firstProp = firstItem.propertySchemas.entrySet().first()
                    if(firstItem.propertySchemas.size() == 1  && firstProp.value instanceof ObjectSchema){
                        // single keyed array item infers sub-object, so use the key for the field name and add the sub-object type
                        def refSchema = toReferenceSchema(firstProp.value, firstProp.key)
                        def arraySchema = ArraySchema.builder().allItemSchema(refSchema).build()
                        objectBuilder.addPropertySchema(key, arraySchema)
                    } else {
                        def refSchema = toReferenceSchema(firstItem as ObjectSchema, key)
                        def arraySchema = ArraySchema.builder().allItemSchema(refSchema).build()
                        objectBuilder.addPropertySchema(key, arraySchema)
                    }
                } else {
                    objectBuilder.addPropertySchema(key, propertySchema)
                }
            } else {
                objectBuilder.addPropertySchema(key, propertySchema)
            }
        }

        sourceObjectSchema.requiredProperties.each { requiredName ->
            objectBuilder.addRequiredProperty(requiredName)
        }
        if (sourceObjectSchema.schemaOfAdditionalProperties) {
            objectBuilder.schemaOfAdditionalProperties(sourceObjectSchema.schemaOfAdditionalProperties)
        }
        if (sourceObjectSchema.propertyNameSchema) {
            objectBuilder.propertyNameSchema(sourceObjectSchema.propertyNameSchema)
        }
        def objectSchema = objectBuilder.build()
        def referenceSchema = referenceBuilder.build()
        referenceSchema.setReferredSchema(objectSchema)
        referenceSchema
    }



    static ReferenceSchema buildReferenceSchema(Schema.Builder<ReferenceSchema> referenceBuilder, CombinedSchema combinedSchema, Boolean toSingularNames = false) {
        def referenceSchema = referenceBuilder.build()
        referenceSchema.setReferredSchema(combinedSchema)
        referenceSchema
    }



}
