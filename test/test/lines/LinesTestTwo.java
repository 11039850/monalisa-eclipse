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


/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class LinesTestTwo {

	public static void main(String[] args) {
		String str = ""+/**~{*/""
			+ "\\\"\\s*/\\*\\*(~|=)!?[a-zA-Z_]*\\s*\\{"
		+ "\r\n"/**}*/
		;

		System.out.println(str);
		System.out.println("==================================================");

		System.out.println(""+/**~{*/""
			+ "SELECT *"
			+ "\r\n	FROM user"
			+ "\r\n	WHERE name=\"zzg.zhou\""
		+ "\r\n"/**}*/
		);
		System.out.println("==================================================");

		System.out.println(""+/**={*/""
+ "\r\n			SELECT *"
+ "\r\n				FROM user"
+ "\r\n				WHERE name=\"zhou\""
+ "\r\n		"/**}*/
		);
		System.out.println("==================================================");
	}

}
