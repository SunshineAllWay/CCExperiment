package org.apache.batik.parser;
import java.util.Calendar;
public interface TimingSpecifierHandler {
    void offset(float offset);
    void syncbase(float offset, String syncbaseID, String timeSymbol);
    void eventbase(float offset, String eventbaseID, String eventType);
    void repeat(float offset, String syncbaseID);
    void repeat(float offset, String syncbaseID, int repeatIteration);
    void accesskey(float offset, char key);
    void accessKeySVG12(float offset, String keyName);
    void mediaMarker(String syncbaseID, String markerName);
    void wallclock(Calendar time);
    void indefinite();
}
