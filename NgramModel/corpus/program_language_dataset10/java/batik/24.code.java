package org.apache.batik.anim.timing;
public class MediaMarkerTimingSpecifier extends TimingSpecifier {
    protected String syncbaseID;
    protected TimedElement mediaElement;
    protected String markerName;
    protected InstanceTime instance;
    public MediaMarkerTimingSpecifier(TimedElement owner, boolean isBegin,
                                      String syncbaseID, String markerName) {
        super(owner, isBegin);
        this.syncbaseID = syncbaseID;
        this.markerName = markerName;
        this.mediaElement = owner.getTimedElementById(syncbaseID);
    }
    public String toString() {
        return syncbaseID + ".marker(" + markerName + ")";
    }
    public boolean isEventCondition() {
        return false;
    }
}
