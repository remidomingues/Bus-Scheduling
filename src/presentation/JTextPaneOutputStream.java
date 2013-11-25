/**
 * File : JTextAreaOutputStream.java
 *
 * Created on May 14, 2012, 1:59:20 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package presentation;

import java.awt.Color;
import java.io.PrintStream;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Write output strings in a JTextPane, the color of the string written dipending on the stream.
 */
public class JTextPaneOutputStream extends PrintStream
{
	/** textpane's document */
	private StyledDocument document;

	/** Object used to configure the string written */
	private MutableAttributeSet attributeSet;

	/** true if the stream is an error stream, false else */
	private boolean err;

	/** JTextPane destination */
	JTextPane textPane;

	/**
	 * Constructor
	 * @param textPane JTextPane destination
	 * @param stream Unused
	 * @param err true if the textpane must be an error stream, false else
	 */
	public JTextPaneOutputStream(JTextPane textPane, PrintStream stream, boolean err)
	{
		super(stream);
		this.textPane = textPane;
		document = textPane.getStyledDocument();
		attributeSet = textPane.getInputAttributes();
		this.err = err;
	}

	/**
	 * Write a string in the textpane, its colours depending on the err boolean
	 * @param s The string to write
	 * @see java.io.PrintStream#print(java.lang.String)
	 */
	@Override
	public void print(String s)
	{
		if (err)
		{
			StyleConstants.setForeground(this.attributeSet, Color.RED);
		}
		else
		{
			StyleConstants.setForeground(this.attributeSet, Color.BLACK);
		}
		try
		{
			document.insertString(document.getLength(), s, attributeSet);
			textPane.setCaretPosition(textPane.getDocument().getLength());
		}
		catch (BadLocationException e)
		{
			return;
		}
	}

	/**
	 * Write a string followed by a carriage return in the textpane, its colours depending on the err boolean
	 * @param s The string to write
	 * @see java.io.PrintStream#print(java.lang.String)
	 */
	@Override
	public void println(String s)
	{
		if (err)
		{
			StyleConstants.setForeground(this.attributeSet, Color.RED);
		}
		else
		{
			StyleConstants.setForeground(this.attributeSet, Color.BLACK);
		}
		try
		{
			document.insertString(document.getLength(), s + "\n", attributeSet);
			textPane.setCaretPosition(textPane.getDocument().getLength());
		}
		catch (BadLocationException e)
		{
			return;
		}
	}

}
