package org.apache.batik.test.xml;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class XMLReflect implements XMLReflectConstants{
    public static final String NO_MATCHING_CONSTRUCTOR
        = "xml.XMLReflect.error.no.matching.constructor";
    public static Object buildObject(Element element) throws Exception {
        Element classDefiningElement =
            getClassDefiningElement(element);
        String className
            = classDefiningElement.getAttribute(XR_CLASS_ATTRIBUTE);
        Class cl = Class.forName(className);
        Object[] argsArray = null;
        Class[]  argsClasses = null;
        NodeList children = element.getChildNodes();
        if(children != null && children.getLength() > 0){
            int n = children.getLength();
            List args = new ArrayList();
            for(int i=0; i<n; i++){
                Node child = children.item(i);
                if(child.getNodeType() == Node.ELEMENT_NODE){
                    Element childElement = (Element)child;
                    String tagName = childElement.getTagName().intern();
                    if(tagName == XR_ARG_TAG){
                        Object arg = buildArgument(childElement);
                        args.add(arg);
                    }
                }
            }
            if(args.size() > 0){
                argsArray = new Object[args.size()];
                args.toArray(argsArray);
                argsClasses = new Class[args.size()];
                for(int i=0; i<args.size(); i++){
                    argsClasses[i] = argsArray[i].getClass();
                }
            }
        }
        Constructor constructor
            = getDeclaredConstructor(cl, argsClasses);
        if (constructor == null) {
            String argsClassesStr = "null";
            if (argsClasses != null) {
                argsClassesStr = "";
                for (int i=0; i<argsClasses.length; i++) {
                    argsClassesStr += argsClasses[i].getName() + " / ";
                }
            }
            throw new Exception(Messages.formatMessage(NO_MATCHING_CONSTRUCTOR,
                                                       new Object[] { className,
                                                                      argsClassesStr }));
        }
        return configureObject(constructor.newInstance(argsArray),
                               element, classDefiningElement);
    }
    public static Object configureObject(Object obj,
                                         Element element,
                                         Element classDefiningElement) throws Exception {
        List v = new ArrayList();
        v.add(element);
        while (element != classDefiningElement) {
            element = (Element) element.getParentNode();
            v.add(element);
        }
        int ne = v.size();
        for (int j=ne-1; j>=0; j--) {
            element = (Element)v.get(j);
            NodeList children = element.getChildNodes();
            if(children != null && children.getLength() > 0){
                int n = children.getLength();
                for(int i=0; i<n; i++){
                    Node child = children.item(i);
                    if(child.getNodeType() == Node.ELEMENT_NODE){
                        Element childElement = (Element)child;
                        String tagName = childElement.getTagName().intern();
                        if(tagName == XR_PROPERTY_TAG){
                            Object arg = buildArgument(childElement);
                            String propertyName
                                = childElement.getAttribute(XR_NAME_ATTRIBUTE);
                            setObjectProperty(obj, propertyName, arg);
                        }
                    }
                }
            }
        }
        return obj;
    }
    public static void setObjectProperty(Object obj,
                                         String propertyName,
                                         Object propertyValue)
        throws Exception {
        Class cl = obj.getClass();
        Method m = null;
        try {
            m = cl.getMethod("set" + propertyName,
                             new Class[]{propertyValue.getClass()});
        } catch (NoSuchMethodException e) {
            Class propertyClass = propertyValue.getClass();
            try {
                if (propertyClass == java.lang.Double.class) {
                    m = cl.getMethod("set" + propertyName,
                                     new Class[] {java.lang.Double.TYPE});
                } else if (propertyClass == java.lang.Float.class) {
                    m = cl.getMethod("set" + propertyName,
                                     new Class[] {java.lang.Float.TYPE});
                } else if (propertyClass == java.lang.Integer.class) {
                    m = cl.getMethod("set" + propertyName,
                                     new Class[] {java.lang.Integer.TYPE});
                } else if (propertyClass == java.lang.Boolean.class) {
                    m = cl.getMethod("set" + propertyName,
                                     new Class[] {java.lang.Boolean.TYPE});
                } else {
                    System.err.println("Could not find a set method for property : " + propertyName
                                       + " with value " + propertyValue + " and class " + propertyValue.getClass().getName());
                    throw e;
                }
            } catch (NoSuchMethodException nsme) {
                throw nsme;
            }
        }
        if(m != null){
            m.invoke(obj, new Object[]{propertyValue});
        }
    }
    public static Constructor getDeclaredConstructor(Class cl,
                                                 Class[] argClasses){
        Constructor[] cs = cl.getDeclaredConstructors();
        for(int i=0; i<cs.length; i++){
            Class[] reqArgClasses = cs[i].getParameterTypes();
            if(reqArgClasses != null && reqArgClasses.length > 0){
                if(reqArgClasses.length == argClasses.length){
                    int j=0;
                    for(; j<argClasses.length; j++){
                        if(!reqArgClasses[j].isAssignableFrom(argClasses[j])){
                            break;
                        }
                    }
                    if(j == argClasses.length){
                        return cs[i];
                    }
                }
            }
            else{
                if(argClasses == null || argClasses.length == 0){
                    return cs[i];
                }
            }
        }
        return null;
    }
    public static Object buildArgument(Element element) throws Exception {
        if(!element.hasChildNodes()){
            Element classDefiningElement =
                getClassDefiningElement(element);
            String classAttr = classDefiningElement.getAttribute(XR_CLASS_ATTRIBUTE);
            Class cl = Class.forName(classAttr);
            if(element.hasAttribute(XR_VALUE_ATTRIBUTE)){
                String value = element.getAttribute(XR_VALUE_ATTRIBUTE);
                Constructor constructor
                    = cl.getDeclaredConstructor(new Class[] { String.class });
                return constructor.newInstance(new Object[] {value});
            }
            else{
                return cl.newInstance();
            }
        }
        else{
            return buildObject(element);
        }
    }
    public static Element getClassDefiningElement(Element element) {
        if(element != null){
            String classAttr = element.getAttribute(XR_CLASS_ATTRIBUTE);
            if(classAttr == null || "".equals(classAttr)){
                Node parent = element.getParentNode();
                if(parent != null && parent.getNodeType() == Node.ELEMENT_NODE){
                    return getClassDefiningElement((Element)parent);
                }
                else{
                    return null;
                }
            }
            return element;
        }
        return null;
    }
}
