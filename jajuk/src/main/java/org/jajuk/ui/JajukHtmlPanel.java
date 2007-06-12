/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $Revision$
 */

package org.jajuk.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xamjwg.html.HtmlParserContext;
import org.xamjwg.html.HtmlRendererContext;
import org.xamjwg.html.gui.HtmlPanel;
import org.xamjwg.html.parser.DocumentBuilderImpl;
import org.xamjwg.html.parser.InputSourceImpl;
import org.xamjwg.html.test.SimpleHtmlParserContext;
import org.xamjwg.html.test.SimpleHtmlRendererContext;

/**
 * Type description
 */
public class JajukHtmlPanel extends HtmlPanel implements ITechnicalStrings {

	private static final long serialVersionUID = -4033441908072591661L;

	public void setURL(URL url) throws Exception {
		File page = new File(Util.getConfFileByPath(FILE_IMAGE_CACHE).getAbsolutePath() + '/'
				+ Util.getOnlyFile(url.toString() + ".html"));
		String sPage = new String(DownloadManager.downloadUrl(url));
		// Remove scripting
		int index = -1;
		StringBuffer sb = new StringBuffer(sPage);
		do {
			index = sb.indexOf("<script");
			if (index > 0) {
				sb.delete(index, sb.indexOf("</script>") + 9);
			}
		} while (index > 0);
		sPage = sb.toString();

		/*
		 * Download images (we can't use Cobra itself as it doesn't support
		 * proxying) index = -1; StringBuffer sb = new StringBuffer(sPage); do{
		 * index = sb.indexOf("<img.*src="); if (index > 0){
		 * sb.delete(index,sb.indexOf("</script>")+9); } } while (index > 0);
		 * sPage = sb.toString();
		 */

		// Write the page itself
		BufferedWriter bw = new BufferedWriter(new FileWriter(page));
		bw.write(sPage);
		bw.flush();
		bw.close();

		// A Reader should be created with the correct charset,
		// which may be obtained from the Content-Type header
		// of an HTTP response.
		Reader reader = new InputStreamReader(new FileInputStream(page), "UTF-8");
		// InputSourceImpl constructor with URI recommended
		// so the renderer can resolve page component URLs.
		InputSourceImpl is = new InputSourceImpl(reader, page.getAbsolutePath());

		HtmlParserContext context = new SimpleHtmlParserContext();
		HtmlRendererContext rcontext = new SimpleHtmlRendererContext(this);
		// Note that document builder should receive both contexts.
		DocumentBuilderImpl dbi = new DocumentBuilderImpl(context, rcontext);
		// A documentURI should be provided to resolve relative
		// URIs.
		Document document = dbi.parse(is);
		org.w3c.dom.Element root = document.getDocumentElement();
		NodeList liste = root.getElementsByTagName("img");
        for(int i=0; i<liste.getLength(); i++){
        	Element e = (Element)liste.item(i);
        	//Remove local images
        	String src = e.getAttribute("src");
        	if (!src.startsWith("http")){
        		e.getParentNode().removeChild(e);
        	}
        	//Download images
        	File img = new File(Util.getConfFileByPath(FILE_IMAGE_CACHE).getAbsolutePath() + '/'
    				+ Util.getOnlyFile(src));
        	DownloadManager.download(new URL(src), img);
        	e.setAttribute("src", "file://"+img.getAbsolutePath());
        }
    	// Now set document in panel. This is what causes the
		// document to render.
		setDocument(document, rcontext);
	}
}
