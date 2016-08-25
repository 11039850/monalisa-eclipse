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
package com.tsc9526.monalisa.plugin.eclipse.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.tsc9526.monalisa.plugin.eclipse.activator.MonalisaPlugin;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		setPreferenceStore(MonalisaPlugin.getDefault().getPreferenceStore());
	}

	protected void performApply() {
		super.performApply();
	}

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		RowLayout layout = new RowLayout();

		CLabel label = new CLabel(parent, SWT.NULL);
		label.setLayoutData(layout);
		label.setText("Installed version: 1.7.0");

		return label;
	}

}