package net.gradleutil.conf.transform

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import net.gradleutil.conf.json.schema.Schema
import net.gradleutil.conf.template.EPackage

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class TransformOptions {
    enum Type{
        java,
        groovy
    }
    Schema schema
    String packageName
    String rootClassName
    File jteDirectory
    File sourceDirectory
    String jteRenderPath
    Map<String,Object> renderParams = [:]
    File outputFile
    String jsonSchema
    Boolean convertToCamelCase
    Boolean singleFile
    Type toType
    EPackage ePackage
    ClassLoader classLoader = TransformOptions.classLoader
}
