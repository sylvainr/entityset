package sr.entityset;

public class EntityColumn {
	
	private int number;
	private String name;

	private Class<?> type;
	private boolean allowNull;
	private EntityTable table;

	public EntityColumn(EntityTable table, int columnNumber, String columnName, Class<?> type, boolean allowNull)
	{
		this.number = columnNumber;
		this.name = columnName;
		this.type = type;
		this.allowNull = allowNull;
		this.table = table;
	}
	
	/**
	 * The zero-based column number.
	 */
	public int getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}
	
	public boolean getAllowNull()
	{
		return this.allowNull;
	}
	
	public EntityTable getTable()
	{
		return this.table;
	}
}
