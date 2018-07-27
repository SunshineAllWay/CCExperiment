package org.apache.xerces.jaxp.datatype;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.apache.xerces.util.DatatypeMessageFormatter;
class XMLGregorianCalendarImpl
	extends XMLGregorianCalendar
	implements Serializable, Cloneable {
    private static final long serialVersionUID = 3905403108073447394L;
    private BigInteger orig_eon;
    private int orig_year = DatatypeConstants.FIELD_UNDEFINED;
    private int orig_month = DatatypeConstants.FIELD_UNDEFINED;
    private int orig_day = DatatypeConstants.FIELD_UNDEFINED;
    private int orig_hour = DatatypeConstants.FIELD_UNDEFINED;
    private int orig_minute = DatatypeConstants.FIELD_UNDEFINED;
    private int orig_second = DatatypeConstants.FIELD_UNDEFINED;
    private BigDecimal orig_fracSeconds;
    private int orig_timezone = DatatypeConstants.FIELD_UNDEFINED;
    private BigInteger eon = null;
    private int year = DatatypeConstants.FIELD_UNDEFINED;
    private int month = DatatypeConstants.FIELD_UNDEFINED;
    private int day = DatatypeConstants.FIELD_UNDEFINED;
    private int timezone = DatatypeConstants.FIELD_UNDEFINED;
    private int hour = DatatypeConstants.FIELD_UNDEFINED;
    private int minute = DatatypeConstants.FIELD_UNDEFINED;
    private int second = DatatypeConstants.FIELD_UNDEFINED ;
    private BigDecimal fractionalSecond = null;
    private static final BigInteger BILLION_B = BigInteger.valueOf(1000000000);
    private static final int BILLION_I = 1000000000;
    private static final Date PURE_GREGORIAN_CHANGE = new Date(Long.MIN_VALUE);
    private static final int YEAR   = 0;
    private static final int MONTH  = 1;
    private static final int DAY    = 2;
    private static final int HOUR   = 3;
    private static final int MINUTE = 4;
    private static final int SECOND = 5;
    private static final int MILLISECOND = 6;
    private static final int TIMEZONE = 7;
    private static final int MIN_FIELD_VALUE[] = { 
        Integer.MIN_VALUE,  
        DatatypeConstants.JANUARY, 
        1,       
        0,       
        0,       
        0,       
        0,       
        -14 * 60 
    };
    private static final int MAX_FIELD_VALUE[] = { 
        Integer.MAX_VALUE,  
        DatatypeConstants.DECEMBER,              
        31,       
        24,       
        59,       
        60,       
        999,      
        14 * 60   
    }; 
    private static final String FIELD_NAME[] = { 
        "Year",
        "Month",
        "Day",
        "Hour",
        "Minute",
        "Second",
        "Millisecond",
        "Timezone"
    };
    public static final XMLGregorianCalendar LEAP_YEAR_DEFAULT =
        createDateTime(
                400,  
                DatatypeConstants.JANUARY,  
                1,  
                0,  
                0,  
                0,  
                DatatypeConstants.FIELD_UNDEFINED,  
                DatatypeConstants.FIELD_UNDEFINED 
        );
    protected XMLGregorianCalendarImpl(String lexicalRepresentation)
        throws IllegalArgumentException {
        String format = null;
        String lexRep = lexicalRepresentation;
        final int NOT_FOUND = -1;
        int lexRepLength = lexRep.length();
        if (lexRep.indexOf('T') != NOT_FOUND) {
            format = "%Y-%M-%DT%h:%m:%s" + "%z";
        } 
        else if (lexRepLength >= 3 && lexRep.charAt(2) == ':') {
            format = "%h:%m:%s" +"%z";
        } 
        else if (lexRep.startsWith("--")) {
            if (lexRepLength >= 3 && lexRep.charAt(2) == '-') {
                format = "---%D" + "%z";
            } 
            else if (lexRepLength == 4 || (lexRepLength >= 6 && (lexRep.charAt(4) == '+' || (lexRep.charAt(4) == '-' && (lexRep.charAt(5) == '-' || lexRepLength == 10))))) {
                format = "--%M--%z";
                Parser p = new Parser(format, lexRep);
                try {
                    p.parse();
                    if (!isValid()) {
                        throw new IllegalArgumentException(
                                DatatypeMessageFormatter.formatMessage(null,"InvalidXGCRepresentation", new Object[]{lexicalRepresentation})
                        );
                    }
                    save();
                    return;
                }
                catch(IllegalArgumentException e) {
                    format = "--%M%z";
                }
            } 
            else {
                format = "--%M-%D" + "%z";
            }
        } 
        else {
            int countSeparator = 0;
            int timezoneOffset = lexRep.indexOf(':');
            if (timezoneOffset != NOT_FOUND) {
                lexRepLength -= 6;
            }
            for (int i=1; i < lexRepLength; i++) {
                if (lexRep.charAt(i) == '-') {
                    countSeparator++;
                }
            }
            if (countSeparator == 0) {
                format = "%Y" + "%z";
            } 
            else if (countSeparator == 1) {
                format = "%Y-%M" + "%z";
            } 
            else {
                format = "%Y-%M-%D" + "%z";
            }
        }
        Parser p = new Parser(format, lexRep);
        p.parse();
        if (!isValid()) {
            throw new IllegalArgumentException(
                    DatatypeMessageFormatter.formatMessage(null,"InvalidXGCRepresentation", new Object[]{lexicalRepresentation})
            );
        }
        save();
    }
    private void save() {
        orig_eon = eon;
        orig_year = year;
        orig_month = month;
        orig_day = day;
        orig_hour = hour;
        orig_minute = minute;
        orig_second = second;
        orig_fracSeconds = fractionalSecond;
        orig_timezone = timezone;
    }
    public XMLGregorianCalendarImpl() {
    }
    protected XMLGregorianCalendarImpl(
        BigInteger year,
        int month,
        int day,
        int hour,
        int minute,
        int second,
        BigDecimal fractionalSecond,
        int timezone) {
		setYear(year);
        setMonth(month);
        setDay(day);
        setTime(hour, minute, second, fractionalSecond);
		setTimezone(timezone);
		if (!isValid()) {
            throw new IllegalArgumentException(
                DatatypeMessageFormatter.formatMessage(null, 
                    "InvalidXGCValue-fractional", 
                    new Object[] { year, new Integer(month), new Integer(day), 
                    new Integer(hour), new Integer(minute), new Integer(second),
                    fractionalSecond, new Integer(timezone)})
			);
		}
        save();
    }
    private XMLGregorianCalendarImpl(
        int year,
        int month,
        int day,
        int hour,
        int minute,
        int second,
        int millisecond,
        int timezone) {
        setYear(year);
        setMonth(month);
        setDay(day);
        setTime(hour, minute, second);
        setTimezone(timezone);
        BigDecimal realMilliseconds = null;
        if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
            realMilliseconds = BigDecimal.valueOf(millisecond, 3);
        }
        setFractionalSecond(realMilliseconds);
        if (!isValid()) {		
            throw new IllegalArgumentException(
                    DatatypeMessageFormatter.formatMessage(null, 
                            "InvalidXGCValue-milli", 
                            new Object[] { new Integer(year), new Integer(month), new Integer(day), 
                            new Integer(hour), new Integer(minute), new Integer(second), 
                            new Integer(millisecond), new Integer(timezone)})                
            );
        }
        save();
    }
    public XMLGregorianCalendarImpl(GregorianCalendar cal) {
        int year = cal.get(Calendar.YEAR);
        if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
            year = -year;
        }
        this.setYear(year);
        this.setMonth(cal.get(Calendar.MONTH) + 1);
        this.setDay(cal.get(Calendar.DAY_OF_MONTH));
        this.setTime(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), 
                cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND));
        int offsetInMinutes = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
        this.setTimezone(offsetInMinutes);
        save();
    }
	public static XMLGregorianCalendar createDateTime(
	        BigInteger year,
	        int month,
	        int day,
	        int hours,
	        int minutes,
	        int seconds,
	        BigDecimal fractionalSecond,
	        int timezone) {
	    return new XMLGregorianCalendarImpl(
	            year,
	            month, 
	            day, 
	            hours,
	            minutes,
	            seconds, 
	            fractionalSecond,
	            timezone);
	}
    public static XMLGregorianCalendar createDateTime(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second) {
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hour,
                minute,
                second,
                DatatypeConstants.FIELD_UNDEFINED,  
                DatatypeConstants.FIELD_UNDEFINED 
        );
    }
    public static XMLGregorianCalendar createDateTime(
            int year,
            int month,
            int day,
            int hours,
            int minutes,
            int seconds,
            int milliseconds,
            int timezone) {
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                hours,
                minutes,
                seconds,
                milliseconds,
                timezone);
    }
    public static XMLGregorianCalendar createDate(
            int year,
            int month,
            int day,
            int timezone) {
        return new XMLGregorianCalendarImpl(
                year,
                month,
                day,
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                timezone);
    }
    public static XMLGregorianCalendar createTime(
            int hours,
            int minutes,
            int seconds,
            int timezone) {
        return new XMLGregorianCalendarImpl(
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                hours,
                minutes,
                seconds, 
                DatatypeConstants.FIELD_UNDEFINED, 
                timezone);
    }
    public static XMLGregorianCalendar createTime(
            int hours,
            int minutes,
            int seconds,
            BigDecimal fractionalSecond,
            int timezone) {
        return new XMLGregorianCalendarImpl(
                null,            
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                hours,
                minutes,
                seconds,
                fractionalSecond,
                timezone);
    }
    public static XMLGregorianCalendar createTime(
            int hours,
            int minutes,
            int seconds,
            int milliseconds,
            int timezone) {
        return new XMLGregorianCalendarImpl(
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                hours,
                minutes,
                seconds,
                milliseconds,
                timezone);
    }
    public BigInteger getEon() {
	   return eon;
    }
    public int getYear() {
	   return year;
    }
    public BigInteger getEonAndYear() {
        if (year != DatatypeConstants.FIELD_UNDEFINED
            && eon != null) {
            return eon.add(BigInteger.valueOf((long) year));
        }
        if (year != DatatypeConstants.FIELD_UNDEFINED
            && eon == null) {
            return BigInteger.valueOf((long) year);
        }
        return null;
    }
    public int getMonth() {
        return month;
    }
    public int getDay() {
        return day;
    }
    public int getTimezone() {
        return timezone;
    }
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
    }
    public int getSecond() {
	   return second;
    }
    private BigDecimal getSeconds() {
        if (second == DatatypeConstants.FIELD_UNDEFINED) {
            return DECIMAL_ZERO;
        } 
        BigDecimal result = BigDecimal.valueOf((long)second);
        if (fractionalSecond != null){
            return result.add(fractionalSecond);
        } 
        else {
            return result;
        }
    }
    public int getMillisecond() {
        if (fractionalSecond == null) {
            return DatatypeConstants.FIELD_UNDEFINED;
        } 
        else {
            return fractionalSecond.movePointRight(3).intValue();
        }
    }
    public BigDecimal getFractionalSecond() {
	   return fractionalSecond;
    }
    public void setYear(BigInteger year) {
        if (year == null) {
            this.eon = null;
            this.year = DatatypeConstants.FIELD_UNDEFINED;
        } 
        else {
            BigInteger temp = year.remainder(BILLION_B);
            this.year = temp.intValue();
            setEon(year.subtract(temp));
        }
    }
    public void setYear(int year) { 
        if (year == DatatypeConstants.FIELD_UNDEFINED) {
            this.year = DatatypeConstants.FIELD_UNDEFINED;
            this.eon = null;
        } 
        else if (Math.abs(year) < BILLION_I) {
            this.year = year;
            this.eon = null;
        } 
        else {
            BigInteger theYear = BigInteger.valueOf((long) year);
            BigInteger remainder = theYear.remainder(BILLION_B);
            this.year = remainder.intValue();
            setEon(theYear.subtract(remainder));
        }
    }
    private void setEon(BigInteger eon) {
        if (eon != null && eon.compareTo(BigInteger.ZERO) == 0) {
            this.eon = null;
        } 
        else {
            this.eon = eon;
        }
    }
    public void setMonth(int month) { 
        checkFieldValueConstraint(MONTH, month);
        this.month = month;
    }
    public void setDay(int day) {  
        checkFieldValueConstraint(DAY, day);
        this.day = day;
    }
    public void setTimezone(int offset) {
        checkFieldValueConstraint(TIMEZONE, offset);
        this.timezone = offset;
    }
    public void setTime(int hour, int minute, int second) {
        setTime(hour, minute, second, null);
    }
    private void checkFieldValueConstraint(int field, int value)
        throws IllegalArgumentException {
        if ((value < MIN_FIELD_VALUE[field] && value != DatatypeConstants.FIELD_UNDEFINED) ||
                value > MAX_FIELD_VALUE[field]) {
            throw new IllegalArgumentException(
                    DatatypeMessageFormatter.formatMessage(null, "InvalidFieldValue", new Object[]{ new Integer(value), FIELD_NAME[field]})
            );
        }
    }
    public void setHour(int hour) {
        checkFieldValueConstraint(HOUR, hour);
        this.hour = hour;
    }
    public void setMinute(int minute) {
        checkFieldValueConstraint(MINUTE, minute);
        this.minute = minute;
    }
    public void setSecond(int second) {
        checkFieldValueConstraint(SECOND, second);
        this.second  = second;
    }
    public void setTime(
            int hour,
            int minute,
            int second,
            BigDecimal fractional) {
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setFractionalSecond(fractional);
    }
    public void setTime(int hour, int minute, int second, int millisecond) {
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setMillisecond(millisecond);
    }
    public int compare(XMLGregorianCalendar rhs) {
        int result = DatatypeConstants.INDETERMINATE;
        XMLGregorianCalendar P = this;
        XMLGregorianCalendar Q = rhs;
        if (P.getTimezone() == Q.getTimezone()) {
            return internalCompare(P, Q);
        } 
        else if (P.getTimezone() != DatatypeConstants.FIELD_UNDEFINED &&
                Q.getTimezone() != DatatypeConstants.FIELD_UNDEFINED) {
            P = (XMLGregorianCalendarImpl) P.normalize();
            Q = (XMLGregorianCalendarImpl) Q.normalize();
            return internalCompare(P, Q);
        } 
        else if (P.getTimezone() != DatatypeConstants.FIELD_UNDEFINED) {
            if (P.getTimezone() != 0) {
                P = (XMLGregorianCalendarImpl) P.normalize();
            }
            XMLGregorianCalendar MinQ = normalizeToTimezone(Q, DatatypeConstants.MIN_TIMEZONE_OFFSET);
            result = internalCompare(P, MinQ);
            if (result == DatatypeConstants.LESSER) {
                return result;
            } 
            XMLGregorianCalendar MaxQ = normalizeToTimezone(Q, DatatypeConstants.MAX_TIMEZONE_OFFSET);
            result = internalCompare(P, MaxQ);
            if (result == DatatypeConstants.GREATER) {
                return result;
            } 
            else {
                return DatatypeConstants.INDETERMINATE;
            }
        } 
        else { 
            if (Q.getTimezone() != 0 ) { 
                Q = (XMLGregorianCalendarImpl) normalizeToTimezone(Q, Q.getTimezone());
            }
            XMLGregorianCalendar MaxP = normalizeToTimezone(P, DatatypeConstants.MAX_TIMEZONE_OFFSET);
            result = internalCompare(MaxP, Q);
            if (result == DatatypeConstants.LESSER) {
                return result;
            } 
            XMLGregorianCalendar MinP = normalizeToTimezone(P, DatatypeConstants.MIN_TIMEZONE_OFFSET);
            result = internalCompare(MinP, Q);
            if (result == DatatypeConstants.GREATER) {
                return result;
            } 
            else {
                return DatatypeConstants.INDETERMINATE;
            }
        }
    }
    public XMLGregorianCalendar normalize() {
        XMLGregorianCalendar normalized = normalizeToTimezone(this, timezone);
        if (getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
            normalized.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        }
        if (getMillisecond() == DatatypeConstants.FIELD_UNDEFINED) {
            normalized.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        }
        return normalized;
    }
    private XMLGregorianCalendar normalizeToTimezone(XMLGregorianCalendar cal, int timezone) {
        int minutes = timezone;    	
        XMLGregorianCalendar result = (XMLGregorianCalendar) cal.clone();
        minutes = -minutes;
        Duration d = new DurationImpl(minutes >= 0, 
                0, 
                0, 
                0, 
                0, 
                minutes < 0 ? -minutes : minutes, 
                        0  
        );
        result.add(d);
        result.setTimezone(0);
        return result;
    }
	private static int internalCompare(XMLGregorianCalendar P,
	        XMLGregorianCalendar Q) {
	    int result;
	    if (P.getEon() == Q.getEon()) {
	        result = compareField(P.getYear(), Q.getYear());
	        if (result != DatatypeConstants.EQUAL) {
	            return result;
	        }
	    } 
        else {
	        result = compareField(P.getEonAndYear(), Q.getEonAndYear());
	        if (result != DatatypeConstants.EQUAL) {
	            return result;
	        }
	    }
	    result = compareField(P.getMonth(), Q.getMonth());
	    if (result != DatatypeConstants.EQUAL) {
	        return result;
	    }
	    result = compareField(P.getDay(), Q.getDay());
	    if (result != DatatypeConstants.EQUAL) {
	        return result;
	    }
	    result = compareField(P.getHour(), Q.getHour());
	    if (result != DatatypeConstants.EQUAL) {
	        return result;
	    }
	    result = compareField(P.getMinute(), Q.getMinute());
	    if (result != DatatypeConstants.EQUAL) {
	        return result;
	    }
	    result = compareField(P.getSecond(), Q.getSecond());
	    if (result != DatatypeConstants.EQUAL) {
	        return result;
	    }
	    result = compareField(P.getFractionalSecond(), Q.getFractionalSecond());
	    return result;
	}
    private static int compareField(int Pfield, int Qfield) {
        if (Pfield == Qfield) {
            return DatatypeConstants.EQUAL;
        } 
        else {
            if (Pfield == DatatypeConstants.FIELD_UNDEFINED || Qfield == DatatypeConstants.FIELD_UNDEFINED) {
                return DatatypeConstants.INDETERMINATE;
            } 
            else {
                return (Pfield < Qfield ? DatatypeConstants.LESSER : DatatypeConstants.GREATER);
            }
        }
    }
    private static int compareField(BigInteger Pfield, BigInteger Qfield) {
        if (Pfield == null) {
            return (Qfield == null ? DatatypeConstants.EQUAL : DatatypeConstants.INDETERMINATE);
        }
        if (Qfield == null) {
            return DatatypeConstants.INDETERMINATE;
        }
        return Pfield.compareTo(Qfield);
    }
    private static int compareField(BigDecimal Pfield, BigDecimal Qfield) {
        if (Pfield == Qfield) {
            return DatatypeConstants.EQUAL;
        }
        if (Pfield == null) {
            Pfield = DECIMAL_ZERO;
        }
        if (Qfield == null) {
            Qfield = DECIMAL_ZERO;
        }
        return Pfield.compareTo(Qfield);
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof XMLGregorianCalendar) {
            return compare((XMLGregorianCalendar) obj) == DatatypeConstants.EQUAL;
        }
        return false;
    }
    public int hashCode() {
        int timezone = getTimezone();
        if (timezone == DatatypeConstants.FIELD_UNDEFINED) {
            timezone = 0;
        }
        XMLGregorianCalendar gc = this;
        if (timezone != 0) {
            gc = normalizeToTimezone(this, getTimezone());
        }
        return gc.getYear() + gc.getMonth() + gc.getDay() + 
        gc.getHour() + gc.getMinute() + gc.getSecond();
    }
    public static XMLGregorianCalendar parse(String lexicalRepresentation) {
		return new XMLGregorianCalendarImpl(lexicalRepresentation);
    }
    public String toXMLFormat() {
        QName typekind = getXMLSchemaType();
        String formatString = null;
        if (typekind == DatatypeConstants.DATETIME) {
            formatString = "%Y-%M-%DT%h:%m:%s"+ "%z";
        } 
        else if (typekind == DatatypeConstants.DATE) {
            formatString = "%Y-%M-%D" +"%z";
        } 
        else if (typekind == DatatypeConstants.TIME) {
            formatString = "%h:%m:%s"+ "%z";
        } 
        else if (typekind == DatatypeConstants.GMONTH) {
            formatString = "--%M--%z";
        } 
        else if (typekind == DatatypeConstants.GDAY) {
            formatString = "---%D" + "%z";
        } 
        else if (typekind == DatatypeConstants.GYEAR) {
            formatString = "%Y" + "%z";
        } 
        else if (typekind == DatatypeConstants.GYEARMONTH) {
            formatString = "%Y-%M" + "%z";
        } 
        else if (typekind == DatatypeConstants.GMONTHDAY) {
            formatString = "--%M-%D" +"%z";
        }
        return format(formatString);
    }
    public QName getXMLSchemaType() {
    	if (year != DatatypeConstants.FIELD_UNDEFINED
    		&& month != DatatypeConstants.FIELD_UNDEFINED
			&& day != DatatypeConstants.FIELD_UNDEFINED
			&& hour != DatatypeConstants.FIELD_UNDEFINED
			&& minute != DatatypeConstants.FIELD_UNDEFINED
			&& second != DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.DATETIME;
    	}
    	if (year != DatatypeConstants.FIELD_UNDEFINED
    		&& month != DatatypeConstants.FIELD_UNDEFINED
			&& day != DatatypeConstants.FIELD_UNDEFINED
			&& hour == DatatypeConstants.FIELD_UNDEFINED
			&& minute == DatatypeConstants.FIELD_UNDEFINED
			&& second == DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.DATE;
    	}
    	if (year == DatatypeConstants.FIELD_UNDEFINED
    		&& month == DatatypeConstants.FIELD_UNDEFINED
			&& day == DatatypeConstants.FIELD_UNDEFINED
			&& hour != DatatypeConstants.FIELD_UNDEFINED
			&& minute != DatatypeConstants.FIELD_UNDEFINED
			&& second != DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.TIME;
    	}
    	if (year != DatatypeConstants.FIELD_UNDEFINED
    		&& month != DatatypeConstants.FIELD_UNDEFINED
			&& day == DatatypeConstants.FIELD_UNDEFINED
			&& hour == DatatypeConstants.FIELD_UNDEFINED
			&& minute == DatatypeConstants.FIELD_UNDEFINED
			&& second == DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.GYEARMONTH;
    	}
    	if (year == DatatypeConstants.FIELD_UNDEFINED
    		&& month != DatatypeConstants.FIELD_UNDEFINED
			&& day != DatatypeConstants.FIELD_UNDEFINED
			&& hour == DatatypeConstants.FIELD_UNDEFINED
			&& minute == DatatypeConstants.FIELD_UNDEFINED
			&& second == DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.GMONTHDAY;
    	}
    	if (year != DatatypeConstants.FIELD_UNDEFINED
    		&& month == DatatypeConstants.FIELD_UNDEFINED
			&& day == DatatypeConstants.FIELD_UNDEFINED
			&& hour == DatatypeConstants.FIELD_UNDEFINED
			&& minute == DatatypeConstants.FIELD_UNDEFINED
			&& second == DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.GYEAR;
    	}
    	if (year == DatatypeConstants.FIELD_UNDEFINED
    		&& month != DatatypeConstants.FIELD_UNDEFINED
			&& day == DatatypeConstants.FIELD_UNDEFINED
			&& hour == DatatypeConstants.FIELD_UNDEFINED
			&& minute == DatatypeConstants.FIELD_UNDEFINED
			&& second == DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.GMONTH;
    	}
    	if (year == DatatypeConstants.FIELD_UNDEFINED
    		&& month == DatatypeConstants.FIELD_UNDEFINED
			&& day != DatatypeConstants.FIELD_UNDEFINED
			&& hour == DatatypeConstants.FIELD_UNDEFINED
			&& minute == DatatypeConstants.FIELD_UNDEFINED
			&& second == DatatypeConstants.FIELD_UNDEFINED) {
    		return DatatypeConstants.GDAY;
    	}
		throw new IllegalStateException(
			this.getClass().getName()
			+ "#getXMLSchemaType() :"
            + DatatypeMessageFormatter.formatMessage(null, "InvalidXGCFields", null)
		);
    }
    public boolean isValid() {
        if (month != DatatypeConstants.FIELD_UNDEFINED && day != DatatypeConstants.FIELD_UNDEFINED) {
            if (year != DatatypeConstants.FIELD_UNDEFINED) {
                if (eon == null) {
                    if (day > maximumDayInMonthFor(year, month)) {
                        return false;
                    }
                }
                else if (day > maximumDayInMonthFor(getEonAndYear(), month)) {
                    return false;
                }
            }
            else if (day > maximumDayInMonthFor(2000, month)) {
                return false;
            }
        }
        if (hour == 24 && (minute != 0 || second != 0 || 
                (fractionalSecond != null && fractionalSecond.compareTo(DECIMAL_ZERO) != 0))) {
            return false;
        }
        if (eon == null && year == 0) {
            return false;
        }
        return true;
    }
    public void add(Duration duration) {
        boolean fieldUndefined[] = {
                false,
                false,
                false, 
                false,
                false,
                false
        };
        int signum = duration.getSign();
        int startMonth = getMonth();
        if (startMonth == DatatypeConstants.FIELD_UNDEFINED) {
            startMonth = MIN_FIELD_VALUE[MONTH];
            fieldUndefined[MONTH] = true;
        }
        BigInteger dMonths = sanitize(duration.getField(DatatypeConstants.MONTHS), signum);
        BigInteger temp = BigInteger.valueOf((long) startMonth).add(dMonths);
        setMonth(temp.subtract(BigInteger.ONE).mod(TWELVE).intValue() + 1);
        BigInteger carry =
            new BigDecimal(temp.subtract(BigInteger.ONE)).divide(new BigDecimal(TWELVE), BigDecimal.ROUND_FLOOR).toBigInteger();
        BigInteger startYear = getEonAndYear();
        if (startYear == null) {
            fieldUndefined[YEAR] = true;
            startYear = BigInteger.ZERO;
        }
        BigInteger dYears = sanitize(duration.getField(DatatypeConstants.YEARS), signum);
        BigInteger endYear = startYear.add(dYears).add(carry);
        setYear(endYear);
        BigDecimal startSeconds;
        if (getSecond() == DatatypeConstants.FIELD_UNDEFINED) {
            fieldUndefined[SECOND] = true;
            startSeconds = DECIMAL_ZERO;
        } 
        else {
            startSeconds = getSeconds();
        }
        BigDecimal dSeconds = DurationImpl.sanitize((BigDecimal) duration.getField(DatatypeConstants.SECONDS), signum);
        BigDecimal tempBD = startSeconds.add(dSeconds);
        BigDecimal fQuotient = 
            new BigDecimal(new BigDecimal(tempBD.toBigInteger()).divide(DECIMAL_SIXTY, BigDecimal.ROUND_FLOOR).toBigInteger());
        BigDecimal endSeconds = tempBD.subtract(fQuotient.multiply(DECIMAL_SIXTY));
        carry = fQuotient.toBigInteger();
        setSecond(endSeconds.intValue());
        BigDecimal tempFracSeconds = endSeconds.subtract(new BigDecimal(BigInteger.valueOf((long) getSecond())));
        if (tempFracSeconds.compareTo(DECIMAL_ZERO) < 0) {
            setFractionalSecond(DECIMAL_ONE.add(tempFracSeconds));
            if (getSecond() == 0) {
                setSecond(59);
                carry = carry.subtract(BigInteger.ONE);
            } 
            else {
                setSecond(getSecond() - 1);
            }
        } 
        else {
            setFractionalSecond(tempFracSeconds);
        }
        int startMinutes = getMinute();
        if (startMinutes == DatatypeConstants.FIELD_UNDEFINED) {
            fieldUndefined[MINUTE] = true;
            startMinutes = MIN_FIELD_VALUE[MINUTE];
        }
        BigInteger dMinutes = sanitize(duration.getField(DatatypeConstants.MINUTES), signum);
        temp = BigInteger.valueOf(startMinutes).add(dMinutes).add(carry);
        setMinute(temp.mod(SIXTY).intValue());
        carry = new BigDecimal(temp).divide(DECIMAL_SIXTY, BigDecimal.ROUND_FLOOR).toBigInteger();
        int startHours = getHour();
        if (startHours == DatatypeConstants.FIELD_UNDEFINED) {
            fieldUndefined[HOUR] = true;
            startHours = MIN_FIELD_VALUE[HOUR];
        }
        BigInteger dHours = sanitize(duration.getField(DatatypeConstants.HOURS), signum);
        temp = BigInteger.valueOf(startHours).add(dHours).add(carry);
        setHour(temp.mod(TWENTY_FOUR).intValue());
        carry = new BigDecimal(temp).divide(new BigDecimal(TWENTY_FOUR), BigDecimal.ROUND_FLOOR).toBigInteger();
        BigInteger tempDays;
        int startDay = getDay();
        if (startDay == DatatypeConstants.FIELD_UNDEFINED) {
            fieldUndefined[DAY] = true;
            startDay = MIN_FIELD_VALUE[DAY];
        }
        BigInteger dDays = sanitize(duration.getField(DatatypeConstants.DAYS), signum);
        int maxDayInMonth = maximumDayInMonthFor(getEonAndYear(), getMonth());
        if (startDay > maxDayInMonth) {
            tempDays =  BigInteger.valueOf(maxDayInMonth);
        } 
        else if (startDay < 1) {
            tempDays = BigInteger.ONE;
        } 
        else {
            tempDays = BigInteger.valueOf(startDay);
        }
        BigInteger endDays = tempDays.add(dDays).add(carry);
        int monthCarry;
        int intTemp;
        while (true) {
            if (endDays.compareTo(BigInteger.ONE) < 0) {
                BigInteger mdimf = null;
                if (month >= 2) {
                    mdimf = BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(), getMonth() - 1));
                } 
                else {
                    mdimf = BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear().subtract(BigInteger.valueOf((long) 1)), 12));
                }
                endDays = endDays.add(mdimf);
                monthCarry = -1;
            } 
            else if (endDays.compareTo(BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(), getMonth()))) > 0) {
                endDays = endDays.add(BigInteger.valueOf(-maximumDayInMonthFor(getEonAndYear(), getMonth())));
                monthCarry = 1;
            } 
            else {
                break;
            } 
            intTemp = getMonth() + monthCarry;
            int endMonth = (intTemp - 1) % (13 - 1);
            int quotient;
            if (endMonth < 0) {
                endMonth = (13 - 1) + endMonth + 1;
                quotient = BigDecimal.valueOf(intTemp - 1).divide(new BigDecimal(TWELVE), BigDecimal.ROUND_UP).intValue();
            } 
            else {
                quotient = (intTemp - 1) / (13 - 1);
                endMonth += 1;
            }
            setMonth(endMonth);
            if (quotient != 0)  {
                setYear(getEonAndYear().add(BigInteger.valueOf(quotient)));
            } 
        }
        setDay(endDays.intValue());
        for (int i = YEAR; i <= SECOND; i++) {
            if (fieldUndefined[i]) {
                switch (i) {
                    case YEAR:
                        setYear(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case MONTH:
                        setMonth(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case DAY:
                        setDay(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case HOUR:
                        setHour(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case MINUTE:
                        setMinute(DatatypeConstants.FIELD_UNDEFINED);
                        break;
                    case SECOND:
                        setSecond(DatatypeConstants.FIELD_UNDEFINED);
                        setFractionalSecond(null);
                        break;
                }
            }
        }
    }
    private static final BigInteger FOUR = BigInteger.valueOf(4);
    private static final BigInteger HUNDRED = BigInteger.valueOf(100);
    private static final BigInteger FOUR_HUNDRED = BigInteger.valueOf(400);
    private static final BigInteger SIXTY = BigInteger.valueOf(60);
    private static final BigInteger TWENTY_FOUR = BigInteger.valueOf(24);
    private static final BigInteger TWELVE = BigInteger.valueOf(12);
    private static final BigDecimal DECIMAL_ZERO = BigDecimal.valueOf(0);
    private static final BigDecimal DECIMAL_ONE = BigDecimal.valueOf(1);
    private static final BigDecimal DECIMAL_SIXTY = BigDecimal.valueOf(60);
    private static class DaysInMonth {
        private static final int [] table = { 0,  
            31, 28, 31, 30, 31, 30,
            31, 31, 30, 31, 30, 31};
    }
    private static int maximumDayInMonthFor(BigInteger year, int month) {
        if (month != DatatypeConstants.FEBRUARY) {
            return DaysInMonth.table[month];
        } 
        else {
            if (year.mod(FOUR_HUNDRED).equals(BigInteger.ZERO) || 
                    (!year.mod(HUNDRED).equals(BigInteger.ZERO) &&
                            year.mod(FOUR).equals(BigInteger.ZERO))) {
                return 29;
            } 
            else {
                return DaysInMonth.table[month];
            }
        }
    }
    private static int maximumDayInMonthFor(int year, int month) {
        if (month != DatatypeConstants.FEBRUARY) {
            return DaysInMonth.table[month];
        } 
        else {
            if ( ((year %400) == 0) || 
                    ( ((year % 100) != 0) && ((year % 4) == 0))) {
                return 29;
            } 
            else {
                return DaysInMonth.table[DatatypeConstants.FEBRUARY];
            }
        }
    }
    public java.util.GregorianCalendar toGregorianCalendar() {
        GregorianCalendar result = null;
        final int DEFAULT_TIMEZONE_OFFSET = DatatypeConstants.FIELD_UNDEFINED;
        TimeZone tz = getTimeZone(DEFAULT_TIMEZONE_OFFSET);
        Locale locale = java.util.Locale.getDefault();
        result = new GregorianCalendar(tz, locale);
        result.clear();
        result.setGregorianChange(PURE_GREGORIAN_CHANGE);
        if (year != DatatypeConstants.FIELD_UNDEFINED) {
            if (eon == null) {
                result.set(Calendar.ERA, year < 0 ? GregorianCalendar.BC : GregorianCalendar.AD);
                result.set(Calendar.YEAR, Math.abs(year));
            }
            else {
                BigInteger eonAndYear = getEonAndYear();
                result.set(Calendar.ERA, eonAndYear.signum() == -1 ? GregorianCalendar.BC : GregorianCalendar.AD);
                result.set(Calendar.YEAR, eonAndYear.abs().intValue());
            }
        }
        if (month != DatatypeConstants.FIELD_UNDEFINED) { 
            result.set(Calendar.MONTH, month  - 1);
        }
        if (day != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.DAY_OF_MONTH, day);
        }
        if (hour != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.MINUTE, minute);
        }
        if (second != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.SECOND, second);
        }
        if (fractionalSecond != null) {
            result.set(Calendar.MILLISECOND, getMillisecond());
        }
        return result;
    }
    public GregorianCalendar toGregorianCalendar(java.util.TimeZone timezone, 
            java.util.Locale aLocale,
            XMLGregorianCalendar defaults) {
        GregorianCalendar result = null;
        TimeZone tz = timezone;
        if (tz == null) {
            int defaultZoneoffset = DatatypeConstants.FIELD_UNDEFINED;
            if (defaults != null) {
                defaultZoneoffset = defaults.getTimezone();
            }
            tz = getTimeZone(defaultZoneoffset);
        }
        if (aLocale == null) {
            aLocale = java.util.Locale.getDefault();
        }
        result = new GregorianCalendar(tz, aLocale);
        result.clear();
        result.setGregorianChange(PURE_GREGORIAN_CHANGE);
        if (year != DatatypeConstants.FIELD_UNDEFINED) {
            if (eon == null) {
                result.set(Calendar.ERA, year < 0 ? GregorianCalendar.BC : GregorianCalendar.AD);
                result.set(Calendar.YEAR, Math.abs(year));
            }
            else {
                final BigInteger eonAndYear = getEonAndYear();
                result.set(Calendar.ERA, eonAndYear.signum() == -1 ? GregorianCalendar.BC : GregorianCalendar.AD);
                result.set(Calendar.YEAR, eonAndYear.abs().intValue());
            }
        }
        else {
            if (defaults != null) {
                final int defaultYear = defaults.getYear();
                if (defaultYear != DatatypeConstants.FIELD_UNDEFINED) {
                    if (defaults.getEon() == null) {
                        result.set(Calendar.ERA, defaultYear < 0 ? GregorianCalendar.BC : GregorianCalendar.AD);
                        result.set(Calendar.YEAR, Math.abs(defaultYear));
                    }
                    else {
                        final BigInteger defaultEonAndYear = defaults.getEonAndYear();
                        result.set(Calendar.ERA, defaultEonAndYear.signum() == -1 ? GregorianCalendar.BC : GregorianCalendar.AD);
                        result.set(Calendar.YEAR, defaultEonAndYear.abs().intValue());
                    }
                }
            }
        }
        if (month != DatatypeConstants.FIELD_UNDEFINED) { 
            result.set(Calendar.MONTH, month  - 1);
        } 
        else {
            final int defaultMonth = (defaults != null) ? defaults.getMonth() : DatatypeConstants.FIELD_UNDEFINED;
            if (defaultMonth != DatatypeConstants.FIELD_UNDEFINED) { 
                result.set(Calendar.MONTH, defaultMonth  - 1);
            }
        }
        if (day != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.DAY_OF_MONTH, day);
        } 
        else {
            final int defaultDay = (defaults != null) ? defaults.getDay() : DatatypeConstants.FIELD_UNDEFINED;
            if (defaultDay != DatatypeConstants.FIELD_UNDEFINED) { 
                result.set(Calendar.DAY_OF_MONTH, defaultDay);
            }
        }
        if (hour != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.HOUR_OF_DAY, hour);
        } 
        else {
            int defaultHour = (defaults != null) ? defaults.getHour() : DatatypeConstants.FIELD_UNDEFINED;
            if (defaultHour != DatatypeConstants.FIELD_UNDEFINED) { 
                result.set(Calendar.HOUR_OF_DAY, defaultHour);
            }
        }
        if (minute != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.MINUTE, minute);
        } 
        else {
            final int defaultMinute = (defaults != null) ? defaults.getMinute() : DatatypeConstants.FIELD_UNDEFINED;
            if (defaultMinute != DatatypeConstants.FIELD_UNDEFINED) { 
                result.set(Calendar.MINUTE, defaultMinute);
            }
        }
        if (second != DatatypeConstants.FIELD_UNDEFINED) {
            result.set(Calendar.SECOND, second);
        } 
        else {
            final int defaultSecond = (defaults != null) ? defaults.getSecond() : DatatypeConstants.FIELD_UNDEFINED;
            if (defaultSecond != DatatypeConstants.FIELD_UNDEFINED) { 
                result.set(Calendar.SECOND, defaultSecond);
            }
        }
        if (fractionalSecond != null) {
            result.set(Calendar.MILLISECOND, getMillisecond());
        } 
        else {
            final BigDecimal defaultFractionalSecond = (defaults != null) ? defaults.getFractionalSecond() : null;
            if (defaultFractionalSecond != null) { 
                result.set(Calendar.MILLISECOND, defaults.getMillisecond());
            }
        }
        return result;
    }
    public TimeZone getTimeZone(int defaultZoneoffset) {
        TimeZone result = null;
        int zoneoffset = getTimezone();
        if (zoneoffset == DatatypeConstants.FIELD_UNDEFINED) {
            zoneoffset = defaultZoneoffset;
        }
        if (zoneoffset == DatatypeConstants.FIELD_UNDEFINED) {
            result = TimeZone.getDefault();
        } 
        else {
            char sign = zoneoffset < 0 ? '-' : '+';
            if (sign == '-') {
                zoneoffset = -zoneoffset;
            }
            int hour = zoneoffset / 60;
            int minutes = zoneoffset - (hour * 60);
            StringBuffer customTimezoneId = new StringBuffer(8);
            customTimezoneId.append("GMT");
            customTimezoneId.append(sign);
            customTimezoneId.append(hour);
            if (minutes != 0) {
                if (minutes < 10) {
                    customTimezoneId.append('0');
                }
                customTimezoneId.append(minutes);
            }
            result = TimeZone.getTimeZone(customTimezoneId.toString());
        }
        return result;
    }
   public Object clone() {
       return new XMLGregorianCalendarImpl(getEonAndYear(),
                        this.month, this.day, 
			this.hour, this.minute, this.second,
			this.fractionalSecond,
			this.timezone);
    }
   public void clear() {
       eon = null;
       year = DatatypeConstants.FIELD_UNDEFINED;
       month = DatatypeConstants.FIELD_UNDEFINED;
       day = DatatypeConstants.FIELD_UNDEFINED;
       timezone = DatatypeConstants.FIELD_UNDEFINED;  
       hour = DatatypeConstants.FIELD_UNDEFINED;
       minute = DatatypeConstants.FIELD_UNDEFINED;
       second = DatatypeConstants.FIELD_UNDEFINED;
       fractionalSecond = null;
   }
    public void setMillisecond(int millisecond) {
        if (millisecond == DatatypeConstants.FIELD_UNDEFINED) {
            fractionalSecond = null;
        } 
        else {
            checkFieldValueConstraint(MILLISECOND, millisecond);
            fractionalSecond = BigDecimal.valueOf(millisecond, 3);
        } 
    }
    public void setFractionalSecond(BigDecimal fractional) {
        if (fractional != null) {
            if ((fractional.compareTo(DECIMAL_ZERO) < 0) ||
                    (fractional.compareTo(DECIMAL_ONE) > 0)) {
                throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, 
                        "InvalidFractional", new Object[]{fractional}));
            }            	                             
        }
        this.fractionalSecond = fractional;
    }
    private final class Parser {
        private final String format;
        private final String value;
        private final int flen;
        private final int vlen;
        private int fidx;
        private int vidx;
        private Parser(String format, String value) {
            this.format = format;
            this.value = value;
            this.flen = format.length();
            this.vlen = value.length();
        }
        public void parse() throws IllegalArgumentException {
            while (fidx < flen) {
                char fch = format.charAt(fidx++);
                if (fch != '%') { 
                    skip(fch);
                    continue;
                }
                switch (format.charAt(fidx++)) {
                    case 'Y' : 
                        parseYear();
                        break;
                    case 'M' : 
                        setMonth(parseInt(2, 2));
                        break;
                    case 'D' : 
                        setDay(parseInt(2, 2));
                        break;
                    case 'h' : 
                        setHour(parseInt(2, 2));
                        break;
                    case 'm' : 
                        setMinute(parseInt(2, 2));
                        break;
                    case 's' : 
                        setSecond(parseInt(2, 2));
                        if (peek() == '.') {
                            setFractionalSecond(parseBigDecimal());
                        }
                        break;
                    case 'z' : 
                        char vch = peek();
                        if (vch == 'Z') {
                            vidx++;
                            setTimezone(0);
                        } 
                        else if (vch == '+' || vch == '-') {
                            vidx++;
                            int h = parseInt(2, 2);
                            skip(':');
                            int m = parseInt(2, 2);
                            setTimezone((h * 60 + m) * (vch == '+' ? 1 : -1));
                        }
                        break;
                    default :
                        throw new InternalError();
                }
            }
            if (vidx != vlen) {
                throw new IllegalArgumentException(value); 
            }
        }
        private char peek() throws IllegalArgumentException {
            if (vidx == vlen) {
                return (char) -1;
            }
            return value.charAt(vidx);
        }
        private char read() throws IllegalArgumentException {
            if (vidx == vlen) {
                throw new IllegalArgumentException(value); 
            }
            return value.charAt(vidx++);
        }
        private void skip(char ch) throws IllegalArgumentException {
            if (read() != ch) {
                throw new IllegalArgumentException(value); 
            }
        }
        private void parseYear()
            throws IllegalArgumentException {
            int vstart = vidx;
            int sign = 0;
            if (peek() == '-') {
                vidx++;
                sign = 1;
            }
            while (isDigit(peek())) {
                vidx++;
            }
            final int digits = vidx - vstart - sign;
            if (digits < 4) {
                throw new IllegalArgumentException(value); 
            }
            final String yearString = value.substring(vstart, vidx);
            if (digits < 10) {
                setYear(Integer.parseInt(yearString));
            }
            else {
                setYear(new BigInteger(yearString));
            }
        }
        private int parseInt(int minDigits, int maxDigits)
            throws IllegalArgumentException {
            int vstart = vidx;
            while (isDigit(peek()) && (vidx - vstart) < maxDigits) {
                vidx++;
            }
            if ((vidx - vstart) < minDigits) {
                throw new IllegalArgumentException(value); 
            }
            return Integer.parseInt(value.substring(vstart, vidx));
        }
        private BigDecimal parseBigDecimal()
            throws IllegalArgumentException {
            int vstart = vidx;
            if (peek() == '.') {
                vidx++;
            } else {
                throw new IllegalArgumentException(value);
            }
            while (isDigit(peek())) {
                vidx++;
            }
            return new BigDecimal(value.substring(vstart, vidx));
        }
    }
    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }
    private String format( String format ) {
        StringBuffer buf = new StringBuffer();
        int fidx=0,flen=format.length();
        while(fidx<flen) {
            char fch = format.charAt(fidx++);
            if(fch!='%') {
                buf.append(fch);
                continue;
            }
            switch(format.charAt(fidx++)) {
                case 'Y':
                    if (eon == null) {
                        int absYear = year;
                        if (absYear < 0) {
                            buf.append('-');
                            absYear = -year;
                        }
                        printNumber(buf, absYear, 4);
                    }
                    else {
                        printNumber(buf, getEonAndYear(), 4);
                    }
                    break;
                case 'M':
                    printNumber(buf,getMonth(),2);
                    break;
                case 'D':
                    printNumber(buf,getDay(),2);
                    break;
                case 'h':
                    printNumber(buf,getHour(),2);
                    break;
                case 'm':
                    printNumber(buf,getMinute(),2);
                    break;
                case 's':
                    printNumber(buf,getSecond(),2);
                    if (getFractionalSecond() != null) {
                        String frac = toString(getFractionalSecond());
                        buf.append(frac.substring(1, frac.length()));
                    } 
                    break;
                case 'z':
                    int offset = getTimezone();
                    if (offset == 0) {
                        buf.append('Z');
                    } 
                    else if (offset != DatatypeConstants.FIELD_UNDEFINED) {
                        if (offset < 0) {
                            buf.append('-');
                            offset *= -1; 
                        } 
                        else {
                            buf.append('+');
                        }
                        printNumber(buf,offset/60,2);
                        buf.append(':');
                        printNumber(buf,offset%60,2);
                    }
                    break;
                default:
                    throw new InternalError();  
            }
        }
        return buf.toString();
    }
    private void printNumber( StringBuffer out, int number, int nDigits ) {
        String s = String.valueOf(number);
        for (int i = s.length(); i < nDigits; i++) {
            out.append('0');
        }
        out.append(s);
    }
    private void printNumber( StringBuffer out, BigInteger number, int nDigits) {
        String s = number.toString();
        for (int i=s.length(); i < nDigits; i++) {
            out.append('0');
        }
        out.append(s);
    }
    private String toString(BigDecimal bd) {
        String intString = bd.unscaledValue().toString();
        int scale = bd.scale();
        if (scale == 0) {
            return intString;
        }
        StringBuffer buf;
        int insertionPoint = intString.length() - scale;
        if (insertionPoint == 0) { 
            return "0." + intString;
        } 
        else if (insertionPoint > 0) { 
            buf = new StringBuffer(intString);
            buf.insert(insertionPoint, '.');
        } 
        else { 
            buf = new StringBuffer(3 - insertionPoint + intString.length());
            buf.append("0.");
            for (int i = 0; i < -insertionPoint; i++) {
                buf.append('0');
            }
            buf.append(intString);
        }
        return buf.toString();
    }
    static BigInteger sanitize(Number value, int signum) {
        if (signum == 0 || value == null) {
            return BigInteger.ZERO;
        }
        return (signum <  0)? ((BigInteger)value).negate() : (BigInteger)value;
    }
    public void reset() {
        eon = orig_eon;
        year = orig_year;
        month = orig_month;
        day = orig_day;
        hour = orig_hour;
        minute = orig_minute;
        second = orig_second;
        fractionalSecond = orig_fracSeconds;
        timezone = orig_timezone;
    }
    private Object writeReplace() throws IOException {
        return new SerializedXMLGregorianCalendar(toXMLFormat());
    }
}
