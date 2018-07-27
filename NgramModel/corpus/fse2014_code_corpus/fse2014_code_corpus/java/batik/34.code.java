package org.apache.batik.anim.timing;
import java.util.Calendar;
import java.util.LinkedList;
import org.apache.batik.parser.DefaultTimingSpecifierListHandler;
import org.apache.batik.parser.TimingSpecifierListParser;
public class TimingSpecifierListProducer
        extends DefaultTimingSpecifierListHandler {
    protected LinkedList timingSpecifiers = new LinkedList();
    protected TimedElement owner;
    protected boolean isBegin;
    public TimingSpecifierListProducer(TimedElement owner, boolean isBegin) {
        this.owner = owner;
        this.isBegin = isBegin;
    }
    public TimingSpecifier[] getTimingSpecifiers() {
        return (TimingSpecifier[]) timingSpecifiers.toArray(new TimingSpecifier[0]);
    }
    public static TimingSpecifier[] parseTimingSpecifierList
            (TimedElement owner, boolean isBegin, String spec,
             boolean useSVG11AccessKeys, boolean useSVG12AccessKeys) {
        TimingSpecifierListParser p =
            new TimingSpecifierListParser(useSVG11AccessKeys,
                                          useSVG12AccessKeys);
        TimingSpecifierListProducer pp =
            new TimingSpecifierListProducer(owner, isBegin);
        p.setTimingSpecifierListHandler(pp);
        p.parse(spec);
        TimingSpecifier[] specs = pp.getTimingSpecifiers();
        return specs;
    }
    public void offset(float offset) {
        TimingSpecifier ts = new OffsetTimingSpecifier(owner, isBegin, offset);
        timingSpecifiers.add(ts);
    }
    public void syncbase(float offset, String syncbaseID,
                         String timeSymbol) {
        TimingSpecifier ts = new SyncbaseTimingSpecifier
            (owner, isBegin, offset, syncbaseID, timeSymbol.charAt(0) == 'b');
        timingSpecifiers.add(ts);
    }
    public void eventbase(float offset, String eventbaseID,
                          String eventType) {
        TimingSpecifier ts = new EventbaseTimingSpecifier
            (owner, isBegin, offset, eventbaseID, eventType);
        timingSpecifiers.add(ts);
    }
    public void repeat(float offset, String syncbaseID) {
        TimingSpecifier ts = new RepeatTimingSpecifier
            (owner, isBegin, offset, syncbaseID);
        timingSpecifiers.add(ts);
    }
    public void repeat(float offset, String syncbaseID,
                       int repeatIteration) {
        TimingSpecifier ts = new RepeatTimingSpecifier
            (owner, isBegin, offset, syncbaseID, repeatIteration);
        timingSpecifiers.add(ts);
    }
    public void accesskey(float offset, char key) {
        TimingSpecifier ts = new AccesskeyTimingSpecifier
            (owner, isBegin, offset, key);
        timingSpecifiers.add(ts);
    }
    public void accessKeySVG12(float offset, String keyName) {
        TimingSpecifier ts = new AccesskeyTimingSpecifier
            (owner, isBegin, offset, keyName);
        timingSpecifiers.add(ts);
    }
    public void mediaMarker(String syncbaseID, String markerName) {
        TimingSpecifier ts = new MediaMarkerTimingSpecifier
            (owner, isBegin, syncbaseID, markerName);
        timingSpecifiers.add(ts);
    }
    public void wallclock(Calendar time) {
        TimingSpecifier ts = new WallclockTimingSpecifier(owner, isBegin, time);
        timingSpecifiers.add(ts);
    }
    public void indefinite() {
        TimingSpecifier ts = new IndefiniteTimingSpecifier(owner, isBegin);
        timingSpecifiers.add(ts);
    }
}
