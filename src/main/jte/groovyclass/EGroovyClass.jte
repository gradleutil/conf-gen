@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@import static net.gradleutil.conf.transform.groovy.EPackageRenderer.featureToClassProp

@param EClass eClass
@param EPackage ePackage
@param TransformOptions options

@if (!options.singleFile()) @template.groovyclass.EGroovyClassImports() @endif

@if(!eClass.geteSuperTypes().isEmpty() && eClass.geteSuperTypes().get(0).toLowerCase() == "enum")
enum ${eClass.name} ${eClass.getEStructuralFeatures().get(0).asEnum()}
@else
@ToStringIncludeNames
class ${eClass.name} @if (!eClass.geteSuperTypes().isEmpty()) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {
@template.groovyclass.EGroovyClassConstructor(eClass=eClass, options = options)
@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    ${featureToClassProp(prop)}
@endfor
}
@endif