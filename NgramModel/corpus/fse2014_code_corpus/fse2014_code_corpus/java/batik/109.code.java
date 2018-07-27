package org.apache.batik.apps.svgpp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
public class Main {
    public static void main(String[] args) {
        new Main(args).run();
    }
    public static final String BUNDLE_CLASSNAME =
        "org.apache.batik.apps.svgpp.resources.Messages";
    protected static LocalizableSupport localizableSupport =
        new LocalizableSupport(BUNDLE_CLASSNAME, Main.class.getClassLoader());
    protected String[] arguments;
    protected int index;
    protected Map handlers = new HashMap();
    {
        handlers.put("-doctype", new DoctypeHandler());
        handlers.put("-doc-width", new DocWidthHandler());
        handlers.put("-newline", new NewlineHandler());
        handlers.put("-public-id", new PublicIdHandler());
        handlers.put("-no-format", new NoFormatHandler());
        handlers.put("-system-id", new SystemIdHandler());
        handlers.put("-tab-width", new TabWidthHandler());
        handlers.put("-xml-decl", new XMLDeclHandler());
    }
    protected Transcoder transcoder = new SVGTranscoder();
    public Main(String[] args) {
        arguments = args;
    }
    public void run() {
        if (arguments.length == 0) {
            printUsage();
            return;
        }
        try {
            for (;;) {
                OptionHandler oh = (OptionHandler)handlers.get(arguments[index]);
                if (oh == null) {
                    break;
                }
                oh.handleOption();
            }
            TranscoderInput in;
            in = new TranscoderInput(new java.io.FileReader(arguments[index++]));
            TranscoderOutput out;
            if (index < arguments.length) {
                out = new TranscoderOutput(new java.io.FileWriter(arguments[index]));
            } else {
                out = new TranscoderOutput(new java.io.OutputStreamWriter(System.out));
            }
            transcoder.transcode(in, out);
        } catch (Exception e) {
            e.printStackTrace();
            printUsage();
        }
    }
    protected void printUsage() {
        printHeader();
        System.out.println(localizableSupport.formatMessage("syntax", null));
        System.out.println();
        System.out.println(localizableSupport.formatMessage("options", null));
        Iterator it = handlers.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(((OptionHandler)handlers.get(s)).getDescription());
        }
    }
    protected void printHeader() {
        System.out.println(localizableSupport.formatMessage("header", null));
    }
    protected interface OptionHandler {
        void handleOption();
        String getDescription();
    }
    protected class DoctypeHandler implements OptionHandler {
        protected final Map values = new HashMap(6);
        {
            values.put("remove", SVGTranscoder.VALUE_DOCTYPE_REMOVE);
            values.put("change", SVGTranscoder.VALUE_DOCTYPE_CHANGE);
        }
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            Object val = values.get(arguments[index++]);
            if (val == null) {
                throw new IllegalArgumentException();
            }
            transcoder.addTranscodingHint(SVGTranscoder.KEY_DOCTYPE, val);
        }
        public String getDescription() {
            return localizableSupport.formatMessage("doctype.description", null);
        }
    }
    protected class NewlineHandler implements OptionHandler {
        protected final Map values = new HashMap(6);
        {
            values.put("cr",    SVGTranscoder.VALUE_NEWLINE_CR);
            values.put("cr-lf", SVGTranscoder.VALUE_NEWLINE_CR_LF);
            values.put("lf",    SVGTranscoder.VALUE_NEWLINE_LF);
        }
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            Object val = values.get(arguments[index++]);
            if (val == null) {
                throw new IllegalArgumentException();
            }
            transcoder.addTranscodingHint(SVGTranscoder.KEY_NEWLINE, val);
        }
        public String getDescription() {
            return localizableSupport.formatMessage("newline.description", null);
        }
    }
    protected class NoFormatHandler implements OptionHandler {
        public void handleOption() {
            index++;
            transcoder.addTranscodingHint(SVGTranscoder.KEY_FORMAT, Boolean.FALSE);
        }
        public String getDescription() {
            return localizableSupport.formatMessage("no-format.description", null);
        }
    }
    protected class PublicIdHandler implements OptionHandler {
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            String s = arguments[index++];
            transcoder.addTranscodingHint(SVGTranscoder.KEY_PUBLIC_ID, s);
        }
        public String getDescription() {
            return localizableSupport.formatMessage("public-id.description", null);
        }
    }
    protected class SystemIdHandler implements OptionHandler {
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            String s = arguments[index++];
            transcoder.addTranscodingHint(SVGTranscoder.KEY_SYSTEM_ID, s);
        }
        public String getDescription() {
            return localizableSupport.formatMessage("system-id.description", null);
        }
    }
    protected class XMLDeclHandler implements OptionHandler {
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            String s = arguments[index++];
            transcoder.addTranscodingHint(SVGTranscoder.KEY_XML_DECLARATION, s);
        }
        public String getDescription() {
            return localizableSupport.formatMessage("xml-decl.description", null);
        }
    }
    protected class TabWidthHandler implements OptionHandler {
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            transcoder.addTranscodingHint(SVGTranscoder.KEY_TABULATION_WIDTH,
                                          new Integer(arguments[index++]));
        }
        public String getDescription() {
            return localizableSupport.formatMessage("tab-width.description", null);
        }
    }
    protected class DocWidthHandler implements OptionHandler {
        public void handleOption() {
            index++;
            if (index >= arguments.length) {
                throw new IllegalArgumentException();
            }
            transcoder.addTranscodingHint(SVGTranscoder.KEY_DOCUMENT_WIDTH,
                                          new Integer(arguments[index++]));
        }
        public String getDescription() {
            return localizableSupport.formatMessage("doc-width.description", null);
        }
    }
}
