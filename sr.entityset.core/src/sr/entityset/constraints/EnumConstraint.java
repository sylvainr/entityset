package sr.entityset.constraints;

import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;

public class EnumConstraint extends Constraint 
{
	private final EntityColumn constrainedColumn;
	private final HashSet<Object> validEntries;

	public EnumConstraint(String constraintName, 
			EntityColumn constrainedColumn, Object[] validEntries) 
	{
		super(constraintName);
		
		this.constrainedColumn = constrainedColumn;
		this.validEntries = new HashSet<Object>();
		for(Object o : validEntries)
			this.validEntries.add(o);
	}

	private ConstraintError validate(Object proposedValue) 
	{
		if (this.validEntries.contains(proposedValue))
			return null;
		else
			return new ConstraintError("Given value '" + String.valueOf(proposedValue)
					+ "' for column '" + this.constrainedColumn.getName()
					+ "' is incorrect. It has to be one of the following values: " 
					+ StringUtils.join(this.validEntries, ", "));
	}
	
	@Override
	public ConstraintError validatePropositionOnRowAdding(
			Object[] proposedRowValueArray) {
		return validate(
				proposedRowValueArray[this.constrainedColumn.getNumber()]);
	}

	@Override
	public ConstraintError validatePropositionOnRowModifiying(EntityRow row,
			Object proposedValue, EntityColumn proposedValueColumn) 
	{
		if (proposedValueColumn.equals(this.constrainedColumn))
			return validate(proposedValue);
		else
			return null;
	}

	@Override
	public ConstraintError validatePropositionOnRowRemoving(EntityRow row) 
	{
		// Nothing to do in such a case
		return null;
	}
	
	public EntityColumn getConstrainedColumn() {
		return constrainedColumn;
	}

	public HashSet<Object> getValidEntries() {
		return validEntries;
	}
}
