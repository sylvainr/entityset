package sr.entityset.utils;

import java.util.*;

import sr.entityset.EntityTable;
import sr.entityset.constraints.Constraint;
import sr.entityset.constraints.ForeignKeyChildConstraint;

public class EntityTableDependencyHelper
{
	public static List<EntityTable> getAllParentTables(EntityTable table)
	{
		List<EntityTable> parentTables = new ArrayList<EntityTable>();
		fillParentTables(table, table, parentTables);
		
		return parentTables;
	}
	
	private static void fillParentTables(EntityTable initTable, 
			EntityTable curTable, 
			List<EntityTable> allParentTables)
	{
		Collection<EntityTable> curParentTables = new ArrayList<EntityTable>();
		for(Constraint constraint : curTable.getConstraints())
		{
			if (constraint instanceof ForeignKeyChildConstraint)
			{
				ForeignKeyChildConstraint foreignKeyChildConstraint = 
						(ForeignKeyChildConstraint)constraint;
				curParentTables.add(foreignKeyChildConstraint.getParentTable());
			}
		}
		
		for(EntityTable parentTable : curParentTables)
		{
			// can be self if parent table = child table
			boolean isSelf = (parentTable == curTable);
			if (allParentTables.contains(parentTable) == false && !isSelf)
				fillParentTables(initTable, parentTable, allParentTables);
		}
		
		if (initTable != curTable)
			allParentTables.add(curTable);
	}
}
