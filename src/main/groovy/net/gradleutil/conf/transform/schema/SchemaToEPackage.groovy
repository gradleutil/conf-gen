package net.gradleutil.conf.transform.schema

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

    static Map<String, Object> toMap(Schema schema) {
        Transformer.toMap(new JSONObject(schema.toString()))
    }

    static EPackage getEPackage(Schema sourceSchema,String rootName, String packageName, Boolean safeStuff) {
        def visitor = new SchemaToEPackageVisitor(packageName)
        if(sourceSchema instanceof ReferenceSchema || sourceSchema instanceof CombinedSchema){
            visitor.visit(sourceSchema)
        } else if(sourceSchema instanceof ObjectSchema){
            def name = rootName ?: sourceSchema.propertySchemas.entrySet()?.find()?.key
            visitor.visit(SchemaToReferenceSchema.toReferenceSchema(sourceSchema, name, null))
        }
        return visitor.ePackage
    }

    static EPackage getEPackage(ObjectSchema sourceSchema, String rootName, String packageName, Boolean safeStuff) {
        def visitor = new SchemaToEPackageVisitor(packageName)
        visitor.visit(SchemaToReferenceSchema.toReferenceSchema(sourceSchema, rootName, null))
        return visitor.ePackage
    }


}

