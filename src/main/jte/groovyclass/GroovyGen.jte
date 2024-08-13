@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.transform.TransformOptions

@param TransformOptions options
package ${ options.ePackage().name }

@template.groovyclass.EGroovyClassImports()
@for (EClassifier configClass : options.ePackage().eClassifiers)
    @template.groovyclass.EGroovyClass(eClass=(EClass)configClass, ePackage = options.ePackage(), options = options)
@endfor