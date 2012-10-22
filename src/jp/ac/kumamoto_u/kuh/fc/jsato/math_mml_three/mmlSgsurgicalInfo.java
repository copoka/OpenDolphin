/**
 *
 * mmlSgsurgicalInfo.java
 * Created on 2003/1/4 2:30:8
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
public class mmlSgsurgicalInfo extends MMLObject {
	
	/* fields */
	private String __mmlSgtype = null;

	private mmlSgdate _date = null;
	private mmlSgstartTime _startTime = null;
	private mmlSgduration _duration = null;
	private mmlSgsurgicalDepartment _surgicalDepartment = null;
	private mmlSgpatientDepartment _patientDepartment = null;
	
	public mmlSgsurgicalInfo() {
	}
	
	
	/* print */
	public void printObject(PrintWriter pw, MMLVisitor visitor) throws IOException {
		if ( this.getQName() != null ) {
			visitor.goDown();// adjust tab
			pw.print( visitor.getTabPadding() + "<" + this.getQName() );
			/* print atts */
			if ( __mmlSgtype != null ) pw.print(" " + "mmlSg:type" +  "=" + "'" + __mmlSgtype + "'");

			if ( this.getLocalName().equals("levelone") ) {
				visitor.printNamespaces(pw);
			}
			pw.print( ">" );
			/* print content */
			if ( _date != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_date.printObject(pw, visitor);
			}
			if ( _startTime != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_startTime.printObject(pw, visitor);
			}
			if ( _duration != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_duration.printObject(pw, visitor);
			}
			if ( _surgicalDepartment != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_surgicalDepartment.printObject(pw, visitor);
			}
			if ( _patientDepartment != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_patientDepartment.printObject(pw, visitor);
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
		if (qName.equals("mmlSg:surgicalInfo") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlSgsurgicalInfo obj = new mmlSgsurgicalInfo();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlSgsurgicalInfo)builder.getElement()).setNamespace( getNamespace() );
			((mmlSgsurgicalInfo)builder.getElement()).setLocalName( getLocalName() );
			((mmlSgsurgicalInfo)builder.getElement()).setQName( getQName() );
			((mmlSgsurgicalInfo)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			if (atts != null) {
				for (int i=0; i < atts.getLength(); ++i) {
					if ( ((String)atts.getQName(i)).equals("mmlSg:type") ) {
						set__mmlSgtype( atts.getValue(i) );
						((mmlSgsurgicalInfo)builder.getElement()).set__mmlSgtype( atts.getValue(i) );
					}
				}
			}

			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlSg:surgicalInfo") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlSg:surgeryItem")) {
				((mmlSgsurgeryItem)builder.getParent()).set_surgicalInfo((mmlSgsurgicalInfo)builder.getElement());
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
	public void set__mmlSgtype(String __mmlSgtype) {
		this.__mmlSgtype = __mmlSgtype;
	}
	public String get__mmlSgtype() {
		return __mmlSgtype;
	}

	public void set_date(mmlSgdate _date) {
		this._date = _date;
	}
	public mmlSgdate get_date() {
		return _date;
	}
	public void set_startTime(mmlSgstartTime _startTime) {
		this._startTime = _startTime;
	}
	public mmlSgstartTime get_startTime() {
		return _startTime;
	}
	public void set_duration(mmlSgduration _duration) {
		this._duration = _duration;
	}
	public mmlSgduration get_duration() {
		return _duration;
	}
	public void set_surgicalDepartment(mmlSgsurgicalDepartment _surgicalDepartment) {
		this._surgicalDepartment = _surgicalDepartment;
	}
	public mmlSgsurgicalDepartment get_surgicalDepartment() {
		return _surgicalDepartment;
	}
	public void set_patientDepartment(mmlSgpatientDepartment _patientDepartment) {
		this._patientDepartment = _patientDepartment;
	}
	public mmlSgpatientDepartment get_patientDepartment() {
		return _patientDepartment;
	}
	
}