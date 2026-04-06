package northern.captain.gamecore.glx.tools.loaders;

import com.badlogic.gdx.files.FileHandle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class XMLContentLoader
{
	public XMLContentLoader()
	{
		this(true);
	}
	
	public XMLContentLoader(boolean needNameNodeMap)
	{
		if(needNameNodeMap)
			nameNodeMap =  new HashMap<String, XMLContentLoader.Node>();
	}
	
	public class Node
	{
		public String qname;
		public String name;
		public String value;
		
		public Node sibling;
		public Node child;
		public Node parent;
		
		public Node getFirstSibling()
		{
			if(parent == null)
				return sibling;
			return parent.child;
		}
		
		public class Attrib
		{
			public String name;
			public String value;
			
			Attrib(String name, String value)
			{
				this.name = name;
				this.value = value;
			}
			
			public String getValue() { return value;}
			public int    getValueInt() { return Integer.parseInt(value);}
			public float  getValueFloat() { return Float.parseFloat(value);}
		}
		
		public Map<String, Attrib> attr;
		
		Node(String qname, Attributes attribs)
		{
			this.qname = qname;
			name = attribs.getValue("name");
			int len = attribs.getLength();
			if(len > 0)
				attr = new HashMap<String, XMLContentLoader.Node.Attrib>();
			
			for(int i=0;i<len;i++)
			{
				Attrib att = new Attrib(attribs.getQName(i), attribs.getValue(i));
				attr.put(att.name, att);
			}
		}
	}
	
	public Node root;
	public Map<String, Node> nameNodeMap;
	
	private class ContentHandler extends DefaultHandler
	{
		Node current;
		StringBuilder textBuf;
		
		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			if(textBuf == null)
				textBuf = new StringBuilder();
			textBuf.append(ch, start, length);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException
		{
			super.endDocument();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			current.value = textBuf.toString();
			textBuf = null;
			if(current.parent == null)
				root = current;
			if(nameNodeMap != null && current.name != null)
				nameNodeMap.put(current.name, current);
			current = current.parent;
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
		 */
		@Override
		public void startDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			super.startDocument();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException
		{
			Node node = new Node(qName, attributes);
			if(current != null)
			{
				if(current.child != null)
				{
					node.sibling = current.child;
				}
				node.parent = current;
				current.child = node;
			}
			current = node;
			textBuf = new StringBuilder();
		}
		
	}
	
	public void loadXML(FileHandle fhandle)
	{
		InputStream instream = null;
		Reader reader = null;
		try
		{
			instream = fhandle.read();
			reader = new InputStreamReader(instream, "UTF-8");
			loadXML(reader);
		}
		catch (Exception e)
		{
		}
		finally
		{
			if(reader!=null)
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}		
	}
	
	public void loadXML(String path)
	{
		InputStream instream = null;
		Reader reader = null;
		try
		{
			instream = new FileInputStream(path);
			reader = new InputStreamReader(instream, "UTF-8");
			loadXML(reader);
		}
		catch (Exception e)
		{
		}
		finally
		{
			if(reader!=null)
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}
	}
	
	public void loadXML(Reader reader)
	{
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		
		try 
		{
			 
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();	
			
			saxParser.parse(is, new ContentHandler());
		}
		catch(Exception sex)
		{
			sex.printStackTrace();
		}
	}
	
	public void clear()
	{
		root = null;
		if(nameNodeMap != null)
			nameNodeMap.clear();
	}
	
	public XMLContentLoader.Node getNode(String name)
	{
		return nameNodeMap.get(name);
	}
	
	public Integer getNodeValueInt(String name)
	{
		Integer val = getNodeValueInteger(name);
		return val == null ? 0 : val;
	}
	
	public Integer getNodeValueInteger(String name)
	{
		XMLContentLoader.Node node = getNode(name);
		if(node == null)
			return null;
		if(node.value == null)
			return null;
		return Integer.parseInt(node.value);
	}
	
	public int[] getNodeIntArray(String name)
	{
		XMLContentLoader.Node node = getNode(name);
		if(node == null)
			return new int[0];

		if(node.child == null)
			return new int[0];
		
		int elements = 0;
		for(Node next = node.child; next != null; next = next.sibling)
		{
			elements++;
		}
		
		int[] result = new int[elements--];
		int i = 0;
		for(Node next = node.child; next != null; next = next.sibling, i++)
		{
			if(next.value == null)
				continue;
			Integer val = Integer.parseInt(next.value);
			result[elements - i] = val == null ? 0 : val;
		}
		
		return result;
	}
	
	public int[] getIntArray(String name)
	{
		return getNodeIntArray(name);
	}
}
