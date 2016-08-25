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

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.tsc9526.monalisa.orm.tools.helper.Helper;
import com.tsc9526.monalisa.plugin.eclipse.resources.Resource;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class MMC extends MessageConsole {
	public final static String CONSOLE_NAME = "Monalisa";
	
	public static MMC getConsole() {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] curConsoles = manager.getConsoles();
		for (IConsole co : curConsoles) {
			if (co.getName().equals(CONSOLE_NAME)){
				return (MMC) co;
			}
		}
		MMC mmc = new MMC();
		manager.addConsoles(new IConsole[] { mmc });
		return mmc;
	}
	
	 
	private int length;
	private boolean clear=false;	

	public MMC() {
		super(CONSOLE_NAME, Resource.getImage("monalisa"));
		length = 0;
	}

	public void clearConsole() {		
		super.clearConsole();
		
		clear=true;
		length = 0;
	}

	public void info(Object message){
		print(Helper.getTime()+" [I] "+getMessage(message)+"\r\n",SWT.COLOR_BLACK);				 
	}
	
	public void warn(Object message){
		print(Helper.getTime()+" [W] "+getMessage(message)+"\r\n",SWT.COLOR_DARK_YELLOW);		
	}
	
	public void error(Object message){
		print(Helper.getTime()+" [E] "+getMessage(message)+"\r\n",SWT.COLOR_RED);		
	}
	
	protected String getMessage(Object message){
		if(message instanceof Throwable){
			return Helper.toString((Throwable)message); 
		}else{
			return message==null?"NULL":message.toString();
		} 
	}
	
	public void code(String message,String code){
		print(Helper.getTime()+" [I] "+message+"\r\n",SWT.COLOR_BLACK);
		print(code+"\r\n",SWT.COLOR_BLUE);
	}
	 
	public void print(final String msg, final Integer SWTColor) {
		print(new HyperLink(null,msg),SWTColor);
	
	}

	public void print(final HyperLink link, final Integer SWTColor) {
		Display.getDefault().syncExec(new Runnable() {				
			@Override
			public void run() {
				activate();
			 	
			 	MessageConsoleStream stream = newMessageStream();
				if (SWTColor != null){
					stream.setColor(Display.getDefault().getSystemColor(SWTColor));
				}
				
				if(link.getUrl()!=null){
					//System.out.println("Lenght: "+length+", Expect: "+(length + link.getText().length())+", text: "+link.getText()+", url: "+link.getUrl());
					getDocument().addDocumentListener(new DocChangeListener(MMC.this, link, length + link.getText().length()));
				}
				
				length += link.getText().length();
				stream.print(link.getText()); 
			}
		});									 		
	}
	 
	static class DocChangeListener implements IDocumentListener {
		int lenBeforeChange;
		int expectedlen;
		final MMC console;
		final HyperLink link;

		public DocChangeListener(MMC console, HyperLink link, int expectedlen) {
			this.console = console;
			this.link = link;
			this.expectedlen = expectedlen;
		}

		public void documentAboutToBeChanged(DocumentEvent event) {
			
		}

		public void documentChanged(final DocumentEvent event) {
			int strLenAfterChange = event.getDocument().getLength();
			if (strLenAfterChange > expectedlen) {
				try{
					//System.out.println("Changed: "+strLenAfterChange+", expect: "+expectedlen+", link: "+link.getText());
					console.addHyperlink(link, expectedlen - link.getText().length(), link.getText().length());
				}catch(Exception e){
					e.printStackTrace();
				}
				event.getDocument().removeDocumentListener(this);
			}
		}
	}
}