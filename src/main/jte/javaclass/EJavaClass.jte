@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaSetter
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaType

@param EClass eClass
@param TransformOptions options
package ${ options.ePackage().name };

@if(!eClass.geteSuperTypes().isEmpty() && eClass.geteSuperTypes().get(0).toLowerCase() == "enum")
public enum ${eClass.name} ${eClass.getEStructuralFeatures().get(0).asEnum()}
@else
import java.io.File;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaType(prop)};
@endfor

@template.javaclass.EJavaClassConstructor(eClass=eClass)

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaSetter(eClass.name, prop)}
@endfor
}
@endif
