package net.gradleutil.conf.util

import net.gradleutil.conf.Loader
import net.gradleutil.conf.json.schema.ReferenceSchema
import net.gradleutil.conf.json.schema.Schema
import net.gradleutil.conf.dna.common.text.Inflector
import net.gradleutil.conf.json.JSONObject
import net.gradleutil.conf.transform.hocon.HoconToSchema
import net.gradleutil.conf.transform.schema.SchemaToReferenceSchema

import java.util.regex.Pattern

class GenUtil {


    static Schema configFileToSchema(File configFile) {
        def config = Loader.resolveWithSystem(configFile).root()
        HoconToSchema.toObjectSchema(config)
    }

    static Schema confToSchema(String conf) {
        def config = Loader.resolveStringWithSystem(conf).root()
        HoconToSchema.toObjectSchema(config)
    }

    static ReferenceSchema configFileToReferenceSchema(File configFile, String ref) {
        def config = Loader.resolveWithSystem(configFile).root()
        SchemaToReferenceSchema.schemaToReferenceSchema(HoconToSchema.toObjectSchema(ref, config), ref)
    }

    static ReferenceSchema confToReferenceSchema(String conf, String ref) {
        def config = Loader.resolveStringWithSystem(conf).root()
        SchemaToReferenceSchema.schemaToReferenceSchema(HoconToSchema.toObjectSchema(ref, config), ref)
    }

    static String configFileToSchemaJson(File configFile) {
        return new JSONObject(configFileToSchema(configFile).toString()).toString(4)
    }

    static String confToSchemaJson(String conf) {
        return new JSONObject(confToSchema(conf).toString()).toString(4)
    }

    static File configFileToSchemaFile(File configFile, File schemaFile) {
        schemaFile.text = configFileToSchemaJson(configFile)
        schemaFile
    }

    static File confToSchemaFile(String conf, File schemaFile) {
        schemaFile.text = confToSchemaJson(conf)
        schemaFile
    }

    static String configFileToReferenceSchemaJson(File configFile, String ref) {
        new JSONObject(configFileToReferenceSchema(configFile, ref).toString()).toString(2)
    }

    static String confToReferenceSchemaJson(String conf, String ref) {
        new JSONObject(confToReferenceSchema(conf, ref).toString()).toString(2)
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
