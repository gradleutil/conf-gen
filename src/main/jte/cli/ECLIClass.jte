@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaSetter
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaType


@param EClass eClass
@param TransformOptions options
package ${ options.ePackage().name };


import java.io.File;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "${eClass.name}", mixinStandardHelpOptions = true, version = "defaults 0.1")

public class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaType(prop)};
@endfor

@template.cli.EJavaClassConstructor(eClass=eClass)

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    @Option(names = "-${eClass.name}.${prop.name}", description = "${prop.name}")
    public ${javaSetter(eClass.name, prop)};
@endfor


}