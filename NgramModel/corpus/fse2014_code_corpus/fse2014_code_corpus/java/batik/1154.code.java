package org.apache.batik.svggen;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public abstract class ImageCacher implements SVGSyntax, ErrorConstants {
    DOMTreeManager  domTreeManager = null;
    Map             imageCache;
    Checksum        checkSum;
    public ImageCacher() {
        imageCache = new HashMap();
        checkSum = new Adler32();
    }
    public ImageCacher(DOMTreeManager domTreeManager) {
        this();
        setDOMTreeManager(domTreeManager);
    }
    public void setDOMTreeManager(DOMTreeManager domTreeManager) {
        if (domTreeManager == null){
            throw new IllegalArgumentException();
        }
        this.domTreeManager = domTreeManager;
    }
    public DOMTreeManager getDOMTreeManager(){
        return domTreeManager;
    }
    public String lookup(ByteArrayOutputStream os,
                         int width, int height,
                         SVGGeneratorContext ctx)
                             throws SVGGraphics2DIOException {
        int     checksum = getChecksum(os.toByteArray());
        Integer key      = new Integer(checksum);
        String  href     = null;
        Object data = getCacheableData(os);
        LinkedList list = (LinkedList) imageCache.get(key);
        if(list == null) {
            list = new LinkedList();
            imageCache.put(key, list);
        } else {
            for(ListIterator i = list.listIterator(0); i.hasNext(); ) {
                ImageCacheEntry entry = (ImageCacheEntry) i.next();
                if(entry.checksum == checksum && imagesMatch(entry.src, data)) {
                    href = entry.href;
                    break;
                }
            }
        }
        if(href == null) {
            ImageCacheEntry newEntry = createEntry(checksum, data,
                                                   width, height,
                                                   ctx);
            list.add(newEntry);
            href = newEntry.href;
        }
        return href;
    }
    abstract Object getCacheableData(ByteArrayOutputStream os);
    abstract boolean imagesMatch(Object o1, Object o2)
                                             throws SVGGraphics2DIOException;
    abstract ImageCacheEntry createEntry(int checksum,
                                         Object data,
                                         int width, int height,
                                         SVGGeneratorContext ctx)
                                             throws SVGGraphics2DIOException;
    int getChecksum(byte[] data) {
        checkSum.reset();
        checkSum.update(data, 0, data.length);
        return (int) checkSum.getValue();
    }
    private static class ImageCacheEntry {
        public int checksum;
        public Object src;
        public String href;
        ImageCacheEntry(int    checksum,
                               Object src,
                               String href) {
            this.checksum = checksum;
            this.src      = src;
            this.href     = href;
        }
    }
    public static class Embedded extends ImageCacher {
        public void setDOMTreeManager(DOMTreeManager domTreeManager) {
            if(this.domTreeManager != domTreeManager) {
                this.domTreeManager = domTreeManager;
                this.imageCache     = new HashMap();
            }
        }
        Object getCacheableData(ByteArrayOutputStream os) {
            return DATA_PROTOCOL_PNG_PREFIX + os.toString();
        }
        boolean imagesMatch(Object o1, Object o2) {
            return o1.equals(o2);
        }
        ImageCacheEntry createEntry(int checksum, Object data,
                                    int width, int height,
                                    SVGGeneratorContext ctx) {
            String id = ctx.idGenerator.generateID(ID_PREFIX_IMAGE);
            addToTree(id, (String) data, width, height, ctx);
            return new ImageCacheEntry(checksum, data, SIGN_POUND + id);    
        }
        private void addToTree(String id,
                               String href,
                               int width, int height,
                               SVGGeneratorContext ctx) {
            Document domFactory = domTreeManager.getDOMFactory();
            Element imageElement = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                              SVG_IMAGE_TAG);
            imageElement.setAttributeNS(null, SVG_ID_ATTRIBUTE,
                                              id);
            imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,
                                              Integer.toString(width));
            imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE,
                                              Integer.toString(height));
            imageElement.setAttributeNS(DefaultImageHandler.XLINK_NAMESPACE_URI,
                                              XLINK_HREF_QNAME,
                                              href);
            domTreeManager.addOtherDef(imageElement);
        }
    }
    public static class External extends ImageCacher {
        private String imageDir;
        private String prefix;
        private String suffix;
        public External(String imageDir, String prefix, String suffix) {
            super();
            this.imageDir = imageDir;
            this.prefix   = prefix;
            this.suffix   = suffix;
        }
        Object getCacheableData(ByteArrayOutputStream os) {
            return os;
        }
        boolean imagesMatch(Object o1, Object o2)
                throws SVGGraphics2DIOException {
            boolean match = false;
            try {
                FileInputStream imageStream =
                                    new FileInputStream((File) o1);
                int imageLen = imageStream.available();
                byte[] imageBytes = new byte[imageLen];
                byte[] candidateBytes =
                        ((ByteArrayOutputStream) o2).toByteArray();
                int bytesRead = 0;
                while (bytesRead != imageLen) {
                    bytesRead += imageStream.read
                      (imageBytes, bytesRead, imageLen-bytesRead);
                }
                match = Arrays.equals(imageBytes, candidateBytes);
            } catch(IOException e) {
                throw new SVGGraphics2DIOException(
                                    ERR_READ+((File) o1).getName());
            }
            return match;
        }
        ImageCacheEntry createEntry(int checksum, Object data,
                                    int width, int height,
                                    SVGGeneratorContext ctx)
            throws SVGGraphics2DIOException {
            File imageFile = null;
            try {
                while (imageFile == null) {
                    String fileId = ctx.idGenerator.generateID(prefix);
                    imageFile = new File(imageDir, fileId + suffix);
                    if (imageFile.exists())
                        imageFile = null;
                }
                OutputStream outputStream = new FileOutputStream(imageFile);
                ((ByteArrayOutputStream) data).writeTo(outputStream);
                ((ByteArrayOutputStream) data).close();
            } catch(IOException e) {
                throw new SVGGraphics2DIOException(ERR_WRITE+imageFile.getName());
            }
            return new ImageCacheEntry(checksum, imageFile, imageFile.getName());   
        }
    }
}
