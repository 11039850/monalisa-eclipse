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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tsc9526.monalisa.orm.datasource.DataSourceManager;
import com.tsc9526.monalisa.orm.datasource.DbProp;
import com.tsc9526.monalisa.orm.tools.generator.DBExchange;
import com.tsc9526.monalisa.orm.tools.generator.DBGenerator;
import com.tsc9526.monalisa.orm.tools.helper.CloseQuietly;
import com.tsc9526.monalisa.orm.tools.helper.FileHelper;
import com.tsc9526.monalisa.orm.tools.helper.Helper;
import com.tsc9526.monalisa.orm.tools.helper.JavaWriter;
import com.tsc9526.monalisa.orm.tools.resources.PkgNames;
import com.tsc9526.monalisa.plugin.eclipse.editors.LinesCodeTransform;
import com.tsc9526.monalisa.plugin.eclipse.jdt.JDTCompiler;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class SelectRun {
	public final static String RUN_PACKAGE="sql.select";
	public final static String RUN_NAME   ="Run";
	public final static String RUN_CLASS  =RUN_PACKAGE+"."+RUN_NAME;		
	 	
	private SourceUnit unit;
 
	public SelectRun(SourceUnit unit){
		this.unit=unit;
	}
	 
	
	public List<DBExchange> run(List<SelectMethod> methods) {
		try{
			DbProp.CFG_ROOT_PATH=unit.getProjectPath();
			
			String runCode=createRunJavaCode(methods);
			 
			String[] classPath=compile(runCode);
			
			return runWithClassloader(classPath);		
		}catch(Exception e){
			List<DBExchange> exchanges=new ArrayList<DBExchange>();
			
			DBExchange exchange=new DBExchange();
			exchange.setIndex(0);
			
			StringWriter sw=new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			exchange.setErrorString(sw.toString());
			
			exchanges.add(exchange);
			return exchanges;
		}
	}	
	
	private String[] compile(String runCode){
		String dirRoot=FileHelper.combinePath(DbProp.TMP_ROOT_PATH+"/sqlrun");
		 
		String[] classpath=FileHelper.combineExistFiles(unit.getRuntimeClasspath(),unit.getPluginClasspath());
		JDTCompiler compiler=new JDTCompiler(dirRoot,classpath);
		
		compiler.clean();
		
		//编译正在编辑的java代码
		String javaCode=unit.getJavaCode();
		javaCode=LinesCodeTransform.toJavaCode(javaCode);
		compiler.compile(unit.getPackageName()+"."+unit.getUnitName(),javaCode);
		
		//编译调用Select的代码
		compiler.compile(RUN_CLASS,runCode);		
		
		String[] classPath=FileHelper.combineExistFiles(compiler.getClasspaths(), unit.getRuntimeClasspath(),unit.getPluginClasspath());
		
		return classPath;
	}
	
	private String createRunJavaCode(List<SelectMethod> methods){
		JavaWriter runWriter=JavaWriter.getBufferedWriter();
		runWriter.println("package "+RUN_PACKAGE+";");		
		runWriter.println("import "+unit.getPackageName()+".*;");
		runWriter.println("import "+DBExchange.class.getName()+";");
		for(String i:unit.getImports().values()){
			runWriter.println("import "+i+";");
		}		
		runWriter.println("public class "+RUN_NAME+"{");
	 	
		for(SelectMethod sm:methods){
			sm.writeRunMethod(runWriter);
		}
		runWriter.println("}");
		
		return runWriter.getContent();
	}

	private List<DBExchange> runWithClassloader(String[] classPath) {
		URLClassLoader loader=null;
		try {
			List<DBExchange> exchanges = new ArrayList<DBExchange>();

			loader = new LoggerClassLoader(Helper.toURLs(classPath),ClassLoader.getSystemClassLoader()); 
			
			beginProcessing(loader);
			
			Class<?> runClass = loader.loadClass(RUN_CLASS);
			Object run = runClass.newInstance();
			for (Method m : runClass.getMethods()) {
				String name=m.getName();
				if (OBJECT_METHODS.contains(name) == false && name.indexOf("$")>0) {
					int p=name.lastIndexOf("$");
					int index=Integer.parseInt(name.substring(p+1));
					try{
						Object exchange=m.invoke(run);
						assert exchange!=null;
						 
						exchanges.add(translateExchange(exchange));
					}catch(Throwable e){
						DBExchange exchange=new DBExchange();
						exchange.setIndex(index);
						
						StringWriter sw=new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						exchange.setErrorString(sw.toString());
						
						exchanges.add(exchange);
					}
				}
			}						
			return exchanges;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			closeLoader(loader);
		}
	}
	
	private void beginProcessing(URLClassLoader loader)throws Exception{
		Class<?> dbPropClass=loader.loadClass(DbProp.class.getName());
		dbPropClass.getField("ProcessingEnvironment").set(null, true);
		
		dbPropClass.getMethod("SET_CFG_ROOT_PATH",String.class).invoke(null, DbProp.CFG_ROOT_PATH);
		 
		Class<?> dbGenClass=loader.loadClass(DBGenerator.class.getName());
		dbGenClass.getField("plogger").set(null, DBGenerator.plogger);
	}
	
	private void closeLoader(URLClassLoader loader){
		if(loader!=null){
			try{
				Class<?> clazz=loader.loadClass(DataSourceManager.class.getName());
				clazz.getMethod("shutdown").invoke(null);
			}catch (Exception e) {
				throw new RuntimeException(e);
			}finally{
				CloseQuietly.close(loader);
			}
		}
	}
 
	private DBExchange translateExchange(Object obj)throws Exception{
		ByteArrayOutputStream buf=new ByteArrayOutputStream();
		ObjectOutputStream out=new ObjectOutputStream(buf);
		out.writeObject(obj);
		out.flush();
		out.close();
		
		ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
		DBExchange exchange=(DBExchange)in.readObject();
		in.close();
		
		return exchange;
	}
	
	
	
	public static Set<String> OBJECT_METHODS=new HashSet<String>(){		 
		private static final long serialVersionUID = -4949935939426517392L;
		{
			add("equals");
			add("getClass");
			add("hashCode");
			add("notify");
			add("notifyAll");
			add("toString");
			add("wait");
		}
	};
	
	private class LoggerClassLoader extends URLClassLoader{
		 
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
	}

	
}
