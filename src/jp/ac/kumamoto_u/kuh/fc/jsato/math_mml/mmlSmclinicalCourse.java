/**
 *
 * mmlSmclinicalCourse.java
 * Created on 2002/7/30 10:0:33
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
public class mmlSmclinicalCourse extends MMLObject {
	
	/* fields */
	private Vector _clinicalRecord = new Vector();
	
	public mmlSmclinicalCourse() {
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
			if (this._clinicalRecord != null) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				// print each element in the vector assumming that it doesn't contain String object...
				for (int i = 0; i < this._clinicalRecord.size(); ++i ) {
					((mmlSmclinicalRecord)this._clinicalRecord.elementAt(i)).printObject(pw, visitor);
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
		if (qName.equals("mmlSm:clinicalCourse") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlSmclinicalCourse obj = new mmlSmclinicalCourse();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlSmclinicalCourse)builder.getElement()).setNamespace( getNamespace() );
			((mmlSmclinicalCourse)builder.getElement()).setLocalName( getLocalName() );
			((mmlSmclinicalCourse)builder.getElement()).setQName( getQName() );
			((mmlSmclinicalCourse)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlSm:clinicalCourse") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlSm:SummaryModule")) {
				((mmlSmSummaryModule)builder.getParent()).setClinicalCourse((mmlSmclinicalCourse)builder.getElement());
			}

			if (parentElement.getQName().equals("mmlRe:ReferralModule")) {
				((mmlReReferralModule)builder.getParent()).setClinicalCourse((mmlSmclinicalCourse)builder.getElement());
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
	public void setClinicalRecord(Vector _clinicalRecord) {
		if (this._clinicalRecord != null) this._clinicalRecord.removeAllElements();
		// copy entire elements in the vector
		this._clinicalRecord = new Vector();
		for (int i = 0; i < _clinicalRecord.size(); ++i) {
			this._clinicalRecord.addElement( _clinicalRecord.elementAt(i) );
		}
	}
	public Vector getClinicalRecord() {
		return _clinicalRecord;
	}
	
}