package sr.entityset.io.converters;

import org.joda.time.DateTime;

public interface IJodaDateTimeConverter {

	public abstract String dateTimeToString(DateTime dateValue) throws Exception;

	public abstract DateTime stringToDateTime(String stringValue) throws Exception;

}