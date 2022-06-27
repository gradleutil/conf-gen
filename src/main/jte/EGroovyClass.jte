@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@param EClass eClass
@param EPackage ePackage

@ToStringIncludeNames
class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif{
@template.EGroovyClassConstructor(eClass=eClass, ePackage=ePackage)
@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    !{String optional = (prop.lowerBound == 0) ? "@Optional" : ""; }
    @if(prop.eType.equalsIgnoreCase("enum"))
    ${ optional }
    ${ prop.eType.toLowerCase() } ${prop.name} ${prop.asEnum()}
    @elseif (prop.upperBound > 1 || prop.upperBound == -1)
    ${ optional }
    List<${ prop.eType }> ${prop.name} @if(prop.defaultValue != null) = ${prop.defaultValue.toString()} as List<${ prop.eType }>@endif
    @else
    ${ optional }
    ${ prop.eType } ${prop.name} @if(prop.defaultValue != null) = ${prop.defaultValue.toString()} @endif
    @endif
@endfor
}