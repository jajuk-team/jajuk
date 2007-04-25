<?xml version="1.0" encoding="UTF-8"?>

<!-- 
	Description: This XSLT transforms a xml file containing the tagging of all the styles
	 into an html file.
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
	<p><i>Please considere the environment, do not print out this page</i></p>
	<xsl:apply-templates select="styles" />
	<xsl:apply-templates select="artists" />
	<xsl:apply-templates select="years" />
	<xsl:apply-templates select="albums" />
</body>
</html>
</xsl:template>

<xsl:template match="styles">
	<xsl:for-each select="style">
	<table>
	<tr>
		<td class="style">Style</td>
		<td><b><xsl:value-of select="name" /></b></td>
	</tr>
	</table>
	<ul>
	<xsl:for-each select="artist">
	<li>
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
	</li>
	</xsl:for-each>
	</ul>
	</xsl:for-each>
</xsl:template>

<xsl:template match="artists">
	<xsl:for-each select="artist">
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
	</xsl:for-each>
</xsl:template>

<xsl:template match="artists">
	<xsl:for-each select="year">
	<table>
	<tr>
		<td class="year">Artist</td>
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
				<td class="album">Year</td>
				<td> <xsl:value-of select="year" /></td>
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
							<td class="track">Track year</td>
							<td><xsl:value-of select="year" /></td>
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
	</xsl:for-each>
</xsl:template>

<xsl:template match="albums">
	<ul>
		<xsl:for-each select="album">
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
		</xsl:for-each>
	</ul>
</xsl:template>

</xsl:stylesheet>