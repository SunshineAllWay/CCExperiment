package org.apache.xerces.impl.xs.traversers;
import org.apache.xerces.impl.xs.opti.ElementImpl;
import org.w3c.dom.Element;
final class XSAnnotationInfo {
    String fAnnotation;
    int fLine;
    int fColumn;
    int fCharOffset;
    XSAnnotationInfo next;
    XSAnnotationInfo(String annotation, int line, int column, int charOffset) {
        fAnnotation = annotation;
        fLine = line;
        fColumn = column;
        fCharOffset = charOffset;
    }
    XSAnnotationInfo(String annotation, Element annotationDecl) {
        fAnnotation = annotation;
        if (annotationDecl instanceof ElementImpl) {
            final ElementImpl annotationDeclImpl = (ElementImpl) annotationDecl;
            fLine = annotationDeclImpl.getLineNumber();
            fColumn = annotationDeclImpl.getColumnNumber();
            fCharOffset = annotationDeclImpl.getCharacterOffset();
        }
        else {
            fLine = -1;
            fColumn = -1;
            fCharOffset = -1;
        }
    }
} 
