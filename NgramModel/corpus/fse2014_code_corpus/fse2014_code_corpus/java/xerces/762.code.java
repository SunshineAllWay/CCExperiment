package dom.events;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
public class Test
{
    EventReporter sharedReporter=new EventReporter();
    public static void main(String[] args)
    {
        Test met=new Test();
        met.runTest();
    }
    void runTest()
    {
        Document doc=new org.apache.xerces.dom.DocumentImpl();
        reportAllMutations(doc);
        Element root=addNoisyElement(doc,doc,0);
        Element e=null;
        int i;
        e=addNoisyElement(doc,root,0);
        Attr a=addNoisyAttr(doc,e,0);
        a.setNodeValue("Updated A0 of E0, prepare to be acidulated.");
        NamedNodeMap nnm=e.getAttributes();
        nnm.removeNamedItem(a.getName());
        nnm.setNamedItem(a);
        System.out.println();
        System.out.println("Add/remove a preconstructed tree; tests AddedToDocument");
        System.out.println();
        sharedReporter.off();
        Element lateAdd=doc.createElement("lateAdd");
        reportAllMutations(lateAdd);
        e=lateAdd;
        for(i=0;i<2;++i)
        {
            e=addNoisyElement(doc,e,i);
            addNoisyAttr(doc,e,i);
        }
        sharedReporter.on();
        root.appendChild(lateAdd);
        root.removeChild(lateAdd);
        System.out.println();
        System.out.println("Replace a preconstructed tree; tests AddedToDocument");
        System.out.println();
        sharedReporter.off();
        Node e0=root.replaceChild(lateAdd,root.getFirstChild());
        sharedReporter.on();
        root.replaceChild(e0,lateAdd);
        sharedReporter.off();
        Text t = addNoisyText(doc, root.getFirstChild(), "fo");
        sharedReporter.on();
        t.insertData(1, "o");
        root.setAttribute("foo", "bar");
        System.out.println("Done");
    }
    Element addNoisyElement(Document doc,Node parent,int index)
    {
        String nodeName="Root";
        if(parent!=doc)
            nodeName=parent.getNodeName()+"_E"+index;
        Element e=doc.createElement(nodeName);
        reportAllMutations(e);
        parent.appendChild(e);
        return e;
    }
    Attr addNoisyAttr(Document doc,Element parent,int index)
    {
        String attrName=parent.getNodeName()+"_A"+index;
        Attr a=doc.createAttribute(attrName);
        reportAllMutations(a);
        a.setNodeValue("Initialized A"+index+" of "+parent.getNodeName());
        parent.setAttributeNode(a);
        return a;
    }
    Text addNoisyText(Document doc, Node parent, String data)
    {
        Text t = doc.createTextNode(data);
        reportAllMutations(t);
        parent.appendChild(t);
        return t;
    }
    void reportAllMutations(Node n)
    {
        String[] evtNames={
            "DOMSubtreeModified","DOMAttrModified","DOMCharacterDataModified",
            "DOMNodeInserted","DOMNodeRemoved",
            "DOMNodeInsertedIntoDocument","DOMNodeRemovedFromDocument",
            };
        EventTarget t=(EventTarget)n;
        for(int i=evtNames.length-1;
            i>=0;
            --i)
        {
            t.addEventListener(evtNames[i], sharedReporter, true);
            t.addEventListener(evtNames[i], sharedReporter, false);
        }
    }
}
