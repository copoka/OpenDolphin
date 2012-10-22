/**
 *
 * mmlPcproblemItem.java
 * Created on 2002/7/30 10:0:31
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
public class mmlPcproblemItem extends MMLObject {
	
	/* fields */
	private mmlPcproblem _problem = null;
	private mmlPcsubjective _subjective = null;
	private mmlPcobjective _objective = null;
	private mmlPcassessment _assessment = null;
	private mmlPcplan _plan = null;
	
	public mmlPcproblemItem() {
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
			if ( _problem != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_problem.printObject(pw, visitor);
			}
			if ( _subjective != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_subjective.printObject(pw, visitor);
			}
			if ( _objective != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_objective.printObject(pw, visitor);
			}
			if ( _assessment != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_assessment.printObject(pw, visitor);
			}
			if ( _plan != null ) {
				visitor.setIgnoreTab( false );
				pw.print( "\n" );
				_plan.printObject(pw, visitor);
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
		if (qName.equals("mmlPc:problemItem") == true) {
			super.buildStart(namespaceURI,localName,qName,atts,builder);
			printlnStatus(parentElement.getQName() + " " + qName);
			
			/* create tree node */
			mmlPcproblemItem obj = new mmlPcproblemItem();
			builder.getMmlTree().addElement( obj );
			obj.setParentIndex( builder.mmlTreeIndex );
			builder.adjustIndex();
			((mmlPcproblemItem)builder.getElement()).setNamespace( getNamespace() );
			((mmlPcproblemItem)builder.getElement()).setLocalName( getLocalName() );
			((mmlPcproblemItem)builder.getElement()).setQName( getQName() );
			((mmlPcproblemItem)builder.getElement()).setAtts( getAtts() );/* :-) */
			/* atts */
			
			return true;
		}
		return false;
	}
	
	public boolean buildEnd(String namespaceURI, String localName, String qName, MMLBuilder builder) {
		if (qName.equals("mmlPc:problemItem") == true) {
			
			/* connection */
			if (parentElement.getQName().equals("mmlPc:structuredExpression")) {
				Vector v = ((mmlPcstructuredExpression)builder.getParent()).getProblemItem();
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
	public void setProblem(mmlPcproblem _problem) {
		this._problem = _problem;
	}
	public mmlPcproblem getProblem() {
		return _problem;
	}
	public void setSubjective(mmlPcsubjective _subjective) {
		this._subjective = _subjective;
	}
	public mmlPcsubjective getSubjective() {
		return _subjective;
	}
	public void setObjective(mmlPcobjective _objective) {
		this._objective = _objective;
	}
	public mmlPcobjective getObjective() {
		return _objective;
	}
	public void setAssessment(mmlPcassessment _assessment) {
		this._assessment = _assessment;
	}
	public mmlPcassessment getAssessment() {
		return _assessment;
	}
	public void setPlan(mmlPcplan _plan) {
		this._plan = _plan;
	}
	public mmlPcplan getPlan() {
		return _plan;
	}
	
}