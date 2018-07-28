package org.apache.xerces.impl.dv.xs;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
class DayTimeDurationDV extends DurationDV {
    public Object getActualValue(String content, ValidationContext context)
        throws InvalidDatatypeValueException {
        try {
            return parse(content, DurationDV.DAYTIMEDURATION_TYPE);
        } 
        catch (Exception ex) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "dayTimeDuration"});
        }
    }
    protected Duration getDuration(DateTimeData date) {
        int sign = 1;
        if (date.day<0 || date.hour<0 || date.minute<0 || date.second<0) {
            sign = -1;
        }
        return datatypeFactory.newDuration(sign == 1, null, null, 
                date.day != DatatypeConstants.FIELD_UNDEFINED?BigInteger.valueOf(sign*date.day):null, 
                date.hour != DatatypeConstants.FIELD_UNDEFINED?BigInteger.valueOf(sign*date.hour):null, 
                date.minute != DatatypeConstants.FIELD_UNDEFINED?BigInteger.valueOf(sign*date.minute):null, 
                date.second != DatatypeConstants.FIELD_UNDEFINED?new BigDecimal(String.valueOf(sign*date.second)):null);
    }
}
