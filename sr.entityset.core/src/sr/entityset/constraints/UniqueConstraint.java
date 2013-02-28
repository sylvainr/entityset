package sr.entityset.constraints;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.Index;

public class UniqueConstraint extends Constraint 
{
	private final EntityTable table;
	private final Collection<EntityColumn> columns;
	private final boolean isPrimaryKey;
	
	public UniqueConstraint(String constraintName, 
			EntityTable table, Collection<EntityColumn> columns) {
		this(constraintName, table, columns, false);
	}
	
	public UniqueConstraint(String constraintName, 
			EntityTable table, Collection<EntityColumn> columns, 
			boolean isPrimaryKey)
	{
		super(constraintName);
		
		this.columns = new LinkedHashSet<EntityColumn>(columns);
		this.table = table;
		this.isPrimaryKey = isPrimaryKey;
	}
	
	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}
	
	@Override
	public ConstraintError validatePropositionOnRowAdding(
			Object[] proposedRowValueArray) 
	{
		return verifyRow(proposedRowValueArray, null);
	}
	
	@Override
	public ConstraintError validateExistingRow(EntityRow existingRow) 
	{
		return verifyRow(existingRow.getObjectArray(), existingRow);
	}

	@Override
	public ConstraintError validatePropositionOnRowModifiying(
			EntityRow modifiyingRow, 
			Object proposedValue, EntityColumn proposedValueColumn) 
	{
		Object[] indexedColumnValueArray = new Object[this.columns.size()];
		Object[] initialValueArray = modifiyingRow.getObjectArray();
		
		int i = 0;
		boolean hasNull = false;
		for(EntityColumn curPkColumn : this.columns)
		{
			Object value;
			if (proposedValueColumn == curPkColumn)
				value = proposedValue;
			else
				value = initialValueArray[curPkColumn.getNumber()];
			
			indexedColumnValueArray[i++] = value;
			if (value == null) hasNull = true;
		}
			
		Index index = this.getAssociatedIndex();
		List<EntityRow> rows = index.findRows(indexedColumnValueArray);
		
		if (hasNull || rows.size() == 0) 
			return null;
		else
			if (rows.get(0) == modifiyingRow)
				return null;
			else
				return buildErrorMessage(indexedColumnValueArray);
	}

	private ConstraintError verifyRow(
			Object[] proposedRowValueArray, EntityRow existingRow) 
	{
		Object[] indexedColumnValueArray = new Object[this.columns.size()];
		
		int i = 0;
		boolean hasNull = false;
		for(EntityColumn curPkColumn : this.columns)
		{
			Object value = proposedRowValueArray[curPkColumn.getNumber()];
			indexedColumnValueArray[i++] = value;
			if (value == null) hasNull = true;
		}

		Index index = this.getAssociatedIndex();
		List<EntityRow> rows = index.findRows(indexedColumnValueArray);

		if (hasNull)
			return null;
		else if (rows.size() > 0 && rows.get(0) != existingRow)
			return buildErrorMessage(indexedColumnValueArray);
		else
			return null;
	}
	
	private ConstraintError buildErrorMessage(
			Object[] indexedColumnValueArray) 
	{
		Collection<String> colNames = Collections2.transform(this.columns, new Function<EntityColumn, String>() {
			@Override public String apply(EntityColumn arg0) {
				return arg0.getName();
			}
		});
		String columnNames = StringUtils.join(colNames, ", ");
		String columnValues = StringUtils.join(Arrays.asList(indexedColumnValueArray), ", ");

		String message = "A row already exist for column(s) '" + columnNames + "' with value(s) '" + columnValues + "' in table '" + getTable().getName() + "'";
		return new ConstraintError(message);
	}

	@Override
	public ConstraintError validatePropositionOnRowRemoving(EntityRow row) {
		return null;
	}
	
	public Collection<EntityColumn> getColumns() {
		return columns;
	}
	
	public EntityTable getTable() {
		return table;
	}
	
	private Index getAssociatedIndex() {
		return this.getTable().getIndex(this.columns);
	}
}
