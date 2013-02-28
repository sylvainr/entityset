package test.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import sr.entityset.io.converters.ISOFormatJodaDateTimeConverter;

public class ISOFormatJodaDateTimeConverterTest {

	@Test
	public void testDateTimeParsing() throws Exception {
		ISOFormatJodaDateTimeConverter converter = new ISOFormatJodaDateTimeConverter();
		
		assertEquals(new DateTime(2005, 2, 3, 4, 5, 6, 0, DateTimeZone.UTC), converter.stringToDateTime("2005-02-03T04:05:06"));
		assertEquals(new DateTime(2005, 2, 3, 4, 5, 6, 0, DateTimeZone.UTC), converter.stringToDateTime("2005-02-03T04:05:06Z"));
		
		Boolean ok = false;
		try {
			converter.stringToDateTime("2005-02-03T04:05:06+02:00");
		}
		catch(Exception ex)
		{
			ok = true;
		}
		assertTrue(ok);
	}
	
	@Test
	public void testConvertToXmlStringForDates() throws Exception
	{
		ISOFormatJodaDateTimeConverter converter = new ISOFormatJodaDateTimeConverter();
		
		assertEquals("2005-02-03T04:05:06", converter.dateTimeToString(new DateTime(2005, 2, 3, 4, 5, 6, 0, DateTimeZone.UTC)));
		
		Boolean ok = false;
		try 
		{
			converter.dateTimeToString(new DateTime(2005, 2, 3, 4, 5, 6, 0, DateTimeZone.forOffsetHours(2)));	
		}
		catch(Exception ex) { ok = true; }
	
		assertTrue(ok);
	}
	
}
