package org.apache.log4j.spi;
public interface ThrowableRenderer {
    public String[] doRender(Throwable t);
}
