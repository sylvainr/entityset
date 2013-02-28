package sr.entityset.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.io.converters.DateTimeFormatterJodaDateTimeConverter;
import sr.entityset.io.converters.StringEntityTableTypeConverter;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class EntityTableCsvSerializer 
{
	private StringEntityTableTypeConverter converter;

	public EntityTableCsvSerializer() {
		this("MM/dd/yyyy hh:mm:ss a");
	}
	
	public EntityTableCsvSerializer(String jodaTimeDateFormat) {
		this.converter = new StringEntityTableTypeConverter(new DateTimeFormatterJodaDateTimeConverter(jodaTimeDateFormat));
	}
	
	public void writeCsvFile(EntityTable table, Writer writer, boolean includeHeader)
	{
		CsvWriter csvWriter = new CsvWriter(writer, '\t');
		try 
		{
			csvWriter.setTextQualifier('"');
			csvWriter.setForceQualifier(true);
			
			if (includeHeader)
			{
				for (EntityColumn col : table.getColumns()) {
					csvWriter.write(col.getName());
				}
				csvWriter.endRecord();
			}

			// Now write the row content
			for (EntityRow row : table.rows()) 
			{
				for (EntityColumn col : table.getColumns()) {
					csvWriter.write(this.converter.convertToString(row.getValue(col)));
				}
				csvWriter.endRecord();
			}
		}
		catch(Exception ex)	{
			throw new RuntimeException("Error while writing csv", ex);
		}
		finally {
			csvWriter.close();
		}
	}
	
	/**
	 * This method assume that given table is ready to be fill:
	 * - it has a name.
	 * - it has columns.
	 */
	public void parseCsvFile(Reader reader, EntityTable table, boolean skipFirstRow)
	{
		CsvReader csvReader = new CsvReader(reader, '\t');
		csvReader.setTextQualifier('"');
		
		try 
		{
			if (skipFirstRow)
				csvReader.readHeaders();
			
			fillTableContent(csvReader, table);
		} 
		catch(Exception ex) {
			throw new RuntimeException("Error while trying to read csv: " + ex.getMessage(), ex);
		}
	}

	public void parseCsvFileWithStringColumns(Reader reader, EntityTable table)
	{
		CsvReader csvReader = new CsvReader(reader, '\t');
		csvReader.setTextQualifier('"');
		
		try 
		{
			addStringColumnsFromHeader(csvReader, table);
			fillTableContent(csvReader, table);
		} 
		catch(Exception ex) {
			throw new RuntimeException("Error while trying to read csv: " + ex.getMessage(), ex);
		}
	}
	
	private void addStringColumnsFromHeader(CsvReader csvReader, EntityTable table)
	{
		try
		{
			if (csvReader.readHeaders() == false)
				throw new Exception("Header not readable, so can not create table.");

			String[] headers = csvReader.getHeaders();
			for (String header : headers)		
				table.addColumn(header, String.class, false);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void fillTableContent(CsvReader csvReader, EntityTable table) throws IOException, Exception 
	{
		int colCount = table.getColumns().size();
		
		while(csvReader.readRecord())
		{
			String[] readArray = csvReader.getValues();
			Object[] rowObjectArray = new Object[colCount];
			
			for (EntityColumn col : table.getColumns()) 
			{
				int index = col.getNumber();
				try
				{
					rowObjectArray[index] = this.converter.parseFromString(readArray[index], col.getType(), col.getAllowNull());	
				}
				catch(Exception ex) {
					throw new RuntimeException("Error while parsing '" + readArray[index] + "' into " + table.getName() + " column " + col.getName(), ex);
				}
			}
			
			try {
				table.addRow(rowObjectArray);
			}
			catch(Exception ex) {
				throw new Exception("Error while trying to add row " + rowObjectArray.toString() + ": " + ex.getMessage(), ex);
			}
		}
	}
}