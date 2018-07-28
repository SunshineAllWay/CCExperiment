package org.apache.batik.dom.svg;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.batik.anim.values.AnimatablePathDataValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathArrayProducer;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedPathData;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegList;
public class SVGOMAnimatedPathData
    extends AbstractSVGAnimatedValue
    implements SVGAnimatedPathData {
    protected boolean changing;
    protected BaseSVGPathSegList pathSegs;
    protected NormalizedBaseSVGPathSegList normalizedPathSegs;
    protected AnimSVGPathSegList animPathSegs;
    protected String defaultValue;
    public SVGOMAnimatedPathData(AbstractElement elt,
                                 String ns,
                                 String ln,
                                 String defaultValue) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
    }
    public SVGPathSegList getAnimatedNormalizedPathSegList() {
        throw new UnsupportedOperationException
            ("SVGAnimatedPathData.getAnimatedNormalizedPathSegList is not implemented"); 
    }
    public SVGPathSegList getAnimatedPathSegList() {
        if (animPathSegs == null) {
            animPathSegs = new AnimSVGPathSegList();
        }
        return animPathSegs;
    }
    public SVGPathSegList getNormalizedPathSegList() {
        if (normalizedPathSegs == null) {
            normalizedPathSegs = new NormalizedBaseSVGPathSegList();
        }
        return normalizedPathSegs;
    }
    public SVGPathSegList getPathSegList() {
        if (pathSegs == null) {
            pathSegs = new BaseSVGPathSegList();
        }
        return pathSegs;
    }
    public void check() {
        if (!hasAnimVal) {
            if (pathSegs == null) {
                pathSegs = new BaseSVGPathSegList();
            }
            pathSegs.revalidate();
            if (pathSegs.missing) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_MISSING, null);
            }
            if (pathSegs.malformed) {
                throw new LiveAttributeException
                    (element, localName,
                     LiveAttributeException.ERR_ATTRIBUTE_MALFORMED,
                     pathSegs.getValueAsString());
            }
        }
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        SVGPathSegList psl = getPathSegList();
        PathArrayProducer pp = new PathArrayProducer();
        SVGAnimatedPathDataSupport.handlePathSegList(psl, pp);
        return new AnimatablePathDataValue(target, pp.getPathCommands(),
                                           pp.getPathParameters());
    }
    protected void updateAnimatedValue(AnimatableValue val) {
        if (val == null) {
            hasAnimVal = false;
        } else {
            hasAnimVal = true;
            AnimatablePathDataValue animPath = (AnimatablePathDataValue) val;
            if (animPathSegs == null) {
                animPathSegs = new AnimSVGPathSegList();
            }
            animPathSegs.setAnimatedValue(animPath.getCommands(),
                                          animPath.getParameters());
        }
        fireAnimatedAttributeListeners();
    }
    public void attrAdded(Attr node, String newv) {
        if (!changing) {
            if (pathSegs != null) {
                pathSegs.invalidate();
            }
            if (normalizedPathSegs != null) {
                normalizedPathSegs.invalidate();
            }
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing) {
            if (pathSegs != null) {
                pathSegs.invalidate();
            }
            if (normalizedPathSegs != null) {
                normalizedPathSegs.invalidate();
            }
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrRemoved(Attr node, String oldv) {
        if (!changing) {
            if (pathSegs != null) {
                pathSegs.invalidate();
            }
            if (normalizedPathSegs != null) {
                normalizedPathSegs.invalidate();
            }
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public class BaseSVGPathSegList extends AbstractSVGPathSegList {
        protected boolean missing;
        protected boolean malformed;
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {
            return ((SVGOMElement)element).createSVGException(type, key, args);
        }
        protected String getValueAsString() {
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            if (attr == null) {
                return defaultValue;
            }
            return attr.getValue();
        }
        protected void setAttributeValue(String value) {
            try {
                changing = true;
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }
        protected void resetAttribute() {
            super.resetAttribute();
            missing = false;
            malformed = false;
        }
        protected void resetAttribute(SVGItem item) {
            super.resetAttribute(item);
            missing = false;
            malformed = false;
        }
        protected void revalidate() {
            if (valid) {
                return;
            }
            valid = true;
            missing = false;
            malformed = false;
            String s = getValueAsString();
            if (s == null) {
                missing = true;
                return;
            }
            try {
                ListBuilder builder = new ListBuilder();
                doParse(s, builder);
                if (builder.getList() != null) {
                    clear(itemList);
                }
                itemList = builder.getList();
            } catch (ParseException e) {
                itemList = new ArrayList(1);
                malformed = true;
            }
        }
    }
    public class NormalizedBaseSVGPathSegList
            extends AbstractSVGNormPathSegList {
        protected boolean missing;
        protected boolean malformed;
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {
            return ((SVGOMElement)element).createSVGException(type, key, args);
        }
        protected String getValueAsString() throws SVGException {
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            if (attr == null) {
                return defaultValue;
            }
            return attr.getValue();
        }
        protected void setAttributeValue(String value) {
            try {
                changing = true;
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }
        protected void revalidate() {
            if (valid) {
                return;
            }
            valid = true;
            missing = false;
            malformed = false;
            String s = getValueAsString();
            if (s == null) {
                missing = true;
                return;
            }
            try {
                ListBuilder builder = new ListBuilder();
                doParse(s, builder);
                if (builder.getList() != null) {
                    clear(itemList);
                }
                itemList = builder.getList();
            } catch (ParseException e) {
                itemList = new ArrayList(1);
                malformed = true;
            }
        }
    }
    public class AnimSVGPathSegList extends AbstractSVGPathSegList {
        public AnimSVGPathSegList() {
            itemList = new ArrayList(1);
        }
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {
            return ((SVGOMElement)element).createSVGException(type, key, args);
        }
        public int getNumberOfItems() {
            if (hasAnimVal) {
                return super.getNumberOfItems();
            }
            return getPathSegList().getNumberOfItems();
        }
        public SVGPathSeg getItem(int index) throws DOMException {
            if (hasAnimVal) {
                return super.getItem(index);
            }
            return getPathSegList().getItem(index);
        }
        protected String getValueAsString() {
            if (itemList.size() == 0) {
                return "";
            }
            StringBuffer sb = new StringBuffer( itemList.size() * 8 );
            Iterator i = itemList.iterator();
            if (i.hasNext()) {
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            while (i.hasNext()) {
                sb.append(getItemSeparator());
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            return sb.toString();
        }
        protected void setAttributeValue(String value) {
        }
        public void clear() throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.pathseg.list", null);
        }
        public SVGPathSeg initialize(SVGPathSeg newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.pathseg.list", null);
        }
        public SVGPathSeg insertItemBefore(SVGPathSeg newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.pathseg.list", null);
        }
        public SVGPathSeg replaceItem(SVGPathSeg newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.pathseg.list", null);
        }
        public SVGPathSeg removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.pathseg.list", null);
        }
        public SVGPathSeg appendItem(SVGPathSeg newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.pathseg.list", null);
        }
        private int[] parameterIndex = new int[1];
        protected SVGPathSegItem newItem(short command, float[] parameters,
                                         int[] j) {
            switch (command) {
                case SVGPathSeg.PATHSEG_ARC_ABS:
                case SVGPathSeg.PATHSEG_ARC_REL:
                    return new SVGPathSegArcItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++] != 0,
                         parameters[j[0]++] != 0,
                         parameters[j[0]++],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_CLOSEPATH:
                    return new SVGPathSegItem
                        (command, PATHSEG_LETTERS[command]);
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL:
                    return new SVGPathSegCurvetoCubicItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
                case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL:
                    return new SVGPathSegCurvetoCubicSmoothItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL:
                    return new SVGPathSegCurvetoQuadraticItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS:
                case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL:
                    return new SVGPathSegCurvetoQuadraticSmoothItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_LINETO_ABS:
                case SVGPathSeg.PATHSEG_LINETO_REL:
                case SVGPathSeg.PATHSEG_MOVETO_ABS:
                case SVGPathSeg.PATHSEG_MOVETO_REL:
                    return new SVGPathSegMovetoLinetoItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:
                case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS:
                    return new SVGPathSegLinetoHorizontalItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++]);
                case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:
                case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS:
                    return new SVGPathSegLinetoVerticalItem
                        (command, PATHSEG_LETTERS[command],
                         parameters[j[0]++]);
            }
            return null;
        }
        protected void setAnimatedValue(short[] commands, float[] parameters) {
            int size = itemList.size();
            int i = 0;
            int[] j = parameterIndex;
            j[0] = 0;
            while (i < size && i < commands.length) {
                SVGPathSeg s = (SVGPathSeg) itemList.get(i);
                if (s.getPathSegType() != commands[i]) {
                    s = newItem(commands[i], parameters, j);
                } else {
                    switch (commands[i]) {
                        case SVGPathSeg.PATHSEG_ARC_ABS:
                        case SVGPathSeg.PATHSEG_ARC_REL: {
                            SVGPathSegArcItem ps = (SVGPathSegArcItem) s;
                            ps.r1 = parameters[j[0]++];
                            ps.r2 = parameters[j[0]++];
                            ps.angle = parameters[j[0]++];
                            ps.largeArcFlag = parameters[j[0]++] != 0;
                            ps.sweepFlag = parameters[j[0]++] != 0;
                            ps.x = parameters[j[0]++];
                            ps.y = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_CLOSEPATH:
                            break;
                        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
                        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL: {
                            SVGPathSegCurvetoCubicItem ps =
                                (SVGPathSegCurvetoCubicItem) s;
                            ps.x1 = parameters[j[0]++];
                            ps.y1 = parameters[j[0]++];
                            ps.x2 = parameters[j[0]++];
                            ps.y2 = parameters[j[0]++];
                            ps.x = parameters[j[0]++];
                            ps.y = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
                        case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL: {
                            SVGPathSegCurvetoCubicSmoothItem ps =
                                (SVGPathSegCurvetoCubicSmoothItem) s;
                            ps.x2 = parameters[j[0]++];
                            ps.y2 = parameters[j[0]++];
                            ps.x = parameters[j[0]++];
                            ps.y = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:
                        case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL: {
                            SVGPathSegCurvetoQuadraticItem ps =
                                (SVGPathSegCurvetoQuadraticItem) s;
                            ps.x1 = parameters[j[0]++];
                            ps.y1 = parameters[j[0]++];
                            ps.x = parameters[j[0]++];
                            ps.y = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS:
                        case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL: {
                            SVGPathSegCurvetoQuadraticSmoothItem ps =
                                (SVGPathSegCurvetoQuadraticSmoothItem) s;
                            ps.x = parameters[j[0]++];
                            ps.y = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_LINETO_ABS:
                        case SVGPathSeg.PATHSEG_LINETO_REL:
                        case SVGPathSeg.PATHSEG_MOVETO_ABS:
                        case SVGPathSeg.PATHSEG_MOVETO_REL: {
                            SVGPathSegMovetoLinetoItem ps =
                                (SVGPathSegMovetoLinetoItem) s;
                            ps.x = parameters[j[0]++];
                            ps.y = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:
                        case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS: {
                            SVGPathSegLinetoHorizontalItem ps =
                                (SVGPathSegLinetoHorizontalItem) s;
                            ps.x = parameters[j[0]++];
                            break;
                        }
                        case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:
                        case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS: {
                            SVGPathSegLinetoVerticalItem ps =
                                (SVGPathSegLinetoVerticalItem) s;
                            ps.y = parameters[j[0]++];
                            break;
                        }
                    }
                }
                i++;
            }
            while (i < commands.length) {
                appendItemImpl(newItem(commands[i], parameters, j));
                i++;
            }
            while (size > commands.length) {
                removeItemImpl(--size);
            }
        }
        protected void resetAttribute() {
        }
        protected void resetAttribute(SVGItem item) {
        }
        protected void revalidate() {
            valid = true;
        }
    }
}
