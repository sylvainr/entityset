package test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static test.utils.ExceptionAsserter.assertException;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import test.utils.ExceptionAsserter.ExceptionAssert;
import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.Index;
import sr.entityset.Index.IndexChangedListener;

public class IndexTest 
{
	private EntityTable table;
	private EntityColumn id1Col;
	private EntityColumn id2Col;
	private EntityColumn valCol;

	@Before
	public void beforeEach() throws Exception
	{
		table = new EntityTable("tableName");
		
		id1Col = table.addColumn("id1", Integer.class);
		id2Col = table.addColumn("id2", String.class);
		valCol = table.addColumn("val", String.class);
	}
	
	@Test
	public void testThrowErrorWhenGivingWrongNumberOfValuesForFindRowsAndIndexDontChange() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col};
		final Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		
		assertException(IllegalArgumentException.class, new ExceptionAssert() {
			@Override
			public void doAction() throws Exception {
				index.findRows(1, "b");
			}
		});
		
		changeObserver.assertHasNotChanged();
	}
	
	@Test
	public void testIndexReturnProperRowWithTwoFieldsIndexAndChangesAtRightTime() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		EntityRow row1 = table.addRow(1, "a", "1a");
		EntityRow row2 = table.addRow(2, "b", "2b");
		table.addRow(3, "c", "3c");
		table.addRow(4, "d", "4d");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		
		changeObserver.assertHasNotChanged();
		assertSame(row1, single(index.findRows(1, "a")));
		changeObserver.assertHasChanged().andReset();
		
		assertSame(row2, single(index.findRows(2, "b")));
		assertEquals(0, index.findRows(10, "a").size());
		changeObserver.assertHasNotChanged();
	}

	@Test
	public void testIndexReturnNRowsWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		EntityRow row1 = table.addRow(1, "a", "1a");
		table.addRow(3, "c", "3c");
		table.addRow(4, "d", "4d");
		EntityRow row2 = table.addRow(1, "a", "2b");
		
		Index index = new Index(table, indexCols);
		List<EntityRow> res = index.findRows(1, "a");
		
		assertEquals(2, res.size());
		assertSame(row1, res.get(0));
		assertSame(row2, res.get(1));
	}
	
	@Test
	/**
	 * Ensure that after building the index the first time, 
	 * rows added through the proper method are properly considered by the index 
	 */
	public void testPostCreationRowAddingWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		EntityRow row1 = table.addRow(1, "a", "1a");
		table.addRow(2, "b", "2b");
		table.addRow(3, "c", "3c");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		
		assertSame(row1, single(index.findRows(1, "a")));
		changeObserver.assertHasChanged().andReset();
		
		EntityRow row3 = table.addRow(4, "d", "pof");
		index.updateOnRowAdded(row3);
		changeObserver.assertHasChanged().andReset();
		
		assertSame(row3, single(index.findRows(4, "d")));
		changeObserver.assertHasNotChanged();
	}
	
	@Test
	/**
	 * Ensure rows modified on NON-indexed fields do not trigger a index changed event,
	 * and return the proper row still.
	 */
	public void testPostCreationModifiedRowsOnNonIndexedFielWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "a", "row2");
		EntityRow row3 = table.addRow(3, "c", "3c");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		
		table.addRow(4, "d", "row4");
		changeObserver.assertHasNotChanged();
		
		assertEquals(2, index.findRows(1, "a").size());
		changeObserver.assertHasChanged().andReset();
		
		row3.setValue(valCol, "toto");
		index.updateOnRowModified(row3);
		changeObserver.assertHasNotChanged().andReset();
		
		assertSame(row3, single(index.findRows(3, "c")));
		changeObserver.assertHasNotChanged();
	}
	
	@Test
	/**
	 * Ensure rows modified on indexed field raise index-changed event and are accessible with
	 * the new key-values while former keyed-values do not return the row.
	 */
	public void testPostCreationModifiedRowsOnIndexedFieldWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "a", "row2");
		EntityRow row3 = table.addRow(3, "c", "3c");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		
		table.addRow(4, "d", "row4");
		changeObserver.assertHasNotChanged();
		
		assertEquals(2, index.findRows(1, "a").size());
		changeObserver.assertHasChanged().andReset();
		
		row3.setValue(id2Col, "x");
		index.updateOnRowModified(row3);
		changeObserver.assertHasChanged().andReset();
		
		assertSame(row3, single(index.findRows(3, "x")));
		assertEquals(0, index.findRows(3, "c").size());
		changeObserver.assertHasNotChanged();
	}
	
	@Test
	/**
	 * Ensure rows deleted raises index changed event and are not accessible anymore in index.
	 */
	public void testPostCreationDeletedRowsWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "a", "row2");
		EntityRow row3 = table.addRow(3, "c", "3c");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		
		assertSame(row3, single(index.findRows(3, "c")));
		changeObserver.assertHasChanged().andReset();
		
		table.removeRow(row3);
		index.updateOnRowRemoved(row3);
		changeObserver.assertHasChanged().andReset();
		
		assertEquals(0, index.findRows(3, "c").size());
		changeObserver.assertHasNotChanged();
	}
	
	@Test
	/**
	 * Ensure ContainsDuplicates works on index creation
	 * when a duplicate actually exists.
	 */
	public void testTrueContainsDuplicateWorksOnIndexCreation() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "a", "row2");
		table.addRow(3, "c", "3c");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		index.build();
		changeObserver.assertHasChanged().andReset();
		
		assertTrue(index.containsDuplicates());
	}
	
	@Test
	/**
	 * Ensure ContainsDuplicates works on index creation
	 * when no duplicate is present.
	 */
	public void testFalseContainsDuplicateWorksOnIndexCreation() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "q", "row1");
		table.addRow(1, "a", "row2");
		table.addRow(3, "c", "3c");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		index.build();
		changeObserver.assertHasChanged().andReset();
		
		assertFalse(index.containsDuplicates());
	}
	
	@Test
	/**
	 * Ensure ContainsDuplicates works when a duplicated row is added
	 */
	public void testPostCreationAddingDuplicatedRowWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "q", "row1");
		table.addRow(1, "a", "row2");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		index.build();
		changeObserver.assertHasChanged().andReset();
		
		assertFalse(index.containsDuplicates());
		
		EntityRow row3 = table.addRow(1, "a", "row3");
		index.updateOnRowAdded(row3);
		changeObserver.assertHasChanged().andReset();
		
		assertTrue(index.containsDuplicates());
	}
	
	@Test
	/**
	 * Ensure ContainsDuplicate works when a row which
	 * was not a duplicate is changed so that to become a duplicate.
	 */
	public void testPostCreationRowModificationToCreateDuplicate() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		EntityRow row2 = table.addRow(1, "a", "row2");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		index.build();
		changeObserver.assertHasChanged().andReset();
		
		assertTrue(index.containsDuplicates());
		
		row2.setValue(id1Col, 2);
		index.updateOnRowModified(row2);
		changeObserver.assertHasChanged().andReset();
		
		assertFalse(index.containsDuplicates());
	}
	
	@Test
	/**
	 * Ensure a formerly duplicated index becomes non duplicated when 
	 * the only duplicated row is deleted (but remain duplicated if other
	 * duplicated rows remains).
	 */
	public void testPostCreationRowDeletionToRemoveDuplicationWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		EntityRow row2 = table.addRow(1, "a", "row2");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		index.build();
		changeObserver.assertHasChanged().andReset();
		
		assertTrue(index.containsDuplicates());
		
		table.removeRow(row2);
		index.updateOnRowRemoved(row2);
		changeObserver.assertHasChanged().andReset();
		
		assertFalse(index.containsDuplicates());
	}
	
	@Test
	/**
	 * Ensure a formerly duplicated index becomes non duplicated when 
	 * the only duplicated row is modified on its index keys.
	 */
	public void testPostCreationRowModificationToRemoveDuplicationWorks() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "a", "row2");
		EntityRow row3 = table.addRow(1, "a", "row3");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		index.build();
		changeObserver.assertHasChanged().andReset();
		
		assertTrue(index.containsDuplicates());
		
		table.removeRow(row3);
		index.updateOnRowRemoved(row3);
		changeObserver.assertHasChanged().andReset();
		
		assertTrue(index.containsDuplicates());
	}
	
	@Test
	/**
	 * Ensure ContainsDuplicate = true works even if index was never created before
	 */
	public void testTrueContainsDuplicateWorksEvenOnFirstIndexCreation() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "a", "row2");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		changeObserver.assertHasNotChanged().andReset();
		
		assertTrue(index.containsDuplicates());
		changeObserver.assertHasChanged();
	}
	
	@Test
	/**
	 * Ensure ContainsDuplicate = false works even if index was never created before.
	 */
	public void testFalseContainsDuplicateWorksEvenOnFirstIndexCreation() throws Exception
	{
		EntityColumn[] indexCols = new EntityColumn[] {id1Col, id2Col};
		
		table.addRow(1, "a", "row1");
		table.addRow(1, "b", "row2");
		
		Index index = new Index(table, indexCols);
		IndexChangeObserver changeObserver = new IndexChangeObserver(index);
		changeObserver.assertHasNotChanged().andReset();
		
		assertFalse(index.containsDuplicates());
		changeObserver.assertHasChanged();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private <T> T single(List<T> list) throws Exception
	{
		if (list.size() != 1) throw new Exception("Expecting exactly one row.");
		return list.get(0);
	}
	
	private class IndexChangeObserver implements IndexChangedListener 
	{
		private boolean changed = false;
		
		public IndexChangeObserver(Index index) {
			index.addIndexChangedListener(this);
		}

		@Override
		public void indexChanged() {
			this.changed = true;
		}
		
		public void reset() {
			this.changed = false;
		}
		
		public IndexChangeObserver assertHasChanged() { assertTrue(this.changed); return this; }
		public IndexChangeObserver assertHasNotChanged() { assertFalse(this.changed); return this; }
		public void andReset() { this.reset(); }
	}
}
