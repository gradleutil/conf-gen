@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaSetter
@import static net.gradleutil.conf.transform.java.EPackageRenderer.javaType


@param EClass eClass
@param TransformOptions options
package ${ options.ePackage().name };

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.File;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.List;

import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "${eClass.name}")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ${eClass.name} @if (eClass.geteSuperTypes().size() > 0) extends ${eClass.geteSuperTypes().get(0)} @endif implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    public Long getId() {
        return this.id;
    }
    
    public ${eClass.name} id(Long id) {
        this.setId(id);
        return this;
    }
    
    public void setId(Long id) {
        this.id = id;
    }


@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaType(prop)};
@endfor

@template.javaclass.EJavaClassConstructor(eClass=eClass)

@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    public ${javaSetter(eClass.name, prop)}
@endfor


}