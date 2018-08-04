package org.apache.tools.ant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import org.apache.tools.ant.util.CollectionUtils;
import org.xml.sax.AttributeList;
import org.xml.sax.helpers.AttributeListImpl;
public class RuntimeConfigurable implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Hashtable EMPTY_HASHTABLE = new Hashtable(0);
    private String elementTag = null;
    private List children = null;
    private transient Object wrappedObject = null;
    private transient IntrospectionHelper.Creator creator;
    private transient AttributeList attributes;
    private LinkedHashMap attributeMap = null;
    private StringBuffer characters = null;
    private boolean proxyConfigured = false;
    private String polyType = null;
    private String id = null;
    public RuntimeConfigurable(Object proxy, String elementTag) {
        setProxy(proxy);
        setElementTag(elementTag);
        if (proxy instanceof Task) {
            ((Task) proxy).setRuntimeConfigurableWrapper(this);
        }
    }
    public synchronized void setProxy(Object proxy) {
        wrappedObject = proxy;
        proxyConfigured = false;
    }
    synchronized void setCreator(IntrospectionHelper.Creator creator) {
        this.creator = creator;
    }
    public synchronized Object getProxy() {
        return wrappedObject;
    }
    public synchronized String getId() {
        return id;
    }
    public synchronized String getPolyType() {
        return polyType;
    }
    public synchronized void setPolyType(String polyType) {
        this.polyType = polyType;
    }
    public synchronized void setAttributes(AttributeList attributes) {
        this.attributes = new AttributeListImpl(attributes);
        for (int i = 0; i < attributes.getLength(); i++) {
            setAttribute(attributes.getName(i), attributes.getValue(i));
        }
    }
    public synchronized void setAttribute(String name, String value) {
        if (name.equalsIgnoreCase(ProjectHelper.ANT_TYPE)) {
            this.polyType = value;
        } else {
            if (attributeMap == null) {
                attributeMap = new LinkedHashMap();
            }
            if (name.equalsIgnoreCase("refid") && !attributeMap.isEmpty()) {
                LinkedHashMap newAttributeMap = new LinkedHashMap();
                newAttributeMap.put(name, value);
                newAttributeMap.putAll(attributeMap);
                attributeMap = newAttributeMap;
            } else {
                attributeMap.put(name, value);
            }
            if (name.equals("id")) {
                this.id = value;
            }
        }
    }
    public synchronized void removeAttribute(String name) {
        attributeMap.remove(name);
    }
    public synchronized Hashtable getAttributeMap() {
        return (attributeMap == null)
            ? EMPTY_HASHTABLE : new Hashtable(attributeMap);
    }
    public synchronized AttributeList getAttributes() {
        return attributes;
    }
    public synchronized void addChild(RuntimeConfigurable child) {
        children = (children == null) ? new ArrayList() : children;
        children.add(child);
    }
    synchronized RuntimeConfigurable getChild(int index) {
        return (RuntimeConfigurable) children.get(index);
    }
    public synchronized Enumeration getChildren() {
        return (children == null) ? new CollectionUtils.EmptyEnumeration()
            : Collections.enumeration(children);
    }
    public synchronized void addText(String data) {
        if (data.length() == 0) {
            return;
        }
        characters = (characters == null)
            ? new StringBuffer(data) : characters.append(data);
    }
    public synchronized void addText(char[] buf, int start, int count) {
        if (count == 0) {
            return;
        }
        characters = ((characters == null)
            ? new StringBuffer(count) : characters).append(buf, start, count);
    }
    public synchronized StringBuffer getText() {
        return (characters == null) ? new StringBuffer(0) : characters;
    }
    public synchronized void setElementTag(String elementTag) {
        this.elementTag = elementTag;
    }
    public synchronized String getElementTag() {
        return elementTag;
    }
    public void maybeConfigure(Project p) throws BuildException {
        maybeConfigure(p, true);
    }
    public synchronized void maybeConfigure(Project p, boolean configureChildren)
        throws BuildException {
        if (proxyConfigured) {
            return;
        }
        Object target = (wrappedObject instanceof TypeAdapter)
            ? ((TypeAdapter) wrappedObject).getProxy() : wrappedObject;
        IntrospectionHelper ih =
            IntrospectionHelper.getHelper(p, target.getClass());
        if (attributeMap != null) {
            for (Iterator iter = attributeMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                Object attrValue = PropertyHelper.getPropertyHelper(p).parseProperties(value);
                try {
                    ih.setAttribute(p, target, name, attrValue);
                } catch (UnsupportedAttributeException be) {
                    if (name.equals("id")) {
                    } else if (getElementTag() == null) {
                        throw be;
                    } else {
                        throw new BuildException(
                            getElementTag() + " doesn't support the \""
                            + be.getAttribute() + "\" attribute", be);
                    }
                } catch (BuildException be) {
                    if (name.equals("id")) {
                    } else {
                        throw be;
                    }
                }
            }
        }
        if (characters != null) {
            ProjectHelper.addText(p, wrappedObject, characters.substring(0));
        }
        if (id != null) {
            p.addReference(id, wrappedObject);
        }
        proxyConfigured = true;
    }
    public void reconfigure(Project p) {
        proxyConfigured = false;
        maybeConfigure(p);
    }
    public void applyPreSet(RuntimeConfigurable r) {
        if (r.attributeMap != null) {
            for (Iterator i = r.attributeMap.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                if (attributeMap == null || attributeMap.get(name) == null) {
                    setAttribute(name, (String) r.attributeMap.get(name));
                }
            }
        }
        polyType = (polyType == null) ? r.polyType : polyType;
        if (r.children != null) {
            List newChildren = new ArrayList();
            newChildren.addAll(r.children);
            if (children != null) {
                newChildren.addAll(children);
            }
            children = newChildren;
        }
        if (r.characters != null) {
            if (characters == null
                || characters.toString().trim().length() == 0) {
                characters = new StringBuffer(r.characters.toString());
            }
        }
    }
}