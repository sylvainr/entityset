package test.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.RowState;
import test.utils.ExceptionAsserter;
import test.utils.ExceptionAsserter.ExceptionAssert;

public class EntityRowOriginalValuesTest
{	
	private EntityTable table;
	private EntityColumn idCol;
	private EntityColumn nameCol;
	private EntityColumn sizeCol;

	@Before
	public void before() throws Exception 
	{
		table = new EntityTable("tableName");
		idCol = table.addPrimaryKeyColumn("id", Integer.class);
		nameCol = table.addColumn("name", String.class);
		sizeCol = table.addColumn("size", Double.class, true);
	}

	@Test
	public void testOriginalValuesThrowsExceptionWhenInDetachedState() throws Exception
	{
		final EntityRow row = table.newRow();
		assertEquals(RowState.Detached, row.getState());
		
		ExceptionAsserter.assertException(IllegalStateException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception {
				row.getOriginalValue(0);
			}
		});
	}
	
	@Test
	public void testOriginalValuesThrowsExceptionWhenInAddedState() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		assertEquals(RowState.Added, row.getState());
		
		ExceptionAsserter.assertException(IllegalStateException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception {
				row.getOriginalValue(0);
			}
		});
	}
	
	@Test
	public void testOriginalValuesThrowsExceptionWhenInUnchangedState() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		
		ExceptionAsserter.assertException(IllegalStateException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception {
				row.getOriginalValue(0);
			}
		});
	}
	
	@Test
	public void testOriginalValuesAreProperlySetAndUnSet() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		
		row.setValue(idCol, 5);
		row.setValue(nameCol, "polo");
		
		assertEquals(RowState.Modified, row.getState());
				
		assertEquals(1, row.getOriginalValue(idCol));
		assertEquals("sylvain", row.getOriginalValue(nameCol));
		assertEquals(173.5, row.getOriginalValue(sizeCol));
		
		assertEquals(5, row.getValue(idCol));
		assertEquals("polo", row.getValue(nameCol));
		assertEquals(173.5, row.getValue(sizeCol));
		
		table.acceptChanges();
		ExceptionAsserter.assertException(IllegalStateException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception {
				row.getOriginalValue(0);
			}
		});
		
		row.setValue(nameCol, "jojo");
		
		assertEquals(5, row.getOriginalValue(idCol));
		assertEquals("polo", row.getOriginalValue(nameCol));
		assertEquals(173.5, row.getOriginalValue(sizeCol));
		
		assertEquals(5, row.getValue(idCol));
		assertEquals("jojo", row.getValue(nameCol));
		assertEquals(173.5, row.getValue(sizeCol));
	}
}
