package org.apache.batik.gvt.renderer;
import org.apache.batik.util.Platform;
public class ConcreteImageRendererFactory implements ImageRendererFactory {
    public Renderer createRenderer() {
        return createStaticImageRenderer();
    }
    public ImageRenderer createStaticImageRenderer() {
        if (Platform.isOSX)
            return new MacRenderer();
        return new StaticRenderer();
    }
    public ImageRenderer createDynamicImageRenderer() {
        if (Platform.isOSX)
            return new MacRenderer();
        return new DynamicRenderer();
    }
}
