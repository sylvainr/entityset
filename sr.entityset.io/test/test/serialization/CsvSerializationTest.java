package test.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import sr.entityset.EntityColumn;
import sr.entityset.EntityTable;
import sr.entityset.io.EntityTableCsvSerializer;

public class CsvSerializationTest 
{
	private static String NewLine = System.getProperty("line.separator");
	
	@Test
	public void testParseCsvFileWorksProperly() throws Exception 
	{
		String csvContent = "\"id\"	\"name\"	\"DOB\"	\"height\"\n" +
		 "\"1\"	\"Jean claude\"	\"12/01/1983 12:00:00 AM\"	\"175.5\"\n" +
		 "\"2\"	\"Paul\"	\"04/01/1982 12:00:00 AM\"	\"163\"\n" +
		 "\"3\"	\"Jacky\"	\"01/25/1949 12:00:00 AM\"	\"180.5\"\n";
		
		StringReader reader = new StringReader(csvContent);
		
		EntityTable table = new EntityTable("table name");
		table.addColumn("id", Integer.class);
		table.addColumn("name", String.class);
		table.addColumn("DOB", DateTime.class);
		table.addColumn("height", Double.class);
		
		EntityTableCsvSerializer serializer = new EntityTableCsvSerializer();
		serializer.parseCsvFile(reader, table, true);
		
		assertEquals(3, table.rows().size());
		assertArrayEquals(new Object[] {1, "Jean claude", date(1983, 12, 1), 175.5}, table.rows().get(0).getObjectArray());
		assertArrayEquals(new Object[] {2, "Paul", date(1982, 4, 1), 163.0}, table.rows().get(1).getObjectArray());
		assertArrayEquals(new Object[] {3, "Jacky", date(1949, 1, 25), 180.5}, table.rows().get(2).getObjectArray());
	}
	
	@Test
	public void testParseCsvFileWithStringColumns() throws IOException
	{
		EntityTable table = new EntityTable("table name");
		
		String csvContent = "\"id\"	\"name\"	\"DOB\"	\"height\"\n" +
		 "\"1\"	\"Jean claude\"	\"12/01/1983 12:00:00 AM\"	\"175.5\"\n" +
		 "\"2\"	\"Paul\"	\"04/01/1982 12:00:00 AM\"	\"163\"\n" +
		 "\"3\"	\"Jacky\"	\"01/25/1949 12:00:00 AM\"	\"180.5\"\n";
		
		StringReader reader = new StringReader(csvContent);
		
		EntityTableCsvSerializer serializer = new EntityTableCsvSerializer();
		serializer.parseCsvFileWithStringColumns(reader, table);
		
		assertEquals(4, table.getColumns().size());
		
		for(EntityColumn col : table.getColumns())
		{
			assertEquals(String.class, col.getType());
			assertFalse(col.getAllowNull());
		}
		
		assertEquals("id", table.getColumns().get(0).getName());
		assertEquals("name", table.getColumns().get(1).getName());
		assertEquals("DOB", table.getColumns().get(2).getName());
		assertEquals("height", table.getColumns().get(3).getName());
		
		assertEquals(3, table.rows().size());
		assertArrayEquals(new Object[] {"1", "Jean claude", "12/01/1983 12:00:00 AM", "175.5"}, table.rows().get(0).getObjectArray());
		assertArrayEquals(new Object[] {"2", "Paul", "04/01/1982 12:00:00 AM", "163"}, table.rows().get(1).getObjectArray());
		assertArrayEquals(new Object[] {"3", "Jacky", "01/25/1949 12:00:00 AM", "180.5"}, table.rows().get(2).getObjectArray());
	}
	
	@Test
	public void testWriteCsvFileWorksProperly() throws Exception {
		EntityTable tableName = new EntityTable("table name");
		
		tableName.addColumn("id", String.class);
		tableName.addColumn("name", String.class);
		
		tableName.addRow(new Object[] {"1", "Jean Dupond"});
		tableName.addRow(new Object[] {"2", "Pierre Durand"});
		
		StringWriter sw = new StringWriter();
		EntityTableCsvSerializer serializer = new EntityTableCsvSerializer();
		serializer.writeCsvFile(tableName, sw, true);
		
		String content = sw.toString();
		String expected = "\"id\"	\"name\"" + NewLine +
		 "\"1\"	\"Jean Dupond\"" + NewLine +
		 "\"2\"	\"Pierre Durand\"" + NewLine;
		
		assertEquals(expected, content);
	}
	
	private DateTime date(int year, int month, int day)
	{
		return new DateTime(year, month, day, 0, 0, 0, 0, DateTimeZone.UTC);
	}
	
}