package net.gradleutil.conf.transform.hocon

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import net.gradleutil.conf.config.ConfigObject
import net.gradleutil.conf.config.ConfigValue
import net.gradleutil.conf.config.ConfigValueType
import net.gradleutil.conf.transform.schema.ArraySchema
import net.gradleutil.conf.transform.schema.BooleanSchema
import net.gradleutil.conf.transform.schema.NullSchema
import net.gradleutil.conf.transform.schema.NumberSchema
import net.gradleutil.conf.transform.schema.ObjectSchema
import net.gradleutil.conf.transform.schema.StringSchema
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.conf.json.schema.*

class HoconToSchema {

    static JsonNode toObjectSchema(ConfigObject config, String schemaProperty = null, Boolean toSingularNames = false) {
        def objectBuilder = ObjectSchema.builder()
        if (schemaProperty) {
            objectBuilder.unprocessedProperties.put('$schema', schemaProperty)
        }
        def propertySchemas = config.keySet().findAll { it != '$schema' }.collect {
            [(it): toSchema(config.get(it))]
        }
        propertySchemas*.each { key, schema ->
            objectBuilder.addPropertySchema(key, schema)
        }
        def objectSchema = objectBuilder.build()
        objectSchema
    }

    static ObjectSchema toObjectSchema(String ref, ConfigObject config, String schemaProperty = null, Boolean toSingularNames = false) {
        def refConfig
        def refName = toSingularNames ? GenUtil.inflector.singularize(ref) : ref
        if (config.keySet().size() != 1) {
            refConfig = config.atKey(refName).root()
        } else {
            def first = config.get(config.keySet().first())
            switch (first.valueType()) {
                case ConfigValueType.OBJECT:
                    refConfig = config.withValue(refName, first).withOnlyKey(refName)
                    break
                default:
                    refConfig = config.atKey(refName).root()
            }
        }
        toObjectSchema(refConfig, null, toSingularNames)
    }

    static JsonSchema toSchema(ConfigValue configValue) {
        assert configValue != null
        switch (configValue.valueType()) {
            case ConfigValueType.STRING:
                return new StringSchema()
                break
            case ConfigValueType.LIST:
                def listVals = (configValue as List<ConfigValue>)
                if(listVals){
                    return ArraySchema.builder().allItemSchema(toSchema(listVals.first())).build()
                } else {
                    return ArraySchema.builder().build()
                }
                break
            case ConfigValueType.NUMBER:
                return new NumberSchema()
                break
            case ConfigValueType.BOOLEAN:
                return BooleanSchema.INSTANCE
                break

            case ConfigValueType.NULL:
                return NullSchema.INSTANCE
                break

            case ConfigValueType.OBJECT:
                toObjectSchema(configValue as ConfigObject)
                break
            default:
                throw new IllegalArgumentException("No ${configValue.valueType()}")

        }
    }
}