package org.apache.tools.ant.taskdefs.optional.ccm;
import java.util.Date;
public class CCMCheckin extends CCMCheck {
    public CCMCheckin() {
        super();
        setCcmAction(COMMAND_CHECKIN);
        setComment("Checkin " + new Date());
    }
}
