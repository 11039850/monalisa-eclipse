/*******************************************************************************************
 *	Copyright (c) 2016, zzg.zhou(11039850@qq.com)
 * 
 *  Monalisa Eclipse Plugin is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.

 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.

 *	You should have received a copy of the GNU Lesser General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************************/
package com.tsc9526.monalisa.plugin.eclipse.cleanup;

import java.util.Map;

import org.eclipse.jdt.internal.ui.fix.AbstractCleanUp;
import org.eclipse.jdt.internal.ui.preferences.cleanup.AbstractCleanUpTabPage;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
 

import com.tsc9526.monalisa.plugin.eclipse.resources.Resource;
import com.tsc9526.monalisa.tools.io.MelpFile;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class SQLCleanUpUI extends AbstractCleanUpTabPage {
	public static final String ID= "com.tsc9526.monalisa.plugin.eclipse.cleanup.SQLCleanUpUI"; 
	
	static String[] FALSE_TRUE=new String[]{CleanUpOptions.FALSE,CleanUpOptions.TRUE};
	 
	public SQLCleanUpUI() {
		super();
	}
  
	@Override
	protected AbstractCleanUp[] createPreviewCleanUps(Map<String, String> values) {
		return new AbstractCleanUp[] { };
	}
  

	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {
		Group annotationsGroup= createGroup(numColumns, composite, SQLCleanUpMessages.SQL);

		final CheckboxPreference annotationsPref= createCheckboxPref(annotationsGroup, numColumns, SQLCleanUpMessages.SQL_SELECT, SQLCleanUp.KEY_SQL_SELECT, FALSE_TRUE);
		
		registerPreference(annotationsPref);
		
		createLabel(numColumns, annotationsGroup, SQLCleanUpMessages.SQL_SELECT_DESC);
		

	}

	public String getPreview() {	
		return MelpFile.readToString(Resource.class.getResourceAsStream("/com/tsc9526/monalisa/plugin/eclipse/resources/preview_select.txt"),"utf-8");
	}
 

}
