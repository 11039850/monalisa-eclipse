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
package test.lines;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tsc9526.monalisa.plugin.eclipse.editors.LinesCodeTransform;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@Test
public class LinesTest {
 
	public void testTranslate1(){
		 
		String doc="\"\"/**~{\r\n"
		+ "\t\tHello\r\n"
		+ "\t\tworld!\r\n"
		+ "\t}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~{*/\"\""
				+"\r\n\t\t+ \"Hello\""
				+"\r\n\t\t+ \"\\r\\nworld!\""
				+"\r\n\t+ \"\\r\\n\"/**}*/"		
				);
	}
	 
	
	public void testTranslate2(){
		String doc="\"\"/**~{\r\n"
		+ "\t\tHello\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~{*/\"\""
				+"\r\n\t\t+ \"Hello\""
				+"\r\n\t\t+ \"\\r\\nworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/"		
				);
	}
	
	
	public void testTranslate3(){
		String doc="\"\"/**~{Hello\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~{*/\"Hello\""
				+"\r\n\t\t+ \"\\r\\nworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/"		
				);
	}
	
	
	public void testTranslate4(){
		String doc="\"\"/**~{\r\n"
		+ "\tHello\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~{*/\"\""
				+"\r\n\t+ \"Hello\""
				+"\r\n\t+ \"\\r\\n\tworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/"		
				);
	}
	
	
	
	public void testTranslate5(){
		String doc="\"\"/**~!{\r\n"
		+ "\tHello $name\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~!{*/\"\""
				+"\r\n\t+ \"Hello \" +(name)+ \"\""
				+"\r\n\t+ \"\\r\\n\tworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/"		
				);
	}
	
	public void testTranslate6(){
		String doc="\"\"/**~!{\r\n"
		+ "\tHello ${name}\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~!{*/\"\""
				+"\r\n\t+ \"Hello \" +((name))+ \"\""
				+"\r\n\t+ \"\\r\\n\tworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/"		
				);
	}
	
	public void testTranslate7(){
		String doc="\"\"/**~!{\r\n"
		+ "\tHello $$name ${name}\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String r=LinesCodeTransform.toJavaCode(doc);
		Assert.assertEquals(r,
				"\"\"+/**~!{*/\"\""
				+"\r\n\t+ \"Hello $name \" +((name))+ \"\""
				+"\r\n\t+ \"\\r\\n\tworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/"		
				);
	}
	
	
	public void testTranslate8(){
		String lines="\"\"/**~!{\r\n"
		+ "\tHello $$name ${name==null?(\"\"):(name+1)}\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String java="\"\"+/**~!{*/\"\""
				+"\r\n\t+ \"Hello $name \" +((name==null?(\"\"):(name+1)))+ \"\""
				+"\r\n\t+ \"\\r\\n\tworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/";
		 
		Assert.assertEquals(LinesCodeTransform.toJavaCode(lines),java); 
		
		Assert.assertEquals(LinesCodeTransform.fromJavaCode(java),lines); 
	}
	
	
	public void testTranslate9(){
		String lines="\"\"/**~!{\r\n"
		+ "\tHello $ $$name ${name==null?(\"\"):(name+1)}\r\n"
		+ "\t\tworld!\r\n"
		+ "}*/";
		
		String java="\"\"+/**~!{*/\"\""
				+"\r\n\t+ \"Hello \" +(\"$\")+ \" $name \" +((name==null?(\"\"):(name+1)))+ \"\""
				+"\r\n\t+ \"\\r\\n\tworld!\""
				+"\r\n+ \"\\r\\n\"/**}*/";
		  
		Assert.assertEquals(LinesCodeTransform.toJavaCode(lines),java); 
		
		Assert.assertEquals(LinesCodeTransform.fromJavaCode(java),lines); 
	}
	
	public void testLines()throws Exception{
		Assert.assertEquals(
			""+/**~{*/""
				+ "Hello"
				+ "\r\nworld!"/**}*/,
			"Hello\r\nworld!");
		
		Assert.assertEquals(
			""+/**~{*/""
				+ "Hello"
				+ "\r\nworld!"
			+ "\r\n"/**}*/,
			"Hello\r\nworld!\r\n");
		
		
		Assert.assertEquals(
			""+/**~{*/""
				+ "Hello  */"
				+ "\r\nworld!"
			+ "\r\n"/**}*/,
			"Hello  */\r\nworld!\r\n");
		
		Assert.assertEquals(
			""+/**~{*/""
				+ "Hello  */"
				+ "\r\n	world!"
			+ "\r\n"/**}*/,
				"Hello  */\r\n\tworld!\r\n");
		
		Assert.assertEquals(
			""+/**~{*/""
				+ "Hello  */"
				+ "\r\nThis is a \"line\" string"
				+ "\r\nworld!"
			+ "\r\n"/**}*/,
			"Hello  */\r\nThis is a \"line\" string\r\nworld!\r\n");
	}
}
