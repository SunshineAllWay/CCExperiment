package org.apache.tools.ant.types.resources;
import java.io.IOException;
public class ImmutableResourceException extends IOException {
    public ImmutableResourceException() {
        super();
    }
    public ImmutableResourceException(String s) {
        super(s);
    }
}
