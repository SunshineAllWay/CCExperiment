package org.apache.batik.anim;
import org.apache.batik.anim.timing.TimedElement;
public class AnimationException extends RuntimeException {
    protected TimedElement e;
    protected String code;
    protected Object[] params;
    protected String message;
    public AnimationException(TimedElement e, String code, Object[] params) {
        this.e = e;
        this.code = code;
        this.params = params;
    }
    public TimedElement getElement() {
        return e;
    }
    public String getCode() {
        return code;
    }
    public Object[] getParams() {
        return params;
    }
    public String getMessage() {
        return TimedElement.formatMessage(code, params);
    }
}
