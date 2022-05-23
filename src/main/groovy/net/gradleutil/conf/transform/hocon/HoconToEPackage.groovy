package net.gradleutil.conf.transform.hocon

import net.gradleutil.conf.config.Config
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.template.ECore
import net.gradleutil.conf.util.GenUtil
import net.gradleutil.conf.json.JSONObject
import net.gradleutil.conf.json.schema.Schema

class HoconToEPackage {

    static ECore ePackage
    static String rootClassName
    static Boolean convertToCamelCase

    static Map<String, Object> toMap(Schema schema) {
        Transformer.toMap(new JSONObject(schema.toString()))
    }

    static ident(String string, Boolean upperCamel = false) {
        GenUtil.ident(string, convertToCamelCase, upperCamel)
    }

    static ECore getEPackage(Config config, String rootClassName, String packageName, Boolean convertToCamelCase = true) {
        def rootName = ident rootClassName, true
        ePackage = new ECore(name: packageName)
        this.convertToCamelCase = convertToCamelCase
        this.rootClassName = rootName
        def packageClass
        
        return ePackage
    }


}