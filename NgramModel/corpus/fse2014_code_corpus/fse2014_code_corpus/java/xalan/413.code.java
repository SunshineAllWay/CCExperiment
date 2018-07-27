package org.apache.xalan.xsltc.compiler.util;
import java.util.ListResourceBundle;
public class ErrorMessages_pl extends ListResourceBundle {
    public Object[][] getContents()
    {
      return new Object[][] {
        {ErrorMsg.MULTIPLE_STYLESHEET_ERR,
        "W jednym pliku zdefiniowano wi\u0119cej ni\u017c jeden arkusz styl\u00f3w."},
        {ErrorMsg.TEMPLATE_REDEF_ERR,
        "Szablon ''{0}'' zosta\u0142 ju\u017c zdefiniowany w tym arkuszu styl\u00f3w."},
        {ErrorMsg.TEMPLATE_UNDEF_ERR,
        "Szablon ''{0}'' nie zosta\u0142 zdefiniowany w tym arkuszu styl\u00f3w."},
        {ErrorMsg.VARIABLE_REDEF_ERR,
        "Zmienna ''{0}'' zosta\u0142a zdefiniowana wielokrotnie w tym samym zasi\u0119gu."},
        {ErrorMsg.VARIABLE_UNDEF_ERR,
        "Nie zdefiniowano zmiennej lub parametru ''{0}''."},
        {ErrorMsg.CLASS_NOT_FOUND_ERR,
        "Nie mo\u017cna znale\u017a\u0107 klasy ''{0}''."},
        {ErrorMsg.METHOD_NOT_FOUND_ERR,
        "Nie mo\u017cna znale\u017a\u0107 metody zewn\u0119trznej ''{0}'' (musi by\u0107 zadeklarowana jako public)."},
        {ErrorMsg.ARGUMENT_CONVERSION_ERR,
        "Nie mo\u017cna przekszta\u0142ci\u0107 typu argumentu lub typu wyniku w wywo\u0142aniu metody ''{0}''"},
        {ErrorMsg.FILE_NOT_FOUND_ERR,
        "Nie mo\u017cna znale\u017a\u0107 pliku lub identyfikatora URI ''{0}''."},
        {ErrorMsg.INVALID_URI_ERR,
        "Niepoprawny identyfikator URI ''{0}''."},
        {ErrorMsg.FILE_ACCESS_ERR,
        "Nie mo\u017cna otworzy\u0107 pliku lub identyfikatora URI ''{0}''."},
        {ErrorMsg.MISSING_ROOT_ERR,
        "Oczekiwano elementu <xsl:stylesheet> lub <xsl:transform>."},
        {ErrorMsg.NAMESPACE_UNDEF_ERR,
        "Nie zadeklarowano przedrostka przestrzeni nazw ''{0}''."},
        {ErrorMsg.FUNCTION_RESOLVE_ERR,
        "Nie mo\u017cna rozstrzygn\u0105\u0107 wywo\u0142ania funkcji ''{0}''."},
        {ErrorMsg.NEED_LITERAL_ERR,
        "Argument funkcji ''{0}'' musi by\u0107 litera\u0142em \u0142a\u0144cuchowym."},
        {ErrorMsg.XPATH_PARSER_ERR,
        "B\u0142\u0105d podczas analizowania wyra\u017cenia XPath ''{0}''."},
        {ErrorMsg.REQUIRED_ATTR_ERR,
        "Brakuje atrybutu wymaganego ''{0}''."},
        {ErrorMsg.ILLEGAL_CHAR_ERR,
        "Niedozwolony znak ''{0}'' w wyra\u017ceniu XPath."},
        {ErrorMsg.ILLEGAL_PI_ERR,
        "Niedozwolona nazwa ''{0}'' instrukcji przetwarzania."},
        {ErrorMsg.STRAY_ATTRIBUTE_ERR,
        "Atrybut ''{0}'' znajduje si\u0119 poza elementem."},
        {ErrorMsg.ILLEGAL_ATTRIBUTE_ERR,
        "Niedozwolony atrybut ''{0}''."},
        {ErrorMsg.CIRCULAR_INCLUDE_ERR,
        "Cykliczny import/include. Arkusz styl\u00f3w ''{0}'' zosta\u0142 ju\u017c za\u0142adowany."},
        {ErrorMsg.RESULT_TREE_SORT_ERR,
        "Nie mo\u017cna posortowa\u0107 fragment\u00f3w drzewa rezultat\u00f3w (elementy <xsl:sort> s\u0105 ignorowane). Trzeba sortowa\u0107 w\u0119z\u0142y podczas tworzenia drzewa rezultat\u00f3w."},
        {ErrorMsg.SYMBOLS_REDEF_ERR,
        "Formatowanie dziesi\u0119tne ''{0}'' zosta\u0142o ju\u017c zdefiniowane."},
        {ErrorMsg.XSL_VERSION_ERR,
        "Wersja XSL ''{0}'' nie jest obs\u0142ugiwana przez XSLTC."},
        {ErrorMsg.CIRCULAR_VARIABLE_ERR,
        "Cykliczne odwo\u0142anie do zmiennej lub parametru w ''{0}''."},
        {ErrorMsg.ILLEGAL_BINARY_OP_ERR,
        "Nieznany operator wyra\u017cenia dwuargumentowego."},
        {ErrorMsg.ILLEGAL_ARG_ERR,
        "Niedozwolone argumenty w wywo\u0142aniu funkcji."},
        {ErrorMsg.DOCUMENT_ARG_ERR,
        "Drugim argumentem funkcji document() musi by\u0107 zbi\u00f3r w\u0119z\u0142\u00f3w."},
        {ErrorMsg.MISSING_WHEN_ERR,
        "W <xsl:choose> wymagany jest przynajmniej jeden element <xsl:when>."},
        {ErrorMsg.MULTIPLE_OTHERWISE_ERR,
        "W <xsl:choose> dozwolony jest tylko jeden element <xsl:otherwise>."},
        {ErrorMsg.STRAY_OTHERWISE_ERR,
        "Elementu <xsl:otherwise> mo\u017cna u\u017cy\u0107 tylko wewn\u0105trz <xsl:choose>."},
        {ErrorMsg.STRAY_WHEN_ERR,
        "Elementu <xsl:when> mo\u017cna u\u017cy\u0107 tylko wewn\u0105trz <xsl:choose>."},
        {ErrorMsg.WHEN_ELEMENT_ERR,
        "Tylko elementy <xsl:when> i <xsl:otherwise> s\u0105 dozwolone w <xsl:choose>."},
        {ErrorMsg.UNNAMED_ATTRIBSET_ERR,
        "<xsl:attribute-set> nie ma atrybutu 'name'."},
        {ErrorMsg.ILLEGAL_CHILD_ERR,
        "Niedozwolony element potomny."},
        {ErrorMsg.ILLEGAL_ELEM_NAME_ERR,
        "Nie mo\u017cna wywo\u0142a\u0107 elementu ''{0}''"},
        {ErrorMsg.ILLEGAL_ATTR_NAME_ERR,
        "Nie mo\u017cna wywo\u0142a\u0107 atrybutu ''{0}''"},
        {ErrorMsg.ILLEGAL_TEXT_NODE_ERR,
        "Dane tekstowe poza elementem <xsl:stylesheet> najwy\u017cszego poziomu."},
        {ErrorMsg.SAX_PARSER_CONFIG_ERR,
        "Analizator sk\u0142adni JAXP nie zosta\u0142 poprawnie skonfigurowany."},
        {ErrorMsg.INTERNAL_ERR,
        "Nienaprawialny b\u0142\u0105d wewn\u0119trzny XSLTC: ''{0}''"},
        {ErrorMsg.UNSUPPORTED_XSL_ERR,
        "Nieobs\u0142ugiwany element XSL ''{0}''."},
        {ErrorMsg.UNSUPPORTED_EXT_ERR,
        "Nierozpoznane rozszerzenie XSLTC ''{0}''."},
        {ErrorMsg.MISSING_XSLT_URI_ERR,
        "Dokument wej\u015bciowy nie jest arkuszem styl\u00f3w (przestrze\u0144 nazw XSL nie zosta\u0142a zadeklarowana w elemencie g\u0142\u00f3wnym)."},
        {ErrorMsg.MISSING_XSLT_TARGET_ERR,
        "Nie mo\u017cna znale\u017a\u0107 elementu docelowego ''{0}'' arkusza styl\u00f3w."},
        {ErrorMsg.NOT_IMPLEMENTED_ERR,
        "Nie zaimplementowano: ''{0}''."},
        {ErrorMsg.NOT_STYLESHEET_ERR,
        "Dokument wej\u015bciowy nie zawiera arkusza styl\u00f3w XSL."},
        {ErrorMsg.ELEMENT_PARSE_ERR,
        "Nie mo\u017cna zanalizowa\u0107 elementu ''{0}''"},
        {ErrorMsg.KEY_USE_ATTR_ERR,
        "Warto\u015bci\u0105 atrybutu use elementu <key> musi by\u0107: node, node-set, string lub number."},
        {ErrorMsg.OUTPUT_VERSION_ERR,
        "Wyj\u015bciowy dokument XML powinien mie\u0107 wersj\u0119 1.0"},
        {ErrorMsg.ILLEGAL_RELAT_OP_ERR,
        "Nieznany operator wyra\u017cenia relacyjnego"},
        {ErrorMsg.ATTRIBSET_UNDEF_ERR,
        "Pr\u00f3ba u\u017cycia nieistniej\u0105cego zbioru atrybut\u00f3w ''{0}''."},
        {ErrorMsg.ATTR_VAL_TEMPLATE_ERR,
        "Nie mo\u017cna zanalizowa\u0107 szablonu warto\u015bci atrybutu ''{0}''."},
        {ErrorMsg.UNKNOWN_SIG_TYPE_ERR,
        "Nieznany typ danych w sygnaturze klasy ''{0}''."},
        {ErrorMsg.DATA_CONVERSION_ERR,
        "Nie mo\u017cna przekszta\u0142ci\u0107 typu danych ''{0}'' w ''{1}''."},
        {ErrorMsg.NO_TRANSLET_CLASS_ERR,
        "Klasa Templates nie zawiera poprawnej definicji klasy transletu."},
        {ErrorMsg.NO_MAIN_TRANSLET_ERR,
        "Ta klasa Templates nie zawiera klasy o nazwie ''{0}''."},
        {ErrorMsg.TRANSLET_CLASS_ERR,
        "Nie mo\u017cna za\u0142adowa\u0107 klasy transletu ''{0}''."},
        {ErrorMsg.TRANSLET_OBJECT_ERR,
        "Za\u0142adowano klas\u0119 transletu, ale nie mo\u017cna utworzy\u0107 jego instancji."},
        {ErrorMsg.ERROR_LISTENER_NULL_ERR,
        "Pr\u00f3ba ustawienia obiektu ErrorListener klasy ''{0}'' na warto\u015b\u0107 null"},
        {ErrorMsg.JAXP_UNKNOWN_SOURCE_ERR,
        "Tylko StreamSource, SAXSource i DOMSource s\u0105 obs\u0142ugiwane przez XSLTC"},
        {ErrorMsg.JAXP_NO_SOURCE_ERR,
        "Obiekt klasy Source przekazany do ''{0}'' nie ma kontekstu."},
        {ErrorMsg.JAXP_COMPILE_ERR,
        "Nie mo\u017cna skompilowa\u0107 arkusza styl\u00f3w."},
        {ErrorMsg.JAXP_INVALID_ATTR_ERR,
        "Klasa TransformerFactory nie rozpoznaje atrybutu ''{0}''."},
        {ErrorMsg.JAXP_SET_RESULT_ERR,
        "Przed wywo\u0142aniem metody startDocument() nale\u017cy wywo\u0142a\u0107 metod\u0119 setResult()."},
        {ErrorMsg.JAXP_NO_TRANSLET_ERR,
        "Obiekt Transformer nie zawiera referencji do obiektu transletu."},
        {ErrorMsg.JAXP_NO_HANDLER_ERR,
        "Nie zdefiniowano procedury obs\u0142ugi wyj\u015bcia rezultat\u00f3w transformacji."},
        {ErrorMsg.JAXP_NO_RESULT_ERR,
        "Obiekt Result przekazany do ''{0}'' jest niepoprawny."},
        {ErrorMsg.JAXP_UNKNOWN_PROP_ERR,
        "Pr\u00f3ba dost\u0119pu do niepoprawnej w\u0142a\u015bciwo\u015bci interfejsu Transformer ''{0}''."},
        {ErrorMsg.SAX2DOM_ADAPTER_ERR,
        "Nie mo\u017cna utworzy\u0107 adaptera SAX2DOM: ''{0}''."},
        {ErrorMsg.XSLTC_SOURCE_ERR,
        "Metoda XSLTCSource.build() zosta\u0142a wywo\u0142ana bez ustawienia warto\u015bci systemId."},
        { ErrorMsg.ER_RESULT_NULL,
            "Rezultat nie powinien by\u0107 pusty"},
        {ErrorMsg.JAXP_INVALID_SET_PARAM_VALUE,
        "Warto\u015bci\u0105 parametru {0} musi by\u0107 poprawny obiekt j\u0119zyka Java."},
        {ErrorMsg.COMPILE_STDIN_ERR,
        "Z opcj\u0105 -o trzeba u\u017cy\u0107 tak\u017ce opcji -i."},
        {ErrorMsg.COMPILE_USAGE_STR,
        "SK\u0141ADNIA\n   java org.apache.xalan.xsltc.cmdline.Compile [-o <wyj\u015bcie>]\n      [-d <katalog>] [-j <plik_jar>] [-p <pakiet>]\n      [-n] [-x] [-u] [-v] [-h] { <arkusz_styl\u00f3w> | -i }\n\nOPCJE\n   -o <wyj\u015bcie>    przypisanie nazwy <wyj\u015bcie> do wygenerowanego\n                   transletu.  Domy\u015blnie nazwa transletu pochodzi \n                   od nazwy <arkusza_styl\u00f3w>. Opcja ta jest ignorowana \n                   w przypadku kompilowania wielu arkuszy styl\u00f3w.\n   -d <katalog>    Okre\u015blenie katalogu docelowego transletu.\n   -j <plik_jar>   Pakowanie klas transletu do pliku jar o nazwie\n                   okre\u015blonej jako <plik_jar>.\n   -p <pakiet>     Okre\u015blenie przedrostka nazwy pakietu dla wszystkich\n                   wygenerowanych klas translet\u00f3w.\n   -n              W\u0142\u0105czenie wstawiania szablon\u00f3w (zachowanie domy\u015blne\n                   zwykle lepsze).\n   -x              W\u0142\u0105czenie wypisywania dodatkowych komunikat\u00f3w debugowania.\n    -u              Interpretowanie argument\u00f3w <arkusz_styl\u00f3w> jako\n                   adres\u00f3w URL.\n   -i              Wymuszenie odczytywania przez kompilator arkusza styl\u00f3w\n                   ze standardowego wej\u015bcia (stdin).\n   -v              Wypisanie wersji kompilatora.\n   -h              Wypisanie informacji o sk\u0142adni.\n"},
        {ErrorMsg.TRANSFORM_USAGE_STR,
        "SK\u0141ADNIA \n   java org.apache.xalan.xsltc.cmdline.Transform [-j <plik_jar>]\n      [-x] [-n <iteracje>] {-u <url_dokumentu> | <dokument>}\n <klasa> [<param1>=<warto\u015b\u01071> ...]\n\n   U\u017cycie transletu <klasa> do transformacji dokumentu XML \n   okre\u015blonego jako <dokument>. Translet <klasa> znajduje si\u0119 w\n   \u015bcie\u017cce CLASSPATH u\u017cytkownika lub w opcjonalnie podanym pliku <plik_jar>.\nOPCJE\n   -j <plik_jar>   Okre\u015blenie pliku jar, z kt\u00f3rego nale\u017cy za\u0142adowa\u0107 translet.\n   -x              W\u0142\u0105czenie wypisywania dodatkowych komunikat\u00f3w debugowania.\n   -n <iteracje>   Okre\u015blenie krotno\u015bci wykonywania transformacji oraz \n                   w\u0142\u0105czenie wy\u015bwietlania informacji z profilowania.\n   -u <url_dokumentu>\n                   Okre\u015blenie wej\u015bciowego dokumentu XML w postaci adresu URL.\n"},
        {ErrorMsg.STRAY_SORT_ERR,
        "Elementu <xsl:sort> mo\u017cna u\u017cy\u0107 tylko wewn\u0105trz <xsl:for-each> lub <xsl:apply-templates>."},
        {ErrorMsg.UNSUPPORTED_ENCODING,
        "Kodowanie wyj\u015bciowe ''{0}'' nie jest obs\u0142ugiwane przez t\u0119 maszyn\u0119 wirtualn\u0105 j\u0119zyka Java."},
        {ErrorMsg.SYNTAX_ERR,
        "B\u0142\u0105d sk\u0142adniowy w ''{0}''."},
        {ErrorMsg.CONSTRUCTOR_NOT_FOUND,
        "Nie mo\u017cna znale\u017a\u0107 konstruktora zewn\u0119trznego ''{0}''."},
        {ErrorMsg.NO_JAVA_FUNCT_THIS_REF,
        "Pierwszy argument funkcji ''{0}'' j\u0119zyka Java (innej ni\u017c static) nie jest poprawnym odniesieniem do obiektu."},
        {ErrorMsg.TYPE_CHECK_ERR,
        "B\u0142\u0105d podczas sprawdzania typu wyra\u017cenia ''{0}''."},
        {ErrorMsg.TYPE_CHECK_UNK_LOC_ERR,
        "B\u0142\u0105d podczas sprawdzania typu wyra\u017cenia w nieznanym po\u0142o\u017ceniu."},
        {ErrorMsg.ILLEGAL_CMDLINE_OPTION_ERR,
        "Niepoprawna opcja ''{0}'' wiersza komend."},
        {ErrorMsg.CMDLINE_OPT_MISSING_ARG_ERR,
        "Brakuje argumentu wymaganego opcji ''{0}'' wiersza komend."},
        {ErrorMsg.WARNING_PLUS_WRAPPED_MSG,
        "OSTRZE\u017bENIE:  ''{0}''\n       :{1}"},
        {ErrorMsg.WARNING_MSG,
        "OSTRZE\u017bENIE:  ''{0}''"},
        {ErrorMsg.FATAL_ERR_PLUS_WRAPPED_MSG,
        "B\u0141\u0104D KRYTYCZNY:  ''{0}''\n           :{1}"},
        {ErrorMsg.FATAL_ERR_MSG,
        "B\u0141\u0104D KRYTYCZNY:  ''{0}''"},
        {ErrorMsg.ERROR_PLUS_WRAPPED_MSG,
        "B\u0141\u0104D:  ''{0}''\n     :{1}"},
        {ErrorMsg.ERROR_MSG,
        "B\u0141\u0104D:  ''{0}''"},
        {ErrorMsg.TRANSFORM_WITH_TRANSLET_STR,
        "Dokonaj transformacji za pomoc\u0105 transletu ''{0}'' "},
        {ErrorMsg.TRANSFORM_WITH_JAR_STR,
        "Dokonaj transformacji za pomoc\u0105 transletu ''{0}'' z pliku jar ''{1}''"},
        {ErrorMsg.COULD_NOT_CREATE_TRANS_FACT,
        "Nie mo\u017cna utworzy\u0107 instancji klasy ''{0}'' interfejsu TransformerFactory."},
        {ErrorMsg.TRANSLET_NAME_JAVA_CONFLICT,
         "Nazwy ''{0}'' nie mo\u017cna u\u017cy\u0107 jako nazwy klasy transletu, poniewa\u017c zawiera ona znaki, kt\u00f3re s\u0105 niedozwolone w nazwach klas j\u0119zyka Java.  Zamiast niej u\u017cyto nazwy ''{1}''."},
        {ErrorMsg.COMPILER_ERROR_KEY,
        "B\u0142\u0119dy kompilatora:"},
        {ErrorMsg.COMPILER_WARNING_KEY,
        "Ostrze\u017cenia kompilatora:"},
        {ErrorMsg.RUNTIME_ERROR_KEY,
        "B\u0142\u0119dy transletu:"},
        {ErrorMsg.INVALID_QNAME_ERR,
        "Atrybut, kt\u00f3rego warto\u015bci\u0105 musi by\u0107 nazwa QName lub lista rozdzielonych odst\u0119pami nazw QName, mia\u0142 warto\u015b\u0107 ''{0}''"},
        {ErrorMsg.INVALID_NCNAME_ERR,
        "Atrybut, kt\u00f3rego warto\u015bci\u0105 musi by\u0107 nazwa NCName, mia\u0142 warto\u015b\u0107 ''{0}''"},
        {ErrorMsg.INVALID_METHOD_IN_OUTPUT,
        "Atrybut method elementu <xsl:output> mia\u0142 warto\u015b\u0107 ''{0}''.  Warto\u015bci\u0105 mo\u017ce by\u0107: ''xml'', ''html'', ''text'' lub nazwa qname nie b\u0119d\u0105ca nazw\u0105 ncname."},
        {ErrorMsg.JAXP_GET_FEATURE_NULL_NAME,
        "Nazwa opcji nie mo\u017ce mie\u0107 warto\u015bci null w TransformerFactory.getFeature(String nazwa)."},
        {ErrorMsg.JAXP_SET_FEATURE_NULL_NAME,
        "Nazwa opcji nie mo\u017ce mie\u0107 warto\u015bci null w TransformerFactory.setFeature(String nazwa, boolean warto\u015b\u0107)."},
        {ErrorMsg.JAXP_UNSUPPORTED_FEATURE,
        "Nie mo\u017cna ustawi\u0107 opcji ''{0}'' w tej klasie TransformerFactory."}
    };
    }
}
