package org.apache.batik.ext.awt.image.codec.imageio;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterParams;
public class ImageIOImageWriter implements ImageWriter, IIOWriteWarningListener {
    private String targetMIME;
    public ImageIOImageWriter(String mime) {
        this.targetMIME = mime;
    }
    public void writeImage(RenderedImage image, OutputStream out) throws IOException {
        writeImage(image, out, null);
    }
    public void writeImage(RenderedImage image, OutputStream out, 
            ImageWriterParams params) 
                throws IOException {
        Iterator iter;
        iter = ImageIO.getImageWritersByMIMEType(getMIMEType());
        javax.imageio.ImageWriter iiowriter = null;
        try {
            iiowriter = (javax.imageio.ImageWriter)iter.next();
            if (iiowriter != null) {
                iiowriter.addIIOWriteWarningListener(this);
                ImageOutputStream imgout = null;
                try {
                    imgout = ImageIO.createImageOutputStream(out);
                    ImageWriteParam iwParam = getDefaultWriteParam(iiowriter, image, params);
                    ImageTypeSpecifier type;
                    if (iwParam.getDestinationType() != null) {
                        type = iwParam.getDestinationType();
                    } else {
                        type = ImageTypeSpecifier.createFromRenderedImage(image);
                    }
                    IIOMetadata meta = iiowriter.getDefaultImageMetadata(
                            type, iwParam);
                    if (params != null && meta != null) {
                        meta = updateMetadata(meta, params); 
                    }
                    iiowriter.setOutput(imgout);
                    IIOImage iioimg = new IIOImage(image, null, meta);
                    iiowriter.write(null, iioimg, iwParam);
                } finally {
                    if (imgout != null) {
                        imgout.close();
                    }
                }
            } else {
                throw new UnsupportedOperationException("No ImageIO codec for writing " 
                        + getMIMEType() + " is available!");
            }
        } finally {
            if (iiowriter != null) {
                iiowriter.dispose();
            }
        }
    }
    protected ImageWriteParam getDefaultWriteParam(
            javax.imageio.ImageWriter iiowriter, RenderedImage image, 
            ImageWriterParams params) {
        ImageWriteParam param = iiowriter.getDefaultWriteParam();
        if ((params != null) && (params.getCompressionMethod() != null)) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(params.getCompressionMethod());
        }
        return param; 
    }
    protected IIOMetadata updateMetadata(IIOMetadata meta, ImageWriterParams params) {
        final String stdmeta = "javax_imageio_1.0";
        if (meta.isStandardMetadataFormatSupported()) {
            IIOMetadataNode root = (IIOMetadataNode)meta.getAsTree(stdmeta);
            IIOMetadataNode dim = getChildNode(root, "Dimension");
            IIOMetadataNode child;
            if (params.getResolution() != null) {
                child = getChildNode(dim, "HorizontalPixelSize");
                if (child == null) {
                    child = new IIOMetadataNode("HorizontalPixelSize");
                    dim.appendChild(child);
                }
                child.setAttribute("value", 
                        Double.toString(params.getResolution().doubleValue() / 25.4));
                child = getChildNode(dim, "VerticalPixelSize");
                if (child == null) {
                    child = new IIOMetadataNode("VerticalPixelSize");
                    dim.appendChild(child);
                }
                child.setAttribute("value", 
                        Double.toString(params.getResolution().doubleValue() / 25.4));
            }
            try {
                meta.mergeTree(stdmeta, root);
            } catch (IIOInvalidTreeException e) {
                throw new RuntimeException("Cannot update image metadata: " 
                            + e.getMessage());
            }
        }
        return meta;
    }
    protected static IIOMetadataNode getChildNode(Node n, String name) {
        NodeList nodes = n.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (name.equals(child.getNodeName())) {
                return (IIOMetadataNode)child;
            }
        }
        return null;
    }
    public String getMIMEType() {
        return this.targetMIME;
    }
    public void warningOccurred(javax.imageio.ImageWriter source, 
            int imageIndex, String warning) {
        System.err.println("Problem while writing image using ImageI/O: " 
                + warning);
    }
}
