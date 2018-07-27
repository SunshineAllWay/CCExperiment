package org.apache.xerces.impl.dv.xs;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
public class TimeDV extends AbstractDateTimeDV {
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return parse(content);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "time"});
        }
    }
    protected DateTimeData parse(String str) throws SchemaDateTimeException{
        DateTimeData date = new DateTimeData(str, this);
        int len = str.length();
        date.year=YEAR;
        date.month=MONTH;
        date.day=15;
        getTime(str, 0, len, date);
        validateDateTime(date);
        saveUnnormalized(date);
        if ( date.utc!=0 && date.utc != 'Z') {
            normalize(date);
            date.day = 15;
        }
        date.position = 2;
        return date;
    }
    protected String dateToString(DateTimeData date) {
        StringBuffer message = new StringBuffer(16);
        append(message, date.hour, 2);
        message.append(':');
        append(message, date.minute, 2);
        message.append(':');
        append(message, date.second);
        append(message, (char)date.utc, 0);
        return message.toString();
    }
    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData date) {
        return datatypeFactory.newXMLGregorianCalendar(null, DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, date.unNormHour, date.unNormMinute, 
                (int)date.unNormSecond, date.unNormSecond != 0 ? getFractionalSecondsAsBigDecimal(date) : null,
                date.hasTimeZone() ? (date.timezoneHr * 60 + date.timezoneMin) : DatatypeConstants.FIELD_UNDEFINED);
    }
}
