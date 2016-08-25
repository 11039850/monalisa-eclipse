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
package com.tsc9526.monalisa.plugin.eclipse.cleanup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.text.edits.MultiTextEdit;

import com.tsc9526.monalisa.orm.datasource.DbProp;
import com.tsc9526.monalisa.plugin.eclipse.generator.JavaBeanGenerator;
import com.tsc9526.monalisa.plugin.eclipse.generator.SelectGenerator;
import com.tsc9526.monalisa.plugin.eclipse.generator.SourceGenerator;
import com.tsc9526.monalisa.plugin.eclipse.generator.SourceUnit;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class SQLCleanUpFix implements ICleanUpFix{
	private CompilationUnit  compilationUnit;
	private ICompilationUnit copy;
	 
	public SQLCleanUpFix(CompilationUnit compilationUnit,ICompilationUnit copy){
		this.compilationUnit=compilationUnit;	
		this.copy=copy;
	}
	 
	public CompilationUnitChange createChange(IProgressMonitor p)throws CoreException {
		IFile file=(IFile)copy.getResource();
		
		MultiTextEdit edit=new MultiTextEdit();
		
		if(isTypeDeclaration(compilationUnit)){
			SourceUnit    unit=new SourceUnit(compilationUnit,copy.getBuffer().getContents());
			
			String projectPath = unit.getProjectPath();
			DbProp.SET_CFG_ROOT_PATH(projectPath);
			
			for(SourceGenerator sg:getSourceGenerators()){
				sg.generate(unit, edit);
			}
		}
		
		CompilationUnitChange unitChange=new CompilationUnitChange(file.getName(),copy);
		unitChange.setEdit(edit);
		 	
		return unitChange;		 
	}
	
	private boolean isTypeDeclaration(CompilationUnit unit){
		if(unit.types().size()>0 && unit.types().get(0) instanceof TypeDeclaration){
			return true;
		}else{
			return false;
		}
	}
	
	private List<SourceGenerator> getSourceGenerators(){
		List<SourceGenerator> sgs=new ArrayList<SourceGenerator>();
		sgs.add(new SelectGenerator());
		sgs.add(new JavaBeanGenerator());
		return sgs;
	}
}
