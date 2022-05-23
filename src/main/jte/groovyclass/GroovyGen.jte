@import net.gradleutil.conf.template.*

@param EPackage ePackage
package ${ ePackage.name }

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
@AnnotationCollector
@interface ToStringIncludeNames { }

@interface Optional { }

@for (EClassifier configClass : ePackage.eClassifiers)
    @template.EGroovyClass(eClass=(EClass)configClass, ePackage = ePackage)
@endfor