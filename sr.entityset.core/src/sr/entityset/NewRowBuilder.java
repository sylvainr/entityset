package sr.entityset;

import java.util.Collection;

public class NewRowBuilder 
{
	private final static int INT_SEED = 0; 

	public EntityRow buildEntityRow(EntityTable table)
	{
		try 
		{
			EntityRow entityRow = table.newRow();
			
			Collection<EntityColumn> pkColumns = table.getPrimaryKeyColumns();
			boolean needsToFillPrimaryKeyValues = pkColumns.size() > 0;
			
			if (needsToFillPrimaryKeyValues)
				fillPrimaryKeyValues(table, entityRow, pkColumns);

			return entityRow;
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void fillPrimaryKeyValues(EntityTable table, 
			EntityRow row, Collection<EntityColumn> pkColumns) throws Exception
	{
		Integer[] maxes = findIntegerMaxesForPrimaryKeyValues(table, pkColumns);
		int i = 0;
		for(EntityColumn pkColumn : pkColumns)
		{
			int curPkColumnMax = maxes[i++];
			row.setValue(pkColumn, curPkColumnMax + 1);
		}
	}
	
	private Integer[] findIntegerMaxesForPrimaryKeyValues(
			EntityTable table, Collection<EntityColumn> pkColumns)
	{
		Integer[] maxes = new Integer[pkColumns.size()]; 
		for(EntityRow row : table.rows())
		{
			int i = 0;
			for(EntityColumn pkCol : pkColumns)
			{
				Integer curVal = (Integer)row.getValue(pkCol);
				
				if (maxes[i] == null)
					maxes[i] = curVal;
				else if (maxes[i] < curVal)
					maxes[i] = curVal;
				
				i++;
			}
		}
		
		for(int i = 0; i < maxes.length; i++) 
		{
			if (maxes[i] == null)
				maxes[i] = INT_SEED;
		}
		
		return maxes;
	}
}
