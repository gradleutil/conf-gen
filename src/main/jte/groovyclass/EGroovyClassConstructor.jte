@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.template.EPackage
@import net.gradleutil.conf.transform.TransformOptions

@param EClass eClass
@param EPackage ePackage
@param TransformOptions options

    ${ eClass.name }(){ }

@if (eClass.equals(ePackage.eClassifiers.get(0)))
    ${ eClass.name }(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

    ${ eClass.name }(String conf, Boolean ignoreMissingProperties = false){
        setBeanFromConf(this, conf, ignoreMissingProperties)
    }
@endif

