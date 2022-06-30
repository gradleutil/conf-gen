@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@param EClass eClass
@param EPackage ePackage
@param TransformOptions options

@ToStringIncludeNames
class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {
@template.groovyclass.EGroovyClassConstructor(eClass=eClass, ePackage=ePackage, options = options)
@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    !{String optional = (prop.lowerBound == 0) ? "@Optional" : ""; }
    @if(prop.eType.equalsIgnoreCase("enum"))
        !{String propCap = prop.name.substring(0, 1).toUpperCase() + prop.name.substring(1); }
        ${ optional }
        ${propCap} ${prop.name}

        ${ prop.eType.toLowerCase() } ${propCap} ${prop.asEnum()}
    @elseif (prop.upperBound > 1 || prop.upperBound == -1)
        ${ optional }
        List<${ prop.eType }> ${prop.name} = @if(prop.defaultValue != null) ${prop.defaultValue.toString()} @else [] @endif as List<${ prop.eType }>
    @else
        ${ optional }
        ${ prop.eType } ${prop.name} @if(prop.defaultValue != null) = ${prop.defaultValue.toString()} @endif
    @endif
@endfor
}