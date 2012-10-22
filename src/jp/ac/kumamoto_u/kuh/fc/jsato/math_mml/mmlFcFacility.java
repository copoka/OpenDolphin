/**
 *
 * mmlFcFacility.java
 * Created on 2002/7/30 10:0:24
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
public class mmlFcFacility extends MMLObject {
	
	/* fields */
	private Vector _name = new Vector();
	private mmlCmId _Id = null;
	
	public mmlFcFacility() {
	}
	
	
	/* print */
	public void printObject(PrintWriter pw, MMLVisitor visitor) throws IOException {
		if ( this.getQName() != null ) {
			visitor.goDown();// adjust tab
			pw.print( visitor.getTabPadding() + "<" + this.getQName() );
			/* print atts */
			if ( this.getLocalName().equals("Mml") ) {
				visitor.printNamespaces(pw);
			}
			pw.print( ">" );
			/* print content */
			if (this._name != null) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				// print each element in the vector assumming that it doesn't contain String object...
				for (int i = 0; i < this._name.size(); ++i ) {
					((mmlFcname)this._name.elementAt(i)).printObject(pw, visitor);
				}
			}
			if ( _Id != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_Id.printObject(pw, visitor);
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
		if (qName.equals("mmlFc:Facility") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlFcFacility obj = new mmlFcFacility();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlFcFacility)builder.getElement()).setNamespace( getNamespace() );
			((mmlFcFacility)builder.getElement()).setLocalName( getLocalName() );
			((mmlFcFacility)builder.getElement()).setQName( getQName() );
			((mmlFcFacility)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlFc:Facility") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlRe:referToFacility")) {
				((mmlRereferToFacility)builder.getParent()).setFacility((mmlFcFacility)builder.getElement());
			}

			if (parentElement.getQName().equals("mmlPsi:PersonalizedInfo")) {
				((mmlPsiPersonalizedInfo)builder.getParent()).setFacility((mmlFcFacility)builder.getElement());
			}

			if (parentElement.getQName().equals("mmlHi:facility")) {
				((mmlHifacility)builder.getParent()).setFacility((mmlFcFacility)builder.getElement());
			}

			if (parentElement.getQName().equals("mmlFcl:birthInfo")) {
				((mmlFclbirthInfo)builder.getParent()).setFacility((mmlFcFacility)builder.getElement());
			}

			
			printlnStatus(parentElement.getQName()+" /"+qName);


			builder.restoreIndex();
			super.buildEnd(namespaceURI,localName,qName,builder);
			return true;
		}
		return false;
	}
	
	/* characters */
	
	
	/* setters and getters */
	public void setName(Vector _name) {
		if (this._name != null) this._name.removeAllElements();
		// copy entire elements in the vector
		this._name = new Vector();
		for (int i = 0; i < _name.size(); ++i) {
			this._name.addElement( _name.elementAt(i) );
		}
	}
	public Vector getName() {
		return _name;
	}
	public void setId(mmlCmId _Id) {
		this._Id = _Id;
	}
	public mmlCmId getId() {
		return _Id;
	}
	
}