package net.gradleutil.conf.transform.hocon

import net.gradleutil.conf.config.Config
import net.gradleutil.conf.json.JSONObject
import net.gradleutil.conf.json.schema.Schema
import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.transform.Transformer
import net.gradleutil.conf.util.ConfUtil

class HoconToEPackage {

    static EPackage ePackage
    static String rootClassName
    static Boolean convertToCamelCase

    static Map<String, Object> toMap(Schema schema) {
        Transformer.toMap(new JSONObject(schema.toString()))
    }

    static ident(String string, Boolean upperCamel = false) {
        ConfUtil.ident(string, convertToCamelCase, upperCamel)
    }

    static EPackage getEPackage(Config config, String rootClassName, String packageName, Boolean convertToCamelCase = true) {
        def rootName = ident rootClassName, true
        ePackage = new EPackage(name: packageName)
        this.convertToCamelCase = convertToCamelCase
        this.rootClassName = rootName
        def packageClass

        return ePackage
    }


}