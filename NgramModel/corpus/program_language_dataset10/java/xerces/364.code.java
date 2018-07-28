package org.apache.xerces.impl.dv.xs;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
public class DayDV extends AbstractDateTimeDV {
    private final static int DAY_SIZE=5;
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        try{
            return parse(content);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "gDay"});
        }
    }
    protected DateTimeData parse(String str) throws SchemaDateTimeException {
        DateTimeData date = new DateTimeData(str, this);
        int len = str.length();
        if (str.charAt(0)!='-' || str.charAt(1)!='-' || str.charAt(2)!='-') {
            throw new SchemaDateTimeException ("Error in day parsing");
        }
        date.year=YEAR;
        date.month=MONTH;
        date.day=parseInt(str, 3,5);
        if ( DAY_SIZE<len ) {
            if (!isNextCharUTCSign(str, DAY_SIZE, len)) {
                throw new SchemaDateTimeException ("Error in day parsing");
            }
            else {
                getTimeZone(str, date, DAY_SIZE, len);
            }
        }
        validateDateTime(date);
        saveUnnormalized(date);
        if ( date.utc!=0 && date.utc!='Z' ) {
            normalize(date);
        }
        date.position = 2;
        return date;
    }
    protected String dateToString(DateTimeData date) {
        StringBuffer message = new StringBuffer(6);
        message.append('-');
        message.append('-');
        message.append('-');
        append(message, date.day, 2);
        append(message, (char)date.utc, 0);
        return message.toString();
    }
    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData date) {
        return datatypeFactory.newXMLGregorianCalendar(DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                date.unNormDay, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                date.hasTimeZone() ? date.timezoneHr * 60 + date.timezoneMin : DatatypeConstants.FIELD_UNDEFINED);
    }
}
