package org.apache.xerces.jaxp.datatype;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
public class DatatypeFactoryImpl extends DatatypeFactory {
    public DatatypeFactoryImpl() {}
    public Duration newDuration(final String lexicalRepresentation) {
        return new DurationImpl(lexicalRepresentation);
    }
    public Duration newDuration(final long durationInMilliseconds) {
        return new DurationImpl(durationInMilliseconds);
    }
    public Duration newDuration(
            final boolean isPositive,
            final BigInteger years,
            final BigInteger months,
            final BigInteger days,
            final BigInteger hours,
            final BigInteger minutes,
            final BigDecimal seconds) {
        return new DurationImpl(
                isPositive,
                years,
                months,
                days,
                hours,
                minutes,
                seconds
        );
    }
    public XMLGregorianCalendar newXMLGregorianCalendar() {
        return new XMLGregorianCalendarImpl();
    }
    public XMLGregorianCalendar newXMLGregorianCalendar(final String lexicalRepresentation) {
        return new XMLGregorianCalendarImpl(lexicalRepresentation);
    }
    public XMLGregorianCalendar newXMLGregorianCalendar(final GregorianCalendar cal) {
        return new XMLGregorianCalendarImpl(cal);
    }
    public XMLGregorianCalendar newXMLGregorianCalendar(
            final int year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final int millisecond,
            final int timezone) {
        return XMLGregorianCalendarImpl.createDateTime(
                year, 
                month, 
                day, 
                hour, 
                minute, 
                second, 
                millisecond, 
                timezone);
    }
    public XMLGregorianCalendar newXMLGregorianCalendar(
            final BigInteger year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final BigDecimal fractionalSecond,
            final int timezone) {
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hour,
                minute,
                second,
                fractionalSecond,
                timezone
        );
    }
}
