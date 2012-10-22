/**
 *
 * mmlRpconDepartment.java
 * Created on 2002/7/30 10:0:36
 */
package jp.ac.kumamoto_u.kuh.fc.jsato.math_mml;

import java.awt.*;
import java.util.*;
import org.xml.sax.*;

import java.io.*;
/**
 *
 * @author	Junzo SATO
 * @version
 */
public class mmlRpconDepartment extends MMLObject {
	
	/* fields */
	private String __mmlRpdepCode = null;
	private String __mmlRpdepCodeId = null;

	private String text = null;
	
	public mmlRpconDepartment() {
	}
	
	
	/* print */
	public void printObject(PrintWriter pw, MMLVisitor visitor) throws IOException {
		if ( this.getQName() != null ) {
			visitor.goDown();// adjust tab
			pw.print( visitor.getTabPadding() + "<" + this.getQName() );
			/* print atts */
			if ( __mmlRpdepCode != null ) pw.print(" " + "mmlRp:depCode" +  "=" + "'" + __mmlRpdepCode + "'");
			if ( __mmlRpdepCodeId != null ) pw.print(" " + "mmlRp:depCodeId" +  "=" + "'" + __mmlRpdepCodeId + "'");

			if ( this.getLocalName().equals("Mml") ) {
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
		if (qName.equals("mmlRp:conDepartment") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlRpconDepartment obj = new mmlRpconDepartment();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlRpconDepartment)builder.getElement()).setNamespace( getNamespace() );
			((mmlRpconDepartment)builder.getElement()).setLocalName( getLocalName() );
			((mmlRpconDepartment)builder.getElement()).setQName( getQName() );
			((mmlRpconDepartment)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			if (atts != null) {
				setMmlRpdepCode( atts.getValue(namespaceURI, "depCode") );
				((mmlRpconDepartment)builder.getElement()).setMmlRpdepCode( atts.getValue(namespaceURI, "depCode") );
				setMmlRpdepCodeId( atts.getValue(namespaceURI, "depCodeId") );
				((mmlRpconDepartment)builder.getElement()).setMmlRpdepCodeId( atts.getValue(namespaceURI, "depCodeId") );
			}

			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlRp:conDepartment") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlRp:consultFrom")) {
				((mmlRpconsultFrom)builder.getParent()).setConDepartment((mmlRpconDepartment)builder.getElement());
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
		if (builder.getCurrentElement().getQName().equals("mmlRp:conDepartment")) {
			StringBuffer buffer=new StringBuffer(length);
			buffer.append(ch, start, length);
			setText(buffer.toString());
			((mmlRpconDepartment)builder.getElement()).setText( getText() );
			printlnStatus(parentElement.getQName()+" "+this.getQName()+":"+this.getText());
			return true;
		}
		return false;
	}
	
	
	/* setters and getters */
	public void setMmlRpdepCode(String __mmlRpdepCode) {
		this.__mmlRpdepCode = __mmlRpdepCode;
	}
	public String getMmlRpdepCode() {
		return __mmlRpdepCode;
	}
	public void setMmlRpdepCodeId(String __mmlRpdepCodeId) {
		this.__mmlRpdepCodeId = __mmlRpdepCodeId;
	}
	public String getMmlRpdepCodeId() {
		return __mmlRpdepCodeId;
	}

	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	
}