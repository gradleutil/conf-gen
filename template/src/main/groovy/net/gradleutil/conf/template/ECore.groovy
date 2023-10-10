package net.gradleutil.conf.template

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


@interface Optional {}

@SimpleStringAnnotation
class EDataType extends EClassifier {

    public String serializable

    EDataType() {}

}

@SimpleStringAnnotation
class EClassifier extends ENamedElement {

    public String instanceTypeName

    public String defaultValue

    public String instanceClassName

    public List<ETypeParameter> eTypeParameters

    public String instanceClass

    EClassifier() {}

}

@SimpleStringAnnotation
class ENamedElement extends EModelElement {

    public String name

    void setName(String name) {
        this.name = name
    }

    ENamedElement() {}

}

@SimpleStringAnnotation
class EModelElement {

    public List<EAnnotation> eAnnotations

    EModelElement() {}

}

@SimpleStringAnnotation
class EAnnotation {

    public List<String> references

    public List<String> contents

    public List<EStringToStringMapEntry> details

    public String source

    EAnnotation() {}

}

@SimpleStringAnnotation
class EStringToStringMapEntry {

    public String value

    public String key

    EStringToStringMapEntry() {}

}

@SimpleStringAnnotation
class ETypeParameter extends ENamedElement {

    public List<EGenericType> eBounds

    ETypeParameter() {}

}

@SimpleStringAnnotation
class EGenericType {

    public String eClassifier

    public String eTypeParameter

    public List<EGenericType> eTypeArguments

    public String eRawType

    EGenericType() {}

}

@SimpleStringAnnotation
class EStructuralFeature extends ETypedElement {

    public String defaultValueLiteral

    public Boolean unsettable

    public String defaultValue

    public Boolean _volatile

    public Boolean changeable

    public Boolean _transient

    public Boolean derived

    public List<Object> valueList

    EStructuralFeature() {}


    String asEnum() {
        def enums = valueList.collect { "${toEnumValue(it as String)}(\"${it}\")" }.join(',')
        return """ {
            ${enums};

            private final String name;
        
            private ${name.capitalize()}(String s) {
                name = s;
            }
        
            public boolean equalsName(String otherName) {
                return name.equals(otherName);
            }
        
            public String toString() {
               return this.name;
            }
        }
        """
    }

    static String toEnumValue(String string) {
        string.replaceAll("[^A-Za-z0-9]+", '_').toUpperCase().with {
            if (Character.isDigit(string.charAt(0))) {
                'V' + it
            } else {
                it
            }
        }
    }
}


@SimpleStringAnnotation
class ETypedElement extends ENamedElement {

    public Boolean ordered = false

    public Integer upperBound = 1

    public Boolean unique = false

    public String eType

    void setEType(String eType) {
        this.eType = eType
    }

    void setEType(EDataType eType) {
        this.eType = eType.name
    }

    public Integer lowerBound = 1

    public Boolean many = false

    public Boolean required = false

    ETypedElement() {}

}

@SimpleStringAnnotation
class EEnum extends EDataType {

    public List<EEnumLiteral> eLiterals

    EEnum() {}

}

@SimpleStringAnnotation
class EEnumLiteral extends ENamedElement {

    public String instance

    public String value

    public String literal

    EEnumLiteral() {}

}

@SimpleStringAnnotation
class EPackage extends ENamedElement {

    public String nsPrefix

    public String nsURI

    public List<EClassifier> eClassifiers = [] as List<EClassifier>

    public List<EPackage> eSubpackages

    public String eFactoryInstance

    EPackage() {}

    List<EClassifier> getEClassifiers() {
        return eClassifiers
    }

    EClassifier getEClassifier(String name) {
        return eClassifiers.find { it.name == name }
    }

    void setNsURI(String nsURI) {
        this.nsURI = nsURI
    }

    void setNsPrefix(String nsPrefix) {
        this.nsPrefix = nsPrefix
    }

    static eINSTANCE = new EInstance()
}


class EString extends EDataType {
}

class EBoolean extends EDataType {
}

class EBigInteger extends EDataType {
}

class ELong extends EDataType {
}

class EInstance {
    static ECorePackage ecorePackage = new ECorePackage()

    static EPackage createEPackage() {
        return new EPackage()
    }

    static EClass createEClass() {
        return new EClass()
    }

    static EAttribute createEAttribute() {
        return new EAttribute()
    }

    static EReference createEReference() {
        return new EReference()
    }

}

class ECorePackage {
    static def getEString() {
        return new EString(name: 'String')
    }

    static def getELong() {
        return new ELong(name: 'Long')
    }

    static def getEBigInteger() {
        return new EBigInteger(name: 'BigInteger')
    }

    static def getEBoolean() {
        return new EBoolean(name: 'Boolean')
    }
}

@SimpleStringAnnotation
class EFactory extends EModelElement {

    String ePackage

    EFactory() {}

}

@SimpleStringAnnotation
class EAttribute extends EStructuralFeature {

    String eAttributeType

    String id

    EAttribute() {}

}

@SimpleStringAnnotation
class EClass extends EClassifier {

    List<EStructuralFeature> eStructuralFeatures = [] as List<EStructuralFeature>

    List<String> eAllAttributes = []

    List<EGenericType> eGenericSuperTypes = []

    List<String> eAttributes = []

    List<String> eSuperTypes = []

    List<String> eAllContainments = []

    String _interface

    List<String> eAllOperations = []

    String _abstract

    List<String> eAllStructuralFeatures = []

    String eidAttribute

    List<String> eAllReferences = []

    List<String> eReferences = []

    List<String> eAllSuperTypes = []

    List<String> eAllGenericSuperTypes = []

    List<EOperation> eOperations = [] as List<EOperation>

    List<EStructuralFeature> getEStructuralFeatures() {
        return eStructuralFeatures
    }

    EClass() {}

}

@SimpleStringAnnotation
class EOperation extends ETypedElement {

    List<EGenericType> eGenericExceptions

    List<ETypeParameter> eTypeParameters

    List<EParameter> eParameters

    List<String> eExceptions

    EOperation() {}

}

@SimpleStringAnnotation
class EParameter extends ETypedElement {

    EParameter() {}

}

@SimpleStringAnnotation
class EReference extends EStructuralFeature {

    String container

    String containment

    String resolveProxies

    String eOpposite

    List<String> eKeys

    String eReferenceType

    EReference() {}

}
