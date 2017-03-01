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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.text.edits.ReplaceEdit;

import com.tsc9526.monalisa.orm.annotation.Column;
import com.tsc9526.monalisa.orm.generator.DBExchange;
import com.tsc9526.monalisa.orm.generator.DBWriterSelect;
import com.tsc9526.monalisa.orm.meta.MetaColumn;
import com.tsc9526.monalisa.orm.meta.MetaTable;
import com.tsc9526.monalisa.plugin.eclipse.console.MMC;
import com.tsc9526.monalisa.tools.clazz.MelpJavaBeans;
import com.tsc9526.monalisa.tools.io.JavaWriter;
import com.tsc9526.monalisa.tools.string.MelpString;
import com.tsc9526.monalisa.tools.template.jsp.JspContext;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class SelectMethod {
	private SourceUnit unit;

	private MethodDeclaration md;

	private Annotation selectAnnotation;

	private String fingerprint;

	private String resultClassName;
	private String resultClassPackage;
	private String buildCode;

	private int index;

	private boolean forceRenameResultClass = false;

	public SelectMethod(SourceUnit unit, MethodDeclaration md, Annotation selectAnnotation) {
		this.unit = unit;
		this.md = md;
		this.selectAnnotation = selectAnnotation;

		calculateResultClassName();

		calculateFingerprint(null);
	}

	public void calculateResultClassName() {
		if (selectAnnotation.isNormalAnnotation()) {
			NormalAnnotation n = (NormalAnnotation) selectAnnotation;
			for (IMemberValuePairBinding mvp : n.resolveAnnotationBinding().getAllMemberValuePairs()) {
				String name = mvp.getName();
				if (name.equals("name")) {
					forceRenameResultClass = true;

					resultClassName = mvp.getValue().toString().trim();
					 
					int x=resultClassName.lastIndexOf(".");
					if(x>0){
						resultClassPackage=resultClassName.substring(0,x);
						resultClassName=resultClassName.substring(x+1);	
					}	
					
				} else if (name.equals("build")) {
					buildCode = mvp.getValue().toString();
				}
			}
		}
		
		if(resultClassPackage==null || resultClassPackage.length()==0){
			resultClassPackage=unit.getSubPackageName();
		}
		
		if (resultClassName == null || resultClassName.length()==0) {
			resultClassName = "Result" +  firstUpper(md.getName().toString());
		}
	}
	
	private String firstUpper(String s){
		return s.substring(0,1).toUpperCase()+(s.length()>1?s.substring(1):"");
	}
 

	public void calculateFingerprint(List<ReplaceEdit> changes) {
		try {
			IBuffer buffer = unit.getUnit().getJavaElement().getOpenable().getBuffer();

			String body = buffer.getText(md.getStartPosition(), md.getLength());
			if (changes != null && changes.size() > 0) {
				List<ReplaceEdit> copy = new ArrayList<ReplaceEdit>();
				copy.addAll(changes);

				Collections.sort(copy, new Comparator<ReplaceEdit>() {
					public int compare(ReplaceEdit o1, ReplaceEdit o2) {
						return o1.getOffset() - o2.getOffset();
					}
				});

				int px = md.getStartPosition();
				StringBuffer sb = new StringBuffer();
				while (copy.size() > 0) {
					ReplaceEdit re = copy.get(0);
					int offset = re.getOffset();
					int length = re.getLength();

					sb.append(buffer.getText(px, offset - px)).append(re.getText());

					px = offset + length;

					copy.remove(0);
				}
				sb.append(buffer.getText(px, md.getStartPosition() + md.getLength() - px));

				body = sb.toString();
			}
			this.fingerprint = MelpString.intToBytesString(body.length()) + MelpString.intToBytesString(body.hashCode());
		} catch (Exception e) {
			MMC.getConsole().error(e);
		}
	}

	public boolean isForceRenameResultClass() {
		return forceRenameResultClass;
	}

	public String createEmptyCode(String exception) {
		exception=exception.replace("*/", "*\\/");
		
		return ""+/**~!{*/""
			+ "package " +(resultClassPackage)+ ";"
			+ "\r\n"
			+ "\r\n/**"
			+ "\r\n *  Auto generated code by monalisa<br>"
			+ "\r\n *  This is an empty class for some exception: <br>"
			+ "\r\n *  <code> "
			+ "\r\n *  " +(exception)+ ""
			+ "\r\n *  </code>"
			+ "\r\n */"
			+ "\r\npublic class " +(resultClassName)+ " implements java.io.Serializable{"
			+ "\r\n	private static final long serialVersionUID = 1L;"
			+ "\r\n	final static String  FINGERPRINT = \"00000000\";"
			+ "\r\n}"
		+ "\r\n"/**}*/;
	}
	
	public String createResultJavaCode(DBExchange exchange) {
		String ps = "";
		List<?> params = md.parameters();
		for (Object p : params) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
			String ptype = svd.getType().toString();

			if (ps.length() > 0) {
				ps += ", ";
			}
			ps += unit.getFullName(ptype);
		}

		MetaTable table = exchange.getTable();
		table.setJavaPackage(this.resultClassPackage);
 
		Set<String> imps = new LinkedHashSet<String>();
		boolean importColumn = false;
 		
		for (MetaColumn c : table.getColumns()) {
			MetaTable columnTable=c.getTable();
			if(columnTable!=null){
				imps.add(columnTable.getJavaPackage() + "." + columnTable.getJavaName());
				importColumn = true;
			}
			
			imps.addAll(c.getImports());
 		}
		table.setJavaName(resultClassName);

		if (importColumn) {
			imps.add(Column.class.getName());
		}

		String see = unit.getPackageName() + "." + unit.getUnitName() + "#" + md.getName() + "(" + ps + ")";
		try {
			JspContext request = new JspContext();

			request.setAttribute("table", table);
			request.setAttribute("imports", imps);
			request.setAttribute("see", see);
			request.setAttribute("fingerprint", this.fingerprint);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			DBWriterSelect dbs = new DBWriterSelect();
			dbs.service(request, new PrintWriter(new OutputStreamWriter(bos, "utf-8")));

			return new String(bos.toByteArray(), "utf-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public boolean isChanged() {
		boolean changed = false;

		String fullname = resultClassPackage + "." + resultClassName;
		String fp = unit.getFingerprint(fullname);
		if (fp != null && fingerprint.equals(fp) == false) {
			changed = true;
		}
		return changed;
	}

	public void writeRunMethod(JavaWriter writer) {
		String method = md.getName().toString();
		StringBuffer sp = new StringBuffer();
		for (Object p : md.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
			Type t = svd.getType();
			String v = svd.getName().toString();

			if (sp.length() > 0) {
				sp.append(",");
			}
			sp.append(v);

			method += "_" + t.toString();

			method = MelpJavaBeans.getCamelCaseString(method, false);
		}
		
		method+="$"+index;

		writer.println("public DBExchange " + method + "()throws Exception{");
		writer.println("DBExchange.setExchange(" + index + ");");
		writer.println(unit.getUnitName() + " X = new " + unit.getUnitName() + "();");
 
		ParameterInitialization pi=new ParameterInitialization(md,buildCode);
		String initParameters=pi.getInitParameters();
		String failMessages  =pi.getFailMessages(); 
		writer.write(initParameters);

		writer.println("X." + md.getName() + "(" + sp + "); \r\n");
		
		writer.println("DBExchange exchange=DBExchange.getExchange(true);");
		
		if(initParameters.length()>0){
			writer.println("exchange.putTag(\"initParameters\", \""+
					initParameters.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n")
					+"\");");
		}
		if(failMessages.length()>0){
			writer.println("exchange.putTag(\"failMessages\", \""+
					failMessages.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n")
					+"\");");
		}
		writer.println("return exchange;");
		writer.println("}");
	}
 

	public String getResultClassName() {
		return resultClassName;
	}
	
	public String getResultClassPackage() {
		return this.resultClassPackage;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public MethodDeclaration getMd() {
		return md;
	}

	public void setMd(MethodDeclaration md) {
		this.md = md;
	}

	public SourceUnit getUnit() {
		return unit;
	}

	public void setUnit(SourceUnit unit) {
		this.unit = unit;
	}

	public Annotation getSelectAnnotation() {
		return selectAnnotation;
	}

	public void setSelectAnnotation(Annotation selectAnnotation) {
		this.selectAnnotation = selectAnnotation;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	
	
}
