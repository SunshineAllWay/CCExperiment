package org.apache.tools.ant;
public interface TypeAdapter {
    void setProject(Project p);
    Project getProject();
    void setProxy(Object o);
    Object getProxy();
    void checkProxyClass(Class proxyClass);
}