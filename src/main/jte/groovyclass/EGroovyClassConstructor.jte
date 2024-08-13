@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions

@param EClass eClass
@param TransformOptions options

    ${ eClass.name }(){ }

@if (eClass.equals(options.ePackage().eClassifiers.get(0)))
    ${ eClass.name }(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

    ${ eClass.name }(String conf, Boolean ignoreMissingProperties = false){
        setBeanFromConf(this, conf, ignoreMissingProperties)
    }
@endif

