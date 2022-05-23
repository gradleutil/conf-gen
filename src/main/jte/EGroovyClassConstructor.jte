@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.template.EPackage

@param EClass eClass
@param EPackage ePackage

    ${ eClass.name }(){ }

@if (eClass.equals(ePackage.eClassifiers.get(0)))
    ${ eClass.name }(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }
@endif

