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
package com.tsc9526.monalisa.plugin.eclipse.console;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class HyperLink implements IHyperlink {
	private String text;
	private URL url;

	public HyperLink(String urlStr) {
		this(urlStr, urlStr);
	}

	public HyperLink(String urlStr, String text) {
		this.text = text;
		
		if(urlStr!=null && urlStr.length()>0){
			try {
				this.url = new URL(urlStr);				
			} catch (MalformedURLException e) {
				e.printStackTrace();				 
			}
		}
	}
	
	public URL getUrl(){
		return this.url;
	}

	public String getText() {
		return text;
	}
 
	@Override
	public void linkActivated() {
		if (url != null) {
			try {
				String protocol=url.getProtocol();
				if(protocol.equals("file")){
					String ref=url.getRef();
					
					IFile[] files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(url.toURI());
					if (files.length > 0) {
						for (int i = 0; i < files.length; i++) {
							IFile curr= files[0];
							IJavaElement element= JavaCore.create(curr);
							if (element != null && element.exists() ) {															
								if(ref!=null && ref.length()>0 && element instanceof ICompilationUnit){											 
									String[] sv=ref.split(",");
									String name=sv[0];
									
									String[] signatures=null;
									if(sv.length>1){
										signatures=new String[sv.length-1];
										for(int x=1;x<sv.length;x++){
											signatures[x-1]= Signature.createTypeSignature(sv[x],false);
										}
									}
									
									IType type=((ICompilationUnit)element).getTypes()[0];
									IMethod method=type.getMethod(name, signatures);
									if(method!=null && method.exists()){
										JavaUI.openInEditor(method, true, true);	
										return;
									}
								}
								
								JavaUI.openInEditor(element, true, true);								 
								return;
							}
						}
					}
				}
				
				PlatformUI.getWorkbench().getBrowserSupport().createBrowser(text).openURL(url);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void linkEntered() {
		 
	}

	@Override
	public void linkExited() {
		 
	}
}