package org.apache.xerces.impl.dv.xs;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
public class MonthDV extends AbstractDateTimeDV {
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return parse(content);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "gMonth"});
        }
    }
    protected DateTimeData parse(String str) throws SchemaDateTimeException{
        DateTimeData date = new DateTimeData(str, this);
        int len = str.length();
        date.year=YEAR;
        date.day=DAY;
        if (str.charAt(0)!='-' || str.charAt(1)!='-') {
            throw new SchemaDateTimeException("Invalid format for gMonth: "+str);
        }
        int stop = 4;
        date.month=parseInt(str,2,stop);
        if (str.length() >= stop+2 &&
            str.charAt(stop) == '-' && str.charAt(stop+1) == '-') {
            stop += 2;
        }
        if (stop < len) {
            if (!isNextCharUTCSign(str, stop, len)) {
                throw new SchemaDateTimeException ("Error in month parsing: "+str);
            }
            else {
                getTimeZone(str, date, stop, len);
            }
        }
        validateDateTime(date);
        saveUnnormalized(date);
        if ( date.utc!=0 && date.utc!='Z' ) {
            normalize(date);
        }
        date.position = 1;
        return date;
    }
    protected String dateToString(DateTimeData date) {
        StringBuffer message = new StringBuffer(5);
        message.append('-');
        message.append('-');
        append(message, date.month, 2);
        append(message, (char)date.utc, 0);
        return message.toString();
    }
    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData date) {
        return datatypeFactory.newXMLGregorianCalendar(DatatypeConstants.FIELD_UNDEFINED, date.unNormMonth, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                date.hasTimeZone() ? date.timezoneHr * 60 + date.timezoneMin : DatatypeConstants.FIELD_UNDEFINED);
    }
}
