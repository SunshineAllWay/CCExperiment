package org.apache.xerces.impl.xs.identity;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xpath.XPath;
import org.apache.xerces.util.IntStack;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.xml.sax.SAXException;
public class XPathMatcher {
    protected static final boolean DEBUG_ALL = false;
    protected static final boolean DEBUG_METHODS = false || DEBUG_ALL;
    protected static final boolean DEBUG_METHODS2 = false || DEBUG_METHODS || DEBUG_ALL;
    protected static final boolean DEBUG_METHODS3 = false || DEBUG_METHODS || DEBUG_ALL;
    protected static final boolean DEBUG_MATCH = false || DEBUG_ALL;
    protected static final boolean DEBUG_STACK = false || DEBUG_ALL;
    protected static final boolean DEBUG_ANY = DEBUG_METHODS ||
                                               DEBUG_METHODS2 ||
                                               DEBUG_METHODS3 ||
                                               DEBUG_MATCH ||
                                               DEBUG_STACK;
    protected static final int MATCHED = 1;
    protected static final int MATCHED_ATTRIBUTE = 3;
    protected static final int MATCHED_DESCENDANT = 5;
    protected static final int MATCHED_DESCENDANT_PREVIOUS = 13;
    private final XPath.LocationPath[] fLocationPaths;
    private final int[] fMatched;
    protected Object fMatchedString;
    private final IntStack[] fStepIndexes;
    private final int[] fCurrentStep;
    private final int [] fNoMatchDepth;
    final QName fQName = new QName();
    public XPathMatcher(XPath xpath) {
        fLocationPaths = xpath.getLocationPaths();
        fStepIndexes = new IntStack[fLocationPaths.length];
        for(int i=0; i<fStepIndexes.length; i++) fStepIndexes[i] = new IntStack();
        fCurrentStep = new int[fLocationPaths.length];
        fNoMatchDepth = new int[fLocationPaths.length];
        fMatched = new int[fLocationPaths.length];        
    } 
    public boolean isMatched() {
        for (int i=0; i < fLocationPaths.length; i++) 
            if (((fMatched[i] & MATCHED) == MATCHED) 
                    && ((fMatched[i] & MATCHED_DESCENDANT_PREVIOUS) != MATCHED_DESCENDANT_PREVIOUS) 
                    && ((fNoMatchDepth[i] == 0)
                    || ((fMatched[i] & MATCHED_DESCENDANT) == MATCHED_DESCENDANT))) 
                return true;
        return false;
    } 
    protected void handleContent(XSTypeDefinition type, boolean nillable, Object value, short valueType, ShortList itemValueType) { 
    } 
    protected void matched(Object actualValue, short valueType, ShortList itemValueType, boolean isNil) {
        if (DEBUG_METHODS3) {
            System.out.println(toString()+"#matched(\""+actualValue+"\")");
        }
    } 
    public void startDocumentFragment(){
        if (DEBUG_METHODS) {
            System.out.println(toString()+"#startDocumentFragment("+
                               ")");
        }
        fMatchedString = null;
        for(int i = 0; i < fLocationPaths.length; i++) {
            fStepIndexes[i].clear();
            fCurrentStep[i] = 0;
            fNoMatchDepth[i] = 0;
            fMatched[i] = 0;
        }
    } 
    public void startElement(QName element, XMLAttributes attributes) {
        if (DEBUG_METHODS2) {
            System.out.println(toString()+"#startElement("+
                               "element={"+element+"},"+
                               "attributes=..."+attributes+
                               ")");                     
        }
        for (int i = 0; i < fLocationPaths.length; i++) {
            int startStep = fCurrentStep[i];
            fStepIndexes[i].push(startStep);
            if ((fMatched[i] & MATCHED_DESCENDANT) == MATCHED || fNoMatchDepth[i] > 0) {
                fNoMatchDepth[i]++;
                continue;
            }
            if((fMatched[i] & MATCHED_DESCENDANT) == MATCHED_DESCENDANT) {
                fMatched[i] = MATCHED_DESCENDANT_PREVIOUS;
            }
            if (DEBUG_STACK) {
                System.out.println(toString()+": "+fStepIndexes[i]);
            }
            XPath.Step[] steps = fLocationPaths[i].steps;
            while (fCurrentStep[i] < steps.length &&
                    steps[fCurrentStep[i]].axis.type == XPath.Axis.SELF) {
                if (DEBUG_MATCH) {
                    XPath.Step step = steps[fCurrentStep[i]];
                    System.out.println(toString()+" [SELF] MATCHED!");
                }
                fCurrentStep[i]++;
            }
            if (fCurrentStep[i] == steps.length) {
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" XPath MATCHED!");
                }
                fMatched[i] = MATCHED;
                continue;
            }
            int descendantStep = fCurrentStep[i];
            while(fCurrentStep[i] < steps.length && steps[fCurrentStep[i]].axis.type == XPath.Axis.DESCENDANT) {
                if (DEBUG_MATCH) {
                    XPath.Step step = steps[fCurrentStep[i]];
                    System.out.println(toString()+" [DESCENDANT] MATCHED!");
                }
                fCurrentStep[i]++;
            }
            boolean sawDescendant = fCurrentStep[i] > descendantStep;
            if (fCurrentStep[i] == steps.length) {
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" XPath DIDN'T MATCH!");
                }
                fNoMatchDepth[i]++;
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" [CHILD] after NO MATCH");
                }
                continue;
            }
            if ((fCurrentStep[i] == startStep || fCurrentStep[i] > descendantStep) &&
                steps[fCurrentStep[i]].axis.type == XPath.Axis.CHILD) {
                XPath.Step step = steps[fCurrentStep[i]];
                XPath.NodeTest nodeTest = step.nodeTest;
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" [CHILD] before");
                }
                if (!matches(nodeTest, element)) {
                    if (fCurrentStep[i] > descendantStep) {
                        fCurrentStep[i] = descendantStep;
                        continue;
                    }
                    fNoMatchDepth[i]++;
                    if (DEBUG_MATCH) {
                        System.out.println(toString()+" [CHILD] after NO MATCH");
                    }
                    continue;
                }
                fCurrentStep[i]++;
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" [CHILD] after MATCHED!");
                }
            }
            if (fCurrentStep[i] == steps.length) {
                if (sawDescendant) {
                    fCurrentStep[i] = descendantStep;
                    fMatched[i] = MATCHED_DESCENDANT;
                } 
                else {
                    fMatched[i] = MATCHED;
                }
                continue;
            }
            if (fCurrentStep[i] < steps.length &&
                steps[fCurrentStep[i]].axis.type == XPath.Axis.ATTRIBUTE) {
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" [ATTRIBUTE] before");
                }
                int attrCount = attributes.getLength();
                if (attrCount > 0) {
                    XPath.NodeTest nodeTest = steps[fCurrentStep[i]].nodeTest;
                    for (int aIndex = 0; aIndex < attrCount; aIndex++) {
                        attributes.getName(aIndex, fQName);
                        if (matches(nodeTest, fQName)) {
                            fCurrentStep[i]++;
                            if (fCurrentStep[i] == steps.length) {
                                fMatched[i] = MATCHED_ATTRIBUTE;
                                int j = 0;
                                for(; j < i && ((fMatched[j] & MATCHED) != MATCHED); j++);
                                if (j == i) {
                                    AttributePSVI attrPSVI = (AttributePSVI)attributes.getAugmentations(aIndex).getItem(Constants.ATTRIBUTE_PSVI);
                                    fMatchedString = attrPSVI.getActualNormalizedValue();
                                    matched(fMatchedString, attrPSVI.getActualNormalizedValueType(), attrPSVI.getItemValueTypes(), false);
                                }
                            }
                            break;
                        }
                    }
                }
                if ((fMatched[i] & MATCHED) != MATCHED) {
                    if(fCurrentStep[i] > descendantStep) {
                        fCurrentStep[i] = descendantStep;
                        continue;
                    }
                    fNoMatchDepth[i]++;
                    if (DEBUG_MATCH) {
                        System.out.println(toString()+" [ATTRIBUTE] after");
                    }
                    continue;
                }
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" [ATTRIBUTE] after MATCHED!");
                }
            }
        }
    } 
    public void endElement(QName element, XSTypeDefinition type, boolean nillable, Object value, short valueType, ShortList itemValueType) {
        if (DEBUG_METHODS2) {
            System.out.println(toString()+"#endElement("+
                               "element={"+element+"},"+
                               ")");
        }
        for (int i = 0; i < fLocationPaths.length; i++) {
            fCurrentStep[i] = fStepIndexes[i].pop();
            if (fNoMatchDepth[i] > 0) {
                fNoMatchDepth[i]--;
            }
            else {
                int j = 0;
                for(; j < i && ((fMatched[j] & MATCHED) != MATCHED); j++);
                if ((j < i) || (fMatched[j] == 0)) {
                    continue;
                }
                if ((fMatched[j] & MATCHED_ATTRIBUTE) == MATCHED_ATTRIBUTE) {
                    fMatched[i] = 0;
                    continue;
                }
                handleContent(type, nillable, value, valueType, itemValueType);
                fMatched[i] = 0;
            }
            if (DEBUG_STACK) {
                System.out.println(toString()+": "+fStepIndexes[i]);
            }
        }
    } 
    public String toString() {
        StringBuffer str = new StringBuffer();
        String s = super.toString();
        int index2 = s.lastIndexOf('.');
        if (index2 != -1) {
            s = s.substring(index2 + 1);
        }
        str.append(s);
        for(int i =0;i<fLocationPaths.length; i++) {
            str.append('[');
            XPath.Step[] steps = fLocationPaths[i].steps;
            for (int j = 0; j < steps.length; j++) {
                if (j == fCurrentStep[i]) {
                    str.append('^');
                }
                str.append(steps[j].toString());
                if (j < steps.length - 1) {
                    str.append('/');
                }
            }
            if (fCurrentStep[i] == steps.length) {
                str.append('^');
            }
            str.append(']');
            str.append(',');
        }
        return str.toString();
    } 
    private String normalize(String s) {
        StringBuffer str = new StringBuffer();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': {
                    str.append("\\n");
                    break;
                }
                default: {
                    str.append(c);
                }
            }
        }
        return str.toString();
    } 
    private static boolean matches(XPath.NodeTest nodeTest, QName value) {
        if (nodeTest.type == XPath.NodeTest.QNAME) {
            return nodeTest.name.equals(value);
        }
        if (nodeTest.type == XPath.NodeTest.NAMESPACE) {
            return nodeTest.name.uri == value.uri;
        }
        return true;
    } 
} 
