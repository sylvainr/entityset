package test.serialization;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import sr.entityset.io.converters.DateTimeFormatterJodaDateTimeConverter;

public class DateTimeFormatterJodaDateTimeConverterTest {

	@Test
	public void testParsing() throws Exception
	{
		DateTimeFormatterJodaDateTimeConverter converter = new DateTimeFormatterJodaDateTimeConverter("MM/dd/yyyy hh:mm:ss a");
		assertEquals(new DateTime(1983, 12, 3, 17, 32, 12, 0, DateTimeZone.UTC), converter.stringToDateTime("12/03/1983 05:32:12 PM"));
		assertEquals(new DateTime(2005, 1, 8, 2, 0, 0, 0, DateTimeZone.UTC), converter.stringToDateTime("01/08/2005 02:00:00 AM"));
	}
	
	@Test
	public void testConvertToString() throws Exception
	{
		DateTimeFormatterJodaDateTimeConverter converter = new DateTimeFormatterJodaDateTimeConverter("MM/dd/yyyy hh:mm:ss a");
		assertEquals("12/03/1983 05:32:12 PM", converter.dateTimeToString(new DateTime(1983, 12, 3, 17, 32, 12, 0, DateTimeZone.UTC)));
		assertEquals("02/01/2011 08:00:00 AM", converter.dateTimeToString(new DateTime(2011, 2, 1, 8, 0, 0, 0, DateTimeZone.UTC)));
	}
	
}
