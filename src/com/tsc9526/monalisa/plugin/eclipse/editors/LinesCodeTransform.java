package com.tsc9526.monalisa.plugin.eclipse.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class LinesCodeTransform {
	public static String regexLines = "\\\"\\s*/\\*\\*(~|=)!?[a-zA-Z_]*\\s*\\{";
	public static String regexJava  = "\\\"\\s*\\+\\s*/\\*\\*(~|=)!?[a-zA-Z_]*\\s*\\{\\s*\\*/[\\s\\S]*?/\\*\\*\\s*\\}\\s*\\*/";

	private static Pattern patternLines(){
		return Pattern.compile(regexLines);
	}

	private static Pattern patternJava(){
		return Pattern.compile(regexJava);
	}

	public static String fromJavaCode(String doc) {
		if (doc == null) {
			return null;
		}

		List<Integer[]> strings=new ArrayList<Integer[]>();
		for(int i=0;i<doc.length();i++){
			char c=doc.charAt(i);
			if(c=='/' && (i+1)<doc.length() ){
				if(doc.charAt(i+1)=='*'){
					i++;
					for(;i<doc.length();i++){
						c=doc.charAt(i);
						if(c=='*' && (i+1)<doc.length() && doc.charAt(i+1)=='/'){
							i++;
							break;
						}
					}
				}else if(doc.charAt(i+1)=='/'){
					i++;
					for(;i<doc.length();i++){
						c=doc.charAt(i);
						if(c=='\n'){
							break;
						}
					}
				}
			}else if(c=='"'){
				int begin=i+1;
			
				i++;
				int splash=0;
				for(;i<doc.length();i++){
					c=doc.charAt(i);
					if(c=='\\'){
						splash++;
					}else{
						if(c=='"' && splash%2==0){
							strings.add(new Integer[]{begin,i-1});
							break;
						}
						splash=0;
					}
				}
			}
		}
		
		
		StringBuilder sb = new StringBuilder("");
 		 
		Matcher matcher = patternJava().matcher(doc);
		int start = 0;
		int p = 0;
		while (matcher.find()) {
			start = matcher.start();
			
			boolean string=false;
			for(Integer[] ss:strings){
				if(start>=ss[0] && start<=ss[1]){
					string=true;
				}
			}
			if(string)continue;
			
			sb.append(doc.substring(p, start+1));

			String b = matcher.group();
			int x1 = b.indexOf("+");
			int x2 = b.indexOf("{");
			
			sb.append(b.substring(x1+1, x2+1));

			boolean trim=b.substring(x1+1, x2+1).indexOf("~")>0;
			
			boolean eval=b.substring(x1+1, x2+1).indexOf("!")>0;;
		 
			int x3 = b.indexOf("*/");
			int x4 = b.lastIndexOf("/**");
			
			String block= b.substring(x3 + 2, x4);
			String[] ls = block.split("\r\n|\n|\r");
			for(int n=0;n<ls.length;n++){
				String v=ls[n];
				
				if(n==1 && trim && ls[0].equals("\"\"")){
					sb.append("\r\n");
				}
				
				StringBuilder padding=new StringBuilder();
				for (int i = 0; i < v.length(); i++) {
					char c = v.charAt(i);
					if(c==' '||c=='\t'){
						padding.append(c);
					}else{
						break;
					}
				} 
			
				boolean padded=false;
				for (int i = 0; i < v.length(); i++) {
					char c = v.charAt(i);
					if (c == '"') { //String start
						StringBuilder line = new StringBuilder();
						
						if(trim &&  !padded){
							padded=true;
							
							String prefix=v.substring(i+1,Math.min(i+1+4,v.length()));
							if(prefix.startsWith("\\r\\n")){
								line.append("\r\n").append(padding);
								i+=4;
							}else if(prefix.startsWith("\\r")){
								line.append("\r").append(padding);
								i+=2;
							}else if(prefix.startsWith("\\n")){
								line.append("\n").append(padding);
								i+=2;
							}else{
								line.append(padding);
							}
						}
						
						for (i=i+1; i < v.length(); i++) {
							char x = v.charAt(i);
							if (x == '"') {
								int k = i - 1;
								while (k>=0 && v.charAt(k) == '\\') {
									k--;
								}

								if ((i - k) % 2 == 1) {
									sb.append(line);
									break;
								}
							}else if(x=='\\'){ 
								if((i+3)<v.length() && v.charAt(i+1)=='r' && v.charAt(i+2)=='\\' && v.charAt(i+3)=='n'){
									line.append("\r\n");
									i+=3;
								}else if((i+1)<v.length() && v.charAt(i+1)=='r'){
									line.append("\r");
									i+=1;
								}else if((i+1)<v.length() && v.charAt(i+1)=='n'){
									line.append("\n");
									i+=1;
								}else if((i+1)<v.length()){
									line.append(v.charAt(i+1));
									i+=1;
								}
							}else if(x=='*' && (i+1)<v.length() && v.charAt(i+1)=='/'){ 
								line.append("*\\/");
								i+=1;
							}else if(x=='$' && eval){
								line.append("$$");
							}else{
								line.append(x);
							}
						}
					}else if(c=='(' && (i+1)<v.length()){  // Eval code
						if(v.charAt(i+1)=='('){
							//(( 寻找对应的结束符: )), 不能简单的indexOf("))"), 因为代码中间也可能会出现: )), 字符串中也可能包含: )) 
							i+=2;
							
							int varStart=i;
							int varEnd=i;
							int cnt=2;
							while(i<v.length()){
								char x=v.charAt(i);
								
								if(x=='"'){
									i++;
									while( i<v.length() && v.charAt(i)!='"'){
										if(v.charAt(i)=='\\'){
											i++;
										}
										
										i++;
									}
									i++;
								}else{
									if(x=='('){
										cnt++; 
									}else if(x==')'){
										cnt--;
									} 
									
									if(cnt>0){
										i++;
									}else{
										varEnd=i+1;
										
										break;
									}
								}
							}
							 
							if(cnt==0){
								sb.append("${").append(v.substring(varStart,varEnd-2)).append("}");
							}
						}else{
							int x=v.indexOf(")",i+1);
							if(x>0){
								String s=v.substring(i+1,x);
								if(s.equals("\"$\"")){
									sb.append("$");
								}else{
									sb.append("$").append(s);
								}
								
								i=x;
							}
						}		 
					}
				}
				
			}
			
			sb.append("}*/");
			p = matcher.end();
		}

		if (p < doc.length()) {
			sb.append(doc.substring(p));
		}

		return sb.toString();
	}

	public static String toJavaCode(String doc) {
		if (doc == null) {
			return null;
		}
 		
		
		StringBuffer sb = new StringBuffer("");

		Matcher matcher = patternLines().matcher(doc);
		int start = 0;
		int end = 0;
		int p = 0;
		while (matcher.find()) {
			start = matcher.start();
	 		
			int z = start-1;
			while (z>=0 && doc.charAt(z) == '\\') {
				z--;
			}
			if ((start-z) % 2 == 0) { // 字符串内的注释
				continue;
			}
			
			if (start >= end) {
				end = doc.indexOf("*/", start);
				if (end > start) {
					int x1 = doc.indexOf("{", start);
					int x2 = end-1;
					for (; x2 > start; x2--) {
						char c = doc.charAt(x2);
						if (c == '}') {
							break;
						} else if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
							break;
						}
					}
					String block = doc.substring(x1 + 1, x2);
					if (x2 == end && block.trim().length() == 0) {
						sb.append(doc.substring(p, end + 2));
						p = end + 2;
					} else {
						String bs=doc.substring(start+1, matcher.end());
						int f1=bs.indexOf("~");
						if(f1<0)f1=bs.indexOf("=");
						int f2=bs.indexOf("{");
						 
						boolean trim=bs.charAt(f1)=='~';
						String type=bs.substring(f1+1,f2).trim();
						
						boolean eval=false;
						if(type.startsWith("!")){
							eval=true;
							type=type.substring(1);
						}
						
						sb.append(doc.substring(p, start));
						sb.append("\"+");
						sb.append(bs);
						sb.append("*/");

						String[] ls = block.split("\r\n|\n|\r");
						boolean firstLN=ls[0].trim().length()==0;
						boolean lastLN =block.endsWith("\n") || block.endsWith("\r");
						 
						Map<String, String> vs=new HashMap<String, String>();
						String firstpadding=null;
						StringBuilder line=new StringBuilder();
						for (int n = 0; n < ls.length; n++) {
							String v = ls[n];
							
							vs.clear();
							if(eval){
								v=processVariables(v,vs);
							}
							
							line.setLength(0);
							
							if(n==0){
								if(firstLN && trim){
									sb.append("\"\"");
									continue;
								}
								
								line.append("\"");
							}else{
								sb.append("\r\n");
								
								String leftpadding="";
								if(trim){
									int m=0;
									for(;m<v.length();m++){
										char x=v.charAt(m);
										if(x!=' ' && x!='\t' && x!='\r' && x!='\n'){
											break;
										}
									}
									
									if(m>0){
										if(firstpadding==null && v.trim().length()>0){
											firstpadding=v.substring(0, m);
										}
										
										if(firstpadding==null){
											line.append(v.substring(0, m));
											
											v=v.substring(m);
										}else{
											int min=Math.min(firstpadding.length(),m);
											line.append(v.substring(0, min));
											
											v=v.substring(min);
											 
										}
									}
								}
								line.append("+ ");
								
								if(firstLN && trim && n==1){
									line.append("\"");
								}else{
									line.append("\"\\r\\n");
									line.append(leftpadding);
								}
							}
							 
							for(int m=0;m<v.length();m++){
								char x=v.charAt(m);
								if(x=='\\'){
									line.append("\\\\");
								}else if(x=='"'){
									line.append("\\\"");
								}else if(x=='*' && (m+2)<v.length() && v.charAt(m+1)=='\\' && v.charAt(m+2)=='/'){
									line.append("*/");
									m+=2;
								}else{
									line.append(x);
								}
							}
							  
							
							if(line.toString().trim().length()==0 && n==ls.length-1){
								line.setLength(0);
							}
							
							if(eval){
								sb.append(replaceVariables(line,vs));
							}else{
								sb.append(line);
							}
							
							
							sb.append("\"");
							
							if(n==ls.length-1 && lastLN){
								sb.append("\r\n+ \"\\r\\n\"");
							}
						}
						
						sb.append("/**}*/");

						p = end + 2;
					}
				} else {
					break;
				}
			}
		}

		if (p < doc.length()) {
			sb.append(doc.substring(p));
		}

		return sb.toString();
	}
	
	private static String replaceVariables(StringBuilder line,Map<String, String> vs){
		StringBuilder sb=new StringBuilder();
		 
		for(int i=0;i<line.length();i++){
			char c=line.charAt(i);
			if(c=='$' && (i+1)<line.length() && line.charAt(i+1)=='{'){
				int x=line.indexOf("}",i+2);
				String key=line.substring(i+2,x);	 
				String value=vs.get(key);
				if(value.length()==0){
					value="\"$\"";
				}
				
				if(key.startsWith("A")){
					sb.append("\" +((").append(value).append("))+ \"");
				}else{
					sb.append("\" +(").append(value).append(")+ \"");
				}
				
				i=x;
			}else{
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	private static String processVariables(String line,Map<String, String> vs){
		StringBuilder sb=new StringBuilder();
		
		int varIndex=0;
		for(int i=0;i<line.length();i++){
			char c1=line.charAt(i);
			if(c1=='$' && (i+1)<line.length()){
				char c2=line.charAt(i+1);
				if(c2=='$'){//双$ 表示一个$
					sb.append("$");
					i++;
				}else if(c2=='{'){//${...}
					int x2=line.indexOf("}",i+1);
					if(x2>0){
						String varKey="A"+varIndex++;
						sb.append("${"+varKey+"}");
						vs.put(varKey,line.substring(i+2,x2));
						 	
						i=x2;
					}else{
						sb.append(c1);
					}
				}else if( (c2>='a' && c2<='z') || (c2>='A' && c2<='Z') || c1=='_'){
					StringBuilder var=new StringBuilder();
					var.append(c2);
					
					i+=2;
					while(i<line.length()){
						char c3=line.charAt(i);
						
						if( (c3>='a' && c3<='z') || (c3>='A' && c3<='Z') || (c3>='0' && c3<='9') || c3=='_'){
							var.append(c3);
						}else{
							i--;
							
							break;
						}
						
						i++;
					}
					
					String varKey="B"+varIndex++;
					sb.append("${"+varKey+"}");
					vs.put(varKey,var.toString());
				}else{//$ 单独一个
					vs.put("", "");
					sb.append("${}");
				}
			}else{
				sb.append(c1);
			}
		}
		
		return sb.toString();
	}

	public static List<Integer[]> getLinesArray(String doc) {
		List<Integer[]> linesArray = new ArrayList<Integer[]>();
		Matcher matcher = patternLines().matcher(doc);
		int start = 0;
		int end = 0;
		while (matcher.find()) {
			start = matcher.start();
			
			int z = start-1;
			while (z>=0 && doc.charAt(z) == '\\') {
				z--;
			}
			if ((start-z) % 2 == 0) { // 字符串内的注释
				continue;
			}
			
			if (start >= end) {
				end = doc.indexOf("*/", start);
				if (end > start) {
					int x = doc.indexOf("{", start);
					String content = doc.substring(x + 1, end);
					if (content.trim().length() == 0) {
						int block_end = doc.indexOf("*/", end + 2);
						if (block_end > 0) {
							end = block_end;
						}
					}
					linesArray.add(new Integer[] { start, end + 1 });
				}
			}
		}

		return linesArray;
	}
}
