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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class ParameterInitialization {
	private MethodDeclaration md;
	private String buildCode;
	
	private List<String> initParameters=new ArrayList<String>();
	
	private List<String> initFailMessages=new ArrayList<String>();
	
	public ParameterInitialization(MethodDeclaration md,String buildCode){
		this.md=md;
		this.buildCode=buildCode;
		
		init();
	}
	
	private void init(){
		final Set<String> filterVariables = new HashSet<String>();
		if (buildCode != null && buildCode.length() > 0) {
			String source = "public class X{\r\n" + buildCode + "\r\n}";
			 
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(source.toCharArray());
			ASTNode node = parser.createAST(null);
			node.accept(new ASTVisitor() {
				public boolean visit(VariableDeclarationFragment var) {
					String nameString = var.getName().toString();
					filterVariables.add(nameString);
					return false;
				}
			});
			
			initParameters.add(buildCode);	 
		}
		
		initDefaultParameters(filterVariables);
	}
	
	public String getInitParameters(){
	 	StringBuilder sb=new StringBuilder(""); 
		for(String s:initParameters){
			if(sb.length()>0){
				sb.append("\r\n");
			}
			sb.append(s);
		}
		return sb.toString();
	}
	
	public String getFailMessages(){
		StringBuilder sb=new StringBuilder(""); 
		for(String s:initFailMessages){
			if(sb.length()>0){
				sb.append("\r\n");
			}
			sb.append(s);
		}
		return sb.toString();
	}
	
	private void initDefaultParameters(Set<String> filterVariables) {
		for (Object p : md.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
			Type t = svd.getType();
			String v = svd.getName().toString();

			if (filterVariables.contains(v) == false) {
				String initValue=getInitParameters(t,v);
				
				initParameters.add(t + " " + v + " = "+initValue+";");
				if(initValue==null){
					initFailMessages.add("Can't auto init parameter: "+t+" "+v+"; You should init it by: @select(build=\""+t+" "+v+"=new ...;"+"\")");
				}
			}
		}
	}
	
	private String getInitParameters(Type t,String v){
		Code code = PrimitiveType.VOID;
		
		String initValue=null;
		
		if (t.isPrimitiveType()) {
			PrimitiveType pt = (PrimitiveType) t;
			code = pt.getPrimitiveTypeCode();
			
			initValue=primitiveCodeInit.get(code);
		}else if (t.isSimpleType()) {
			String name = ((SimpleType) t).getName().toString();
			if (name.startsWith("java.lang.")) {
				name = name.substring(10);
			}
			
			code=primitiveShortNameToCode.get(name);
			initValue=primitiveCodeInit.get(code);
			if(initValue==null){ 
				ITypeBinding bind=t.resolveBinding();
				String xs=objectParameters.get(bind.getBinaryName());
				if(xs!=null){
					initValue= "new "+xs+"()";
				}else if(bind.isClass()){
					initValue=" new " + t + "()";
				}else {
					//interface, enum ...
				}
			}
		}else if (t.isArrayType()) {
			initValue="new " + t + "{}";
		}else{
			ITypeBinding bind=t.resolveBinding();
			String xs=objectParameters.get(bind.getBinaryName());
			if(xs!=null){
				initValue= "new "+xs+"()";
			}else if(bind.isClass()){
				initValue=" new " + t + "()";
			}
		}
		
		return initValue;
	}
	
	private static Map<Code,String> primitiveCodeInit=new HashMap<Code, String>(){
		private static final long serialVersionUID = 1L;
		{
			put(PrimitiveType.INT     , "0");
			put(PrimitiveType.CHAR    , "'0'");
			put(PrimitiveType.BOOLEAN , "false");
			put(PrimitiveType.SHORT   , "0");
			put(PrimitiveType.LONG    , "0L");
			put(PrimitiveType.FLOAT   , "0F");
			put(PrimitiveType.DOUBLE  , "0D");
			put(PrimitiveType.BYTE    , "0");
		}
	};
	
	private static Map<String,Code> primitiveShortNameToCode=new HashMap<String, Code>(){
		private static final long serialVersionUID = 1L;
		{
			put(Integer.class.getSimpleName(),PrimitiveType.INT);
			put(Character.class.getSimpleName(),PrimitiveType.CHAR);
			put(Boolean.class.getSimpleName(),PrimitiveType.BOOLEAN);
			put(Short.class.getSimpleName(),PrimitiveType.SHORT);
			put(Long.class.getSimpleName(),PrimitiveType.LONG);
			put(Float.class.getSimpleName(),PrimitiveType.FLOAT);
			put(Double.class.getSimpleName(),PrimitiveType.DOUBLE);
			put(Byte.class.getSimpleName(),PrimitiveType.BYTE);
		}
	};
	
	private static Map<String,String> objectParameters=new HashMap<String, String>(){
		private static final long serialVersionUID = 1L;
		{
			put(List.class.getName(),ArrayList.class.getName());
			put(Collection.class.getName(),ArrayList.class.getName());
			put(Set.class.getName(),HashSet.class.getName());
			put(Map.class.getName(),HashMap.class.getName());
		}
	};
}
