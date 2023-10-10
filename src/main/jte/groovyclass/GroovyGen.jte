@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.transform.TransformOptions

@param TransformOptions options
package ${ options.getePackage().name }

@template.groovyclass.EGroovyClassImports()
@for (EClassifier configClass : options.getePackage().eClassifiers)
    @template.groovyclass.EGroovyClass(eClass=(EClass)configClass, ePackage = options.getePackage(), options = options)
@endfor