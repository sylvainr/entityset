package sr.entityset.constraints;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.Index;

public class ForeignKeyChildConstraint extends Constraint 
{
	private EntityTable parentTable;
	private Collection<EntityColumn> childColumns;
	private Collection<EntityColumn> parentColumns;

	public ForeignKeyChildConstraint(
			String constraintName,
			EntityTable parentTable, 
			Collection<EntityColumn> parentColumns, 
			Collection<EntityColumn> childColumns) 
	{
		super(constraintName);
		
		this.parentTable = parentTable;
		this.childColumns = childColumns;
		this.parentColumns = parentColumns;
		
		for(EntityColumn parentColumn : parentColumns)
		{
			if (parentTable != parentColumn.getTable()) 
				throw new IllegalArgumentException("given parent column " +
					"'" + parentColumn.getName() + "' " +
					"does not belong to passed parent table " +
					"'" + parentTable.getName() + "'");
		}
			
		
		if (childColumns.size() != parentColumns.size())
			throw new IllegalArgumentException("parent table columns and " +
					"child table columns should be of the same size.");
	}

	@Override
	public ConstraintError validatePropositionOnRowAdding(
			Object[] proposedRowValueArray) 
	{
		Object[] childColumnsValueArray = ConstraintHelper.buildColumnFilteredArray(
				this.childColumns, proposedRowValueArray);
		
		return calculatePotentialViolation(childColumnsValueArray);
	}

	@Override
	public ConstraintError validatePropositionOnRowModifiying(
			EntityRow row, 
			Object proposedValue, 
			EntityColumn proposedValueColumn) 
	{
		if (this.childColumns.contains(proposedValueColumn) == false) return null;
		
		Object[] childColumnsValueArray = ConstraintHelper.buildColumnFilteredArray(
				this.childColumns, 
				row.getObjectArray(), 
				proposedValueColumn, 
				proposedValue);
		
		return calculatePotentialViolation(childColumnsValueArray);
	}

	@Override
	public ConstraintError validatePropositionOnRowRemoving(EntityRow row) {
		return null;
	}
	
	public EntityTable getParentTable() {
		return this.parentTable;
	}

	private ConstraintError calculatePotentialViolation(Object[] childColumnsValueArray) 
	{
		Index index = this.getParentTableIndex();
		
		List<EntityRow> parentRows = index.findRows(childColumnsValueArray);
		if (parentRows.size() == 0)
		{
			if (hasAtLeastOneNull(childColumnsValueArray)) 
				return null;
			else
			{
				Collection<String> parentColumnNames = Collections2.
						transform(this.parentColumns, new Function<EntityColumn, String>() 
				{
					@Override
					public String apply(EntityColumn entityCol) {
						return ((EntityColumn)entityCol).getName();
					}
				});
				
				return new ConstraintError("Foreign key '" + this.getName() 
						+ "' on columns '" + StringUtils.join(parentColumnNames, ", ") 
						+ "' with values '" + toString(childColumnsValueArray) 
						+ "' does not exist in parent table '" 
						+ this.parentTable.getName() + "'");
			}
		}
		else
			return null;
	}

	private String toString(Object[] childColumnsValueArray) 
	{
		Collection<String> arr = Collections2.transform(
				Arrays.asList(childColumnsValueArray), 
				new Function<Object, String>()
		{
			@Override public String apply(Object arg0) {
				return String.valueOf(arg0);
			}
		});
		
		return StringUtils.join(arr, ", ");
	}
	
	private static boolean hasAtLeastOneNull(Object[] arr)
	{
		for(Object o : arr)
			if (o == null) return true;
		
		return false;
	}
	
	private Index getParentTableIndex()	{
		return this.parentTable.getIndex(this.parentColumns);
	}

	public Collection<EntityColumn> getChildColumns() {
		return childColumns;
	}

	public Collection<EntityColumn> getParentColumns() {
		return parentColumns;
	}
}
