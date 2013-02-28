package test.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.NewRowBuilder;

public class NewRowBuilderTest 
{
	private EntityTable table;
	private EntityColumn id1Col;
	private EntityColumn id2Col;

	@Before
	public void before() throws Exception {
		table = new EntityTable("table1");
		id1Col = table.addPrimaryKeyColumn("id1", Integer.class);
		id2Col = table.addPrimaryKeyColumn("id2", Integer.class);
		table.addColumn("name", String.class);
		
		table.setRejectNullViolations(false);
	}
	
	@Test
	public void testNewRowHasProperPrimaryKeyValuesWhenTableWasEmpty() throws Exception 
	{
		NewRowBuilder builder = new NewRowBuilder();
		EntityRow newRow = builder.buildEntityRow(table);
		
		assertEquals(1, newRow.getValue(id1Col));
		assertEquals(1, newRow.getValue(id2Col));
		
		table.addRow(newRow);
	}

	@Test
	public void testNewRowHasProperPrimaryKeyPropertiesWhenPreviousRowsExist() throws Exception 
	{
		table.addRow(1, 2, "first");
		table.addRow(3, 1, "second");
		table.addRow(10, 3, "third");
		
		NewRowBuilder builder = new NewRowBuilder();
		EntityRow newRow = builder.buildEntityRow(table);
		
		assertEquals(11, newRow.getValue(id1Col));
		assertEquals(4, newRow.getValue(id2Col));
		
		table.addRow(newRow);
	}
}
