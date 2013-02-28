package sr.entityset.exceptions;

public class WrongTypeException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private Class<?> expectedType;
	private Class<? extends Object> receivedType;
	private String columnName;

	public WrongTypeException(Class<?> expectedType, 
			Class<? extends Object> receivedType, 
			String columnName)
	{
		super("Wrong type: received " + receivedType + " when expecting type " + expectedType + " for column " + columnName);
		
		this.expectedType = expectedType;
		this.receivedType = receivedType;
		this.columnName = columnName;
	}

	public Class<?> getExpectedType() {
		return expectedType;
	}

	public Class<? extends Object> getReceivedType() {
		return receivedType;
	}

	public String getColumnName() {
		return columnName;
	}

}
