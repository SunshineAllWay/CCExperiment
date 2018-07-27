package org.apache.xalan.xsltc.runtime;
import java.util.ListResourceBundle;
public class ErrorMessages_de extends ListResourceBundle {
    public Object[][] getContents()
    {
      return new Object[][] {
        {BasisLibrary.RUN_TIME_INTERNAL_ERR,
        "Interner Fehler bei der Ausf\u00fchrung in ''{0}''"},
        {BasisLibrary.RUN_TIME_COPY_ERR,
        "Fehler bei der Ausf\u00fchrung von <xsl:copy>."},
        {BasisLibrary.DATA_CONVERSION_ERR,
        "Ung\u00fcltige Konvertierung von ''{0}'' in ''{1}''."},
        {BasisLibrary.EXTERNAL_FUNC_ERR,
        "Die externe Funktion ''{0}'' wird nicht von XSLTC unterst\u00fctzt."},
        {BasisLibrary.EQUALITY_EXPR_ERR,
        "Unbekannter Argumenttyp in Gleichheitsausdruck."},
        {BasisLibrary.INVALID_ARGUMENT_ERR,
        "Ung\u00fcltiger Argumenttyp ''{0}'' in Aufruf von ''{1}''"},
        {BasisLibrary.FORMAT_NUMBER_ERR,
        "Es wird versucht, Nummer ''{0}'' mit Muster ''{1}'' zu formatieren."},
        {BasisLibrary.ITERATOR_CLONE_ERR,
        "Iterator ''{0}'' kann nicht geklont werden."},
        {BasisLibrary.AXIS_SUPPORT_ERR,
        "Iterator f\u00fcr Achse ''{0}'' wird nicht unterst\u00fctzt."},
        {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
        "Iterator f\u00fcr Achse ''{0}'' mit Typangabe wird nicht unterst\u00fctzt."},
        {BasisLibrary.STRAY_ATTRIBUTE_ERR,
        "Attribut ''{0}'' befindet sich nicht in einem Element."},
        {BasisLibrary.STRAY_NAMESPACE_ERR,
        "Namensbereichdeklaration ''{0}''=''{1}'' befindet sich nicht in einem Element."},
        {BasisLibrary.NAMESPACE_PREFIX_ERR,
        "Der Namensbereich f\u00fcr Pr\u00e4fix ''{0}'' wurde nicht deklariert."},
        {BasisLibrary.DOM_ADAPTER_INIT_ERR,
        "DOMAdapter wurde mit dem falschen Typ f\u00fcr das Dokumentobjektmodell der Quelle erstellt."},
        {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
        "Der von Ihnen verwendete SAX-Parser bearbeitet keine DTD-Deklarationsereignisse."},
        {BasisLibrary.NAMESPACES_SUPPORT_ERR,
        "Der von Ihnen verwendete SAX-Parser unterst\u00fctzt keine XML-Namensbereiche."},
        {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
        "Der URI-Verweis ''{0}'' konnte nicht aufgel\u00f6st werden."},
        {BasisLibrary.UNSUPPORTED_XSL_ERR,
        "Nicht unterst\u00fctztes XSL-Element ''{0}''"},
        {BasisLibrary.UNSUPPORTED_EXT_ERR,
        "Nicht erkannte XSLTC-Erweiterung ''{0}''"},
        {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
        "Das angegebene Translet ''{0}'' wurde mit einer neueren XSLTC-Version erstellt als die verwendete Version der XSLTC-Laufzeitsoftware. Sie m\u00fcssen die Formatvorlage erneut kompilieren oder eine neuere XSLTC-Version zum Ausf\u00fchren dieses Translets verwenden."},
        {BasisLibrary.INVALID_QNAME_ERR,
        "Ein Attribut, dessen Wert ein QName sein muss, hatte den Wert ''{0}''."},
        {BasisLibrary.INVALID_NCNAME_ERR,
        "Ein Attribut, dessen Wert ein NCName sein muss, hatte den Wert ''{0}''."},
        {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
        "Die Verwendung der Erweiterungsfunktion ''{0}'' ist nicht zul\u00e4ssig, wenn f\u00fcr die Funktion zur sicheren Verarbeitung der Wert ''true'' festgelegt wurde."},
        {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
        "Die Verwendung des Erweiterungselements ''{0}'' ist nicht zul\u00e4ssig, wenn f\u00fcr die Funktion zur sicheren Verarbeitung der Wert ''true'' festgelegt wurde."},
    };
    }
}
