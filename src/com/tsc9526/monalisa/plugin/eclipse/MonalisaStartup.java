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
package com.tsc9526.monalisa.plugin.eclipse;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import com.tsc9526.monalisa.orm.tools.generator.DBGenerator;
import com.tsc9526.monalisa.orm.tools.logger.ConsoleLoggerFactory;
import com.tsc9526.monalisa.plugin.eclipse.console.MMC;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */

public class MonalisaStartup implements IStartup {
 
	public void earlyStartup() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setupProcessingLogger(); 
			}
	    });
	}
	 
	
	public void setupProcessingLogger(){
		DBGenerator.plogger=new ConsoleLoggerFactory.ConsoleLogger(){
			protected void write(String level,String message) {
				MMC console=MMC.getConsole();
				
				if("ERROR".equalsIgnoreCase(level)){
					console.error(message);
				}else if("WARN".equalsIgnoreCase(level)){
					console.warn(message);
				}else{
					console.info(message);
				}
			}
		};
	}

}
