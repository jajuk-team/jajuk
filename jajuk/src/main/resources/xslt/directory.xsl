<?xml version="1.0" encoding="UTF-8"?>

<!-- 
	Description: This XSLT transforms a xml file containing the tagging of a directory and it's
				children directories and files into an html file.
	Artist: Ronak Patel
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
		
		.device {
			background-color: #f9f7ed;
			font-weight: bold;
			padding: 3px;
		}

		.directory {
			background-color: #c3d9ff;
			font-weight: bold;
			padding: 3px;
		}
		
		.file {
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
	<p><i>Please consider the environment and do not print unless absolutely necessary</i></p>
	<xsl:apply-templates select="directory" />
</body>
</html>
</xsl:template>

<xsl:template match="directory">
	<ul>
		<li>
			<table>
			<tr>
				<td class="directory">Directory Name</td>
				<td> <b><xsl:value-of select="name" /></b></td>
			</tr>
			<tr>
				<td class="directory">Directory Path</td>
				<td> <xsl:value-of select="path" /></td>
			</tr>
			</table>
			<xsl:apply-templates select="directory" />
			<ul>
				<xsl:for-each select="file">
					<li>
						<table>
						<tr>
							<td class="file">File Name</td>
							<td><b><xsl:value-of select="name" /></b></td>
						</tr>
						<tr>
							<td class="file">File Path</td>
							<td><xsl:value-of select="path" /></td>
						</tr>
						<tr>
							<td class="track">Track Name</td>
							<td><xsl:value-of select="track/name" /></td>
						</tr>
						<tr>
							<td class="track">Track Genre</td>
							<td><xsl:value-of select="track/genre" /></td>
						</tr>
						<tr>
							<td class="track">Track Artist</td>
							<td><xsl:value-of select="track/author" /></td>
						</tr>
						<tr>
							<td class="track">Track Album</td>
							<td><xsl:value-of select="track/album" /></td>
						</tr>
						<tr>
							<td class="track">Track Length</td>
							<td><xsl:value-of select="track/length" /></td>
						</tr>
						<tr>
							<td class="track">Track Rate</td>
							<td><xsl:value-of select="track/rate" /></td>
						</tr>
						<tr>
							<td class="track">Track Comment</td>
							<td><xsl:value-of select="track/comment" /></td>
						</tr>
						<tr>
							<td class="track">Track Order</td>
							<td><xsl:value-of select="track/order" /></td>
						</tr>
						</table>
						<br />
					</li>
				</xsl:for-each>
				<br />
			</ul>
		</li>
	</ul>
</xsl:template>

</xsl:stylesheet>