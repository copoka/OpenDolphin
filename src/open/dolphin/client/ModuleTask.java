/*
 * ModuleTask.java
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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
package open.dolphin.client;

import java.util.List;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.dto.ModuleSearchSpec;

/**
 * モジュールと予約を検索するタスク。
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 *
 */
public class ModuleTask extends AbstractInfiniteTask {
	
	private List modules;
	private List appointments;
	private boolean appo;
	private ModuleSearchSpec spec;
	private DocumentDelegater ddl;
	
	public ModuleTask(ModuleSearchSpec spec, DocumentDelegater ddl, boolean appo, int taskLength) {
		this.spec = spec;
		this.ddl = ddl;
		this.appo = appo;
		setTaskLength(taskLength);
	}
    
	public List getModuleList() {
		return modules;
	}
	
	public List getAppointmentList() {
		return appointments;
	}

	protected void doTask() {
        modules = ddl.getModuleList(spec);
		if (appo) {
			appointments = ddl.getAppoinmentList(spec);
		}
		setDone(true);
	}
}