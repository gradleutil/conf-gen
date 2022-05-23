@import net.gradleutil.conf.template.*
@param EClass eClass

@ToPrettyString
class ${eClass.instanceClassName} extends Block {
@for (EStructuralFeature prop : eClass.getEStructuralFeatures())
    @if (prop.lowerBound < 1)
    @Optional
    @endif
    @if(prop.eType.toLowerCase().equals("enum"))
    ${ prop.eType.toLowerCase() } ${prop.name} ${prop.asEnum()}

    @elseif (prop instanceof EReference)

    ${ prop.eType } ${prop.name} @if(prop.defaultValue != null) = ${prop.defaultValue.toString()} @endif
        ${ prop.eType } ${prop.name}( @DelegatesTo( value = ${ prop.eType }.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
        ${prop.eType} = new ${ prop.eType }( configurer: configurer )
        ${prop.name}
    }

    @elseif (prop.upperBound > 1 || prop.upperBound == -1)

    List<${ prop.eType }> ${prop.name} = [] as List<${ prop.eType }>
    ${ prop.eType } ${prop.name}( @DelegatesTo( value = ${ prop.eType }.class, strategy = Closure.DELEGATE_FIRST ) Closure<?> configurer ) {
        ${prop.name}.add( new ${ prop.eType }( configurer: configurer ) )
    }

    @else

    ${ prop.eType } ${prop.name} @if(prop.defaultValue != null) = ${prop.defaultValue.toString()} @endif

    @endif
@endfor
}