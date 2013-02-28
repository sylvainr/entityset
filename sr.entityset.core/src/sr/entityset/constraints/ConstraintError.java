package sr.entityset.constraints;

public class ConstraintError {

	private String message;

	public ConstraintError(String message)
	{
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
