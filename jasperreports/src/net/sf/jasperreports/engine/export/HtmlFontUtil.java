/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.engine.export;

import java.io.UnsupportedEncodingException;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fonts.FontFace;
import net.sf.jasperreports.engine.fonts.FontFamily;
import net.sf.jasperreports.repo.RepositoryUtil;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class HtmlFontUtil
{

	private final RepositoryUtil repositoryUtil;


	/**
	 *
	 */
	private HtmlFontUtil(JasperReportsContext jasperReportsContext)
	{
		this.repositoryUtil = RepositoryUtil.getInstance(jasperReportsContext);
	}
	
	
	/**
	 *
	 */
	public static HtmlFontUtil getInstance(JasperReportsContext jasperReportsContext)
	{
		return new HtmlFontUtil(jasperReportsContext);
	}
	
	
	/**
	 * @deprecated Replaced by {@link #handleHtmlFont(HtmlResourceHandler, HtmlFont)}.
	 */
	public static void handleFont(HtmlResourceHandler resourceHandler, HtmlFont htmlFont)
	{
		getInstance(DefaultJasperReportsContext.getInstance()).handleHtmlFont(resourceHandler, htmlFont);
	}
	
	
	/**
	 * @deprecated Replaced by {@link #handleHtmlFont(HtmlResourceHandler, HtmlResourceHandler, HtmlResourceHandler, HtmlFontFamily)}.
	 */
	public void handleHtmlFont(HtmlResourceHandler resourceHandler, HtmlFont htmlFont)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("@charset \"UTF-8\";\n");
		sb.append(getHtmlFont(resourceHandler, resourceHandler, htmlFont));
		
		if (resourceHandler != null)
		{
			try
			{
				resourceHandler.handleResource(htmlFont.getId(), sb.toString().getBytes("UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new JRRuntimeException(e);
			}
		}
	}
	
	
	/**
	 *
	 */
	private String getHtmlFont(
		HtmlResourceHandler fontPathProvider,
		HtmlResourceHandler fontResourceSaver,
		HtmlFont htmlFont
		)
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			sb.append("@font-face {\n");
			sb.append("\tfont-family: " + (htmlFont.getFamily() == null ? htmlFont.getShortId() : htmlFont.getFamily().getShortId()) + ";\n");
			if (htmlFont.getEot() != null)
			{
				String eotId = htmlFont.getId() + ".eot";
				String eotFileName = eotId;
				if (fontPathProvider != null)
				{
					eotFileName = fontPathProvider.getResourcePath(eotId);
				}
				if (fontResourceSaver != null)
				{
					fontResourceSaver.handleResource(eotId, repositoryUtil.getBytesFromLocation(htmlFont.getEot()));
				}
				sb.append("\tsrc: url('" + eotFileName + "');\n");
				sb.append("\tsrc: url('" + eotFileName + "?#iefix') format('embedded-opentype');\n");
				//sbuffer.append("\tsrc: url('" + eotFileName + "?#iefix') format('eot');\n");
			}
			if (
				htmlFont.getTtf() != null
				|| htmlFont.getSvg() != null
				|| htmlFont.getWoff() != null
				)
			{
				sb.append("\tsrc: local('\u263A')");
				if (htmlFont.getWoff() != null)
				{
					String woffId = htmlFont.getId() + ".woff";
					String woffFileName = woffId;
					if (fontPathProvider != null)
					{
						woffFileName = fontPathProvider.getResourcePath(woffId);
					}
					if (fontResourceSaver != null)
					{
						fontResourceSaver.handleResource(woffId, repositoryUtil.getBytesFromLocation(htmlFont.getWoff()));
					}
					sb.append(",\n\t\turl('" + woffFileName + "') format('woff')"); 
				}
				if (htmlFont.getTtf() != null)
				{
					String ttfId = htmlFont.getId() + ".ttf";
					String ttfFileName = ttfId;
					if (fontPathProvider != null)
					{
						ttfFileName = fontPathProvider.getResourcePath(ttfId);
					}
					if (fontResourceSaver != null)
					{
						fontResourceSaver.handleResource(ttfId, repositoryUtil.getBytesFromLocation(htmlFont.getTtf()));
					}
					sb.append(",\n\t\turl('" + ttfFileName + "') format('truetype')"); 
				}
				if (htmlFont.getSvg() != null)
				{
					String svgId = htmlFont.getId() + ".svg";
					String svgFileName = svgId;
					if (fontPathProvider != null)
					{
						svgFileName = fontPathProvider.getResourcePath(svgId);
					}
					if (fontResourceSaver != null)
					{
						fontResourceSaver.handleResource(svgId, repositoryUtil.getBytesFromLocation(htmlFont.getSvg()));
					}
					sb.append(",\n\t\turl('" + svgFileName + "') format('svg')");
				}
				sb.append(";\n");
			}
			sb.append("\tfont-weight: " + (htmlFont.isBold() ? "bold" : "normal") + ";\n");
			sb.append("\tfont-style: " + (htmlFont.isItalic() ? "italic" : "normal") + ";\n");
			sb.append("}");
		}
		catch (JRException e)
		{
			throw new JRRuntimeException(e);
		}
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 */
	public void handleHtmlFont(
		HtmlResourceHandler cssResourceSaver,
		HtmlResourceHandler fontPathProvider,
		HtmlResourceHandler fontResourceSaver,
		HtmlFontFamily htmlFontFamily
		)
	{
		if (cssResourceSaver != null)
		{
			String fontCss = getHtmlFont(fontPathProvider, fontResourceSaver, htmlFontFamily);

			try
			{
				cssResourceSaver.handleResource(htmlFontFamily.getId(), fontCss.getBytes("UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new JRRuntimeException(e);
			}
		}
	}
	
	
	/**
	 * 
	 */
	public String getHtmlFont(
		HtmlResourceHandler fontPathProvider,
		HtmlResourceHandler fontResourceSaver,
		HtmlFontFamily htmlFontFamily
		)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("@charset \"UTF-8\";\n\n");

		FontFamily fontFamily = htmlFontFamily.getFontFamily();
		
		FontFace fontFace = fontFamily.getNormalFace();
		if (fontFace != null)
		{
			HtmlFont htmlFont = HtmlFont.getInstance(htmlFontFamily, htmlFontFamily.getLocale(), fontFace, false, false);
			sb.append(getHtmlFont(fontPathProvider, fontResourceSaver, htmlFont));
			sb.append("\n\n");
		}
		
		fontFace = fontFamily.getBoldFace();
		if (fontFace != null)
		{
			HtmlFont htmlFont = HtmlFont.getInstance(htmlFontFamily, htmlFontFamily.getLocale(), fontFace, true, false);
			sb.append(getHtmlFont(fontPathProvider, fontResourceSaver, htmlFont));
			sb.append("\n\n");
		}
		
		fontFace = fontFamily.getItalicFace();
		if (fontFace != null)
		{
			HtmlFont htmlFont = HtmlFont.getInstance(htmlFontFamily, htmlFontFamily.getLocale(), fontFace, false, true);
			sb.append(getHtmlFont(fontPathProvider, fontResourceSaver, htmlFont));
			sb.append("\n\n");
		}
		
		fontFace = fontFamily.getBoldItalicFace();
		if (fontFace != null)
		{
			HtmlFont htmlFont = HtmlFont.getInstance(htmlFontFamily, htmlFontFamily.getLocale(), fontFace, true, true);
			sb.append(getHtmlFont(fontPathProvider, fontResourceSaver, htmlFont));
			sb.append("\n\n");
		}
		
		return sb.toString();
	}
}
