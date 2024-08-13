@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.transform.TransformOptions

@param EPackage ePackage
@param TransformOptions options
@for (EClassifier configClass : ePackage.eClassifiers)
 
    @template.cli.ECLIClass(eClass=(EClass)configClass, options = options)
@endfor