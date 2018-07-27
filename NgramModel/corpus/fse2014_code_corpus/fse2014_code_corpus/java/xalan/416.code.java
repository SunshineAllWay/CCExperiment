package org.apache.xalan.xsltc.compiler.util;
import java.util.ListResourceBundle;
public class ErrorMessages_sk extends ListResourceBundle {
    public Object[][] getContents()
    {
      return new Object[][] {
        {ErrorMsg.MULTIPLE_STYLESHEET_ERR,
        "Viac ne\u017e jeden \u0161t\u00fdl dokumentu bol definovan\u00fd v rovnakom s\u00fabore."},
        {ErrorMsg.TEMPLATE_REDEF_ERR,
        "V tomto \u0161t\u00fdle dokumentu u\u017e bola \u0161abl\u00f3na ''{0}'' definovan\u00e1."},
        {ErrorMsg.TEMPLATE_UNDEF_ERR,
        "\u0160abl\u00f3na ''{0}'' nebola definovan\u00e1 v tomto \u0161t\u00fdle dokumentu."},
        {ErrorMsg.VARIABLE_REDEF_ERR,
        "Premenn\u00e1 ''{0}'' je viackr\u00e1t definovan\u00e1 v rovnakom rozsahu."},
        {ErrorMsg.VARIABLE_UNDEF_ERR,
        "Premenn\u00e1 alebo parameter ''{0}'' nie je definovan\u00fd."},
        {ErrorMsg.CLASS_NOT_FOUND_ERR,
        "Trieda ''{0}'' sa ned\u00e1 n\u00e1js\u0165."},
        {ErrorMsg.METHOD_NOT_FOUND_ERR,
        "Ned\u00e1 sa n\u00e1js\u0165 extern\u00e1 met\u00f3da ''{0}'' (mus\u00ed by\u0165 verejn\u00e1)."},
        {ErrorMsg.ARGUMENT_CONVERSION_ERR,
        "Argument/typ n\u00e1vratu vo volan\u00ed sa ned\u00e1 skonvertova\u0165 na met\u00f3du ''{0}''"},
        {ErrorMsg.FILE_NOT_FOUND_ERR,
        "S\u00fabor alebo URI ''{0}'' nebol n\u00e1jden\u00fd."},
        {ErrorMsg.INVALID_URI_ERR,
        "URI ''{0}'' je neplatn\u00fd."},
        {ErrorMsg.FILE_ACCESS_ERR,
        "S\u00fabor alebo URI ''{0}'' sa ned\u00e1 otvori\u0165."},
        {ErrorMsg.MISSING_ROOT_ERR,
        "O\u010dak\u00e1va sa element <xsl:stylesheet> alebo <xsl:transform>."},
        {ErrorMsg.NAMESPACE_UNDEF_ERR,
        "Predpona n\u00e1zvov\u00e9ho priestoru ''{0}'' nie je deklarovan\u00e1."},
        {ErrorMsg.FUNCTION_RESOLVE_ERR,
        "Nie je mo\u017en\u00e9 rozl\u00ed\u0161i\u0165 volanie pre funkciu ''{0}''."},
        {ErrorMsg.NEED_LITERAL_ERR,
        "Argument pre ''{0}'' mus\u00ed by\u0165 liter\u00e1lov\u00fdm re\u0165azcom."},
        {ErrorMsg.XPATH_PARSER_ERR,
        "Chyba pri syntaktickej anal\u00fdze XPath v\u00fdrazu ''{0}''."},
        {ErrorMsg.REQUIRED_ATTR_ERR,
        "Ch\u00fdba vy\u017eadovan\u00fd atrib\u00fat ''{0}''."},
        {ErrorMsg.ILLEGAL_CHAR_ERR,
        "Vo v\u00fdraze XPath je neplatn\u00fd znak ''{0}''."},
        {ErrorMsg.ILLEGAL_PI_ERR,
        "Pre in\u0161trukciu spracovania je n\u00e1zov ''{0}'' neplatn\u00fd."},
        {ErrorMsg.STRAY_ATTRIBUTE_ERR,
        "Atrib\u00fat ''{0}'' je mimo prvku."},
        {ErrorMsg.ILLEGAL_ATTRIBUTE_ERR,
        "Atrib\u00fat ''{0}'' je neplatn\u00fd."},
        {ErrorMsg.CIRCULAR_INCLUDE_ERR,
        "Cirkul\u00e1rny import/zahrnutie. \u0160t\u00fdl dokumentu ''{0}'' je u\u017e zaveden\u00fd."},
        {ErrorMsg.RESULT_TREE_SORT_ERR,
        "Fragmenty stromu v\u00fdsledkov nemo\u017eno triedi\u0165 (elementy <xsl:sort> s\u00fa ignorovan\u00e9). Ke\u010f vytv\u00e1rate v\u00fdsledkov\u00fd strom, mus\u00edte triedi\u0165 uzly."},
        {ErrorMsg.SYMBOLS_REDEF_ERR,
        "Desiatkov\u00e9 form\u00e1tovanie ''{0}'' je u\u017e definovan\u00e9."},
        {ErrorMsg.XSL_VERSION_ERR,
        "XSLTC nepodporuje XSL verziu ''{0}''."},
        {ErrorMsg.CIRCULAR_VARIABLE_ERR,
        "Referencia na cyklick\u00fa premenn\u00fa/parameter v ''{0}''."},
        {ErrorMsg.ILLEGAL_BINARY_OP_ERR,
        "Nezn\u00e1my oper\u00e1tor pre bin\u00e1rny v\u00fdraz."},
        {ErrorMsg.ILLEGAL_ARG_ERR,
        "Neplatn\u00fd argument(y) pre volanie funkcie."},
        {ErrorMsg.DOCUMENT_ARG_ERR,
        "Druh\u00fd argument pre funkciu dokumentu() mus\u00ed by\u0165 sada uzlov."},
        {ErrorMsg.MISSING_WHEN_ERR,
        "V <xsl:choose> sa vy\u017eaduje najmenej jeden element <xsl:when>."},
        {ErrorMsg.MULTIPLE_OTHERWISE_ERR,
        "V  <xsl:choose> je povolen\u00fd len jeden element <xsl:otherwise>."},
        {ErrorMsg.STRAY_OTHERWISE_ERR,
        "<xsl:otherwise> mo\u017eno pou\u017ei\u0165 len v <xsl:choose>."},
        {ErrorMsg.STRAY_WHEN_ERR,
        "<xsl:when> mo\u017eno pou\u017ei\u0165 len v <xsl:choose>."},
        {ErrorMsg.WHEN_ELEMENT_ERR,
        "V <xsl:choose> s\u00fa povolen\u00e9 len elementy <xsl:when> a <xsl:otherwise>."},
        {ErrorMsg.UNNAMED_ATTRIBSET_ERR,
        "<xsl:attribute-set> ch\u00fdba atrib\u00fat 'name'."},
        {ErrorMsg.ILLEGAL_CHILD_ERR,
        "Neplatn\u00fd element potomka."},
        {ErrorMsg.ILLEGAL_ELEM_NAME_ERR,
        "Nem\u00f4\u017eete vola\u0165 prvok ''{0}''"},
        {ErrorMsg.ILLEGAL_ATTR_NAME_ERR,
        "Nem\u00f4\u017eete vola\u0165 atrib\u00fat ''{0}''"},
        {ErrorMsg.ILLEGAL_TEXT_NODE_ERR,
        "Textov\u00e9 \u00fadaje s\u00fa mimo elementu vrchnej \u00farovne <xsl:stylesheet>."},
        {ErrorMsg.SAX_PARSER_CONFIG_ERR,
        "Analyz\u00e1tor JAXP nie je spr\u00e1vne nakonfigurovan\u00fd"},
        {ErrorMsg.INTERNAL_ERR,
        "Neopravite\u013en\u00e1 intern\u00e1 chyba XSLTC: ''{0}''"},
        {ErrorMsg.UNSUPPORTED_XSL_ERR,
        "XSL prvok ''{0}'' nie je podporovan\u00fd."},
        {ErrorMsg.UNSUPPORTED_EXT_ERR,
        "XSLTC pr\u00edpona ''{0}'' nebola rozpoznan\u00e1."},
        {ErrorMsg.MISSING_XSLT_URI_ERR,
        "Vstupn\u00fd dokument nie je \u0161t\u00fdlom dokumentu (n\u00e1zvov\u00fd priestor XSL nie je deklarovan\u00fd v kore\u0148ovom elemente)."},
        {ErrorMsg.MISSING_XSLT_TARGET_ERR,
        "Nebolo mo\u017en\u00e9 n\u00e1js\u0165 cie\u013e \u0161t\u00fdlu dokumentu ''{0}''."},
        {ErrorMsg.NOT_IMPLEMENTED_ERR,
        "Nie je implementovan\u00e9: ''{0}''."},
        {ErrorMsg.NOT_STYLESHEET_ERR,
        "Vstupn\u00fd dokument neobsahuje \u0161t\u00fdl dokumentu XSL."},
        {ErrorMsg.ELEMENT_PARSE_ERR,
        "Nebolo mo\u017en\u00e9 analyzova\u0165 prvok ''{0}''"},
        {ErrorMsg.KEY_USE_ATTR_ERR,
        "Atrib\u00fat pou\u017eitia <key> mus\u00ed by\u0165 uzol, sada uzlov, re\u0165azec alebo \u010d\u00edslo."},
        {ErrorMsg.OUTPUT_VERSION_ERR,
        "Verzia v\u00fdstupn\u00e9ho dokumentu XML by mala by\u0165 1.0"},
        {ErrorMsg.ILLEGAL_RELAT_OP_ERR,
        "Nezn\u00e1my oper\u00e1tor pre rela\u010dn\u00fd v\u00fdraz"},
        {ErrorMsg.ATTRIBSET_UNDEF_ERR,
        "Prebieha pokus o pou\u017eitie neexistuj\u00facej sady atrib\u00fatov ''{0}''."},
        {ErrorMsg.ATTR_VAL_TEMPLATE_ERR,
        "Ned\u00e1 sa analyzova\u0165 \u0161abl\u00f3na hodn\u00f4t atrib\u00fatu ''{0}''."},
        {ErrorMsg.UNKNOWN_SIG_TYPE_ERR,
        "V podpise pre triedu ''{0}'' je nezn\u00e1my typ \u00fadajov."},
        {ErrorMsg.DATA_CONVERSION_ERR,
        "Typ \u00fadajov ''{0}'' sa ned\u00e1 skonvertova\u0165 na ''{1}''."},
        {ErrorMsg.NO_TRANSLET_CLASS_ERR,
        "Tento vzor neobsahuje platn\u00fa defin\u00edciu triedy transletu."},
        {ErrorMsg.NO_MAIN_TRANSLET_ERR,
        "T\u00e1to \u0161abl\u00f3na neobsahuje triedu s n\u00e1zvom ''{0}''."},
        {ErrorMsg.TRANSLET_CLASS_ERR,
        "Nebolo mo\u017en\u00e9 zavies\u0165 triedu transletov ''{0}''."},
        {ErrorMsg.TRANSLET_OBJECT_ERR,
        "Trieda transletu zaveden\u00e1, ale nie je mo\u017en\u00e9 vytvori\u0165 in\u0161tanciu transletu."},
        {ErrorMsg.ERROR_LISTENER_NULL_ERR,
        "Prebieha pokus o nastavenie ErrorListener pre ''{0}'' na hodnotu null"},
        {ErrorMsg.JAXP_UNKNOWN_SOURCE_ERR,
        "XSLTC podporuje len StreamSource, SAXSource a DOMSource"},
        {ErrorMsg.JAXP_NO_SOURCE_ERR,
        "Zdrojov\u00fd objekt, ktor\u00fd pre\u0161iel do ''{0}'', nem\u00e1 \u017eiadny obsah."},
        {ErrorMsg.JAXP_COMPILE_ERR,
        "Nebolo mo\u017en\u00e9 skompilova\u0165 \u0161t\u00fdl dokumentu"},
        {ErrorMsg.JAXP_INVALID_ATTR_ERR,
        "TransformerFactory nerozpozn\u00e1va atrib\u00fat ''{0}''."},
        {ErrorMsg.JAXP_SET_RESULT_ERR,
        "setResult() sa mus\u00ed vola\u0165 pred startDocument()."},
        {ErrorMsg.JAXP_NO_TRANSLET_ERR,
        "Transform\u00e1tor nem\u00e1 \u017eiadny zapuzdren\u00fd objekt transletu."},
        {ErrorMsg.JAXP_NO_HANDLER_ERR,
        "Pre v\u00fdsledok transform\u00e1cie nebol definovan\u00fd \u017eiadny v\u00fdstupn\u00fd handler."},
        {ErrorMsg.JAXP_NO_RESULT_ERR,
        "V\u00fdsledn\u00fd objekt, ktor\u00fd pre\u0161iel do ''{0}'', je neplatn\u00fd."},
        {ErrorMsg.JAXP_UNKNOWN_PROP_ERR,
        "Prebieha pokus o prist\u00fapenie na neplatn\u00fa Transformer vlastnos\u0165 ''{0}''."},
        {ErrorMsg.SAX2DOM_ADAPTER_ERR,
        "Nebolo mo\u017en\u00e9 vytvori\u0165 SAX2DOM adapt\u00e9r: ''{0}''."},
        {ErrorMsg.XSLTC_SOURCE_ERR,
        "XSLTCSource.build() bol zavolan\u00fd bez nastaven\u00e9ho systemId."},
        { ErrorMsg.ER_RESULT_NULL,
            "V\u00fdsledok by nemal by\u0165 nulov\u00fd"},
        {ErrorMsg.JAXP_INVALID_SET_PARAM_VALUE,
        "Hodnotou parametra {0} mus\u00ed by\u0165 platn\u00fd objekt Java"},
        {ErrorMsg.COMPILE_STDIN_ERR,
        "Vo\u013eba -i sa mus\u00ed pou\u017e\u00edva\u0165 s vo\u013ebou -o."},
        {ErrorMsg.COMPILE_USAGE_STR,
        "SYNOPSIS\n   java org.apache.xalan.xsltc.cmdline.Compile [-o <output>]\n      [-d <directory>] [-j <jarfile>] [-p <package>]\n      [-n] [-x] [-u] [-v] [-h] { <stylesheet> | -i }\n\nOPTIONS\n   -o <output>    prirad\u00ed n\u00e1zov <output> k vygenerovan\u00e9mu\n                  transletu.  \u0160tandardne sa n\u00e1zov objektu translet \n                  odvodzuje od n\u00e1zvu <stylesheet>.  T\u00e1to vo\u013eba sa ignoruje pri kompilovan\u00ed viacer\u00fdch \u0161t\u00fdlov dokumentov\n\n.   -d <directory> uv\u00e1dza cie\u013eov\u00fd adres\u00e1r pre translet\n   -j <jarfile>   pakuje triedy transletov do s\u00faboru jar n\u00e1zvu \n uveden\u00e9ho ako <jarfile>\n   -p <package>   uv\u00e1dza predponu n\u00e1zvu bal\u00edku pre v\u0161etky generovan\u00e9 triedy transletu.\n\n   -n             povo\u013euje zoradenie vzorov v riadku (\u0161tandardn\u00e9 chovanie v priemere lep\u0161ie). \n\n   -x             zapne \u010fal\u0161\u00ed v\u00fdstup spr\u00e1v z procesu ladenia\n   -u             argumenty <stylesheet> prelo\u017e\u00ed do URL\n   -i             prin\u00fati kompil\u00e1tor, aby pre\u010d\u00edtal \u0161t\u00fdl dokumentu zo stdin\n   -v             vytla\u010d\u00ed verziu kompil\u00e1tora\n   -h             vytla\u010d\u00ed tento n\u00e1vod na pou\u017eitie\n"},
        {ErrorMsg.TRANSFORM_USAGE_STR,
        "SYNOPSIS \n   java org.apache.xalan.xsltc.cmdline.Transform [-j <jarfile>]\n      [-x] [-n <iterations>] {-u <document_url> | <document>}\n      <class> [<param1>=<value1> ...]\n\n   pou\u017eije translet <class> na transform\u00e1ciu XML dokumentu \n,   ktor\u00fd je \u0161pecifikovan\u00fd ako <document>. <class> transletu je bu\u010f v \n u\u017e\u00edvate\u013eovej CLASSPATH alebo vo volite\u013ene uvedenom <jarfile>.\nOPTIONS\n   -j <jarfile>    \u0161pecifikuje jarfile, z ktor\u00e9ho sa m\u00e1 zavies\u0165 translet\n   -x              zapne \u010fal\u0161\u00ed v\u00fdstup spr\u00e1v z procesu ladenia\n   -n <iterations> spust\u00ed transform\u00e1ciu <iterations> \u010dasov a\n                   zobraz\u00ed profilovacie inform\u00e1cie\n   -u <document_url> \u0161pecifikuje vstupn\u00fd dokument XML ako URL\n"},
        {ErrorMsg.STRAY_SORT_ERR,
        "<xsl:sort> mo\u017eno pou\u017ei\u0165 len v <xsl:for-each> alebo <xsl:apply-templates>."},
        {ErrorMsg.UNSUPPORTED_ENCODING,
        "V\u00fdstupn\u00e9 k\u00f3dovanie ''{0}'' nie je na tomto JVM podporovan\u00e9."},
        {ErrorMsg.SYNTAX_ERR,
        "V ''{0}'' je chyba syntaxe."},
        {ErrorMsg.CONSTRUCTOR_NOT_FOUND,
        "Ned\u00e1 sa n\u00e1js\u0165 extern\u00fd kon\u0161trukt\u00e9r ''{0}''."},
        {ErrorMsg.NO_JAVA_FUNCT_THIS_REF,
        "Prv\u00fd argument pre nestatick\u00fa Java funkciu ''{0}'' nie je odkaz na platn\u00fd objekt."},
        {ErrorMsg.TYPE_CHECK_ERR,
        "''{0}'' je typ v\u00fdrazu na kontrolu ch\u00fdb."},
        {ErrorMsg.TYPE_CHECK_UNK_LOC_ERR,
        "Chyba pri kontrole typu v\u00fdrazu na nezn\u00e1mom mieste."},
        {ErrorMsg.ILLEGAL_CMDLINE_OPTION_ERR,
        "Vo\u013eba ''{0}'' pr\u00edkazov\u00e9ho riadku je neplatn\u00e1."},
        {ErrorMsg.CMDLINE_OPT_MISSING_ARG_ERR,
        "Vo vo\u013ebe ''{0}'' pr\u00edkazov\u00e9ho riadku ch\u00fdba vy\u017eadovan\u00fd argument."},
        {ErrorMsg.WARNING_PLUS_WRAPPED_MSG,
        "WARNING:  ''{0}''\n       :{1}"},
        {ErrorMsg.WARNING_MSG,
        "WARNING:  ''{0}''"},
        {ErrorMsg.FATAL_ERR_PLUS_WRAPPED_MSG,
        "FATAL ERROR:  ''{0}''\n           :{1}"},
        {ErrorMsg.FATAL_ERR_MSG,
        "FATAL ERROR:  ''{0}''"},
        {ErrorMsg.ERROR_PLUS_WRAPPED_MSG,
        "ERROR:  ''{0}''\n     :{1}"},
        {ErrorMsg.ERROR_MSG,
        "ERROR:  ''{0}''"},
        {ErrorMsg.TRANSFORM_WITH_TRANSLET_STR,
        "Transform\u00e1cia s pou\u017eit\u00edm transletu ''{0}'' "},
        {ErrorMsg.TRANSFORM_WITH_JAR_STR,
        "Transform\u00e1cia s pou\u017eit\u00edm transletu ''{0}'' z jar s\u00faboru ''{1}''"},
        {ErrorMsg.COULD_NOT_CREATE_TRANS_FACT,
        "Nebolo mo\u017en\u00e9 vytvori\u0165 in\u0161tanciu TransformerFactory triedy ''{0}''."},
        {ErrorMsg.TRANSLET_NAME_JAVA_CONFLICT,
         "N\u00e1zov ''{0}'' sa nedal pou\u017ei\u0165 ako n\u00e1zov triedy transletov, preto\u017ee obsahuje znaky, ktor\u00e9 nie s\u00fa povolen\u00e9 v n\u00e1zve Java triedy.  Namiesto neho bol pou\u017eit\u00fd n\u00e1zov ''{1}''."},
        {ErrorMsg.COMPILER_ERROR_KEY,
        "Chyby preklada\u010da:"},
        {ErrorMsg.COMPILER_WARNING_KEY,
        "Upozornenia preklada\u010da:"},
        {ErrorMsg.RUNTIME_ERROR_KEY,
        "Chyby transletu:"},
        {ErrorMsg.INVALID_QNAME_ERR,
        "Atrib\u00fat, ktor\u00fd mus\u00ed ma\u0165 hodnotu QName alebo medzerami oddelen\u00fd zoznam hodn\u00f4t QName, mal hodnotu ''{0}''"},
        {ErrorMsg.INVALID_NCNAME_ERR,
        "Atrib\u00fat, ktor\u00fd mus\u00ed ma\u0165 hodnotu NCName, mal hodnotu ''{0}''"},
        {ErrorMsg.INVALID_METHOD_IN_OUTPUT,
        "Atrib\u00fat met\u00f3dy v prvku <xsl:output> mal hodnotu ''{0}''.  Touto hodnotou mus\u00ed by\u0165 bu\u010f ''xml'', ''html'', ''text'' alebo qname-but-not-ncname."},
        {ErrorMsg.JAXP_GET_FEATURE_NULL_NAME,
        "N\u00e1zov vlastnosti nem\u00f4\u017ee by\u0165 null v TransformerFactory.getFeature(N\u00e1zov re\u0165azca)."},
        {ErrorMsg.JAXP_SET_FEATURE_NULL_NAME,
        "N\u00e1zov vlastnosti nem\u00f4\u017ee by\u0165 null v TransformerFactory.setFeature(N\u00e1zov re\u0165azca, boolovsk\u00e1 hodnota)."},
        {ErrorMsg.JAXP_UNSUPPORTED_FEATURE,
        "V tomto TransformerFactory sa ned\u00e1 nastavi\u0165 vlastnos\u0165 ''{0}''."}
    };
    }
}
