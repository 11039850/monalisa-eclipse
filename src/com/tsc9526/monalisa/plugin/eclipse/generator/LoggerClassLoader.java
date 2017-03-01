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
package com.tsc9526.monalisa.plugin.eclipse.generator;

import java.net.URL;
import java.net.URLClassLoader;

import com.tsc9526.monalisa.tools.PkgNames;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class LoggerClassLoader extends URLClassLoader{
	 
	public LoggerClassLoader(URL[] urls,ClassLoader parent) {
		super(urls,parent);
	}
	
	protected Class<?> findClass(String name) throws ClassNotFoundException{
		String prefix=PkgNames.ORM_LOGGER_PKG+".";
		 
		if(name.startsWith(prefix)){
			return Class.forName(name);
		}else{
			return super.findClass(name);
		}
	}
	
	public URL findResource(final String name) {
		URL r= super.findResource(name);	
		return r;
    }
}