/**
 * File : ProcessFrame.java
 *
 * Created on May 16, 2012, 9:59:53 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import presentation.ProgressionEvent.ProgressionType;
import application.BusScheduling;
import application.BusSchedulingException;

/**
 * Process frame used to show process progression and output
 */
public class ProcessFrame extends JDialog implements ProgressionListener, WindowListener
{
	/** Default serial version ID */
	private static final long serialVersionUID = 1L;

	/** Shell output */
	private JTextPane taShell = new JTextPane();

	/** Progress bar */
	private GradientProgressBar progressBar = new GradientProgressBar(new Color(191, 251, 203));
	
	/** BusScheduling from which progression events are fired */
	private BusScheduling busScheduling;

	/**
	 * Constructor
	 * @param parent Container
	 * @param subscription true if Gidion owns a Google Business subscription, false else
	 * @param configFile Configuration input file
	 * @param pathsFile Paths input file
	 * @param sheet Paths file sheet number
	 * @param resultsFile Output file
	 * @throws BusSchedulingException 
	 */
	public ProcessFrame(GraphicalUserInterface parent, File configFile, File pathsFile,
			int sheet, File resultsFile, boolean subscription) throws BusSchedulingException
	{
		super(parent, true);

		progressBar.setStringPainted(true);
		progressBar.setBackground(new Color(11, 212, 48));
		progressBar.setForeground(new Color(11, 212, 48));

		taShell.setPreferredSize(new Dimension(100, 100));
		taShell.setFocusable(true);
		taShell.setAutoscrolls(true);
		taShell.setEditable(false);
		JScrollPane spShell = new JScrollPane(taShell);

		// Main frame
		this.add(progressBar, BorderLayout.NORTH);
		this.add(spShell, BorderLayout.CENTER);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(550, 250));
		this.setMinimumSize(new Dimension(475, 200));
		this.setLocationRelativeTo(getParent());
		this.setTitle("Carregamento");
		this.addWindowListener(this);

		PrintStream out = new JTextPaneOutputStream(taShell, System.out, false);
		System.setOut(out);
		PrintStream err = new JTextPaneOutputStream(taShell, System.out, true);
		System.setErr(err);

		try
		{
			this.busScheduling = new BusScheduling(configFile, pathsFile, sheet, resultsFile,
				parent, subscription, this);
			busScheduling.addProgressionListener(this);
		}
		catch (SQLException e1)
		{
			throw new BusSchedulingException("A conexão ao base de dados voltou um erro. (The database connection has failed)");
		}
		catch (ClassNotFoundException e1)
		{
			throw new BusSchedulingException("A conexão de base de dados voltou um erro. Isto pode ser causado por uma falta do driver SQL.\n(The database connection has failed. This may be caused by a miss of driver)");
		}
		catch (Exception e1)
		{
			throw new BusSchedulingException("O programa encontrou um erro interno.\nPesaroso para as dificuldades encontradas.\n\n(The program has encountered an internal error.)\n(Please forgive for difficulties brought on).");
		}
		busScheduling.start();
		this.setVisible(true);
	}

	/**
	 * Interrupt the progression bar
	 */
	private void setInterrupted()
	{
		progressBar.setBackground(new Color(251, 40, 0));
		progressBar.setGradientEnd(new Color(251, 191, 191));
		progressBar.setForeground(new Color(251, 40, 0));
	}

	/**
	 * 
	 * @param e 
	 * @see presentation.ProgressionListener#progressionPerformed(presentation.ProgressionEvent)
	 */
	@Override
	public void progressionPerformed(ProgressionEvent e)
	{
		if (e.getTime() != null && e.getType() == ProgressionType.INCREMENT)
		{
			long sleep = e.getTime() / e.getValue();
			for (int i = 0 ; i < e.getValue() ; ++i)
			{
				try
				{
					Thread.sleep(sleep);
				}
				catch (InterruptedException e1)
				{}
				progressBar.setValue(progressBar.getValue() + 1);
			}
		}
		else if (e.getType() == ProgressionType.INCREMENT)
		{
			progressBar.setValue(progressBar.getValue() + e.getValue());
		}
		else if (e.getType() == ProgressionType.SET)
		{
			progressBar.setValue(e.getValue());
		}
		else if (e.getType() == ProgressionType.INTERRUPT)
		{
			setInterrupted();
		}
	}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent e)
	{}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent e)
	{
		busScheduling.interrupt();
		((GraphicalUserInterface)getParent()).configureOutput();
	}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e)
	{}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent e)
	{}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent e)
	{}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent e)
	{}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent e)
	{}
}
