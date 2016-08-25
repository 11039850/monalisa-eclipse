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

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class SQLCleanUpMessages extends NLS{	
	private static final String BUNDLE_NAME= "com.tsc9526.monalisa.plugin.eclipse.cleanup.SQLCleanUpMessages";  
  
	public static String SQL;
	
	public static String SQL_SELECT;
	
	public static String SQL_SELECT_DESC;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, SQLCleanUpMessages.class);
	}

 
	private SQLCleanUpMessages() {
 	}

}
