package test.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;

public class InheritanceTest {

	BusTable buses;
	BusRow bus1;
	BusRow bus2;
	
	@Before
	public void doBefore() throws Exception 
	{
		buses = new BusTable();
		bus1 = buses.AddBusRow(1, "Bus1", 60.0);
		bus2 = buses.AddBusRow(2, "Bus2", 150.0);
	}
	
	@Test
	public void testBusRowValuesAreCorrect() throws Exception
	{	
		assertEquals((Integer)1, bus1.getBusId());
		assertEquals("Bus1", bus1.getName());
		assertEquals((Double)60.0, bus1.getSpeed());
		
		assertEquals((Integer)2, bus2.getBusId());
		assertEquals("Bus2", bus2.getName());
		assertEquals((Double)150.0, bus2.getSpeed());
	}
	
	@Test
	public void testBusRowsMethodOnTable() throws Exception
	{
		List<BusRow> busRows = buses.BusRows();
		
		assertEquals(2, busRows.size());
		assertEquals(bus1, busRows.get(0));
		assertEquals(bus2, busRows.get(1));
	}
	
	@Test
	public void testFindByBusIdMethod() throws Exception
	{
		assertEquals(bus1, buses.FindByBusId(1));
		assertEquals(bus2, buses.FindByBusId(2));
	}
	
	//
	// The following classes should be automatically generated in real life 
	//
	class BusTable extends EntityTable 
	{
		private EntityColumn busIdColumn;
		private EntityColumn nameColumn;
		private EntityColumn speedColumn;
		
		public BusTable() throws Exception {
			super("Bus");
			
			this.busIdColumn = this.addPrimaryKeyColumn("BusId", Integer.class);
			this.nameColumn = this.addColumn("Name", String.class);
			this.speedColumn = this.addColumn("Speed", Double.class);
		}
		
		@SuppressWarnings("unchecked")
		public List<BusRow> BusRows()
		{
			return (List<BusRow>)(List<?>) this.rows();
		}
		
		public BusRow newRow()
		{
			return new BusRow(this);
		}
		
		public BusRow AddBusRow(Integer busId, String name, Double speed) throws Exception
		{
			Object[] values = new Object[] { busId, name, speed };
			return (BusRow) this.addRow(values);
		}
		
		public BusRow FindByBusId(Integer busId) throws Exception
		{
			Object[] pkValues = new Object[] {busId};
			return (BusRow)this.findByPrimaryKey(pkValues);
		}
		
		public EntityColumn getBusIdColumn(){
			return this.busIdColumn;
		}
		
		public EntityColumn getSpeedColumn(){
			return this.speedColumn;
		}
		
		public EntityColumn getNameColumn(){
			return this.nameColumn;
		}
	}
	
	class BusRow extends EntityRow 
	{
		public BusRow(BusTable parentTable) {
			super(parentTable);
		}
		
		private BusTable getBusTable() {
			return (BusTable)this.getParentTable();
		}
		
		public Integer getBusId() {
			return (Integer)this.getValue(this.getBusTable().getBusIdColumn());
		}
		
		public String getName() {
			return (String)this.getValue(this.getBusTable().getNameColumn());
		}
		
		public Double getSpeed() {
			return (Double)this.getValue(this.getBusTable().getSpeedColumn());
		}
		
		public void setBusId(Integer value) throws Exception {
			this.setValue(this.getBusTable().getBusIdColumn(), value);
		}
		
		public void setName(String value) throws Exception {
			this.setValue(this.getBusTable().getNameColumn(), value);
		}
		
		public void setSpeed(Double value) throws Exception {
			this.setValue(this.getBusTable().getSpeedColumn(), value);
		}
	}
}
