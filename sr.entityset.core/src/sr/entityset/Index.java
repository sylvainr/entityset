package sr.entityset;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import sr.entityset.utils.ArrayHelper;

public class Index
{
	private EntityColumn[] columns;
	private Hashtable<String, List<EntityRow>> indexHashTable;
	private Hashtable<EntityRow, String> rowRefToStringKeyTable;
	private EntityTable table;
	private Boolean containsDuplicates = null;
	private boolean built = false;
	private final boolean unique;
	
	private ArrayList<IndexChangedListener> indexChangedListeners = 
			new ArrayList<IndexChangedListener>();
	
	public Index(EntityTable table, EntityColumn... columns) {
		this(table, false, columns);
	}
	
	public Index(EntityTable table, boolean unique, EntityColumn... columns)
	{
		if (columns == null) throw new NullArgumentException("columns");
		if (table == null) throw new NullArgumentException("table");
		
		if (columns.length == 0) 
			throw new IllegalArgumentException("given columns set must have at least one column.");
		
		if (ArrayHelper.isUnique(columns) == false) 
			throw new IllegalArgumentException("At least one column is passed twice " +
					"in the input columns set.");
		
		this.unique = unique;
		this.table = table;
		
		for (EntityColumn entityColumn : columns) {
			if (entityColumn.getTable() != this.table)
				throw new IllegalArgumentException(
						"given columns does not belong to the same table (found " 
						+ entityColumn.getName() + " and " + this.table.getName() + ")");
		}
		
		this.columns = columns;
	}
	
	public List<EntityRow> findRows(Object... values)
	{
		if (values.length != this.columns.length)
			throw new IllegalArgumentException("Expecting '" 
					+ this.columns.length + "' values but received '" 
					+ values.length + "'.");
		
		String key = buildKey(values);
		List<EntityRow> rows = this.getIndexHashtable().get(key);
		
		if (rows == null)
			return new ArrayList<EntityRow>();
		
		return rows;
	}
	
	public void build()
	{
		this.indexHashTable = new Hashtable<String, List<EntityRow>>();
		this.rowRefToStringKeyTable = new Hashtable<EntityRow, String>();
		
		for (EntityRow row : this.table.rows()) 
		{
			String key = buildKey(row);
			addRow(key, row, this.indexHashTable, this.rowRefToStringKeyTable);			
		}
		
		this.built = true;
		this.fireIndexChangedEvent();
	}
	
	public void updateOnRowAdded(EntityRow row)
	{
		if (this.built == false) return;
		
		String key = buildKey(row);
		int similarRowsCount = addRow(key, row, 
				this.indexHashTable, 
				this.rowRefToStringKeyTable);
		if (similarRowsCount > 1)
			this.containsDuplicates = true;
		
		this.fireIndexChangedEvent();
	}
	
	public void updateOnRowModified(EntityRow row)
	{
		if (this.built == false) return;
		
		String oldKey = this.getRowRefToStringKeyTable().get(row);
		if (oldKey == null) throw new RuntimeException("unexpected null key for given row.");
		
		String newKey = buildKey(row);

		boolean keyHasChanged = (oldKey.equals(newKey) == false);
		if (keyHasChanged)
		{
			removeRow(row);
			int similarRowsCount = addRow(
					newKey, 
					row, 
					this.getIndexHashtable(), 
					this.getRowRefToStringKeyTable());
			
			if (similarRowsCount > 1)
				this.containsDuplicates = true;
			else
				this.containsDuplicates = null;
			
			this.fireIndexChangedEvent();
		}
	}
	
	public void updateOnRowRemoved(EntityRow row)
	{
		if (this.built == false) return;
		
		removeRow(row);
		this.containsDuplicates = null;
		
		this.fireIndexChangedEvent();
	}

	public boolean containsDuplicates()
	{
		if (this.containsDuplicates == null)
		{
			this.containsDuplicates = false;
			
			for(List<EntityRow> rowList : this.getIndexHashtable().values())
			{
				if (rowList.size() > 1)
				{
					this.containsDuplicates = true;
					break;
				}
			}
		}
		
		return this.containsDuplicates;
	}
	
	public void addIndexChangedListener(IndexChangedListener listener)	{
		this.indexChangedListeners.add(listener);
	}
	
	public void removeIndexChangedListener(IndexChangedListener listener)	{
		this.indexChangedListeners.remove(listener);
	}
	
	public EntityColumn[] getColumns() {
		return this.columns;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////

	private void fireIndexChangedEvent()
	{
		for (IndexChangedListener listener : this.indexChangedListeners) {
			listener.indexChanged();
		}
	}
	
	private void removeRow(EntityRow row)
	{
		String rowKey = this.rowRefToStringKeyTable.get(row);
		
		List<EntityRow> rows = this.getIndexHashtable().get(rowKey);
		if (rows == null) throw new RuntimeException(
				"Unexpected null row list for key " + rowKey);
		
		rows.remove(row);
		this.rowRefToStringKeyTable.remove(row);
	}

	private static int addRow(
			String key, 
			EntityRow row, 
			Hashtable<String, 
			List<EntityRow>> indexHash, 
			Hashtable<EntityRow, String> rowRefToStringKeyTable)
	{
		List<EntityRow> rows = indexHash.get(key);
		if (rows == null)
		{
			rows = new ArrayList<EntityRow>();
			indexHash.put(key, rows);
		}
		
		rowRefToStringKeyTable.put(row, key);
		
		rows.add(row);
		return rows.size();
	}
	
	private Hashtable<String, List<EntityRow>> getIndexHashtable()
	{
		if (this.built == false)
			this.build();
			
		return this.indexHashTable;
	}
	
	private Hashtable<EntityRow, String> getRowRefToStringKeyTable() 
	{
		if (this.built == false)
			this.build();
		
		return rowRefToStringKeyTable;
	}
	
	private String buildKey(EntityRow row)
	{
		Object[] keyValues = new Object[this.columns.length];
		
		for (int i = 0; i < this.columns.length; i++) {
			EntityColumn keyColumn = this.columns[i];
			keyValues[i] = row.getValue(keyColumn);
		}
		
		return buildKey(keyValues);
	}
	
	private static String buildKey(Object[] values)
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < values.length; i++) {
			sb.append("||" + String.valueOf(values[i]) + ";;");
		}
		
		return sb.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean isUnique() {
		return unique;
	}

	public interface IndexChangedListener {
		void indexChanged();
	}
}
