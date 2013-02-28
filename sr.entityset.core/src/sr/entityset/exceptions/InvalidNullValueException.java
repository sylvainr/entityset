package sr.entityset.exceptions;

public class InvalidNullValueException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public InvalidNullValueException(String columnName)
	{
		super("Column " + columnName + " does not allow Null values.");
	}
}
