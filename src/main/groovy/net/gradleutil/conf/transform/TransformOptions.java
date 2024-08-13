package net.gradleutil.conf.transform;

import com.networknt.schema.JsonSchema;
import net.gradleutil.conf.template.EPackage;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransformOptions {
    public JsonSchema schema;
    public String packageName;
    public String rootClassName;
    public File jteDirectory;
    public Boolean jarJteDirectory;
    public File sourceDirectory;
    public String basePath;
    public String jteRenderPath;
    public Map<String, Object> renderParams = new LinkedHashMap<>();
    public File outputFile;
    public String jsonSchema;
    public Boolean convertToCamelCase;
    public Boolean singleFile;
    public Type toType;
    public EPackage ePackage;
    public ClassLoader classLoader = TransformOptions.class.getClassLoader();

    public TransformOptions schema(JsonSchema schema) {
        this.schema = schema;
        return this;
    }

    public TransformOptions packageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public TransformOptions rootClassName(String rootClassName) {
        this.rootClassName = rootClassName;
        return this;
    }

    public TransformOptions jteDirectory(File jteDirectory) {
        this.jteDirectory = jteDirectory;
        return this;
    }

    public TransformOptions jarJteDirectory(Boolean jarJteDirectory) {
        this.jarJteDirectory = jarJteDirectory;
        return this;
    }

    public TransformOptions sourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        return this;
    }

    public TransformOptions jteRenderPath(String jteRenderPath) {
        this.jteRenderPath = jteRenderPath;
        return this;
    }

    public TransformOptions basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public TransformOptions renderParams(Map<String, Object> renderParams) {
        this.renderParams.putAll(renderParams);
        return this;
    }

    public TransformOptions outputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public TransformOptions jsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
        return this;
    }

    public TransformOptions convertToCamelCase(Boolean convertToCamelCase) {
        this.convertToCamelCase = convertToCamelCase;
        return this;
    }

    public TransformOptions singleFile(Boolean singleFile) {
        this.singleFile = singleFile;
        return this;
    }

    public TransformOptions toType(Type toType) {
        this.toType = toType;
        return this;
    }

    public TransformOptions ePackage(EPackage ePackage) {
        this.ePackage = ePackage;
        return this;
    }

    public TransformOptions classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    // Getter methods without "get" prefix
    public JsonSchema schema() {
        return schema;
    }

    public String packageName() {
        return packageName;
    }

    public String rootClassName() {
        return rootClassName;
    }

    public File jteDirectory() {
        return jteDirectory;
    }

    public Boolean jarJteDirectory() { return jarJteDirectory; }

    public File sourceDirectory() {
        return sourceDirectory;
    }

    public String basePath() {
        return basePath;
    }
    public String jteRenderPath() {
        return jteRenderPath;
    }

    public Map<String, Object> renderParams() {
        return renderParams;
    }

    public File outputFile() {
        return outputFile;
    }

    public String jsonSchema() {
        return jsonSchema;
    }

    public Boolean convertToCamelCase() {
        return convertToCamelCase;
    }

    public Boolean singleFile() {
        return singleFile;
    }

    public Type toType() {
        return toType;
    }

    public EPackage ePackage() {
        return ePackage;
    }

    public ClassLoader classLoader() {
        return classLoader;
    }

    public enum Type {
        java, groovy, cli, jpa
    }

}