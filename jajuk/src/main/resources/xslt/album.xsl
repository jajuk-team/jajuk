<?xml version="1.0" encoding="UTF-8"?>

<!-- 
	Description: This XSLT transforms a xml file containing the tagging of an album into an html file.
	Author: The Jajuk Team
	Created: August 23, 2006
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<xsl:template match="/">
		<html>
			<head>
				<title>Jajuk Music Report</title>
				<style type="text/css">
					ul { list-style-type: none; }

					.style { background-color: #f9f7ed; font-weight:
					bold; padding: 3px; }

					.author { background-color: #c3d9ff; font-weight:
					bold; padding: 3px; }

					.album { background-color: #cdeb8b; font-weight:
					bold; padding: 3px; }

					.track { font-weight: bold; background-color:
					#eeeeee; padding: 3px; width: 120px; }
				</style>
			</head>
			<body>
				<h1>Jajuk Music Report</h1>
				<p>
					<CAPTION>
						Please consider the environment and do not print
						unless absolutely necessary
					</CAPTION>
				</p>
				<p>
					<a href='#album_list'>Album list</a>
				</p>
				<a href='#album_tracks'>Album / tracks</a>
				<p>
					<h1 id='album_list'>Album List</h1>
					<xsl:apply-templates select="collection" />
				</p>
				<p>
					<h1 id='album_tracks'>Album / tracks</h1>
					<xsl:apply-templates select="collection/album" />
				</p>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="collection">
		<table border="0" cellspacing="5">
			<TR>
				<TH>Name</TH>
				<TH>Author</TH>
				<TH>Genre</TH>
			</TR>
			<xsl:for-each select="album">
				<tr>
					<xsl:variable name="id" select='id' />
					<td width='50%'>
						<a href='#{id}'>
							<xsl:value-of select="name" />
						</a>
					</td>
					<td width='30%'>
						<xsl:value-of select="author" />
					</td>
					<td width='20%'>
						<xsl:value-of select="genre" />
					</td>
				</tr>
			</xsl:for-each>
		</table>

	</xsl:template>


	<xsl:template match="collection/album">
		<hr />

		<xsl:variable name="id" select='id' />
		<p id='{id}'>
			<b>
				<xsl:value-of select="name" /> (<xsl:value-of select="author" />)
			</b>
		</p>
		<table border="0" cellspacing="5">
			<TR>
				<TH>Track</TH>
				<TH>Title</TH>
				<TH>Album</TH>
				<TH>Artist</TH>
				<TH>Genre</TH>
				<TH>Length</TH>
				<TH>Rate</TH>
				<TH>Comment</TH>
			</TR>

			<xsl:for-each select="track">
				<tr>
					<td width='5%'>
						<xsl:value-of select="order" />
					</td>
					<td width='20%'>
						<xsl:value-of select="name" />
					</td>
					<td width='10%'>
						<xsl:value-of select="album" />
					</td>
					<td width='10%'>
						<xsl:value-of select="author" />
					</td>
					<td width='10%'>
						<xsl:value-of select="genre" />
					</td>
					<td width='10%'>
						<xsl:value-of select="length" />
					</td>
					<td width='5%'>
						<xsl:value-of select="rate" />
					</td>
					<td>
						<xsl:value-of select="comment" />
					</td>

				</tr>
			</xsl:for-each>
		</table>

		<br />
	</xsl:template>



</xsl:stylesheet>