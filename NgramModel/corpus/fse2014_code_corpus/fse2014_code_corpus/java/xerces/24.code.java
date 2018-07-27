package simpletype;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSFacets;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.validation.ValidationState;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
public class SimpleTypeUsage{
static SchemaDVFactory factory = null;
XSFacets facets = new XSFacets();
short fPresentFacets ;
short fFixedFacets ;
short fFinalSet ;
public SimpleTypeUsage(){
        factory = SchemaDVFactory.getInstance();
} 
private ValidationContext getValidationContext(){
        ValidationState validationState = null;
    validationState = new ValidationState();
        return validationState;
}
public ValidatedInfo validateString(String content, XSSimpleType simpleType){
    ValidatedInfo validatedInfo = new ValidatedInfo();
        ValidationContext validationState = getValidationContext();
    try{
        simpleType.validate(content, validationState, validatedInfo);
    }catch(InvalidDatatypeValueException ex){
        System.err.println(ex.getMessage());
    }
    Object value = validatedInfo.actualValue;
    String normalizedValue = validatedInfo.normalizedValue ;
    XSSimpleType memberType = validatedInfo.memberType ;
    return validatedInfo;
}
public void querySimpleType(XSSimpleType simpleType){
    System.err.println();
    System.err.println( "Properties information of 'Simple Type' definiton schema component" );
    System.err.println();
    if( simpleType.getAnonymous() )
        System.err.println( "Anonymous Simple Type" );
    else{
        System.err.println("'name' \t\t\t\t: " + simpleType.getName()  );
    }
    String targetNameSpace = simpleType.getNamespace() ;
    System.err.println("'target namespace' \t\t: " + targetNameSpace  );
    short variety = simpleType.getVariety();
    printVariety(variety);
    XSTypeDefinition baseType = (XSTypeDefinition)simpleType.getBaseType() ;
    System.err.println("'base type definition' name \t: " + ( baseType != null ? baseType.getName() : "null" )   );
    System.err.println("'base type definition' target namespace : " + ( baseType != null ? baseType.getNamespace() : "null" )   );
    if(baseType != null && (baseType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) ){
        XSSimpleType simpleTypeDecl = (XSSimpleType)baseType;
    }
    short facets = simpleType.getDefinedFacets() ;
    printFacets(facets);
    short finalSet = simpleType.getFinal() ;
    printFinal(finalSet);
    if( variety == XSSimpleType.VARIETY_LIST ){
        XSSimpleType listDecl = (XSSimpleType)simpleType.getItemType();
    }else if(variety == XSSimpleType.VARIETY_UNION ){
        XSObjectList memberTypes = simpleType.getMemberTypes();
    }
    short ordered = simpleType.getOrdered();
    printOrdered(ordered);
    boolean bounded = simpleType.getBounded();    
    if(bounded){
        System.err.println("'bounded' \t\t\t\t: true"  );
    }
    else{
        System.err.println("'bounded' \t\t\t\t: false"  );
    }
    boolean isFinite = simpleType.getFinite();
    printCardinality(isFinite);
    boolean numeric = simpleType.getNumeric();
    if(numeric){
        System.err.println("'numeric' \t\t\t\t: true"  );
    }
    else{
        System.err.println("'numeric' \t\t\t\t: false"  );
    }
}
void printOrdered (short ordered){
    switch(ordered){
        case XSSimpleType.ORDERED_FALSE:
            System.err.println("'ordered' \t\t\t\t: false"  );
            break;
        case XSSimpleType.ORDERED_PARTIAL:
            System.err.println("'ordered' \t\t\t\t: partial"  );
            break;
        case XSSimpleType.ORDERED_TOTAL:
            System.err.println("'ordered' \t\t\t\t: total"  );
            break;
    }
}
void printCardinality (boolean isFinite){
    if(!isFinite)
        System.err.println("'cardinality' \t\t\t\t: countably infinite"  );
    else
        System.err.println("'cardinality' \t\t\t\t: finite"  );
}
void printFacets(short facets){
    System.err.println("'facets' present \t\t: " );
    if(( facets & XSSimpleType.FACET_ENUMERATION) != 0){
        System.err.println("\t\t\t\t ENUMERATION");
    }
    if((facets & XSSimpleType.FACET_LENGTH) != 0){
        System.err.println("\t\t\t\t LENGTH");
    }
    if((facets & XSSimpleType.FACET_MINLENGTH) != 0){
        System.err.println("\t\t\t\t MINLENGTH");
    }
    if((facets & XSSimpleType.FACET_MAXLENGTH) != 0){
        System.err.println("\t\t\t\t MAXLENGTH");
    }
    if((facets & XSSimpleType.FACET_PATTERN) != 0){
        System.err.println("\t\t\t\t PATTERN");
    }
    if((facets & XSSimpleType.FACET_WHITESPACE) != 0){
        System.err.println("\t\t\t\t WHITESPACE");
    }
    if((facets & XSSimpleType.FACET_MAXINCLUSIVE) != 0){
        System.err.println("\t\t\t\t MAXINCLUSIVE");
    }
    if((facets & XSSimpleType.FACET_MAXEXCLUSIVE) != 0){
        System.err.println("\t\t\t\t MAXEXCLUSIVE");
    }
    if((facets & XSSimpleType.FACET_MININCLUSIVE) != 0){
        System.err.println("\t\t\t\t MININCLUSIVE");
    }
    if((facets & XSSimpleType.FACET_MINEXCLUSIVE) != 0){
        System.err.println("\t\t\t\t MINEXCLUSIVE");
    }
    if((facets & XSSimpleType.FACET_TOTALDIGITS) != 0){
        System.err.println("\t\t\t\t TOTALDIGITS");
    }
    if((facets & XSSimpleType.FACET_FRACTIONDIGITS) != 0){
        System.err.println("\t\t\t\t FRACTIONDIGITS");
    }
}
void printFinal(short finalSet){
    System.err.println("'final' values \t\t\t: " );
    if ((finalSet & XSConstants.DERIVATION_EXTENSION ) != 0) {
        System.err.println("\t\t\t\t Extension");
    }
    if ((finalSet & XSConstants.DERIVATION_RESTRICTION) != 0) {
        System.err.println("\t\t\t\t Restriction");
    }
    if ((finalSet & XSConstants.DERIVATION_LIST ) != 0) {
        System.err.println("\t\t\t\t List");
    }
    if ((finalSet & XSConstants.DERIVATION_UNION ) != 0) {
        System.err.println("\t\t\t\t Union");
    }
    if (finalSet == XSConstants.DERIVATION_NONE) {
        System.err.println("\t\t\t\t EMPTY");
    }
}
void printVariety(short variety){
    switch(variety){
    case XSSimpleType.VARIETY_ATOMIC:
        System.err.println("'variety' \t\t\t: ATOMIC");
        break;
    case XSSimpleType.VARIETY_LIST:
        System.err.println("'variety' \t\t\t: LIST");
        break;
    case XSSimpleType.VARIETY_UNION:
        System.err.println("'variety' \t\t\t: UNION");
        break;
    default:
        System.err.println("Invalid value of 'Variety' property , it should be one of atomic, list or union.");
        break;
    }
} 
public static void main(String [] args){
    SimpleTypeUsage usage = new SimpleTypeUsage();
    if(args.length == 1 ){         
        XSSimpleType builtInType = factory.getBuiltInType(args[0]);
        if(builtInType == null){
            System.err.println("Invalid built-in Simple datatype given as argument.");
            printUsage();
        }
        else {
            usage.querySimpleType(builtInType);
        }
    }else{
        printUsage();
    }
}
static void printUsage(){
        System.err.println("USAGE: java simpletype.SimpleTypeUsage  'Built-InDatatypeName' ");
        System.err.println();
        System.err.println("  Built-InDatatypeName  \t\tBuilt-In Datatype name as defined by W3C Schema Spec, \n\t\t\t\t\t \"http://www.w3.org/TR/xmlschema-2/#built-in-datatypes\" .");        
        System.err.println();
}
}
