package org.apache.tools.ant.types.resources;
import org.apache.tools.ant.types.ResourceCollection;
public abstract class CompressedResource extends ContentTransformingResource {
    protected CompressedResource() {
    }
    protected CompressedResource(ResourceCollection other) {
        addConfigured(other);
    }
    public String toString() {
        return getCompressionName() + " compressed " + super.toString();
    }
    protected abstract String getCompressionName();
}
