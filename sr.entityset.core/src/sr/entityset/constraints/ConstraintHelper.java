package sr.entityset.constraints;

import java.util.Collection;

import sr.entityset.EntityColumn;

public class ConstraintHelper 
{
	public static Object[] buildColumnFilteredArray(
			Collection<EntityColumn> columns, Object[] fullRowValueArray)
	{
		return buildColumnFilteredArray(columns, fullRowValueArray, null, null);
	}
	
	public static Object[] buildColumnFilteredArray(
			Collection<EntityColumn> columns, 
			Object[] fullRowValueArray, 
			EntityColumn specialColumn, 
			Object specialValue)
	{
		int i = 0;
		Object[] retArray = new Object[columns.size()];
		
		for(EntityColumn col : columns)
		{
			if (col == specialColumn)
				retArray[i++] = specialValue;
			else
				retArray[i++] = fullRowValueArray[col.getNumber()];
		}
			
		return retArray;
	}
}
