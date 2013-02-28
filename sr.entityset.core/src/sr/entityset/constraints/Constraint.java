package sr.entityset.constraints;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;

public abstract class Constraint 
{
	private String name;

	public Constraint(String constraintName)
	{
		this.name = constraintName;
	}
	
	public ConstraintError validateExistingRow(EntityRow existingRow)
	{
		return validatePropositionOnRowAdding(existingRow.getObjectArray());
	}
	
	public abstract ConstraintError validatePropositionOnRowAdding(
			Object[] proposedRowValueArray);
	
	public abstract ConstraintError validatePropositionOnRowModifiying(
			EntityRow row, Object proposedValue, EntityColumn proposedValueColumn);
	
	public abstract ConstraintError validatePropositionOnRowRemoving(EntityRow row);
	
	public String getName() {
		return name;
	}
}
