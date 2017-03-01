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

import com.tsc9526.monalisa.orm.Version;
import com.tsc9526.monalisa.plugin.eclipse.generator.LoggerClassLoader;
import com.tsc9526.monalisa.plugin.eclipse.generator.SourceUnit;
import com.tsc9526.monalisa.tools.io.MelpClose;
import com.tsc9526.monalisa.tools.io.MelpFile;
import com.tsc9526.monalisa.tools.misc.MelpException;
import com.tsc9526.monalisa.tools.string.MelpString;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class PluginHelper {
	public static String getProjectORMVersion(SourceUnit unit){
		String[] classpath=MelpFile.combineExistFiles(unit.getRuntimeClasspath(),unit.getPluginClasspath());
		LoggerClassLoader loader = new LoggerClassLoader(MelpString.toURLs(classpath),ClassLoader.getSystemClassLoader()); 
		try{
			
			Class<?> versionClass=loader.loadClass(Version.class.getName());
			return (String)versionClass.getMethod("getVersion").invoke(null);
		}catch(Exception e){
			return MelpException.throwRuntimeException(e);
		}finally{
			MelpClose.close(loader);
		}
	}
	
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
