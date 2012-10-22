/**
 *
 * mmlLbtechnician.java
 * Created on 2003/1/4 2:30:12
 */
package jp.ac.kumamoto_u.kuh.fc.jsato.math_mml_three;

import java.awt.*;
import java.util.*;
import org.xml.sax.*;

import java.io.*;
/**
 *
 * @author	Junzo SATO
 * @version
 */
public class mmlLbtechnician extends MMLObject {
	
	/* fields */
	private String __mmlLbtechCode = null;
	private String __mmlLbtechCodeId = null;

	private String text = null;
	
	public mmlLbtechnician() {
	}
	
	
	/* print */
	public void printObject(PrintWriter pw, MMLVisitor visitor) throws IOException {
		if ( this.getQName() != null ) {
			visitor.goDown();// adjust tab
			pw.print( visitor.getTabPadding() + "<" + this.getQName() );
			/* print atts */
			if ( __mmlLbtechCode != null ) pw.print(" " + "mmlLb:techCode" +  "=" + "'" + __mmlLbtechCode + "'");
			if ( __mmlLbtechCodeId != null ) pw.print(" " + "mmlLb:techCodeId" +  "=" + "'" + __mmlLbtechCodeId + "'");

			if ( this.getLocalName().equals("levelone") ) {
				visitor.printNamespaces(pw);
			}
			pw.print( ">" );
			/* print content */
			// this element need not to print tab padding before the closing tag.
			visitor.setIgnoreTab( true );
			if (text != null) {
				if ( this.getText().equals("") == false ) pw.print( this.getText() );
			}

			// only compound element requires to add tab padding before closing tag
			if ( visitor.getIgnoreTab() == false ) {
				pw.print( visitor.getTabPadding() );
			}
			pw.print( "</" + this.getQName() + ">\n" );
			pw.flush();
			visitor.setIgnoreTab( false );
			visitor.goUp();// adjust tab
		}
	}
	
	public boolean buildStart(String namespaceURI, String localName, String qName, Attributes atts, MMLBuilder builder) {
		if (qName.equals("mmlLb:technician") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlLbtechnician obj = new mmlLbtechnician();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlLbtechnician)builder.getElement()).setNamespace( getNamespace() );
			((mmlLbtechnician)builder.getElement()).setLocalName( getLocalName() );
			((mmlLbtechnician)builder.getElement()).setQName( getQName() );
			((mmlLbtechnician)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			if (atts != null) {
				for (int i=0; i < atts.getLength(); ++i) {
					if ( ((String)atts.getQName(i)).equals("mmlLb:techCode") ) {
						set__mmlLbtechCode( atts.getValue(i) );
						((mmlLbtechnician)builder.getElement()).set__mmlLbtechCode( atts.getValue(i) );
					}
					if ( ((String)atts.getQName(i)).equals("mmlLb:techCodeId") ) {
						set__mmlLbtechCodeId( atts.getValue(i) );
						((mmlLbtechnician)builder.getElement()).set__mmlLbtechCodeId( atts.getValue(i) );
					}
				}
			}

			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlLb:technician") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlLb:information")) {
				((mmlLbinformation)builder.getParent()).set_technician((mmlLbtechnician)builder.getElement());
			}

			
			printlnStatus(parentElement.getQName()+" /"+qName);


			builder.restoreIndex();
			super.buildEnd(namespaceURI,localName,qName,builder);
			return true;
		}
		return false;
	}
	
	/* characters */
	public boolean characters(char[] ch, int start, int length, MMLBuilder builder) {
		if (builder.getCurrentElement().getQName().equals("mmlLb:technician")) {
			StringBuffer buffer=new StringBuffer(length);
			buffer.append(ch, start, length);
			setText(buffer.toString());
			((mmlLbtechnician)builder.getElement()).setText( getText() );
			printlnStatus(parentElement.getQName()+" "+this.getQName()+":"+this.getText());
			return true;
		}
		return false;
	}
	
	
	/* setters and getters */
	public void set__mmlLbtechCode(String __mmlLbtechCode) {
		this.__mmlLbtechCode = __mmlLbtechCode;
	}
	public String get__mmlLbtechCode() {
		return __mmlLbtechCode;
	}
	public void set__mmlLbtechCodeId(String __mmlLbtechCodeId) {
		this.__mmlLbtechCodeId = __mmlLbtechCodeId;
	}
	public String get__mmlLbtechCodeId() {
		return __mmlLbtechCodeId;
	}

	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	
}