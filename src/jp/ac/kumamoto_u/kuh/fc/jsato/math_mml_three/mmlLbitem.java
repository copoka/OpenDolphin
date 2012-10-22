/**
 *
 * mmlLbitem.java
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
public class mmlLbitem extends MMLObject {
	
	/* fields */
	private mmlLbitemName _itemName = null;
	private mmlLbvalue _value = null;
	private mmlLbnumValue _numValue = null;
	private mmlLbunit _unit = null;
	private mmlLbreferenceInfo _referenceInfo = null;
	private Vector _itemMemo = new Vector();
	private mmlLbitemMemoF _itemMemoF = null;
	
	public mmlLbitem() {
	}
	
	
	/* print */
	public void printObject(PrintWriter pw, MMLVisitor visitor) throws IOException {
		if ( this.getQName() != null ) {
			visitor.goDown();// adjust tab
			pw.print( visitor.getTabPadding() + "<" + this.getQName() );
			/* print atts */
			if ( this.getLocalName().equals("levelone") ) {
				visitor.printNamespaces(pw);
			}
			pw.print( ">" );
			/* print content */
			if ( _itemName != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_itemName.printObject(pw, visitor);
			}
			if ( _value != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_value.printObject(pw, visitor);
			}
			if ( _numValue != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_numValue.printObject(pw, visitor);
			}
			if ( _unit != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_unit.printObject(pw, visitor);
			}
			if ( _referenceInfo != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_referenceInfo.printObject(pw, visitor);
			}
			if (this._itemMemo != null) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				// print each element in the vector assumming that it doesn't contain String object...
				for (int i = 0; i < this._itemMemo.size(); ++i ) {
					((mmlLbitemMemo)this._itemMemo.elementAt(i)).printObject(pw, visitor);
				}
			}
			if ( _itemMemoF != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_itemMemoF.printObject(pw, visitor);
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
		if (qName.equals("mmlLb:item") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlLbitem obj = new mmlLbitem();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlLbitem)builder.getElement()).setNamespace( getNamespace() );
			((mmlLbitem)builder.getElement()).setLocalName( getLocalName() );
			((mmlLbitem)builder.getElement()).setQName( getQName() );
			((mmlLbitem)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlLb:item") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlLb:laboTest")) {
				Vector v = ((mmlLblaboTest)builder.getParent()).get_item();
				v.addElement(builder.getElement());
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
	public void set_itemName(mmlLbitemName _itemName) {
		this._itemName = _itemName;
	}
	public mmlLbitemName get_itemName() {
		return _itemName;
	}
	public void set_value(mmlLbvalue _value) {
		this._value = _value;
	}
	public mmlLbvalue get_value() {
		return _value;
	}
	public void set_numValue(mmlLbnumValue _numValue) {
		this._numValue = _numValue;
	}
	public mmlLbnumValue get_numValue() {
		return _numValue;
	}
	public void set_unit(mmlLbunit _unit) {
		this._unit = _unit;
	}
	public mmlLbunit get_unit() {
		return _unit;
	}
	public void set_referenceInfo(mmlLbreferenceInfo _referenceInfo) {
		this._referenceInfo = _referenceInfo;
	}
	public mmlLbreferenceInfo get_referenceInfo() {
		return _referenceInfo;
	}
	public void set_itemMemo(Vector _itemMemo) {
		if (this._itemMemo != null) this._itemMemo.removeAllElements();
		// copy entire elements in the vector
		this._itemMemo = new Vector();
		for (int i = 0; i < _itemMemo.size(); ++i) {
			this._itemMemo.addElement( _itemMemo.elementAt(i) );
		}
	}
	public Vector get_itemMemo() {
		return _itemMemo;
	}
	public void set_itemMemoF(mmlLbitemMemoF _itemMemoF) {
		this._itemMemoF = _itemMemoF;
	}
	public mmlLbitemMemoF get_itemMemoF() {
		return _itemMemoF;
	}
	
}