/**
 *
 * mmlLbTestModule.java
 * Created on 2002/7/30 10:0:35
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
public class mmlLbTestModule extends MMLObject {
	
	/* fields */
	private mmlLbinformation _information = null;
	private Vector _laboTest = new Vector();
	
	public mmlLbTestModule() {
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
			if ( _information != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_information.printObject(pw, visitor);
			}
			if (this._laboTest != null) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				// print each element in the vector assumming that it doesn't contain String object...
				for (int i = 0; i < this._laboTest.size(); ++i ) {
					((mmlLblaboTest)this._laboTest.elementAt(i)).printObject(pw, visitor);
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
		if (qName.equals("mmlLb:TestModule") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlLbTestModule obj = new mmlLbTestModule();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlLbTestModule)builder.getElement()).setNamespace( getNamespace() );
			((mmlLbTestModule)builder.getElement()).setLocalName( getLocalName() );
			((mmlLbTestModule)builder.getElement()).setQName( getQName() );
			((mmlLbTestModule)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlLb:TestModule") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("content")) {
				((content)builder.getParent()).setTestModule((mmlLbTestModule)builder.getElement());
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
	public void setInformation(mmlLbinformation _information) {
		this._information = _information;
	}
	public mmlLbinformation getInformation() {
		return _information;
	}
	public void setLaboTest(Vector _laboTest) {
		if (this._laboTest != null) this._laboTest.removeAllElements();
		// copy entire elements in the vector
		this._laboTest = new Vector();
		for (int i = 0; i < _laboTest.size(); ++i) {
			this._laboTest.addElement( _laboTest.elementAt(i) );
		}
	}
	public Vector getLaboTest() {
		return _laboTest;
	}
	
}