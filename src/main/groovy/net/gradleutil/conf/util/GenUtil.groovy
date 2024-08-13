package net.gradleutil.conf.util

import com.networknt.schema.JsonSchema
import net.gradleutil.conf.Loader
import net.gradleutil.conf.json.JSONObject
import net.gradleutil.conf.json.schema.SchemaUtil
import net.gradleutil.conf.transform.hocon.HoconToSchema
import net.gradleutil.conf.transform.schema.SchemaToReferenceSchema


class GenUtil {


    static JsonSchema configFileToSchema(File configFile) {
        def config = Loader.resolveWithSystem(configFile)
        SchemaUtil.getSchema(config,'conf', configFile.parentFile.absolutePath)
    }

    static JsonSchema confToSchema(String conf, String ref) {
        def config = Loader.resolveStringWithSystem(conf)
        SchemaUtil.getSchema(config,ref, "")
    }

    static JsonSchema configFileToReferenceSchema(File configFile, String ref) {
        def config = Loader.resolveWithSystem(configFile)
        SchemaUtil.getSchema(config,ref, "")
    }

    static JsonSchema confToReferenceSchema(String conf, String ref) {
        def config = Loader.resolveStringWithSystem(conf)
        SchemaUtil.getSchema(config,ref, "")
    }

    static String configFileToSchemaJson(File configFile) {
        return configFileToSchema(configFile).schemaNode.toPrettyString()
    }

    static String confToSchemaJson(String conf, String ref) {
        return confToSchema(conf, ref).schemaNode.toPrettyString()
    }

    static File configFileToSchemaFile(File configFile, File schemaFile) {
        schemaFile.text = configFileToSchemaJson(configFile)
        schemaFile
    }

    static File confToSchemaFile(String conf, String ref, File schemaFile) {
        schemaFile.text = confToSchemaJson(conf, ref)
        schemaFile
    }

    static String configFileToReferenceSchemaJson(File configFile, String ref) {
        configFileToReferenceSchema(configFile, ref).schemaNode.toPrettyString()
    }

    static String confToReferenceSchemaJson(String conf, String ref) {
        confToReferenceSchema(conf, ref).schemaNode.toPrettyString()
    }

    static File configFileToReferenceSchemaFile(File configFile, String ref, File schemaFile) {
        schemaFile.text = configFileToReferenceSchemaJson(configFile, ref)
        schemaFile
    }

    static File confToReferenceSchemaFile(String conf, String ref, File schemaFile) {
        schemaFile.text = confToReferenceSchemaJson(conf, ref)
        schemaFile
    }


}
