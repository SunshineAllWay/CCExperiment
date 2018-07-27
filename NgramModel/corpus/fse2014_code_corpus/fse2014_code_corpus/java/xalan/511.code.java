package org.apache.xalan.xsltc.runtime;
import java.util.ListResourceBundle;
public class ErrorMessages_hu extends ListResourceBundle {
    public Object[][] getContents()
    {
      return new Object[][] {
        {BasisLibrary.RUN_TIME_INTERNAL_ERR,
        "Fut\u00e1s k\u00f6zbeni bels\u0151 hiba a(z) ''{0}'' oszt\u00e1lyban. "},
        {BasisLibrary.RUN_TIME_COPY_ERR,
        "Fut\u00e1s k\u00f6zbeni bels\u0151 hiba az <xsl:copy> v\u00e9grehajt\u00e1sakor."},
        {BasisLibrary.DATA_CONVERSION_ERR,
        "\u00c9rv\u00e9nytelen \u00e1talak\u00edt\u00e1s ''{0}'' t\u00edpusr\u00f3l ''{1}'' t\u00edpusra."},
        {BasisLibrary.EXTERNAL_FUNC_ERR,
        "A(z) ''{0}'' k\u00fcls\u0151 f\u00fcggv\u00e9nyt az XSLTC nem t\u00e1mogatja."},
        {BasisLibrary.EQUALITY_EXPR_ERR,
        "Ismeretlen argumentumt\u00edpus tal\u00e1lhat\u00f3 az egyenl\u0151s\u00e9gi kifejez\u00e9sben."},
        {BasisLibrary.INVALID_ARGUMENT_ERR,
        "A(z) ''{0}'' \u00e9rv\u00e9nytelen argumentumt\u00edpus a(z) ''{1}'' h\u00edv\u00e1s\u00e1hoz "},
        {BasisLibrary.FORMAT_NUMBER_ERR,
        "K\u00eds\u00e9rlet a(z) ''{0}'' form\u00e1z\u00e1s\u00e1ra a(z) ''{1}'' mint\u00e1val."},
        {BasisLibrary.ITERATOR_CLONE_ERR,
        "A(z) ''{0}'' iter\u00e1tor nem kl\u00f3nozhat\u00f3."},
        {BasisLibrary.AXIS_SUPPORT_ERR,
        "A(z) ''{0}'' tengelyre az iter\u00e1tor nem t\u00e1mogatott."},
        {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
        "A tipiz\u00e1lt ''{0}'' tengelyre az iter\u00e1tor nem t\u00e1mogatott."},
        {BasisLibrary.STRAY_ATTRIBUTE_ERR,
        "A(z) ''{0}'' attrib\u00fatum k\u00edv\u00fcl esik az elemen."},
        {BasisLibrary.STRAY_NAMESPACE_ERR,
        "A(z) ''{0}''=''{1}'' n\u00e9vt\u00e9rdeklar\u00e1ci\u00f3 k\u00edv\u00fcl esik az elemen."},
        {BasisLibrary.NAMESPACE_PREFIX_ERR,
        "A(z) ''{0}'' el\u0151tag n\u00e9vtere nincs deklar\u00e1lva."},
        {BasisLibrary.DOM_ADAPTER_INIT_ERR,
        "Nem megfelel\u0151 t\u00edpus\u00fa forr\u00e1s DOM haszn\u00e1lat\u00e1val j\u00f6tt l\u00e9tre a DOMAdapter."},
        {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
        "A haszn\u00e1lt SAX \u00e9rtelmez\u0151 nem kezeli a DTD deklar\u00e1ci\u00f3s esem\u00e9nyeket."},
        {BasisLibrary.NAMESPACES_SUPPORT_ERR,
        "A haszn\u00e1lt SAX \u00e9rtelmez\u0151 nem t\u00e1mogatja az XML n\u00e9vtereket."},
        {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
        "Nem lehet feloldani a(z) ''{0}'' URI hivatkoz\u00e1st."},
        {BasisLibrary.UNSUPPORTED_XSL_ERR,
        "Nem t\u00e1mogatott XSL elem: ''{0}''"},
        {BasisLibrary.UNSUPPORTED_EXT_ERR,
        "Ismeretlen XSLTC kiterjeszt\u00e9s: ''{0}''"},
        {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
        "A megadott ''{0}'' translet az XSLTC egy \u00fajabb verzi\u00f3j\u00e1val k\u00e9sz\u00fclt, mint a haszn\u00e1latban l\u00e9v\u0151 XSLTC verzi\u00f3. \u00dajra kell ford\u00edtania a st\u00edluslapot, vagy a translet futtat\u00e1s\u00e1hoz az XSLTC \u00fajabb verzi\u00f3j\u00e1t kell haszn\u00e1lnia."},
        {BasisLibrary.INVALID_QNAME_ERR,
        "Egy olyan attrib\u00fatum, aminek az \u00e9rt\u00e9ke csak QName lehet, ''{0}'' \u00e9rt\u00e9kkel rendelkezett."},
        {BasisLibrary.INVALID_NCNAME_ERR,
        "Egy olyan attrib\u00fatum, amelynek \u00e9rt\u00e9ke csak NCName lehet, ''{0}'' \u00e9rt\u00e9kkel rendelkezett."},
        {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
        "A(z) ''{0}'' kiterjeszt\u00e9si f\u00fcggv\u00e9ny haszn\u00e1lata nem megengedett, ha biztons\u00e1gos feldolgoz\u00e1s be van kapcsolva. "},
        {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
        "A(z) ''{0}'' kiterjeszt\u00e9si elem haszn\u00e1lata nem megengedett, ha biztons\u00e1gos feldolgoz\u00e1s be van kapcsolva. "},
    };
    }
}
