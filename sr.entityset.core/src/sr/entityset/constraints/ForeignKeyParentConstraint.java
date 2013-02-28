package sr.entityset.constraints;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.Index;

public class ForeignKeyParentConstraint extends Constraint 
{
	private final EntityTable childTable;
	private final EntityTable parentTable;
	private final Collection<EntityColumn> childColumns;
	private final Collection<EntityColumn> parentColumns;

	public ForeignKeyParentConstraint(
			String constraintName, 
			EntityTable childTable, 
			EntityTable parentTable, 
			Collection<EntityColumn> childColumns, 
			Collection<EntityColumn> parentColumns) 
	{
		super(constraintName);
		
		this.childTable = childTable;
		this.parentTable = parentTable;
		this.childColumns = childColumns;
		this.parentColumns = parentColumns;
	}

	@Override
	public ConstraintError validatePropositionOnRowAdding(
			Object[] proposedRowValueArray) {
		return null;
	}

	@Override
	public ConstraintError validatePropositionOnRowModifiying(
			EntityRow row, 
			Object proposedValue, 
			EntityColumn proposedValueColumn) 
	{
		if (this.parentColumns.contains(proposedValueColumn) == false) 
			return null;
		
		return this.checkForViolation(row, "modify");
	}

	@Override
	public ConstraintError validatePropositionOnRowRemoving(
			EntityRow row) {
		return this.checkForViolation(row, "remove");
	}
	
	private ConstraintError checkForViolation(
			EntityRow parentRow, String actionVerb)
	{
		fireBeforeChildRowValidation(this, parentRow);
		
		Object[] preChangeFullRowValueArray = ConstraintHelper.
				buildColumnFilteredArray(
					this.parentColumns, 
					parentRow.getObjectArray(), 
					null, 
					null);
		
		boolean hasChildRowsDependantOnInitialValue = this.getChildTableIndex()
				.findRows(preChangeFullRowValueArray).size() > 0;

		if (hasChildRowsDependantOnInitialValue == false)
			return null;
		
		boolean otherSimilarParentRowsExist = this.getParentTableIndex()
				.findRows(preChangeFullRowValueArray).size() > 1;
				
		if (otherSimilarParentRowsExist)
			return null;
		
		return new ConstraintError("Can not " + actionVerb + 
				" row as a child row depends on values '" + 
				StringUtils.join(preChangeFullRowValueArray, ", ") + 
				"' because of constraint '" + this.getName() + "'");
	}

	///////////////////////////////////////////////////////////////////////////
	
	private Index getChildTableIndex() {
		return this.childTable.getIndex(this.childColumns);
	}

	private Index getParentTableIndex() {
		return this.parentTable.getIndex(this.parentColumns);
	}
	
	public EntityTable getChildTable() {
		return childTable;
	}

	public EntityTable getParentTable() {
		return parentTable;
	}

	public Collection<EntityColumn> getChildColumns() {
		return childColumns;
	}

	public Collection<EntityColumn> getParentColumns() {
		return parentColumns;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static interface IBeforeChildRowValidationListener 
	{
		void beforeValidation(
				ForeignKeyParentConstraint foreignKeyParentConstraint, 
				EntityRow parentRow);
	}
	
	private final ArrayList<IBeforeChildRowValidationListener> beforeChildRowValidationListeners 
		= new ArrayList<IBeforeChildRowValidationListener>();
	
	public void addBeforeChildRowValidationListener(IBeforeChildRowValidationListener listener)	{
		this.beforeChildRowValidationListeners.add(listener);
	}
	
	public void removeBeforeChildRowValidationListener(IBeforeChildRowValidationListener listener)	{
		this.beforeChildRowValidationListeners.remove(listener);
	}
	
	private void fireBeforeChildRowValidation(
			ForeignKeyParentConstraint foreignKeyParentConstraint, EntityRow parentRow)
	{
		for (IBeforeChildRowValidationListener listener : 
			this.beforeChildRowValidationListeners) 
		{
			listener.beforeValidation(foreignKeyParentConstraint, parentRow);
		}
	}
}
