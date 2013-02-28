package sr.entityset;

public class InvalidRowStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidRowStateException(String message)
	{
		super(message);
	}
}
