package sr.entityset.io.converters;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeFormatterJodaDateTimeConverter implements IJodaDateTimeConverter
{
	private DateTimeFormatter formatter;

	public DateTimeFormatterJodaDateTimeConverter(String datePattern) {
		this.formatter = DateTimeFormat.forPattern(datePattern);
	}
	
	/* (non-Javadoc)
	 * @see sr.lentils.serialization.IJodaDateTimeConverter#ISODateTimeToString(org.joda.time.DateTime)
	 */
	@Override
	public String dateTimeToString(DateTime dateValue) throws Exception
	{
		if (dateValue.getZone().equals(DateTimeZone.UTC) == false)
			throw new Exception("Only support UTC timezone date-time, which is not the case for date " + dateValue.toString()); 
	
		return dateValue.toString(formatter.withZone(DateTimeZone.UTC));
	}
	
	/* (non-Javadoc)
	 * @see sr.lentils.serialization.IJodaDateTimeConverter#ISOStringToDateTime(java.lang.String)
	 */
	@Override
	public DateTime stringToDateTime(String stringValue) throws Exception {
		return this.formatter.withZone(DateTimeZone.UTC).parseDateTime(stringValue);
	}
}