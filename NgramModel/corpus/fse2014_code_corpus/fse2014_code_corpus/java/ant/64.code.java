package org.apache.tools.ant;
public interface DynamicElementNS {
    Object createDynamicElement(
        String uri, String localName, String qName) throws BuildException;
}
