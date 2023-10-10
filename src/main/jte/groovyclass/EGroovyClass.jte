@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@import static net.gradleutil.conf.transform.groovy.EPackageRenderer.featureToClassProp

@param EClass eClass
@param EPackage ePackage
@param TransformOptions options

@if (!options.getSingleFile()) @template.groovyclass.EGroovyClassImports() @endif

@ToStringIncludeNames
class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {
@template.groovyclass.EGroovyClassConstructor(eClass=eClass, options = options)
@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    ${featureToClassProp(prop)}
@endfor
}