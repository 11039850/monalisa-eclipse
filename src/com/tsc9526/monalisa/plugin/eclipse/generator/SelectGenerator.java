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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.swt.SWT;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import com.tsc9526.monalisa.orm.Query;
import com.tsc9526.monalisa.orm.annotation.Select;
import com.tsc9526.monalisa.orm.datatable.DataMap;
import com.tsc9526.monalisa.orm.datatable.DataTable;
import com.tsc9526.monalisa.orm.datatable.Page;
import com.tsc9526.monalisa.orm.tools.generator.DBExchange;
import com.tsc9526.monalisa.orm.tools.helper.Helper;
import com.tsc9526.monalisa.plugin.eclipse.console.HyperLink;
import com.tsc9526.monalisa.plugin.eclipse.console.MMC;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class SelectGenerator implements SourceGenerator {
	private static Set<String> collectionSets = new HashSet<String>() {
		private static final long serialVersionUID = 7272496460388839701L;
		{
			add(List.class.getSimpleName());
			add(List.class.getName());

			add(Page.class.getSimpleName());
			add(Page.class.getName());

			add(DataTable.class.getSimpleName());
			add(DataTable.class.getName());
		}
	};

	private SourceUnit unit;
	private MultiTextEdit edit = new MultiTextEdit();

	private List<SelectMethod> methods = new ArrayList<SelectMethod>();
	private Set<String> referTypes = new HashSet<String>();

	public SelectGenerator() {
	}

	public void generate(SourceUnit unit, MultiTextEdit edit) {
		this.unit = unit;
		this.edit = edit;

		// org.jboss.tools.ws.jaxrs.core.jdt.Annotation
		// db=this.unit.findClassAnnotation(DB.class.getName());
		// if(db!=null){
		// dbClass=db.getValues("url").get(0);

		doGenerate();
		// };
	}

	private void doGenerate() {
		try {
			// Get @Select methods
			findSelectMethods();

			if (methods.size() > 0) {
				IResource r = unit.getUnit().getJavaElement().getResource();
				String filePath = unit.getProjectPath() + "/" + r.getProjectRelativePath();

				MMC mmc = MMC.getConsole();
				mmc.print(Helper.getTime() + " [I] ****** Starting generate result classes from: ", SWT.COLOR_BLACK);
				mmc.print(new HyperLink("file://" + filePath, unit.getPackageName() + "." + unit.getUnitName()), SWT.COLOR_DARK_BLUE);
				mmc.print(" ******\r\n", SWT.COLOR_BLACK);	
				
				// Run select methods
				SelectRun run = new SelectRun(unit);
				List<DBExchange> exchanges = run.run(methods);

				delayGenerate(exchanges);
			}
		} catch (Exception e) {
			MMC.getConsole().error(e);
		}
	}

	private void delayGenerate(final List<DBExchange> exchanges) {
		if (exchanges.size() > 0) {
			Job job = new WorkspaceJob("Monalisa building ...") {
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					try {
						writeResultClasses(exchanges);
					} catch (Exception e) {
						MMC.getConsole().error(e);
					}
					return Status.OK_STATUS;
				}
			};
			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
			job.schedule();
		}
	}

	private void writeResultClasses(List<DBExchange> exchanges) throws JavaModelException {
		for (DBExchange exchange : exchanges) {
			writeResultClass(exchange);
		}
	}

	private void writeResultClass(DBExchange exchange) throws JavaModelException {
		SelectMethod method = methods.get(exchange.getIndex());
		String className = method.getResultClassPackage() + "." + method.getResultClassName();

		IPackageFragmentRoot pfr = unit.getPackageFragmentRoot();

		IResource r = unit.getUnit().getJavaElement().getResource();
		String linkMethodUrl = "file://" + unit.getProjectPath() + "/" + r.getProjectRelativePath() + "#" + method.getMd().getName();
		String linkMethodText = "";

		String linkClassUrl = "file://" + unit.getProjectPath() + "/" + pfr.getResource().getProjectRelativePath();
		linkClassUrl += "/" + className.replace(".", "/") + ".java";

		String linkClassText = className;

		List<?> params = method.getMd().parameters();
		for (Object p : params) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
			String ptype = svd.getType().toString();
			linkMethodUrl += "," + ptype;

			if (linkMethodText.length() > 0) {
				linkMethodText += ", ";
			}
			linkMethodText += ptype;
		}

		linkMethodText = method.getMd().getName() + "(" + linkMethodText + ")";

		MMC mmc = MMC.getConsole();
		mmc.print(Helper.getTime() + " [I] ", SWT.COLOR_BLACK);
		mmc.print("Create class: ", SWT.COLOR_BLACK);
		mmc.print(new HyperLink(linkClassUrl, linkClassText), SWT.COLOR_DARK_BLUE);
		mmc.print(", from: [", SWT.COLOR_BLACK);
		mmc.print(new HyperLink(linkMethodUrl, linkMethodText), SWT.COLOR_DARK_BLUE);
		mmc.print("]\r\n", SWT.COLOR_BLACK);
		
		String initParameters=(String)exchange.getTag("initParameters");
		String failMessages  =(String)exchange.getTag("failMessages");
		if(failMessages!=null){
			mmc.warn("Can't init parameters as default:\r\n"+failMessages);
		}
		if(initParameters!=null){
			mmc.code("Init parameters as following:",initParameters);
		}
		
		if(exchange.getSql()!=null){
			mmc.code("Running SQL:",exchange.getSql());
		}
		
		IPackageFragment pf = pfr.createPackageFragment(method.getResultClassPackage(), true, null);
		if (exchange.getErrorString() == null) {
			String java = method.createResultJavaCode(exchange);
			pf.createCompilationUnit(method.getResultClassName() + ".java", java, true, new NullProgressMonitor());
		}else{
			mmc.error(exchange.getErrorString());
			
			String java =method.createEmptyCode(exchange.getErrorString()); 
			pf.createCompilationUnit(method.getResultClassName() + ".java", java, true, new NullProgressMonitor());
		}
	}
	 
	private void addSelectMethod(SelectMethod sm) {
		sm.setIndex(methods.size());
		methods.add(sm);
	}

	private void findSelectMethods() {
		Set<String> imps = new HashSet<String>();

		for (MethodDeclaration md : unit.getUnitType().getMethods()) {
			Type rt = md.getReturnType2();

			if (rt == null)
				continue;

			String returnClazz = rt.toString();
			Set<String> returnParameter = new HashSet<String>();

			if (rt.isParameterizedType()) {
				ParameterizedType ptype = (ParameterizedType) rt;
				returnClazz = ptype.getType().toString();
				for (Object arg : ptype.typeArguments()) {
					returnParameter.add(arg.toString());

					referTypes.add(arg.toString());
				}
			} else {
				referTypes.add(returnClazz);
			}

			Annotation a = unit.getAnnotation(md, Select.class);
			if (a != null) {
				SelectMethod sm = new SelectMethod(unit, md, a);
				String rcn = sm.getResultClassName();

				String newReturnType = null;
				if (isCollectionType(returnClazz)) {
					String ps = returnParameter.size() > 0 ? returnParameter.iterator().next() : "";
					if (ps.equals("") || isObjectOrDataMap(ps)) {
						newReturnType = returnClazz + "<" + rcn + ">";
						imps.add(sm.getResultClassPackage() + "." + rcn);
					} else {
						if (rcn.equals(ps) == false && sm.isForceRenameResultClass()) {
							newReturnType = returnClazz + "<" + rcn + ">";
							imps.add(sm.getResultClassPackage() + "." + rcn);
						}
					}
				} else if (isObjectOrDataMap(returnClazz)) {
					newReturnType = rcn;
					imps.add(sm.getResultClassPackage() + "." + rcn);
				} else {
					if (rcn.equals(returnClazz) == false && sm.isForceRenameResultClass()) {
						newReturnType = rcn;
						imps.add(sm.getResultClassPackage() + "." + rcn);
					}
				}

				if (newReturnType != null) {
					QueryRewriteVisitor rewrite = new QueryRewriteVisitor(rcn);
					md.accept(rewrite);

					List<ReplaceEdit> changes = rewrite.getChanges();
					changes.add(new ReplaceEdit(rt.getStartPosition(), rt.getLength(), newReturnType));

					for (ReplaceEdit re : changes) {
						edit.addChild(re);
					}
					sm.calculateFingerprint(changes);

					addSelectMethod(sm);
				} else if (sm.isChanged()) {
					addSelectMethod(sm);
				}
			}
		}

		if (imps.size() > 0) {
			TextEdit importEdit = unit.createImports(imps);
			edit.addChild(importEdit);
		}
	}

	private boolean isObjectOrDataMap(String ps) {
		return ps.equals("Object") || ps.equals("java.lang.Object") || ps.equals(DataMap.class.getSimpleName()) || ps.equals(DataMap.class.getName());
	}

	private boolean isCollectionType(String returnClazz) {
		return collectionSets.contains(returnClazz);
	}

	class QueryRewriteVisitor extends ASTVisitor {
		private String parameterType;
		private String queryVarName;
		private List<ReplaceEdit> changes = new ArrayList<ReplaceEdit>();

		public QueryRewriteVisitor(String parameterType) {
			this.parameterType = parameterType;
		}

		public List<ReplaceEdit> getChanges() {
			return changes;
		}

		public boolean visit(ReturnStatement node){ 
			node.accept(new ASTVisitor(){
				public boolean visit(MethodInvocation m) {
					String var=""+m.getExpression();
					if(var.equals(queryVarName)){
						String name=m.getName().toString();
						if(name.equals("getResult")){
							rewriteGetResult(m);
						}else if(name.equals("getList")){
							rewriteGetList(m);
						}else if(name.equals("getPage")){
							rewriteGetPage(m);
						}
					}					
					return true;
				}
			});
			return true;
		}
		
		
		private void rewriteGetResult(MethodInvocation m){
			List<?> args=m.arguments();
			
			boolean retwite=false;
			if(args==null || args.size()==0){
				retwite=true;
			}else if(args!=null && args.size()==1){
				String s=args.get(0).toString();
				if(!s.equals(parameterType + ".class")){
					retwite=true;
				}
			}
			
			if(retwite){
				changes.add(new ReplaceEdit(m.getStartPosition(), m.getLength(),queryVarName + ".getResult(" + parameterType + ".class)"));
			}
		}
		
		private void rewriteGetList(MethodInvocation m){
			rewriteGetPageOrList(m,"getList");
		}
		
		private void rewriteGetPage(MethodInvocation m){
			rewriteGetPageOrList(m,"getPage");
		}
		
		private void rewriteGetPageOrList(MethodInvocation m,String methodName){
			List<?> args=m.arguments();
			
			if(args==null || args.size()<=1){
				boolean retwite=false;
				if(args==null || args.size()==0){
					retwite=true;
				}else if(args!=null && args.size()==1){
					String s=args.get(0).toString();
					if(s.endsWith(".class") && !s.equals(parameterType + ".class")){
						retwite=true;
					}
				}
				if(retwite){
					changes.add(new ReplaceEdit(m.getStartPosition(), m.getLength(),queryVarName + "."+methodName+"(" + parameterType + ".class)"));
				}
			}else if(args!=null && args.size()>=2){
				boolean retwite=false;
				
				String ps=", ";
				if(args.size()==2){
					ps+=args.get(0)+", "+args.get(1);
					
					retwite=true;
				}else if(args.size()==3){
					ps+=args.get(1)+", "+args.get(2);
					
					String s=""+args.get(0);
					if(s.endsWith(".class") && !s.equals(parameterType + ".class")){
						retwite=true;
					}
				}
				
				if(retwite){
					changes.add(new ReplaceEdit(m.getStartPosition(), m.getLength(),queryVarName + "."+methodName+"(" + parameterType + ".class"+ps+")"));
				}
			}
		}

		public boolean visit(VariableDeclarationStatement node) {
			Type type = node.getType();
			String typeString = "" + type;

			String query = unit.getFullName(typeString);
			if (query.equals(Query.class.getName())) {
				VariableDeclarationFragment var=(VariableDeclarationFragment)node.fragments().get(0);
				queryVarName = var.getName().toString();

				//rewriteQueryDeclaration(node);
			}
			return true;
		}

		void rewriteQueryDeclaration(VariableDeclarationStatement node) {
			List<?> fragments = node.fragments();
			if (fragments.size() > 0) {
				Object object = fragments.get(0);
				if (object instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) object;
					Expression expr = vdf.getInitializer();
					if (expr instanceof ClassInstanceCreation) {
						ClassInstanceCreation cic = (ClassInstanceCreation) expr;
						List<?> args = cic.arguments();
						if (args.size() == 1) {
							String a = args.get(0).toString();
							if (!a.endsWith(".class") && !a.endsWith("Class")) {
								changes.add(new ReplaceEdit(expr.getStartPosition(), expr.getLength(), "new " + queryVarName + "(" + a + ", " + parameterType
										+ ".class)"));
							}
						} else {
							changes.add(new ReplaceEdit(expr.getStartPosition(), expr.getLength(), "new " + queryVarName + "(" + parameterType + ".class)"));
						}
					}
				}
			}
		}
	}

}
