package net.gradleutil.conf.transform.hocon

import net.gradleutil.conf.config.ConfigObject
import net.gradleutil.conf.config.ConfigValue
import net.gradleutil.conf.config.ConfigValueType
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.conf.json.schema.*

class HoconToSchema {

    static ObjectSchema toObjectSchema(ConfigObject config, String schemaProperty = null, Boolean toSingularNames = false) {
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

    static Schema toSchema(ConfigValue configValue) {
        assert configValue != null
        switch (configValue.valueType()) {
            case ConfigValueType.STRING:
                return new StringSchema()
                break
            case ConfigValueType.LIST:
                return ArraySchema.builder().allItemSchema(toSchema((configValue as List<ConfigValue>).first())).build()
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