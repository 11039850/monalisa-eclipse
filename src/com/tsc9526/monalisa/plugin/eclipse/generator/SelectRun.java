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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.tsc9526.monalisa.orm.datasource.DataSourceManager;
import com.tsc9526.monalisa.orm.datasource.DbProp;
import com.tsc9526.monalisa.orm.generator.DBExchange;
import com.tsc9526.monalisa.orm.generator.DBGenerator;
import com.tsc9526.monalisa.plugin.eclipse.editors.LinesCodeTransform;
import com.tsc9526.monalisa.plugin.eclipse.jdt.JDTCompiler;
import com.tsc9526.monalisa.tools.clazz.MelpClass;
import com.tsc9526.monalisa.tools.io.JavaWriter;
import com.tsc9526.monalisa.tools.io.MelpClose;
import com.tsc9526.monalisa.tools.io.MelpFile;
import com.tsc9526.monalisa.tools.string.MelpString;

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
		String dirRoot=MelpFile.combinePath(DbProp.TMP_ROOT_PATH+"/sqlrun");
		 
		String[] classpath=MelpFile.combineExistFiles(unit.getRuntimeClasspath(),unit.getPluginClasspath());
		JDTCompiler compiler=new JDTCompiler(dirRoot,classpath);
		
		compiler.clean();
		
		//编译正在编辑的java代码
		String javaCode=unit.getJavaCode();
		javaCode=LinesCodeTransform.toJavaCode(javaCode);
		compiler.compile(unit.getPackageName()+"."+unit.getUnitName(),javaCode);
		
		//编译调用Select的代码
		compiler.compile(RUN_CLASS,runCode);		
		
		String[] classPath=MelpFile.combineExistFiles(compiler.getClasspaths(), unit.getRuntimeClasspath(),unit.getPluginClasspath());
		
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

			loader = new LoggerClassLoader(MelpString.toURLs(classPath),ClassLoader.getSystemClassLoader()); 
			
			beginProcessing(loader);
			
			Class<?> runClass = loader.loadClass(RUN_CLASS);
			Object run = runClass.newInstance();
			for (Method m : runClass.getMethods()) {
				String name=m.getName();
				if (MelpClass.OBJECT_METHODS.contains(name) == false && name.indexOf("$")>0) {
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
				MelpClose.close(loader);
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
	 
}
