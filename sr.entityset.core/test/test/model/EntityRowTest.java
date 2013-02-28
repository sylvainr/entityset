package test.model;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.RowState;
import sr.entityset.exceptions.InvalidNullValueException;
import sr.entityset.exceptions.WrongTypeException;

public class EntityRowTest {

	EntityTable table;
	EntityColumn col1;
	EntityColumn col2;
	
	@Before
	public void before() throws Exception
	{
		table = new EntityTable("table1");
		col1 = table.addColumn("id", Integer.class);
		col2 = table.addColumn("name", String.class, true);
	}
	
	@Test
	public void testDefaultRowStateIsDetached()
	{
		EntityRow row = new EntityRow(table);
		assertEquals(RowState.Detached, row.getState());
	}
	
	@Test
	public void testJustReturnNullWhenNoValueWereEnteredForRow()
	{
		EntityRow row = new EntityRow(table);
		assertNull(row.getValue(0));
		assertNull(row.getValue(1));
	}
	
	@Test
	public void testValuesAreProperlyReturnedWhenEnteredIndividuallyByColumnNumber() throws Exception
	{
		EntityRow row = new EntityRow(table);
		row.setValue(col1.getNumber(), 12);
		row.setValue(col2.getNumber(), "polo");
		
		assertEquals(12, row.getValue(0));
		assertEquals("polo", row.getValue(1));
		assertEquals(12, row.getValue(col1));
		assertEquals("polo", row.getValue(col2));
	}
	
	@Test
	public void testValuesAreProperlyReturnedWhenEnteredIndividuallyByColumn() throws Exception
	{
		EntityRow row = new EntityRow(table);
		row.setValue(col1, 12);
		row.setValue(col2, "polo");
		
		assertEquals(12, row.getValue(0));
		assertEquals("polo", row.getValue(1));
		assertEquals(12, row.getValue(col1));
		assertEquals("polo", row.getValue(col2));
	}
	
	@Test
	public void testRowStateChangesToModifiedWhenValueChanges() throws Exception
	{
		EntityRow row = new EntityRow(table);
		row.setState(RowState.Unchanged);
		
		assertEquals(RowState.Unchanged, row.getState());
		row.setValue(col1, 12);
		row.setValue(col2, "polo");
		assertEquals(RowState.Modified, row.getState());
	}
	
	@Test
	public void testRowStateIsEqualToAddedWhenRowIsAddedToATable() throws Exception 
	{
		EntityRow row = table.newRow();
		assertEquals(RowState.Detached, row.getState());
		
		// required to avoid null exception
		row.setValue(col1, 1);
		
		table.addRow(row);
		assertEquals(RowState.Added, row.getState());
	}
	
	@Test
	public void testConstantRowStateRemainsAfterValueModification() throws Exception
	{
		EntityRow row = table.newRow();
		assertEquals(RowState.Detached, row.getState());
		row.setValue(col1, 12);
		assertEquals(RowState.Detached, row.getState());
		
		table.addRow(row);
		assertEquals(RowState.Added, row.getState());
		row.setValue(col1, 12);
		assertEquals(RowState.Added, row.getState());
	}
	
	@Test
	public void testTypeIsCheckedWhenValueIsSetAndOldValueRemains() throws Exception
	{
		EntityRow row = table.newRow();
		assertEquals(Integer.class, col1.getType());
		
		Integer oldValue = (Integer)row.getValue(col1);
		
		boolean ex = false;
		try 
		{
			row.setValue(col1, "toto");	
		}
		catch(WrongTypeException e) { ex = true; }
		
		assertTrue(ex);
		assertEquals(oldValue, row.getValue(col1));
	}
	
	@Test
	public void testRowValueSupportsNull() throws Exception
	{
		EntityRow row = table.newRow();
		assertEquals(String.class, col2.getType());
		assertTrue(col2.getAllowNull());
		
		row.setValue(col2, null);

		assertEquals(null, row.getValue(col2));
	}
	
	@Test
	public void testSetNullValueToRowThatDoesNotAllowNullShouldRaiseError() throws Exception
	{
		assertFalse(col1.getAllowNull());
		
		EntityRow row = table.newRow();
		row.setValue(col1, 10);
		boolean ex = false;
		
		try
		{
			row.setValue(col1, null);
		} 
		catch(InvalidNullValueException e) { ex = true; }
		
		assertTrue(ex);
		assertEquals(10, row.getValue(col1));
	}
}
