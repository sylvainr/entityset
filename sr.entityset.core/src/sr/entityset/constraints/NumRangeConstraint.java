package sr.entityset.constraints;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;

public class NumRangeConstraint extends Constraint {

	private Double includedMin;
	private Double excludedMax;
	private EntityColumn column;

	public NumRangeConstraint(String constraintName, 
			EntityColumn column, 
			Double includedMin, Double excludedMax) 
	{
		super(constraintName);
		
		this.column = column;
		this.includedMin = includedMin;
		this.excludedMax = excludedMax;
	}

	@Override
	public ConstraintError validatePropositionOnRowAdding(
			Object[] proposedRowValueArray) 
	{
		Double proposedValue = (Double)proposedRowValueArray[this.column.getNumber()];
		return validateCellValue(proposedValue);
	}

	@Override
	public ConstraintError validatePropositionOnRowModifiying(
			EntityRow row, Object proposedValue, 
			EntityColumn proposedValueColumn) 
	{
		if (proposedValueColumn != this.column) return null;
		
		return validateCellValue((Double)proposedValue);
	}

	@Override
	public ConstraintError validatePropositionOnRowRemoving(EntityRow row) {
		return null;
	}
	
	private ConstraintError validateCellValue(Double proposedValue) {
		if (proposedValue == null) return null;
		
		boolean violated = !(proposedValue >= this.includedMin
				&& proposedValue < this.excludedMax);
		
		if (violated)
			return new ConstraintError("Value '" + proposedValue + "' for column '" 
					+ this.column.getName() 
					+ "' is out of the constraint bounderies and this should be between '" 
					+ this.includedMin + "' (included) and '" 
					+ this.excludedMax + "' (excluded).");
		else
			return null;
	}
}
