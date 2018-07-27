package org.apache.batik.svggen;
public abstract class SVGGraphicObjectConverter implements SVGSyntax {
    protected SVGGeneratorContext generatorContext;
    public SVGGraphicObjectConverter(SVGGeneratorContext generatorContext) {
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_CONTEXT_NULL);
        this.generatorContext = generatorContext;
    }
    public final String doubleString(double value) {
        return generatorContext.doubleString(value);
    }
}
