package net.gradleutil.conf.transform

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import net.gradleutil.conf.json.schema.Schema

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class TransformOptions {
    Schema schema
    String packageName
    String rootClassName
    File outputFile
    String jsonSchema
    Boolean convertToCamelCase
    ClassLoader classLoader = TransformOptions.classLoader
}

