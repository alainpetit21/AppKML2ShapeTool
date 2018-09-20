package com.bianisoft.tools.kml2Sshapetool;

import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.xml.sax.*;
import java.io.*;
import java.util.ArrayList;


public class AppKML2ShapeTool{
	class MyContentHandler implements ContentHandler{
		String m_stTemp= new String();
		boolean m_isEditingString= false;
		
		public MyContentHandler()	{	}

		public void characters(char[] p_stText, int p_nStart, int p_nLength)	throws SAXException{
			if(m_isEditingString)
				m_stTemp= String.copyValueOf(p_stText, p_nStart, p_nLength);
		}
		public void setDocumentLocator(Locator locator)	{	}
		public void startDocument()	{	}
		public void endDocument()	{	}
		public void startPrefixMapping(String prefix, String uri)	{	}
		public void endPrefixMapping(String prefix)	{	}
		public void startElement(String p_stNamespaceURI, String p_stLocalName, String p_stQualifiedName, Attributes p_atts){
			if(p_stLocalName.equals("coordinates")){
				m_isEditingString= true;
			}
		}
		public void endElement(String p_stNamespaceURI, String p_stLocalName, String p_stQualifiedName){
			if(p_stLocalName.equals("coordinates")){
				m_arListCoordinate.add(m_stTemp);
				m_isEditingString= false;
				m_nNbCoordinate++;
			}
		}
		public void ignorableWhitespace(char[] text, int start, int length) throws SAXException	{	}
		public void processingInstruction(String target, String data)	{	}
		public void skippedEntity(String name)	{	}
	}


	ArrayList<String>	m_arListCoordinate= new ArrayList<String>();

	String	m_stFilenameIn;
	String	m_stFilenameOut;

	int		m_nNbCoordinate= 0;
	int		m_nShapeID= 0;

	
	public AppKML2ShapeTool(String p_stTiledMapIn){
		m_stFilenameIn= p_stTiledMapIn;

		System.out.print("\nStarting Conversion of Tiled map " + m_stFilenameIn);
	}

	public void doRead() throws SAXException, IOException{
		System.out.print("\nStarting Reading" + m_stFilenameIn);

		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(new MyContentHandler());
		parser.parse(m_stFilenameIn);
		
		System.out.println(m_stFilenameIn + " is well-formed.");
	}

	public void doManage() throws SAXException, IOException{
		m_stFilenameOut= m_stFilenameIn.substring(0, m_stFilenameIn.indexOf(".kml")) + ".txt";
		
		//TODO - popup dialog to ask for an ShapeID
		m_nShapeID= 4;
	}

	public void doSave() throws IOException{
		String line;
		System.out.print("\nSaving " + m_stFilenameOut);

		FileOutputStream file	= new FileOutputStream(m_stFilenameOut);
		DataOutputStream dos	= new DataOutputStream(file);
		
		dos.writeBytes("shape_id,shape_pt_lon,shape_pt_lat,shape_pt_sequence\r\n");
		for(int i= 0; i < m_nNbCoordinate; ++i){
			String stCoor=  m_arListCoordinate.get(i);
			stCoor= stCoor.substring(0, stCoor.length()-2);
			
			line= String.format("%d,%s,%d\r\n", m_nShapeID, stCoor, i+1);
			dos.writeBytes(line);
		}

		dos.close();
		file.close();
	}
	
    public static void main(String[] args){
		String	stFilename;

		if(args.length < 1){
			JFileChooser fc= new JFileChooser(".");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Google maps KML file", "kml");

			fc.setFileFilter(filter);
			if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return;

			stFilename= fc.getSelectedFile().getAbsolutePath();
		}else{
			stFilename= args[0];
		}

		try{
			AppKML2ShapeTool app= new AppKML2ShapeTool(stFilename);
			app.doRead();
			app.doManage();
			app.doSave();
		}catch(Exception e){
			System.out.print(e);
		}
    }
}
