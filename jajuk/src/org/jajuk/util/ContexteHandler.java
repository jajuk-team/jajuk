/*
 *  Jajuk
 *  Copyright (C) 2003 sgringoi
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
 * Revision 1.1  2003/10/24 15:19:41  sgringoi
 * Initial commit
 *
 *
 */
package org.jajuk.util;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Sébastien Gringoire
 *
 * Handler utilisé pour lire un flux XML et en faire un Contexte Portail.
 */
class ContexteHandler
	extends DefaultHandler
{
	/** Contexte racine en cours de construction. */
	private Contexte contexteRacine = null;
	/** Pile contenant les contextes parcourus par le parsing */
	private Stack pileContextes = null;
	
	/**
	 * Constructeur
	 */
	ContexteHandler()
	{
		super();
	}
	/**
	 * Renvoie le contexte créé après le parsing du flux XML.
	 * @return Contexte - Renvoie le contexte construit.
	 */
	Contexte getContexte()
	{
		return contexteRacine;
	}

	/**
	 * Méthode invoquée par le parser SAX lorsque celui-ci rencontre un tag d'ouverture.
	 *
	 * @param pUri - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param pLocalName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param pQName - The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param pAttrs - The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object
	 */
	public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs)
	{
			// Création d'un contexte pour stocker l'élément en cours de parsing
		Contexte contexte = new Contexte(pQName);
		if (contexteRacine == null)
		{
				// On lit le noeud racine du flux
			contexteRacine = contexte;
			pileContextes = new Stack();
		} else {
				// Insertion de l'élément dans l'arborescence
			Contexte contexteCourant = (Contexte)pileContextes.peek();
			contexteCourant.inserer(pQName, contexte);
		}
		
			// On empile le contexte courant
		pileContextes.push(contexte);
		
			// Parcourt des attributs et ajout comme éléments fils
		for (int i = 0; i<pAttrs.getLength(); i++)
		{
			contexte.insererAttribut(pAttrs.getQName(i), pAttrs.getValue(i));
		}
	}

	/**
	 * Méthode invoquée par le parser lorsqu'il rencontre une balise de fermeture.
	 * @see org.xml.sax.ContentHandler#endElement(String, String, String)
	 * 
	 * @param pNamespaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param pLocalName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param pQName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(String pNamespaceURI, String pLocalName, String pQName)
		throws SAXException
	{
			// On dépile le contexte Courant
		pileContextes.pop();
	}

	/**
	 * Méthode invoquée par le parser SAX lorsque celui-ci rencontre le contenu d'un tag XML.
	 *
	 * @param pBuffer[] - Le buffer de lecture du parser SAX.
	 * @param pOffset - La position de la chaine rencontrée dans le buffer.
	 * @param pLength - La longueur de la chaine rencontrée.
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] pBuffer, int pOffset, int pLength)
		throws SAXException
	{
		Contexte contexteCourant = (Contexte) pileContextes.peek();
			// Insertion de la valeur
		contexteCourant.ecrireChaine( new String(pBuffer, pOffset, pLength) );
	}

}
