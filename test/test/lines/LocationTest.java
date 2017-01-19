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
package test.lines;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.testng.annotations.Test;


/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@Test
public class LocationTest {
	public static void main(String[] args) throws Exception{
		//String x=""+/**~!{*/"D:\\workspace\\zzg\\.metadata\\.plugins\\org.eclipse.core.resources\\.projects\\monalisa-orm\\.location"/**}*/;
		 
		IProgressMonitor progressMonitor = new NullProgressMonitor();
	 	
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("monalisa-orm");
		project.create(progressMonitor);
		project.open(progressMonitor);
	}
}
