package sr.entityset.io.converters;

import org.joda.time.DateTime;

public class StringEntityTableTypeConverter {

	private IJodaDateTimeConverter jodaDateTimeConverter;

	public StringEntityTableTypeConverter(IJodaDateTimeConverter jodaDateTimeConverter)	{
		this.jodaDateTimeConverter = jodaDateTimeConverter;
	}
	
	public String convertToString(Object value) 
	{
		try 
		{
			if (value == null) 
				return "";
			
			if (value.getClass().equals(DateTime.class))
			{
				DateTime dateValue = (DateTime)value;
				return this.jodaDateTimeConverter.dateTimeToString(dateValue);
			}
			else
				return value.toString();
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object parseFromString(String stringValue, Class<?> type, boolean allowNull)
	{
		try 
		{
			if (stringValue == "" && allowNull) return null;
			if (type.equals(String.class)) return stringValue;
			
			if (stringValue == "")
				throw new IllegalArgumentException("Unexpected null value when not allowed.");
			
			if (type.equals(Double.class)) return Double.parseDouble(stringValue);
			if (type.equals(Integer.class)) return Integer.parseInt(stringValue);
			if (type.equals(Boolean.class)) return Boolean.parseBoolean(stringValue);
			if (type.equals(DateTime.class)) { return jodaDateTimeConverter.stringToDateTime(stringValue); }
			
			throw new Exception("Unsupported conversion from XML string value with type " + type.toString());
		}
		catch(Exception ex)	{
			throw new RuntimeException("Unable to parse value '" + stringValue + "' into type '" + type.getName() +"'", ex);
		}
	}
}
