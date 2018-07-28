package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.SVGConstants;
public class JPainterCompare extends JPanel implements SVGConstants{
    public static final Dimension CANVAS_SIZE
        = new Dimension(300, 400);
    public static String MESSAGES_USAGE
        = "JPainterCompare.messages.usage";
    public static String MESSAGES_LOADING_CLASS
        = "JPainterCompare.messages.loading.class";
    public static String MESSAGES_LOADED_CLASS
        = "JPainterCompare.messages.loaded.class";
    public static String MESSAGES_INSTANCIATED_OBJECT
        = "JPainterCompare.messages.instanciated.object";
    public static String ERROR_COULD_NOT_LOAD_CLASS
        = "JPainterCompare.error.could.not.load.class";
    public static String ERROR_COULD_NOT_INSTANCIATE_OBJECT
        = "JPainterCompare.error.could.not.instanciate.object";
    public static String ERROR_CLASS_NOT_PAINTER
        = "JPainterCompare.error.class.not.painter";
    public static String ERROR_COULD_NOT_TRANSCODE_TO_SVG
        = "JPainterCompare.error.could.not.transcode.to.svg";
    public static String ERROR_COULD_NOT_CONVERT_FILE_PATH_TO_URL
        = "JPainterCompare.error.could.not.convert.file.path.to.url";
    public static String ERROR_COULD_NOT_RENDER_GENERATED_SVG
        = "JPainterCompare.error.could.not.render.generated.svg";
    public static String CONFIG_TMP_FILE_PREFIX
        = "JPainterCompare.config.tmp.file.prefix";
    protected SVGGraphics2D buildSVGGraphics2D() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document domFactory = impl.createDocument(namespaceURI, SVG_SVG_TAG, null);
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(domFactory);
        GenericImageHandler ihandler = new CachedImageHandlerBase64Encoder();
        ctx.setGenericImageHandler(ihandler);
        return new SVGGraphics2D(ctx, false);
    }
    static class LoaderListener extends SVGDocumentLoaderAdapter{
        public final String sem = "sem";
        public boolean success = false;
        public void documentLoadingFailed(SVGDocumentLoaderEvent e){
            synchronized(sem){
                sem.notifyAll();
            }
        }
        public void documentLoadingCompleted(SVGDocumentLoaderEvent e){
            success = true;
            synchronized(sem){
                sem.notifyAll();
            }
        }
    }
    public JPainterCompare(Painter painter){
        JPainterComponent ref = new JPainterComponent(painter);
        SVGGraphics2D g2d = buildSVGGraphics2D();
        g2d.setSVGCanvasSize(CANVAS_SIZE);
        File tmpFile = null;
        try{
            tmpFile = File.createTempFile(CONFIG_TMP_FILE_PREFIX,
                                          ".svg");
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8");
            painter.paint(g2d);
            g2d.stream(osw);
            osw.flush();
        }catch(Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException
                (Messages.formatMessage(ERROR_COULD_NOT_TRANSCODE_TO_SVG,
                                        new Object[]{e.getClass().getName()}));
        }
        JSVGCanvas svgCanvas = new JSVGCanvas();
        LoaderListener l = new LoaderListener();
        svgCanvas.addSVGDocumentLoaderListener(l);
        try{
            svgCanvas.setURI(tmpFile.toURL().toString());
            synchronized(l.sem){
                l.sem.wait();
            }
        }catch(Exception e){
            e.printStackTrace();
            new Error
                (Messages.formatMessage(ERROR_COULD_NOT_CONVERT_FILE_PATH_TO_URL,
                                        new Object[]{e.getMessage()}));
        }
        if(l.success){
            setLayout(new GridLayout(1,2));
            add(ref);
            add(svgCanvas);
        }
        else{
            throw new Error
                (Messages.formatMessage(ERROR_COULD_NOT_RENDER_GENERATED_SVG,null));
        }
    }
    public Dimension getPreferredSize(){
        return new Dimension(CANVAS_SIZE.width*2, CANVAS_SIZE.height);
    }
    public static void main(String[] args){
        if(args.length <= 0){
            System.out.println(Messages.formatMessage
                               (MESSAGES_USAGE, null));
            System.exit(0);
        }
        String className = args[0];
        System.out.println
            (Messages.formatMessage(MESSAGES_LOADING_CLASS,
                                    new Object[]{className}));
        Class cl = null;
        try{
            cl = Class.forName(className);
            System.out.println
                (Messages.formatMessage(MESSAGES_LOADED_CLASS,
                                        new Object[]{className}));
        }catch(Exception e){
            System.out.println
                (Messages.formatMessage(ERROR_COULD_NOT_LOAD_CLASS,
                                        new Object[] {className,
                                                      e.getClass().getName() }));
            System.exit(0);
        }
        Object o = null;
        try{
            o = cl.newInstance();
            System.out.println
                (Messages.formatMessage(MESSAGES_INSTANCIATED_OBJECT,
                                        null));
        }catch(Exception e){
            System.out.println
                (Messages.formatMessage(ERROR_COULD_NOT_INSTANCIATE_OBJECT,
                                        new Object[] {className,
                                                      e.getClass().getName()}));
            System.exit(0);
        }
        Painter p = null;
        try{
            p = (Painter)o;
        }catch(ClassCastException e){
            System.out.println
                (Messages.formatMessage(ERROR_CLASS_NOT_PAINTER,
                                        new Object[]{className}));
            System.exit(0);
        }
        JFrame f = new JFrame();
        JPainterCompare c = new JPainterCompare(p);
        c.setBackground(Color.white);
        c.setPreferredSize(new Dimension(300, 400));
        f.getContentPane().add(c);
        f.getContentPane().setBackground(Color.white);
        f.pack();
        f.setVisible(true);
    }
}
