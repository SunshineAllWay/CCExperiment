package org.apache.tools.ant.util.facade;
import org.apache.tools.ant.types.Commandline;
public class ImplementationSpecificArgument extends Commandline.Argument {
    private String impl;
    public ImplementationSpecificArgument() {
        super();
    }
    public void setImplementation(String impl) {
        this.impl = impl;
    }
    public final String[] getParts(String chosenImpl) {
        if (impl == null || impl.equals(chosenImpl)) {
            return super.getParts();
        } else {
            return new String[0];
        }
    }
}
