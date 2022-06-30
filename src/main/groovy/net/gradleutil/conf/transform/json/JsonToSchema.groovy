package net.gradleutil.conf.transform.json

import net.gradleutil.conf.annotation.ToStringIncludeNames


@ToStringIncludeNames
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

}
