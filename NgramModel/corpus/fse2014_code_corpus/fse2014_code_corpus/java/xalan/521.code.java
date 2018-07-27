package org.apache.xalan.xsltc.runtime;
import java.util.ListResourceBundle;
public class ErrorMessages_tr extends ListResourceBundle {
    public Object[][] getContents()
    {
      return new Object[][] {
        {BasisLibrary.RUN_TIME_INTERNAL_ERR,
        "''{0}'' i\u00e7inde y\u00fcr\u00fctme zaman\u0131 i\u00e7 hatas\u0131"},
        {BasisLibrary.RUN_TIME_COPY_ERR,
        "<xsl:copy> y\u00fcr\u00fct\u00fcl\u00fcrken y\u00fcr\u00fctme zaman\u0131 hatas\u0131."},
        {BasisLibrary.DATA_CONVERSION_ERR,
        "''{0}'' tipinden ''{1}'' tipine d\u00f6n\u00fc\u015ft\u00fcrme ge\u00e7ersiz."},
        {BasisLibrary.EXTERNAL_FUNC_ERR,
        "''{0}'' d\u0131\u015f i\u015flevi XSLTC taraf\u0131ndan desteklenmiyor."},
        {BasisLibrary.EQUALITY_EXPR_ERR,
        "E\u015fitlik ifadesinde bilinmeyen ba\u011f\u0131ms\u0131z de\u011fi\u015fken tipi."},
        {BasisLibrary.INVALID_ARGUMENT_ERR,
        "''{1}'' i\u015flevi \u00e7a\u011fr\u0131s\u0131nda ba\u011f\u0131ms\u0131z de\u011fi\u015fken tipi ''{0}'' ge\u00e7ersiz."},
        {BasisLibrary.FORMAT_NUMBER_ERR,
        "''{0}'' say\u0131s\u0131n\u0131 ''{1}'' \u00f6r\u00fcnt\u00fcs\u00fcn\u00fc kullanarak bi\u00e7imleme giri\u015fimi."},
        {BasisLibrary.ITERATOR_CLONE_ERR,
        "''{0}'' yineleyicisinin e\u015fkopyas\u0131 yarat\u0131lam\u0131yor."},
        {BasisLibrary.AXIS_SUPPORT_ERR,
        "''{0}'' ekseni i\u00e7in yineleyici desteklenmiyor."},
        {BasisLibrary.TYPED_AXIS_SUPPORT_ERR,
        "Tip atanm\u0131\u015f ''{0}'' ekseni i\u00e7in yineleyici desteklenmiyor."},
        {BasisLibrary.STRAY_ATTRIBUTE_ERR,
        "''{0}'' \u00f6zniteli\u011fi \u00f6\u011fenin d\u0131\u015f\u0131nda."},
        {BasisLibrary.STRAY_NAMESPACE_ERR,
        "''{0}''=''{1}'' ad alan\u0131 bildirimi \u00f6\u011fenin d\u0131\u015f\u0131nda."},
        {BasisLibrary.NAMESPACE_PREFIX_ERR,
        "''{0}'' \u00f6nekine ili\u015fkin ad alan\u0131 bildirilmedi."},
        {BasisLibrary.DOM_ADAPTER_INIT_ERR,
        "DOMAdapter, yanl\u0131\u015f tipte kaynak DOM kullan\u0131larak yarat\u0131ld\u0131."},
        {BasisLibrary.PARSER_DTD_SUPPORT_ERR,
        "Kulland\u0131\u011f\u0131n\u0131z SAX ayr\u0131\u015ft\u0131r\u0131c\u0131s\u0131 DTD bildirim olaylar\u0131n\u0131 i\u015flemiyor."},
        {BasisLibrary.NAMESPACES_SUPPORT_ERR,
        "Kulland\u0131\u011f\u0131n\u0131z SAX ayr\u0131\u015ft\u0131r\u0131c\u0131s\u0131n\u0131n XML ad alanlar\u0131 deste\u011fi yok."},
        {BasisLibrary.CANT_RESOLVE_RELATIVE_URI_ERR,
        "''{0}'' URI ba\u015fvurusu \u00e7\u00f6z\u00fclemedi."},
        {BasisLibrary.UNSUPPORTED_XSL_ERR,
        "XSL \u00f6\u011fesi ''{0}'' desteklenmiyor"},
        {BasisLibrary.UNSUPPORTED_EXT_ERR,
        "XSLTC uzant\u0131s\u0131 ''{0}'' tan\u0131nm\u0131yor"},
        {BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR,
        "Belirtilen derleme sonucu s\u0131n\u0131f dosyas\u0131 ''{0}'', kullan\u0131lmakta olan XSLTC s\u00fcr\u00fcm\u00fcnden daha yeni bir XSLTC s\u00fcr\u00fcm\u00fcyle yarat\u0131lm\u0131\u015f.  Bi\u00e7em yapra\u011f\u0131n\u0131 yeniden derlemeli ya da bu derleme sonucu s\u0131n\u0131f dosyas\u0131n\u0131 \u00e7al\u0131\u015ft\u0131rmak i\u00e7in daha yeni bir XSLTC s\u00fcr\u00fcm\u00fcn\u00fc kullanmal\u0131s\u0131n\u0131z."},
        {BasisLibrary.INVALID_QNAME_ERR,
        "De\u011ferinin bir QName olmas\u0131 gereken \u00f6zniteli\u011fin de\u011feri ''{0}''"},
        {BasisLibrary.INVALID_NCNAME_ERR,
        "De\u011ferinin bir NCName olmas\u0131 gereken \u00f6zniteli\u011fin de\u011feri ''{0}''"},
        {BasisLibrary.UNALLOWED_EXTENSION_FUNCTION_ERR,
        "G\u00fcvenli i\u015fleme \u00f6zelli\u011fi true de\u011ferine ayarland\u0131\u011f\u0131nda ''{0}'' eklenti i\u015flevinin kullan\u0131lmas\u0131na izin verilmez."},
        {BasisLibrary.UNALLOWED_EXTENSION_ELEMENT_ERR,
        "G\u00fcvenli i\u015fleme \u00f6zelli\u011fi true de\u011ferine ayarland\u0131\u011f\u0131nda ''{0}'' eklenti \u00f6\u011fesinin kullan\u0131lmas\u0131na izin verilmez."},
    };
    }
}
