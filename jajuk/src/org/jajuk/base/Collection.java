/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * $Log$
 * Revision 1.1  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
 */
package org.jajuk.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 *  Items root container
 *<p> Singletton
 * @author     bflorat
 * @created    16 oct. 2003
 */
public class Collection implements TechnicalStrings{
	/**Self instance*/
	private static Collection collection;
	
	/**Instance getter*/
	public static Collection getInstance(){
		if (collection == null){
			collection = new Collection();
		}
		return collection;
	}

	/**Hidden constructor*/
	private Collection(){
			
	}
	
	/** Write current collection to collection file for persistence between sessions*/
	public static void commit() throws IOException{
		BufferedWriter bw=new BufferedWriter(new FileWriter(new File(FILE_COLLECTION)));
		bw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
		bw.write("<collection>\n");
			bw.write("\t<types>\n");
				Iterator it = TypeManager.getTypes().iterator();
				while (it.hasNext()){
					Type type =(Type)it.next();
					bw.write(type.toXml());		
				}
			bw.write("\t</types>\n");
		bw.write("</collection>\n");
		bw.flush();
		bw.close();
	}
	
	

}

