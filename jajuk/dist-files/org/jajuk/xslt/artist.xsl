<?xml version="1.0" encoding="UTF-8"?>

<!-- 
	Description: This XSLT transforms a xml file containing the tagging of an author into an html file.
	Author: Ronak Patel
	Created: August 23, 2006
 -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">
<html>
<head>
	<title>Jajuk Music Report</title>
	<style type="text/css">
		ul {
			list-style-type: none;
		}
		
		.style {
			background-color: #f9f7ed;
			font-weight: bold;
			padding: 3px;
		}
		
		.artist {
			background-color: #c3d9ff;
			font-weight: bold;
			padding: 3px;
		}
		
		.album {
			background-color: #cdeb8b;
			font-weight: bold;
			padding: 3px;			
		}
		
		.track {
			font-weight: bold;
			background-color: #eeeeee;
			padding: 3px;			
			width: 120px;
		}
	</style>
</head>
<body>
	<h1>Jajuk Music Report</h1>
	<p><i>Protect the environment, please do not print</i></p>
	<xsl:apply-templates select="artist" />
</body>
</html>
</xsl:template>

<xsl:template match="artist">
	<table>
	<tr>
		<td class="artist">Artist</td>
		<td><b><xsl:value-of select="name" /></b></td>
	</tr>
	</table>
	<xsl:for-each select="album">
		<ul>
		<li>
			<table>
			<tr>
				<td class="album">Album</td>
				<td> <b><xsl:value-of select="name" /></b></td>
			</tr>
			<tr>
				<td class="album">Artist</td>
				<td> <xsl:value-of select="artist" /></td>
			</tr>
			<tr>
				<td class="album">Genre</td>
				<td> <xsl:value-of select="genre" /></td>
			</tr>
			</table>
			<ul>
				<xsl:for-each select="track">
					<li>
						<table>
						<tr>
							<td class="track">Track Name</td>
							<td><b><xsl:value-of select="name" /></b></td>
						</tr>
						<tr>
							<td class="track">Track Genre</td>
							<td><xsl:value-of select="genre" /></td>
						</tr>
						<tr>
							<td class="track">Track Artist</td>
							<td><xsl:value-of select="artist" /></td>
						</tr>
						<tr>
							<td class="track">Track Album</td>
							<td><xsl:value-of select="album" /></td>
						</tr>
						<tr>
							<td class="track">Track Length</td>
							<td><xsl:value-of select="length" /></td>
						</tr>
						<tr>
							<td class="track">Track Rate</td>
							<td><xsl:value-of select="rate" /></td>
						</tr>
						<tr>
							<td class="track">Track Comment</td>
							<td><xsl:value-of select="comment" /></td>
						</tr>
						<tr>
							<td class="track">Track Order</td>
							<td><xsl:value-of select="order" /></td>
						</tr>
						</table>
						<br />
					</li>
				</xsl:for-each>
				<br />
			</ul>
		</li>
	</ul>
	</xsl:for-each>
</xsl:template>

</xsl:stylesheet>