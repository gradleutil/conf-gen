package net.gradleutil.conf.transform.schema

import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import net.gradleutil.conf.json.schema.*
import net.gradleutil.conf.template.*
import net.gradleutil.conf.transform.Transformer

/*
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature
*/
//import static org.eclipse.emf.ecore.EcoreFactory.eINSTANCE

import net.gradleutil.conf.json.JSONObject

class SchemaToEPackage {

    static Map<String, Object> toMap(JsonSchema schema) {
        Transformer.toMap(new JSONObject(schema.toString()))
    }

    static EPackage getEPackage(JsonSchema sourceSchema,String rootName, String packageName, Boolean singularizeClassNames) {
        def visitor = new SchemaToEPackageVisitor(packageName, rootName, singularizeClassNames)
        visitor.visit(sourceSchema, rootName)
        return visitor.ePackage
    }

}

