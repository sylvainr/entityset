package sr.entityset.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sr.entityset.EntityColumn;
import sr.entityset.EntityRow;
import sr.entityset.EntitySet;
import sr.entityset.EntityTable;
import sr.entityset.io.converters.ISOFormatJodaDateTimeConverter;
import sr.entityset.io.converters.StringEntityTableTypeConverter;

public class EntityTableXmlSerializer 
{
	protected static boolean SKIP_NULL_FIELDS = true;
	protected static StringEntityTableTypeConverter stringEntityTableTypeConverter = 
			new StringEntityTableTypeConverter(new ISOFormatJodaDateTimeConverter());

	public EntityTableXmlSerializer() {
		super();
	}

	public static void loadWholeEntitySetFromStream(EntitySet entitySet, InputStream inputStream) 
	{
		EntityTableXmlSerializer serializer = new EntityTableXmlSerializer();
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document xmlDocument = docBuilder.parse(inputStream);

			for(EntityTable table : entitySet.getTables())
				serializer.fillTableFromXml(table, xmlDocument);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void saveWholeEntitySetToStream(
			EntitySet entitySet, 
			OutputStream outputStream) 
	{
		saveWholeEntitySetToStream(entitySet, outputStream, null);
	}
	
	protected static void saveWholeEntitySetToStream(
			EntitySet entitySet, 
			OutputStream outputStream,
			IXmlDocumentModifier preFlushModifier) 
	{
		try
		{
			EntityTableXmlSerializer xmlSerializer = new EntityTableXmlSerializer();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document newXmlDocument = docBuilder.newDocument();
			newXmlDocument.setXmlStandalone(true);

			Node rootNode = newXmlDocument.appendChild(newXmlDocument.createElement("EntityDS"));
			
			Element schemaVersionNode = newXmlDocument.createElement("SchemaVersion");
			Element versionNode = newXmlDocument.createElement("Version");
			versionNode.appendChild(newXmlDocument.createTextNode(Integer.toString(entitySet.getSchemaVersion())));
			schemaVersionNode.appendChild(versionNode);

			rootNode.appendChild(schemaVersionNode);

			for (EntityTable curTable : entitySet.getTables()) {
				xmlSerializer.saveTableToXml(curTable, newXmlDocument);
			}
			
			if (preFlushModifier != null)
				preFlushModifier.modify(newXmlDocument);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			DOMSource source = new DOMSource(newXmlDocument);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void saveTableToXml(EntityTable table, Document xmlDocument) throws Exception 
	{
		Node rootElement = xmlDocument.getFirstChild();

		for (EntityRow row : table.rows())
		{
			Element rowNode = xmlDocument.createElement(table.getName());

			for (EntityColumn column : table.getColumns())
			{
				Object value = row.getValue(column);

				if (SKIP_NULL_FIELDS && value == null)
					continue;

				Element valueNode = xmlDocument.createElement(column.getName());
				valueNode.appendChild(xmlDocument.createTextNode(
						stringEntityTableTypeConverter.
						convertToString(row.getValue(column))));
				rowNode.appendChild(valueNode);
			}

			rootElement.appendChild(rowNode);
		}
	}

	public void fillTableFromXml(EntityTable table, Document xmlDocument) throws Exception 
	{
		Hashtable<String, EntityColumn> columnsIndex = 
				new Hashtable<String, EntityColumn>();
		for (EntityColumn column : table.getColumns())
			columnsIndex.put(column.getName(), column);

		NodeList rowNodes = xmlDocument.getElementsByTagName(table.getName());
		int count = rowNodes.getLength();
		for (int i = 0; i < count; i++)
		{
			Node curNode = rowNodes.item(i);
			EntityRow newRow = table.newRow();

			NodeList childNodes = curNode.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++)
			{
				Node curChildNode = childNodes.item(j);
				EntityColumn column = columnsIndex.get(curChildNode.getNodeName());
				if (column != null)
				{
					String textValue = curChildNode.getTextContent();
					Object value = stringEntityTableTypeConverter.
							parseFromString(textValue, column.getType(), column.getAllowNull());

					try
					{
						newRow.setValue(column, value);
					} 
					catch (Exception e) {
						throw new Exception("Error while reading value '" + textValue 
								+ "' for column " + column.getName() 
								+ " in table " + table.getName(), e);
					}
				}
			}

			table.addRow(newRow);
		}
	}
	
	protected static interface IXmlDocumentModifier {
		void modify(Document xmlDocument);
	}
}