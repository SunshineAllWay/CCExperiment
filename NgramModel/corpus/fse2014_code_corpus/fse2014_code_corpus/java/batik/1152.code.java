package org.apache.batik.svggen;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
public interface ExtensionHandler {
    SVGPaintDescriptor handlePaint(Paint paint,
                                          SVGGeneratorContext generatorContext);
    SVGCompositeDescriptor handleComposite(Composite composite,
                                                  SVGGeneratorContext generatorContext);
SVGFilterDescriptor handleFilter(BufferedImageOp filter,
                                            Rectangle filterRect,
                                            SVGGeneratorContext generatorContext);
}
