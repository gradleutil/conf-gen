@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.transform.TransformOptions

@param EPackage ePackage
@param TransformOptions options
@for (EClassifier configClass : ePackage.eClassifiers)
    @if(configClass.name.contains("."))
    @else
        @template.javaclass.EJavaClass(eClass=(EClass)configClass, options = options)
    @endif
@endfor