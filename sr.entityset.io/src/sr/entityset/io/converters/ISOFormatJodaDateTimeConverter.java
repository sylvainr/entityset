package sr.entityset.io.converters;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

public class ISOFormatJodaDateTimeConverter implements IJodaDateTimeConverter
{
	/* (non-Javadoc)
	 * @see sr.lentils.serialization.IJodaDateTimeConverter#ISODateTimeToString(org.joda.time.DateTime)
	 */
	@Override
	public String dateTimeToString(DateTime dateValue) throws Exception
	{
		if (dateValue.getZone().equals(DateTimeZone.UTC) == false)
			throw new Exception("Only support UTC timezone date-time, which is not the case for date " + dateValue.toString()); 
	
		return dateValue.toString(ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC)).substring(0, 19);
	}
	
	/* (non-Javadoc)
	 * @see sr.lentils.serialization.IJodaDateTimeConverter#ISOStringToDateTime(java.lang.String)
	 */
	@Override
	public DateTime stringToDateTime(String stringValue) throws Exception
	{
		Boolean haveNoTimeZone = stringValue.length() == 19;
		if (haveNoTimeZone)
			return ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime(stringValue + "Z");
		else if (stringValue.substring(19).equals("Z"))
			return ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime(stringValue);
		else
			throw new Exception("Datetime ("+ stringValue + ") not supported because it has a timezone specified. Only dates without are compatible.");
	}
}