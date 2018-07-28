package dom.events;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
class EventReporter implements EventListener
{
    boolean silent=false; 
    int count=0;
    String[] phasename={"?","CAPTURING","AT_TARGET","BUBBLING","?"};
    String[] attrChange={"?","MODIFICATION","ADDITION","REMOVAL"};
    public void on()
    {
        System.out.println();
        System.out.println("EventReporter awakened:");
        System.out.println();
        silent=false;
    }
    public void off()
    {
        System.out.println();
        System.out.println("EventReporter muted");
        System.out.println();
        silent=true;
    }
    public void handleEvent(Event evt)
    {
        ++count;
        if(silent)
            return;
        System.out.print("EVT "+count+": '"+
            evt.getType()+
            "' listener '"+((Node)evt.getCurrentTarget()).getNodeName()+
            "' target '"+((Node)evt.getTarget()).getNodeName()+
            "' while "+phasename[evt.getEventPhase()] +
            "... ");
        if(evt.getBubbles()) System.out.print("will bubble");
        if(evt.getCancelable()) System.out.print("can cancel");
        System.out.println();
        if(evt instanceof MutationEvent)
        {
            MutationEvent me=(MutationEvent)evt;
            if(me.getRelatedNode()!=null)
                System.out.println("\trelatedNode='"+me.getRelatedNode()+"'");
            if(me.getAttrName()!=null)
                System.out.println("\tattrName='"+me.getAttrName()+"'");
            if(me.getPrevValue()!=null)
                System.out.println("\tprevValue='"+me.getPrevValue()+"'");
            if(me.getNewValue()!=null)
                System.out.println("\tnewValue='"+me.getNewValue()+"'");
            if(me.getType().equals("DOMAttrModified"))
                System.out.println("\tattrChange='"+attrChange[me.getAttrChange()]+"'");
        }
    }
}
