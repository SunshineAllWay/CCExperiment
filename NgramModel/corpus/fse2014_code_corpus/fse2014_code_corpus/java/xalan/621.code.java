package org.apache.xml.res;
public class XMLErrorResources_sv extends XMLErrorResources
{
  public static final int MAX_CODE = 61;
  public static final int MAX_WARNING = 0;
  public static final int MAX_OTHERS = 4;
  public static final int MAX_MESSAGES = MAX_CODE + MAX_WARNING + 1;
  public Object[][] getContents()
  {
    return new Object[][] {
    {"ER0000" , "{0}" },
  {
    ER_FUNCTION_NOT_SUPPORTED, "Funktion inte underst\u00f6dd:"},
  {
    ER_CANNOT_OVERWRITE_CAUSE,
			"Kan inte skriva \u00f6ver orsak"},
  {
    ER_NO_DEFAULT_IMPL,
         "Standardimplementering saknas i:"},
  {
    ER_CHUNKEDINTARRAY_NOT_SUPPORTED,
       "ChunkedIntArray({0}) underst\u00f6ds f\u00f6r n\u00e4rvarande inte"},
  {
    ER_OFFSET_BIGGER_THAN_SLOT,
       "Offset st\u00f6rre \u00e4n fack"},
  {
    ER_COROUTINE_NOT_AVAIL,
       "Sidorutin inte tillg\u00e4nglig, id={0}"},
  {
    ER_COROUTINE_CO_EXIT,
       "CoroutineManager mottog co_exit()-f\u00f6rfr\u00e5gan"},
  {
    ER_COJOINROUTINESET_FAILED,
       "co_joinCoroutineSet() misslyckades"},
  {
    ER_COROUTINE_PARAM,
       "Sidorutin fick parameterfel ({0})"},
  {
    ER_PARSER_DOTERMINATE_ANSWERS,
       "\nOV\u00c4NTAT: Parser doTerminate-svar {0}"},
  {
    ER_NO_PARSE_CALL_WHILE_PARSING,
       "parse f\u00e5r inte anropas medan tolkning sker"},
  {
    ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED,
       "Fel: typad upprepare f\u00f6r axel {0} inte implementerad"},
  {
    ER_ITERATOR_AXIS_NOT_IMPLEMENTED,
       "Fel: upprepare f\u00f6r axel {0} inte implementerad"},
  {
    ER_ITERATOR_CLONE_NOT_SUPPORTED,
       "Uppreparklon underst\u00f6ds inte"},
  {
    ER_UNKNOWN_AXIS_TYPE,
       "Ok\u00e4nd axeltraverstyp: {0}"},
  {
    ER_AXIS_NOT_SUPPORTED,
       "Axeltravers underst\u00f6ds inte: {0}"},
  {
    ER_NO_DTMIDS_AVAIL,
       "Inga fler DTM-IDs \u00e4r tillg\u00e4ngliga"},
  {
    ER_NOT_SUPPORTED,
       "Underst\u00f6ds inte: {0}"},
  {
    ER_NODE_NON_NULL,
       "Nod m\u00e5ste vara icke-null f\u00f6r getDTMHandleFromNode"},
  {
    ER_COULD_NOT_RESOLVE_NODE,
       "Kunde inte l\u00f6sa nod till ett handtag"},
  {
    ER_STARTPARSE_WHILE_PARSING,
       "startParse f\u00e5r inte anropas medan tolkning sker"},
  {
    ER_STARTPARSE_NEEDS_SAXPARSER,
       "startParse beh\u00f6ver en SAXParser som \u00e4r icke-null"},
  {
    ER_COULD_NOT_INIT_PARSER,
       "kunde inte initialisera tolk med"},
  {
    ER_EXCEPTION_CREATING_POOL,
       "undantag skapar ny instans f\u00f6r pool"},
  {
    ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
       "V\u00e4g inneh\u00e5ller ogiltig flyktsekvens"},
  {
    ER_SCHEME_REQUIRED,
       "Schema kr\u00e4vs!"},
  {
    ER_NO_SCHEME_IN_URI,
       "Schema saknas i URI: {0}"},
  {
    ER_NO_SCHEME_INURI,
       "Schema saknas i URI"},
  {
    ER_PATH_INVALID_CHAR,
       "V\u00e4g inneh\u00e5ller ogiltigt tecken: {0}"},
  {
    ER_SCHEME_FROM_NULL_STRING,
       "Kan inte s\u00e4tta schema fr\u00e5n null-str\u00e4ng"},
  {
    ER_SCHEME_NOT_CONFORMANT,
       "Schemat \u00e4r inte likformigt."},
  {
    ER_HOST_ADDRESS_NOT_WELLFORMED,
       "V\u00e4rd \u00e4r inte en v\u00e4lformulerad adress"},
  {
    ER_PORT_WHEN_HOST_NULL,
       "Port kan inte s\u00e4ttas n\u00e4r v\u00e4rd \u00e4r null"},
  {
    ER_INVALID_PORT,
       "Ogiltigt portnummer"},
  {
    ER_FRAG_FOR_GENERIC_URI,
       "Fragment kan bara s\u00e4ttas f\u00f6r en allm\u00e4n URI"},
  {
    ER_FRAG_WHEN_PATH_NULL,
       "Fragment kan inte s\u00e4ttas n\u00e4r v\u00e4g \u00e4r null"},
  {
    ER_FRAG_INVALID_CHAR,
       "Fragment inneh\u00e5ller ogiltigt tecken"},
  {
    ER_PARSER_IN_USE,
        "Tolk anv\u00e4nds redan"},
  {
    ER_CANNOT_CHANGE_WHILE_PARSING,
        "Kan inte \u00e4ndra {0} {1} medan tolkning sker"},
  {
    ER_SELF_CAUSATION_NOT_PERMITTED,
        "Sj\u00e4lvorsakande inte till\u00e5ten"},
  {
    ER_NO_USERINFO_IF_NO_HOST,
        "Userinfo f\u00e5r inte anges om v\u00e4rden inte \u00e4r angiven"},
  {
    ER_NO_PORT_IF_NO_HOST,
        "Port f\u00e5r inte anges om v\u00e4rden inte \u00e4r angiven"},
  {
    ER_NO_QUERY_STRING_IN_PATH,
        "F\u00f6rfr\u00e5gan-str\u00e4ng kan inte anges i v\u00e4g och f\u00f6rfr\u00e5gan-str\u00e4ng"},
  {
    ER_NO_FRAGMENT_STRING_IN_PATH,
        "Fragment kan inte anges i b\u00e5de v\u00e4gen och fragmentet"},
  {
    ER_CANNOT_INIT_URI_EMPTY_PARMS,
        "Kan inte initialisera URI med tomma parametrar"},
  {
    ER_METHOD_NOT_SUPPORTED,
        "Metod \u00e4nnu inte underst\u00f6dd "},
  {
    ER_INCRSAXSRCFILTER_NOT_RESTARTABLE,
     "IncrementalSAXSource_Filter kan f\u00f6r n\u00e4rvarande inte startas om"},
  {
    ER_XMLRDR_NOT_BEFORE_STARTPARSE,
     "XMLReader inte innan startParse-beg\u00e4ran"},
  {
    ER_AXIS_TRAVERSER_NOT_SUPPORTED,
     "Det g\u00e5r inte att v\u00e4nda axeln: {0}"},
  {
    ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER,
     "ListingErrorHandler skapad med null PrintWriter!"},
  {
    ER_SYSTEMID_UNKNOWN,
     "SystemId ok\u00e4nt"},
  {
    ER_LOCATION_UNKNOWN,
     "Platsen f\u00f6r felet \u00e4r ok\u00e4nd"},
  {
    ER_PREFIX_MUST_RESOLVE,
      "Prefix must resolve to a namespace: {0}"},
  {
    ER_CREATEDOCUMENT_NOT_SUPPORTED,
      "createDocument() underst\u00f6ds inte av XPathContext!"},
  {
    ER_CHILD_HAS_NO_OWNER_DOCUMENT,
      "Attributbarn saknar \u00e4gardokument!"},
  {
    ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT,
      "Attributbarn saknar \u00e4gardokumentelement!"},
  {
    ER_CANT_OUTPUT_TEXT_BEFORE_DOC,
      "Varning: kan inte skriva ut text innan dokumentelement!  Ignorerar..."},
  {
    ER_CANT_HAVE_MORE_THAN_ONE_ROOT,
      "Kan inte ha mer \u00e4n en rot p\u00e5 en DOM!"},
  {
    ER_ARG_LOCALNAME_NULL,
       "Argument 'localName' \u00e4r null"},
  {
    ER_ARG_LOCALNAME_INVALID,
       "Localname i QNAME b\u00f6r vara ett giltigt NCName"},
  {
    ER_ARG_PREFIX_INVALID,
       "Prefixet i QNAME b\u00f6r vara ett giltigt NCName"},
  { "BAD_CODE",
      "Parameter till createMessage ligger utanf\u00f6r till\u00e5tet intervall"},
  { "FORMAT_FAILED",
      "Undantag utl\u00f6st vid messageFormat-anrop"},
  { "line",  "Rad #"},
  { "column", "Kolumn #"}
  };
  }
}
