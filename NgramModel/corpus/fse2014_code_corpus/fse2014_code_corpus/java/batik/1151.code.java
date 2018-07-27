package org.apache.batik.svggen;
public interface ErrorHandler {
    void handleError(SVGGraphics2DIOException ex)
        throws SVGGraphics2DIOException;
    void handleError(SVGGraphics2DRuntimeException ex)
        throws SVGGraphics2DRuntimeException;
}
