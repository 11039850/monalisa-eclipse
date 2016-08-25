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
package com.tsc9526.monalisa.plugin.eclipse.tools;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class MonalisaConstants {  
	public final static String PLUGIN_ID = "com.tsc9526.monalisa.plugin.eclipse";
 
	public static final String CONTENT_TYPE_QUERY = "com.tsc9526.monalisa.query"; //$NON-NLS-1$
   
	public static final String DEBUG_BEAN_PROPERTY_CACHE = PLUGIN_ID + "/debug/beanPropertyCache";

	public static final IContentType queryContentType;
 
	static{
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		queryContentType = contentTypeManager.getContentType(CONTENT_TYPE_QUERY);	 
	}

	private MonalisaConstants(){
	}

}
