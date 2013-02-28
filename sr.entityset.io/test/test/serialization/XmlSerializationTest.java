package test.serialization;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntityTable;
import sr.entityset.io.EntityTableXmlSerializer;

public class XmlSerializationTest {

	@Test
	public void testTableXmlSerialization() throws Exception
	{
		EntityTable table = new EntityTable("TableABC");
		table.addPrimaryKeyColumn("Id", Integer.class);
		table.addColumn("Name", String.class);
		table.addColumn("Size", Double.class, true);
		
		table.addRow(new Object[]{ 1, "Pierre Angel", 25.36});
		table.addRow(new Object[]{ 2, "Paul", 11.2});
		table.addRow(new Object[]{ 3, "Jack", null});
		
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document xmlDocument = docBuilder.newDocument();
        xmlDocument.appendChild(xmlDocument.createElement("root"));
		
        EntityTableXmlSerializer serializer = new EntityTableXmlSerializer();
        serializer.saveTableToXml(table, xmlDocument);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		printXmlDocument(xmlDocument, bos);
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
				"<TableABC><Id>1</Id><Name>Pierre Angel</Name><Size>25.36</Size></TableABC>" +
				"<TableABC><Id>2</Id><Name>Paul</Name><Size>11.2</Size></TableABC>" +
				"<TableABC><Id>3</Id><Name>Jack</Name></TableABC>" +
				"</root>";
		
		String received = new String(bos.toByteArray());
		assertEquals(expected, received);
	}

	@Test
	public void testTableXmlDeserialization() throws Exception
	{
		EntityTable table = new EntityTable("TableABC");
		EntityColumn idCol = table.addPrimaryKeyColumn("Id", Integer.class);
		EntityColumn nameCol = table.addColumn("Name", String.class);
		EntityColumn sizeCol = table.addColumn("Size", Double.class, true);
		
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>" +
			"<TableABC><Id>1</Id><Name>Pierre Angel</Name><Size>25.36</Size></TableABC>" +
			"<TableABC><Id>2</Id><Name>Paul</Name><Size>11.2</Size></TableABC>" +
			"<TableABC><Id>3</Id><Name>Jack</Name></TableABC>" +
			"</root>";
		InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document xmlDocument = docBuilder.parse(is);
		
		EntityTableXmlSerializer serializer = new EntityTableXmlSerializer();
		serializer.fillTableFromXml(table, xmlDocument);
		
		assertEquals(3, table.rows().size());
		
		EntityRow row1 = table.rows().get(0);
		assertEquals(1, row1.getValue(idCol));
		assertEquals("Pierre Angel", row1.getValue(nameCol));
		assertEquals(25.36, row1.getValue(sizeCol));
				
		EntityRow row3 = table.rows().get(2);
		assertEquals(3, row3.getValue(idCol));
		assertEquals("Jack", row3.getValue(nameCol));
		assertNull(row3.getValue(sizeCol));
	}
	
	////////////////////////////////////////////////////////////
	
	private void printXmlDocument(Document xmlDocument, OutputStream outputStream) throws Exception
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(xmlDocument);
	    StreamResult result =  new StreamResult(outputStream);
	    transformer.transform(source, result);
	}
	
}
