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
package com.tsc9526.monalisa.plugin.eclipse.jdt;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class ProblemDetail {
	 
	private CompilationResult result;
	
	public ProblemDetail(CompilationResult result){
		this.result=result;
	}
	
	public String getDetails(){
		StringBuilder sb=new StringBuilder();
		if (result.hasErrors()) {
			for (IProblem problem : result.getErrors()) {
				sb.append(getDetail(problem));
				break;
			}
		}
		
		return sb.toString();
	}
	
	private String getDetail(IProblem problem){
		StringBuilder sb=new StringBuilder();
		
		String className = new String(problem.getOriginatingFileName()).replace("/", ".");
		className = className.substring(0, className.length() - 5);
		String message = problem.getMessage();
		if (problem.getID() == IProblem.CannotImportPackage) {
			message = problem.getArguments()[0] + " cannot be resolved";
		}
		 
		sb.append(className + ":" + message+", Source: ");
		sb.append("\r\n==============================================\r\n");
		
		int start=problem.getSourceStart();
		int end=problem.getSourceEnd();
		char[] contents=result.getCompilationUnit().getContents();
		 
		if(end>start && start>=0 && end<contents.length){
			int lineOffset =-1;
			for(int i=0;i<=start;i++){
				if(contents[i]=='\n'){
					lineOffset=-1;
				}else{
					lineOffset++;
				}
			}		
			 
			int lineStart  =0;
			for(int i=start;i<contents.length;i++){
				if(contents[i]=='\n'){
					lineStart=i+1;
					break;
				}
			}
			
			int minEnd=end;
			for(int i=start;i<=end;i++){
				if(contents[i]=='\n'){
					minEnd=i;
					break;
				}
			}
			
			sb.append(new String(contents,0,lineStart));
			
			for(int i=0;i<lineOffset;i++){
				sb.append(" ");
			}
			
			for(int i=0;i<=(minEnd-start);i++){
				sb.append("^");
			}
			
			sb.append(" <--- "+message+"\r\n");
			 
			sb.append(new String(contents,lineStart,contents.length-lineStart));
		}else{
			sb.append(new String(contents));
		}
		
		sb.append("\r\n=====================================================\r\n");
		
		return sb.toString();
	}
}
