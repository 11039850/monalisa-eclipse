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

import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class PluginHelper {

	public static String getJavadocField(Javadoc doc,String property){
		String value=null;	
		if(doc!=null && doc.isDocComment()){			
			for(Object o:doc.tags()){
				TagElement tag=(TagElement)o;
				String tn=tag.getTagName();
				if(property.equals(tn)){
					value=tag.toString();
					int p=value.indexOf(property);
					value=value.substring(p+property.length()).trim();
					break;
				}
			}
		}
		return value;
	}
}
