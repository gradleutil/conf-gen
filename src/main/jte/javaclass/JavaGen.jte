@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.transform.TransformOptions

@param EPackage ePackage
@param TransformOptions options
@for (EClassifier configClass : ePackage.eClassifiers)
 
    @template.javaclass.EJavaClass(eClass=(EClass)configClass, options = options)
@endfor