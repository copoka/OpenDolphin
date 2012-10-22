/*
 * ParentId.java
 * Copyright (C) 2003 Digital Globe, Inc. All rights reserved. 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *	
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.infomodel;

/**
 * ParentIdModel
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ParentIdModel extends InfoModel {

	private static final long serialVersionUID = 762420403350918058L;

	String id;

	String relation; // IMP

	String relationTableId;

	/** Creates new ParentId */
	public ParentIdModel() {
	}

	public String getId() {
		return id;
	}

	public void setId(String val) {
		id = val;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelationTableId(String relationTableId) {
		this.relationTableId = relationTableId;
	}

	public String getRelationTableId() {
		return relationTableId;
	}
}