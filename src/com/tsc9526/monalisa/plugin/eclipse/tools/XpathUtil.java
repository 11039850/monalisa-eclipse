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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class XpathUtil
{
	public static boolean xpathBool(Node node, String expression) throws XPathExpressionException
	{
		return ((Boolean)evaluateXpath(expression, node, XPathConstants.BOOLEAN, null)).booleanValue();
	}

	public static String xpathString(Node node, String expression)
		throws XPathExpressionException
	{
		return (String)evaluateXpath(expression, node, XPathConstants.STRING, null);
	}

	public static Node xpathNode(Node node, String expression) throws XPathExpressionException
	{
		return (Node)evaluateXpath(expression, node, XPathConstants.NODE, null);
	}

	public static NodeList xpathNodes(Node node, String expression)
		throws XPathExpressionException
	{
		return xpathNodes(node, expression, null);
	}

	public static NodeList xpathNodes(Node node, String expression, NamespaceContext nsContext)
		throws XPathExpressionException
	{
		return (NodeList)evaluateXpath(expression, node, XPathConstants.NODESET, nsContext);
	}

	public static Object evaluateXpath(String expression, Object node, QName returnType,
		NamespaceContext nsContext) throws XPathExpressionException
	{
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		if (nsContext != null)
		{
			xpath.setNamespaceContext(nsContext);
		}
		return xpath.evaluate(expression, node, returnType);
		// XPathExpression xpathExpr = xpath.compile(expression);
		// return xpathExpr.evaluate(node, returnType);
	}
}
