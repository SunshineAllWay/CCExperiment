package org.apache.xerces.impl.dv.xs;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
public class YearMonthDV extends AbstractDateTimeDV{
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return parse(content);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "gYearMonth"});
        }
    }
    protected DateTimeData parse(String str) throws SchemaDateTimeException{
        DateTimeData date = new DateTimeData(str, this);
        int len = str.length();
        int end = getYearMonth(str, 0, len, date);
        date.day = DAY;
        parseTimeZone (str, end, len, date);
        validateDateTime(date);
        saveUnnormalized(date);
        if ( date.utc!=0 && date.utc!='Z' ) {
            normalize(date);
        }
        date.position = 0;
        return date;
    }
    protected String dateToString(DateTimeData date) {
        StringBuffer message = new StringBuffer(25);
        append(message, date.year, 4);
        message.append('-');
        append(message, date.month, 2);
        append(message, (char)date.utc, 0);
        return message.toString();
    }
    protected XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData date) {
        return datatypeFactory.newXMLGregorianCalendar(date.unNormYear, date.unNormMonth, DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, 
                date.hasTimeZone() ? date.timezoneHr * 60 + date.timezoneMin : DatatypeConstants.FIELD_UNDEFINED);
    }
}
