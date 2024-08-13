package net.gradleutil.conf.template

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false, includeSuperFields = true)
@AnnotationCollector
interface SimpleStringAnnotation { }