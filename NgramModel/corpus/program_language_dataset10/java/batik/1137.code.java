package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
public abstract class AbstractSVGConverter implements SVGConverter, ErrorConstants {
    protected SVGGeneratorContext generatorContext;
    protected Map descMap = new HashMap();
    protected List defSet = new LinkedList();
    public AbstractSVGConverter(SVGGeneratorContext generatorContext) {
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_CONTEXT_NULL);
        this.generatorContext = generatorContext;
    }
    public List getDefinitionSet() {
        return defSet;
    }
    public final String doubleString(double value) {
        return generatorContext.doubleString(value);
    }
}
