package application;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import presentation.GraphicalUserInterface;

/**
 *  Main class, launching the Bus Scheduling program.
 */
public class Main
{
	/** Error log file path */
	private static final String LOG_PATH = "resources/errorLog";
	
	/**
	 *  Main function, launching the Bus Scheduling program
	 * @param args Unused
	 */
	public static void main(String[] args)
	{
		try
		{
			new GraphicalUserInterface();
		}
		catch(Exception e)
		{
			saveLog(e);
		}
	}
	
	/**
	 * Save an exception stack trace in a log file
	 * @param e The exception which contains the stack trace
	 */
	public static void saveLog(Exception e)
	{
		try
		{
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String logInfo = "---------- " + format.format(new Date()) + " ----------\n";
			PrintStream writer = new PrintStream(new BufferedOutputStream(new FileOutputStream(LOG_PATH, true)));
			writer.append(logInfo);
			e.printStackTrace(writer);
			writer.append("\n");
			writer.close();
		}
		catch (Exception e1) {}
	}
}
