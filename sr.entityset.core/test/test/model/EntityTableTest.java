package test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import test.utils.ExceptionAsserter;
import test.utils.ExceptionAsserter.ExceptionAssert;
import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.exceptions.InvalidNullValueException;
import sr.entityset.exceptions.PrimaryKeyConstraintException;

public class EntityTableTest 
{
	@Test
	public void testAddColumnMethod() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		assertEquals(0, table.getColumns().size());
		
		EntityColumn col1 = table.addColumn("id", Integer.class);
		EntityColumn col2 = table.addColumn("name", String.class);
		assertEquals(2, table.getColumns().size());
		
		List<EntityColumn> columns = table.getColumns();
		assertEquals(col1, columns.get(0));
		assertEquals(col2, columns.get(1));
	}

	@Test
	public void testAddedRowIsSimplyRemovedFromAllRowsCollectionWhenRemoved() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addColumn("id", Integer.class, true);
		table.addColumn("name", String.class, true);
		
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		EntityRow row = table.newRow();
		table.addRow(row);
		
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		table.removeRow(row);
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
	}
	
	@Test
	public void testNormalRowDeletedShouldBeRemovedFromRowsButNotAllRowsMethods() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addColumn("id", Integer.class, true);
		table.addColumn("name", String.class, true);
		
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		EntityRow row = table.newRow();
		table.addRow(row);
		table.acceptChanges();
		
		table.removeRow(row);
		assertEquals(0, table.rows().size());
		assertEquals(1, table.changedRows().size());
	}
	
	@Test
	public void testOneFieldPrimaryKeyQuery() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addColumn("name", String.class);
		
		// Add rows
		EntityRow row1 = table.addRow(new Object[] {1, "toto"});
		table.addRow(new Object[] {2, "titi"});
		EntityRow row3 = table.addRow(new Object[] {10, "tata"});
		
		assertEquals(row1, table.findByPrimaryKey(1));
		assertEquals(row3, table.findByPrimaryKey(10));
		assertNull(table.findByPrimaryKey(50));
	}
	
	@Test
	public void testTwoFieldsPrimaryKeyQuery() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addPrimaryKeyColumn("name", String.class);
		table.addColumn("size", Double.class);
		
		// Add rows
		EntityRow row1 = table.addRow(new Object[] {1, "toto", 25.2});
		table.addRow(new Object[] {2, "titi", 12.4});
		EntityRow row3 = table.addRow(new Object[] {10, "tata", 30.6});
		
		assertEquals(row1, table.findByPrimaryKey(new Object[] {1, "toto"}));
		assertEquals(row3, table.findByPrimaryKey(new Object[] {10, "tata"}));
		assertNull(table.findByPrimaryKey(new Object[] {10, "titi"}));
	}
	
	@Test
	public void testUpdatingAnIndexFieldIsTakenCareOf() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		EntityColumn col1 = table.addPrimaryKeyColumn("id", Integer.class);
		table.addPrimaryKeyColumn("name", String.class);
		table.addColumn("size", Double.class);
		
		EntityRow row1 = table.addRow(new Object[] {1, "toto", 25.2});
		table.addRow(new Object[] {1, "titi", 30.2});
		
		assertSame(row1, table.findByPrimaryKey(new Object[] {1, "toto"}));
		assertNull(table.findByPrimaryKey(new Object[] {50, "toto"}));
		
		// Now change a value for the primary key of the field. 
		row1.setValue(col1, 50);
		assertNull(table.findByPrimaryKey(new Object[] {1, "toto"}));
		assertSame(row1, table.findByPrimaryKey(new Object[] {50, "toto"}));
		
		// Test that I still get the constraint error if I try to add a row with id = 50, name = "toto"
		
		// Should work
		table.addRow(new Object[] {1, "toto", 52.3});
		
		// Should raise an error
		boolean ex = false;
		try {
			table.addRow(new Object[] {50, "toto", 54.8});
		} 
		catch(PrimaryKeyConstraintException e) { ex = true; }
		assertTrue(ex);
	}
	
	@Test
	public void testRemovingRowDoesNotMessupIndex() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addPrimaryKeyColumn("name", String.class);
		table.addColumn("size", Double.class);
		
		EntityRow row1 = table.addRow(new Object[] {1, "toto", 25.2});
		table.addRow(new Object[] {1, "titi", 30.2});
		
		assertEquals(row1, table.findByPrimaryKey(new Object[] {1, "toto"}));
		
		table.removeRow(row1);
		
		assertNull(table.findByPrimaryKey(new Object[] {1, "toto"}));
		
		row1 = table.addRow(new Object[] {1, "toto", 25.2});
		assertEquals(row1, table.findByPrimaryKey(new Object[] {1, "toto"}));
	}

	@Test
	public void testAddRowWithWrongNullValueShouldRaiseError() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addColumn("name", String.class, false);
	
		// should work
		table.addRow(new Object[] {1, "toto"});
		
		// should not work
		boolean ex = false;
		try {
			table.addRow(new Object[] {2, null});	
		} 
		catch(InvalidNullValueException e) { ex = true; }
		
		assertTrue(ex);
		assertEquals(1, table.rows().size());
	}
	
	@Test
	public void testRowsMethodIsalwaysTheSameInstance() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addColumn("name", String.class, false);

		table.addRow(new Object[] {1, "toto"});
		table.addRow(new Object[] {2, "titi"});
		
		List<EntityRow> rows = table.rows();
		assertEquals(2, rows.size());
		
		table.addRow(new Object[] {3, "tutu"});
		assertEquals(3, rows.size());
		
		assertTrue(rows == table.rows());
	}
	
	@Test
	public void testAddRowWithObjectArrayWithWrongFieldTypeShouldFail() throws Exception
	{
		EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addColumn("name", String.class, false);
		table.addColumn("age", Integer.class, false);
		
		table.addRow(new Object[] {1, "Paul", 28});
		
		boolean ex = false;
		try {
			table.addRow(new Object[] {2, "Pierre", "wrong type"});
		} 
		catch(Exception e)
		{
			ex = true;
		}
		assertTrue(ex);
	}
	
	@Test
	public void testColumnAdditionIsNotPossibleIfDataHasBeenAddedToTable() throws Exception
	{
		final EntityTable table = new EntityTable("table1");
		table.addPrimaryKeyColumn("id", Integer.class);
		table.addColumn("name", String.class, false);
		
		table.addRow(new Object[] {1, "Paul"});
		
		ExceptionAsserter.assertException(IllegalStateException.class, new ExceptionAssert() {
			@Override
			public void doAction() throws Exception {
				table.addColumn("age", Integer.class, false);
			}
		});
	}
	
	@Test
	public void testNullPrimaryKeyIsNotAllowedDespiteNonRejectionOfNullViolations() throws Exception 
	{
		final EntityTable table = new EntityTable("table1");
		final EntityColumn pkCol = table.addPrimaryKeyColumn("id", Integer.class);
		table.addColumn("name", String.class, false);
		table.setRejectNullViolations(false);
		
		table.addRow(1, "toto");
		table.addRow(2, "tutu");
		
		ExceptionAsserter.assertException(InvalidNullValueException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception {
				table.addRow(null, "titi");
			}
		});
		
		assertEquals(2, table.rows().size());
		
		final EntityRow row = table.addRow(3, "titi");
		ExceptionAsserter.assertException(InvalidNullValueException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception {
				row.setValue(pkCol, null);
			}
		});
	}
}
