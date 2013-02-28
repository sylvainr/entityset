package sr.entityset;

import java.util.*;

public abstract class EntitySet
{
	private Hashtable<String, EntityTable> tablesByName =
			new Hashtable<String, EntityTable>();
	
	protected void addTable(String tableName, EntityTable table) {
		this.tablesByName.put(tableName, table);
	}

	public abstract void buildConstraints();
	public abstract int getSchemaVersion();
	
	public Collection<EntityTable> getTables() {
		return tablesByName.values();
	}

	public EntityTable getTable(String tableName) {
		return tablesByName.get(tableName);
	}
	
	public Collection<String> getTableNames()
	{
		Collection<String> tableNames = new ArrayList<String>();
		
		for(EntityTable table : this.tablesByName.values())
			tableNames.add(table.getName());
		
		return tableNames;
	}
	
	public boolean isChanged() 
	{
		for(EntityTable table : this.getTables())
			if (table.isChanged()) return true;
		
		return false;
	}
	
	public void acceptAllTableChanges() 
	{
		for(EntityTable table : this.getTables())
			table.acceptChanges();
	}
}
