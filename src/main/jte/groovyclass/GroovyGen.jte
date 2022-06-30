@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.transform.TransformOptions

@param EPackage ePackage
@param TransformOptions options
package ${ ePackage.name }

import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile
import static net.gradleutil.conf.util.ConfUtil.setBeanFromConf

import net.gradleutil.conf.annotation.ToStringIncludeNames
import net.gradleutil.conf.annotation.Optional

@for (EClassifier configClass : ePackage.eClassifiers)
    @template.groovyclass.EGroovyClass(eClass=(EClass)configClass, ePackage = ePackage, options = options)
@endfor