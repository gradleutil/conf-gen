package net.gradleutil.conf.transform


import net.gradleutil.conf.transform.json.JsonToSchema
import net.gradleutil.conf.json.schema.Schema
import net.gradleutil.conf.json.JSONArray
import net.gradleutil.conf.json.JSONObject

class Transformer {

    Transformer() {}

    static TransformOptions defaultOptions() {
        new TransformOptions().convertToCamelCase(true)
    }

    static void editor(File path, Schema schema, String json) {
        path.text = JsonToSchema.editor(schema.toString(), json)
    }

    static Map<String, Object> toMap(JSONObject jsonObject, List<String> definitionTypes = []) {
        Map<String, Object> map = new HashMap<String, Object>()
        Iterator<String> keys = jsonObject.keys()
        def key, value
        while (keys.hasNext()) {
            key = keys.next()
            value = jsonObject.get(key)
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value, definitionTypes)
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value, definitionTypes)
            } else if (value instanceof String && value.startsWith('#') && value.length() > 1) {
                def definition = value.toString().replace('#/definitions/', '')
                if (!definitionTypes.find { it == definition }) {
                    def closeMatch = definitionTypes.findAll { it.contains(definition) }.sort { it.length() }.find()
                    if (closeMatch) {
                        value = "#/definitions/" + closeMatch
                    } else {
                        key = 'type'
                        value = 'object'
                    }
                }
            }
            map.put(key, value)
        }
        return map
    }

    static List<Object> toList(JSONArray array, List<String> definitionTypes = []) {
        List<Object> list = new ArrayList<Object>()
        def value
        for (int i = 0; i < array.length(); i++) {
            value = array.get(i)
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value, definitionTypes)
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value, definitionTypes)
            }
            list.add(value)
        }
        return list
    }


}
