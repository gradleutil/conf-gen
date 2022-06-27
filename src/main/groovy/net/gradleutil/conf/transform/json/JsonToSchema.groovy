package net.gradleutil.conf.transform.json


import net.gradleutil.conf.transform.json.JsonObject as JSONObject

class JsonToSchema {

    static sanitize(Map originalMap) {
        Map<String, Object> map = [:]
        def handle = { v, k ->
            if (['eaterObject', 'json'].contains(k)) return null
            if (v instanceof Map || v instanceof ConfigObject) {
                if (v.size()) {
                    def res = sanitize(v)
                    return res
                }
                return null
            } else if (v instanceof File) {
                return 'file://' + v.path
            }
            return v.hasProperty('eaterObject') ? sanitize(v.eaterObject as Map) : v
        }
        (originalMap as Map<String, Object>)?.each { k, v ->
            if (!['eaterObject', 'json'].contains(k)) {
                def val = handle(v, k)
                if (val != null) {
                    map.put(k, val)
                }
            }
        }
        map
    }


    /**
     * Pretty println JSON
     * @param config
     * @param namespace limit by dotted object path, e.g. `object.subObject' for only object.subObject keys/values
     */
    static void jsonPrint(Object config, String namespace = '') {
        System.out.println(new JSONObject(config).toString())
    }

}
