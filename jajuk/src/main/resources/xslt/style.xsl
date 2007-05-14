<?xml version='1.0' encoding='UTF-8'?>

<!-- 
	Description: This XSLT transforms a xml file containing the tagging of a style into an html file.
	Author: The Jajuk Team
	Created: August 23, 2006
-->

<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'>

	<xsl:template match='/'>
		<html>
			<head>
				<title>
					<xsl:value-of
						select='/collection/i18n/ReportAction.1' />
				</title>
				<style type='text/css'>
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
				<h1>
					<xsl:value-of
						select='/collection/i18n/ReportAction.1' />
				</h1>
				<p>
					<CAPTION>
						<xsl:value-of
							select='/collection/i18n/ReportAction.2' />
					</CAPTION>
				</p>
				<p>
					<ul>
						<li>
							<a href='#1'>
								<xsl:value-of
									select='/collection/i18n/ReportAction.7' />
							</a>
						</li>
						<li>
							<a href='#2'>
								<xsl:value-of
									select='/collection/i18n/ReportAction.8' />
							</a>
						</li>
						<li>
							<a href='#3'>
								<xsl:value-of
									select='/collection/i18n/ReportAction.9' />
							</a>
						</li>
					</ul>
				</p>
				<p>
					<h2 id='1'>
						<xsl:value-of
							select='/collection/i18n/ReportAction.7' />
					</h2>
					<xsl:call-template name='styles' />
				</p>
				<p>
					<h2 id='2'>
						<xsl:value-of
							select='/collection/i18n/ReportAction.8' />
					</h2>
					<xsl:call-template name='style-album' />
				</p>
				<p>
					<h2 id='3'>
						<xsl:value-of
							select='/collection/i18n/ReportAction.9' />
					</h2>
					<xsl:call-template name='style-author-album' />
				</p>
			</body>
		</html>
	</xsl:template>


	<xsl:template name='styles'>
		<table border='0' cellspacing='5'>
			<xsl:for-each select='/collection/style'>
				<tr>
					<xsl:variable name='id' select='id' />
					<td>
						<a href='#{id}'>
							<xsl:value-of select='name' />
						</a>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>


	<xsl:template name='style-album'>
		<table border='0' cellspacing='5'>
			<xsl:for-each select='/collection/style'>
				<hr />
				<xsl:variable name='id' select='id' />
				<h2 id='{id}'>
					<xsl:value-of select='name' />
				</h2>
				<table border='0' cellspacing='5'>
					<TR>
						<TH>
							<xsl:value-of
								select='/collection/i18n/ReportAction.name' />
						</TH>
						<TH>
							<xsl:value-of
								select='/collection/i18n/ReportAction.author' />
						</TH>
						<TH>
							<xsl:value-of
								select='/collection/i18n/ReportAction.year' />
						</TH>

					</TR>
					<xsl:for-each select='album'>
						<tr>
							<td width='50%'>
								<xsl:value-of select='name' />
							</td>
							<td width='30%'>
								<xsl:value-of select='author' />
							</td>
							<td width='5%'>
								<xsl:value-of select='year' />
							</td>
						</tr>
					</xsl:for-each>
				</table>
			</xsl:for-each>
		</table>

	</xsl:template>

	<xsl:template name='style-author-album'>
		<table border='0' cellspacing='5'>
			<xsl:for-each select='/collection/style'>
				<hr />
				<xsl:variable name='id' select='id' />
				<h2 id='{id}'>
					<xsl:value-of select='name' />
				</h2>
				<xsl:for-each select='author'>
					<h3>
						<xsl:value-of select='name' />
					</h3>
					<table border='0' cellspacing='5'>
						<TR>
							<TH>
								<xsl:value-of
									select='/collection/i18n/ReportAction.name' />
							</TH>
							<TH>
								<xsl:value-of
									select='/collection/i18n/ReportAction.year' />
							</TH>

						</TR>
						<xsl:for-each select='album'>
							<tr>
								<td>
									<xsl:value-of select='name' />
								</td>
								<td>
									<xsl:value-of select='year' />
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:for-each>
			</xsl:for-each>
		</table>

	</xsl:template>



</xsl:stylesheet>