/**
 *
 * mmlPiemailAddresses.java
 * Created on 2002/7/30 10:0:25
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
public class mmlPiemailAddresses extends MMLObject {
	
	/* fields */
	private Vector _email = new Vector();
	
	public mmlPiemailAddresses() {
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
			if (this._email != null) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				// print each element in the vector assumming that it doesn't contain String object...
				for (int i = 0; i < this._email.size(); ++i ) {
					((mmlCmemail)this._email.elementAt(i)).printObject(pw, visitor);
				}
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
		if (qName.equals("mmlPi:emailAddresses") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlPiemailAddresses obj = new mmlPiemailAddresses();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlPiemailAddresses)builder.getElement()).setNamespace( getNamespace() );
			((mmlPiemailAddresses)builder.getElement()).setLocalName( getLocalName() );
			((mmlPiemailAddresses)builder.getElement()).setQName( getQName() );
			((mmlPiemailAddresses)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlPi:emailAddresses") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlPi:PatientModule")) {
				((mmlPiPatientModule)builder.getParent()).setEmailAddresses((mmlPiemailAddresses)builder.getElement());
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
	public void setEmail(Vector _email) {
		if (this._email != null) this._email.removeAllElements();
		// copy entire elements in the vector
		this._email = new Vector();
		for (int i = 0; i < _email.size(); ++i) {
			this._email.addElement( _email.elementAt(i) );
		}
	}
	public Vector getEmail() {
		return _email;
	}
	
}