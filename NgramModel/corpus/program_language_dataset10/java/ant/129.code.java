package org.apache.tools.ant.input;
public interface InputHandler {
    void handleInput(InputRequest request)
        throws org.apache.tools.ant.BuildException;
}