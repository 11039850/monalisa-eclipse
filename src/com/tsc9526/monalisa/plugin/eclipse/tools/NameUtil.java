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
package com.tsc9526.monalisa.plugin.eclipse.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class NameUtil
{
	/**
	 * @param packageName
	 * @param simpleTypeName
	 * @param enclosingTypeNames
	 * @param useDollarForInnerClassSeparator Simply put, MyBatis uses '$' and Eclipse uses '.'.
	 *          In other words, pass <code>true</code> for auto-completion and <code>false</code>
	 *          for validation.
	 * @return
	 */
	public static String buildQualifiedName(char[] packageName, char[] simpleTypeName,
		char[][] enclosingTypeNames, boolean useDollarForInnerClassSeparator)
	{
		final char innerClassSeparator = useDollarForInnerClassSeparator ? '$' : '.';
		StringBuilder typeFqn = new StringBuilder().append(packageName).append('.');
		for (char[] enclosingTypeName : enclosingTypeNames)
		{
			typeFqn.append(enclosingTypeName).append(innerClassSeparator);
		}
		typeFqn.append(simpleTypeName).toString();
		return typeFqn.toString();
	}

	public static String stripTypeArguments(String src)
	{
		int idx = src.indexOf('<');
		return idx == -1 ? src : src.substring(0, idx);
	}

	public static List<String> extractTypeParams(String src)
	{
		int paramPartStart = src.indexOf('<');
		int paramPartEnd = src.lastIndexOf('>');
		if (paramPartStart == -1 || paramPartEnd == -1 || paramPartEnd - paramPartStart < 2)
			return Collections.emptyList();

		List<String> result = new ArrayList<String>();
		int nestedParamLevel = 0;
		int markStart = paramPartStart + 1;
		for (int i = paramPartStart + 1; i < paramPartEnd; i++)
		{
			char c = src.charAt(i);
			if (nestedParamLevel == 0 && c == ',')
			{
				result.add(src.substring(markStart, i));
				markStart = i + 1;
			}
			else if (c == '<')
			{
				nestedParamLevel++;
			}
			else if (c == '>')
			{
				nestedParamLevel--;
			}
		}
		if (markStart < paramPartEnd)
			result.add(src.substring(markStart, paramPartEnd));
		return result;
	}

	private NameUtil()
	{
	}
}
