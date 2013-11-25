/**
 * File : xlsFileFilter.java
 *
 * Created on May 14, 2012, 11:22:19 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package presentation;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

/**
 * Block every files, except the ones of a specified type
 */
public class XLSFileFilter extends FileFilter
{
	/** Type to allow */
	private String type;

	/** Extension corresponding to the type */
	private String extension;

	/**
	 * Constructor
	 * @param type Type to allow
	 * @param extension Extension corresponding to the type
	 */
	public XLSFileFilter(String type, String extension)
	{
		super();
		this.type = type;
		this.extension = extension;
	}

	/**
	 * Return a description of the type blocked
	 * @return A description of the type blocked
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return type;
	}

	/**
	 * Return true if the file have the right extension, false else
	 * @param e The file
	 * @return true if the file have the right extension, false else
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File e)
	{
		Pattern p;
		p = Pattern.compile(extension);
		Matcher m = p.matcher(e.getName());
		return ((e.isDirectory() || m.matches()) ? true : false);
	}
}
