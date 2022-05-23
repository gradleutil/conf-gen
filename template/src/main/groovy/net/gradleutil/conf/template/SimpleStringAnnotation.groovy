package net.gradleutil.conf.template

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false, includeSuperFields = true)
@Immutable
@AnnotationCollector
interface SimpleStringAnnotation { }