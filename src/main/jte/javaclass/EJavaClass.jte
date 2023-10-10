@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaSetter
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaType


@param EClass eClass
@param TransformOptions options
package ${ options.getePackage().name };


import java.io.File;
import java.io.Serializable;
import java.util.List;

public class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaType(prop)};
@endfor

@template.javaclass.EJavaClassConstructor(eClass=eClass)

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaSetter(eClass.name, prop)};
@endfor


}