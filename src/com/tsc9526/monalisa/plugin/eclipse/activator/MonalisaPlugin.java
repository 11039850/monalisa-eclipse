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
package com.tsc9526.monalisa.plugin.eclipse.activator;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.tsc9526.monalisa.orm.datasource.DataSourceManager;
import com.tsc9526.monalisa.plugin.eclipse.proposal.QueryResourceChangeListener;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class MonalisaPlugin extends AbstractUIPlugin {
	private static MonalisaPlugin plugin;

	public static MonalisaPlugin getDefault() {
		return plugin;
	}

	private BundleContext context;
	
	private IResourceChangeListener resourceChangeListener;

	private boolean linesOn=true;
	
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		
		this.context = bundleContext;

		plugin = this;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		resourceChangeListener = new QueryResourceChangeListener();
		workspace.addResourceChangeListener(resourceChangeListener);	
		
		 
	}
	 
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		 
		try {
			DataSourceManager.shutdown();
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (workspace != null && resourceChangeListener != null){
				workspace.removeResourceChangeListener(resourceChangeListener);
			}
		} finally {
			plugin = null;
		}
	}

	public BundleContext getContext() {
		return context;
	}

	protected void initializeDefaultPreferences(IPreferenceStore store) {

	}

	public boolean isLinesOn() {
		return linesOn;
	}

	public void setLinesOn(boolean linesOn) {
		this.linesOn = linesOn;
	}
    
}
