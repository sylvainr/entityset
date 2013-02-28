package test.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import test.utils.ExceptionAsserter;
import test.utils.ExceptionAsserter.ExceptionAssert;
import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.InvalidRowStateException;
import sr.entityset.RowState;
import sr.entityset.exceptions.InvalidNullValueException;

public class EntityRowStateTest
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
	public void testAddDetachedRow() throws Exception
	{
		final EntityRow row = table.newRow();
		assertEquals(RowState.Detached, row.getState());
		
		row.setValue(idCol, 1);
		row.setValue(sizeCol, 173.5);
		
		ExceptionAsserter.assertException(InvalidNullValueException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception	{
				table.addRow(row);		
			}
		});
		
		assertEquals(RowState.Detached, row.getState());
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		row.setValue(nameCol, "sylvain");
		table.addRow(row);
		assertEquals(RowState.Added, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
	}
	
	@Test
	public void testRemoveAddedRow() throws Exception
	{
		ExceptionAsserter.assertException(InvalidRowStateException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception	{
				table.removeRow(table.newRow());
			}
		});
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		assertEquals(RowState.Added, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		table.removeRow(row);
		assertEquals(RowState.Detached, row.getState());
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
	}
	
	@Test
	public void testModifyAddedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		assertEquals(RowState.Added, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		ExceptionAsserter.assertException(InvalidNullValueException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception	{
				row.setValue(nameCol, null);
			}
		});
		assertEquals(RowState.Added, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		row.setValue(nameCol, "jean claude");
		assertEquals(RowState.Added, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
	}
	
	@Test
	public void testAcceptAddedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		assertEquals(RowState.Added, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
	}
	
	@Test
	public void testAcceptUnchangedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
	}
	
	@Test
	public void testModifyUnchangedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		ExceptionAsserter.assertException(InvalidNullValueException.class, new ExceptionAssert() {
			@Override public void doAction() throws Exception	{
				row.setValue(nameCol, null);
			}
		});
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		row.setValue(nameCol, "hey hey");
		assertEquals(RowState.Modified, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
	}
	
	@Test
	public void testRemoveUnchangedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		table.removeRow(row);
		assertEquals(RowState.Removed, row.getState());
		assertEquals(0, table.rows().size());
		assertEquals(1, table.changedRows().size());
	}
	
	@Test
	public void testAcceptModifiedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
		
		row.setValue(nameCol, "hey hey");
		assertEquals(RowState.Modified, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		table.acceptChanges();
		assertEquals(RowState.Unchanged, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(0, table.changedRows().size());
	}
	
	@Test
	public void testRemoveModifiedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		row.setValue(nameCol, "hey hey");
		assertEquals(RowState.Modified, row.getState());
		assertEquals(1, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		table.removeRow(row);
		assertEquals(RowState.Removed, row.getState());
		assertEquals(0, table.rows().size());
		assertEquals(1, table.changedRows().size());
	}
	
	@Test
	public void testAcceptRemovedRow() throws Exception
	{
		final EntityRow row = table.addRow(1, "sylvain", 173.5);
		table.acceptChanges();
		table.removeRow(row);
		assertEquals(RowState.Removed, row.getState());
		assertEquals(0, table.rows().size());
		assertEquals(1, table.changedRows().size());
		
		table.acceptChanges();
		assertEquals(0, table.rows().size());
		assertEquals(0, table.changedRows().size());
	}
}
