package presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import application.BusSchedulingException;
import application.Main;

/**
 * File : GraphicalUserInterface.java
 *
 * Created on May 14, 2012, 10:26:03 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

/**
 * Window containing some user fields and an output
 */
public class GraphicalUserInterface extends JFrame implements ActionListener, MouseListener,
		ComponentListener, WindowListener
{
	/** Default serial version ID */
	private static final long serialVersionUID = 1L;

	/** Cache file used to save and loads file paths */
	private static final String CACHE_FILE = "resources/cache";

	/** Software icon */
	private static final String ICON = "/onibus.png";

	/** Gidion logo */
	private static final String LOGO = "/gidion.png";

	/** User fields main panel */
	private JPanel plUserFields = new JPanel(new GridLayout(3, 1));
	
	/** Checked if Gidion owns a Google Business subscription, non checked else */
	private JCheckBox cbSubscription = new JCheckBox("Assinatura Google Business");

	// Configuration fields
	/** Configuration label */
	private JLabel lbConfigFile = new JLabel("Ficheiro de configuração");

	/** File path */
	private JTextField tfConfigFile = new JTextField();

	/** File chooser start button */
	private JButton btConfigFile = new JButton("Consulte");

	/** Panel used for file path and file chooser button */
	private JPanel plConfigChooser = new JPanel(new FlowLayout());

	// Data fields
	/** Data label */
	private JLabel lbDataFile = new JLabel("Ficheiro de lugares");

	/** Path file */
	private JTextField tfDataFile = new JTextField();

	/** Data panel */
	private JPanel plDataChooser = new JPanel(new FlowLayout());

	/** Data sheet panel */
	private JPanel plDataSheet = new JPanel(new FlowLayout());

	/** Data sheet label */
	private JLabel lbDataSheet = new JLabel("Número da folha :");

	/** Data sheet number */
	private JTextField tfDataSheet = new JTextField("0");

	/** File chooser start button */
	private JButton btDataFile = new JButton("Consulte");

	// Results
	/** Results label */
	private JLabel lbResultsFile = new JLabel("Ficheiro de resultados");

	/** Path file */
	private JTextField tfResultsFile = new JTextField();

	/** Results panel */
	private JPanel plResultsChooser = new JPanel(new FlowLayout());

	/** File chooser start button */
	private JButton btResultsFile = new JButton("Consulte");

	/** Shell output */
	private JTextPane taShell = new JTextPane();

	/** Start button */
	private JButton btStart = new JButton("Start");

	/** Exit button */
	private JButton btExit = new JButton("Exit");

	/** Vertical split pane, between the main fields panel and the shell console.
	 * Permits to resize the space shared by these components */
	private JSplitPane verticalSplitPane;
	
	/** Last path selected by a JFileChooser */
	private String path;
	
	/**
	 * Constructor
	 */
	@SuppressWarnings("serial")
	public GraphicalUserInterface()
	{
		super();
		GridLayout subPanelLayout = new GridLayout(3, 1);
		Image img = null;
		try
		{
			img = new ImageIcon(getClass().getResource(ICON)).getImage();
		}
		catch(Exception e)
		{
			Main.saveLog(e);
		}

		JPanel plPicture = new JPanel(new FlowLayout())
		{
			boolean report = false;
			@Override
			public void paint(Graphics g)
			{
				try
				{
					BufferedImage image = ImageIO.read(getClass().getResource(LOGO));
					int x = (getParent().getParent().getParent().getParent().getWidth() - image.getWidth()) / 2;
					g.drawImage(image, x, 3, null);
				}
				catch (Exception e)
				{
					if(!report)
					{
						Main.saveLog(e);
						report = true;
					}
				}
			}
		};
		plPicture.setPreferredSize(new Dimension(100, 63));
		
		// Config
		tfConfigFile.addMouseListener(this);
		tfConfigFile.setPreferredSize(new Dimension(250, 25));
		btConfigFile.setFocusable(false);
		btConfigFile.setPreferredSize(new Dimension(100, 25));
		btConfigFile.addActionListener(this);

		plConfigChooser.add(tfConfigFile);
		plConfigChooser.add(btConfigFile);
		JPanel plConfig = new JPanel(new GridLayout(3, 1));
		plConfig.setBorder(new EmptyBorder(0, 0, 10, 0));
		lbConfigFile.setBorder(new EmptyBorder(0, 8, 0, 0));
		plConfig.add(new JPanel(new FlowLayout()).add(cbSubscription));
		plConfig.add(lbConfigFile);
		plConfig.add(plConfigChooser);
		plUserFields.add(plConfig);

		// Data
		tfDataSheet.setPreferredSize(new Dimension(30, 25));
		tfDataFile.addMouseListener(this);
		tfDataFile.setPreferredSize(new Dimension(250, 25));
		btDataFile.setPreferredSize(new Dimension(100, 25));
		btDataFile.setFocusable(false);
		btDataFile.addActionListener(this);

		JPanel plData = new JPanel(subPanelLayout);
		plDataSheet.add(lbDataSheet);
		plDataSheet.add(tfDataSheet);
		plDataChooser.add(tfDataFile);
		plDataChooser.add(btDataFile);
		lbDataFile.setBorder(new EmptyBorder(0, 8, 0, 0));
		plData.add(lbDataFile);
		plData.add(plDataChooser);
		plData.add(plDataSheet);
		plUserFields.add(plData);

		// Results
		tfResultsFile.addMouseListener(this);
		tfResultsFile.setPreferredSize(new Dimension(250, 25));
		btResultsFile.setFocusable(false);
		btResultsFile.setPreferredSize(new Dimension(100, 25));
		btResultsFile.addActionListener(this);
		lbResultsFile.setBorder(new EmptyBorder(0, 8, 0, 0));

		JPanel plResults = new JPanel(subPanelLayout);
		plResults.add(lbResultsFile);
		plResultsChooser.add(tfResultsFile);
		plResultsChooser.add(btResultsFile);
		plResults.add(plResultsChooser);

		btStart.setFocusable(false);
		btStart.addActionListener(this);
		btStart.setPreferredSize(new Dimension(75, 28));

		btExit.setFocusable(false);
		btExit.addActionListener(this);
		btExit.setPreferredSize(new Dimension(75, 28));

		JPanel plButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		plButtons.add(btStart);
		plButtons.add(btExit);

		plResults.add(plButtons);
		plUserFields.add(plResults);

		taShell.setPreferredSize(new Dimension(100, 100));
		taShell.setFocusable(true);
		taShell.setAutoscrolls(true);
		taShell.setEditable(false);
		JScrollPane spShell = new JScrollPane(taShell);
		JScrollPane spUserFields = new JScrollPane(plUserFields);
		spUserFields.setMinimumSize(new Dimension(400, 100));
		spShell.setMinimumSize(new Dimension(400, 50));
		verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spUserFields, spShell);

		// Main frame
		if(img != null)
			this.setIconImage(img);
		this.add(plPicture, BorderLayout.NORTH);
		this.add(verticalSplitPane, BorderLayout.CENTER);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(390, 583));
		this.setMinimumSize(new Dimension(300, 400));
		this.setLocationRelativeTo(getParent());
		this.setVisible(true);
		this.setTitle("Programa do ônibus");
		this.addComponentListener(this);
		this.addWindowListener(this);
		verticalSplitPane.setDividerLocation((int) (getSize().height / 1.6));

		PrintStream out = new JTextPaneOutputStream(taShell, System.out, false);
		System.setOut(out);
		PrintStream err = new JTextPaneOutputStream(taShell, System.out, true);
		System.setErr(err);

		importCache();
		printGoogleDistanceLimitsInformation();
	}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if (e.getSource() == btStart)
			{
				String empty = "";
				if (tfConfigFile.getText().equals(empty) || !new File(tfConfigFile.getText()).isFile())
				{
					System.err.println("O ficheiro de configuração é errado. (The configuration file is wrong)");
					return;
				}
				if (tfDataSheet.getText().equals(empty))
				{
					System.err.println("O número da folha deve ser informado. (The sheet number must be specified)");
					return;
				}
				int sheet;
				
				try
				{
					sheet = Integer.parseInt(tfDataSheet.getText());
					if (sheet < 1)
						throw new NumberFormatException();
				}
				catch (NumberFormatException e1)
				{
					System.err.println("O número da folha está errado. (The sheet number is wrong)");
					return;
				}
				
				if (tfDataFile.getText().equals(empty) || !new File(tfDataFile.getText()).isFile())
				{
					System.err.println("O ficheiro de lugares é errado. (The paths file is wrong)");
					return;
				}
				
				if (tfResultsFile.getText().equals(empty))
				{
					System.err.println("O ficheiro de saída deve ser informado. (The output file must be specified)");
					return;
				}
				
				try
				{
					saveCache();
					new ProcessFrame(this, new File(tfConfigFile.getText()),
							new File(tfDataFile.getText()), sheet - 1,
							new File(tfResultsFile.getText()), cbSubscription.isSelected());
				}
				catch(BusSchedulingException e1)
				{
					configureOutput();
					System.err.println(e1.getMessage());
				}
				catch (Exception e1)
				{
					configureOutput();
					System.err.println("O programa encontrou um erro interno.\nPesaroso para as dificuldades encontradas.\n\n(The program has encountered an internal error.)\n(Please forgive for difficulties brought on).");
				}
			}
			else if (e.getSource() == btConfigFile)
			{
				chooseConfigurationFile();
			}
			else if (e.getSource() == btDataFile)
			{
				chooseDataFile();
			}
			else if (e.getSource() == btResultsFile)
			{
				chooseResultsFile();
			}
			else if (e.getSource() == btExit)
			{
				this.dispose();
			}
		}
		catch(Exception e1)
		{
			Main.saveLog(e1);
		}
	}

	/**
	 * Open a JFileChooser in order to allow the user to select an XLS file in input
	 */
	private void chooseConfigurationFile()
	{
		JFileChooser xlsFileChooser = new JFileChooser(path);
		xlsFileChooser.setAcceptAllFileFilterUsed(false);
		xlsFileChooser.setFileFilter(new XLSFileFilter("XLS ficheiro", ".*\\.xls"));
		int action = xlsFileChooser.showDialog(getParent(), "Open");

		if(action == JFileChooser.APPROVE_OPTION)
			{
			File xlsFile = xlsFileChooser.getSelectedFile();
			if (xlsFile != null && xlsFile.isFile())
			{
				tfConfigFile.setText(xlsFile.getPath());
				tfConfigFile.setCaretPosition(tfConfigFile.getText().length());
			}
			else if (xlsFile != null)
			{
				System.err.println("Ficheiro incorreto. (Incorrect file)");
			}
			path = xlsFile.getAbsolutePath();
		}
	}

	/**
	 * Open a JFileChooser in order to allow the user to select an XLS file in input
	 */
	private void chooseDataFile()
	{
		JFileChooser xlsFileChooser = new JFileChooser(path);
		xlsFileChooser.setAcceptAllFileFilterUsed(false);
		xlsFileChooser.setFileFilter(new XLSFileFilter("XLS ficheiro", ".*\\.xls"));
		int action = xlsFileChooser.showDialog(getParent(), "Open");
		
		if(action == JFileChooser.APPROVE_OPTION)
		{
			File xlsFile = xlsFileChooser.getSelectedFile();
			if (xlsFile != null && xlsFile.isFile())
			{
				tfDataFile.setText(xlsFile.getPath());
				tfDataFile.setCaretPosition(tfDataFile.getText().length());
			}
			else if (xlsFile != null)
			{
				System.err.println("Ficheiro incorreto. (Incorrect file)");
			}
			path = xlsFile.getAbsolutePath();
		}
	}

	/**
	 * Open a JFileChooser in order to allow the user to select an output results file
	 */
	private void chooseResultsFile()
	{
		JFileChooser xlsFileChooser;
		xlsFileChooser = new JFileChooser(path);
		xlsFileChooser.setApproveButtonText("Open");
		xlsFileChooser.setAcceptAllFileFilterUsed(false);
		xlsFileChooser.setFileFilter(new XLSFileFilter("XLS ficheiro", ".*\\.xls"));
		int action = xlsFileChooser.showSaveDialog(getParent());

		if (action == JFileChooser.APPROVE_OPTION && xlsFileChooser.getSelectedFile() != null)
		{
			Pattern p;
			p = Pattern.compile(".*\\.xls");
			Matcher m = p.matcher(xlsFileChooser.getSelectedFile().getName());

			File xlsFile;
			if (!m.matches())
			{
				xlsFile = new File(xlsFileChooser.getSelectedFile() + ".xls");
			}
			else
			{
				xlsFile = xlsFileChooser.getSelectedFile();
			}
			if (!xlsFile.isFile()
					|| (xlsFile.isFile() && JOptionPane.showOptionDialog(getParent(),
							"Querem realmente esmagar o ficheiro " + xlsFile.getName() + " ?", "Cuidado",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) == 0))
			{
				tfResultsFile.setText(xlsFile.getPath());
				tfResultsFile.setCaretPosition(tfResultsFile.getText().length());
			}
		}
	}

	/**
	 * Read the cache set user fields to the values read
	 */
	private void importCache()
	{
		try
		{
			String s = readInputStream(new FileInputStream(new File(CACHE_FILE)));
			String[] content = s.split("\n");
			
			tfConfigFile.setText(content[0]);
			tfConfigFile.setCaretPosition(tfConfigFile.getText().length());
			tfDataFile.setText(content[1]);
			tfDataFile.setCaretPosition(tfDataFile.getText().length());
			tfDataSheet.setText(content[2]);
			tfDataSheet.setCaretPosition(tfDataSheet.getText().length());
			cbSubscription.setSelected(Boolean.parseBoolean(content[3]));
			File file = new File(content[1]);
			if(file.isFile())
				path = file.getAbsolutePath();
			else
			{
				file = new File(content[0]);
				if(file.isFile())
					path = file.getAbsolutePath();
			}
		}
		catch (IOException e)
		{
			System.err.println("O ficheiro de carregamento dos campos não foi encontrado. (Load file not found)");
		}
		catch(Exception e)
		{
			System.err.println("A importação do esconderijo falhado. (Cache importation failed)");
		}
	}
	
	/**
	 * Read an input stream an returns its content
	 * @param is The input stream to read
	 * @return The input stream content
	 * @throws IOException 
	 */
	private String readInputStream(InputStream is) throws IOException
	{
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		try {
		  int read;
		  do {
		    read = in.read(buffer, 0, buffer.length);
		    if (read>0) {
		      out.append(buffer, 0, read);
		    }
		  } while (read>=0);
		} finally {
		  in.close();
		}
		return out.toString();
	}

	/**
	 * Save user fields in a cache
	 */
	private synchronized void saveCache()
	{
		String[] content = new String[4];
		try
		{
			content[0] = tfConfigFile.getText();
			content[1] = tfDataFile.getText();
			content[2] = tfDataSheet.getText();
			content[3] = Boolean.toString(cbSubscription.isSelected());
			File file = new File(CACHE_FILE);
			file.createNewFile();
//			writeOutputStream(new FileOutputStream(URLDecoder.decode(getClass().getResource(CACHE_FILE).getPath(), "UTF-8")), content);
			writeOutputStream(new FileOutputStream(file), content);
		}
		catch (Exception e)
		{
			System.err.println("O ficheiro de salvaguarda dos campos especificados não foi encontrado. (Save file not found)");
		}
	}
	
	/**
	 * Write a string array in an output stream
	 * @param os The output stream to write in
	 * @param content The strings to write
	 * @throws IOException 
	 */
	private void writeOutputStream(OutputStream os, String[] content) throws IOException
	{
		Writer out = new OutputStreamWriter(os, "UTF-8");
		try {
			for(String s : content)
			{
				out.write(s + '\n');
			}
		} finally {
		  out.close();
		}
	}

	/**
	 * Redirect system output and error stream on the interface text pane
	 */
	public void configureOutput()
	{
		PrintStream out = new JTextPaneOutputStream(taShell, System.out, false);
		System.setOut(out);
		PrintStream err = new JTextPaneOutputStream(taShell, System.out, true);
		System.setErr(err);
	}

	/**
	 * Method call when the mouse is clicked.
	 * @param e Source mouse event 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		try
		{
			if (e.getSource() == tfConfigFile && !new File(tfConfigFile.getText()).exists())
			{
				chooseConfigurationFile();
			}
			else if (e.getSource() == tfDataFile && !new File(tfDataFile.getText()).exists())
			{
				chooseDataFile();
			}
			else if (e.getSource() == tfResultsFile)
			{
				chooseResultsFile();
			}
		}
		catch(Exception e1)
		{
			Main.saveLog(e1);
		}
	}

	/**
	 * Print some pieces of information relative to the Google Distance Matrix limits
	 * in order to warn the user about his use
	 */
	private void printGoogleDistanceLimitsInformation()
	{
		System.out
				.println("--- Informações ---\n"
						+ "Este software usa a Google Distance Matrix API.\n"
						+ "A versão livre deste API é altamente limitada.\n"
						+ "O tempo necessário para pedidos é multiplicado por 6, e o número de pedidos é dividido por 40.\n"
						+ "Você pode planificar 50 lugares (25 trajetos), este número incluido os lugares do ficheiro de configuração,"
						+ " pelo 24 horas para livre, e mais de 300 (150 trajetos) com uma assinatura.\n"
						+ "Em cada caso, se um lugar é desconhecido, isto consumirá suas quotas.\n"
						+ "--- Informações ---\n");
	}

	/**
	 * Unused
	 * @param arg0 Unused
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0)
	{}

	/**
	 * Unused
	 * @param arg0 Unused
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0)
	{}

	/**
	 * Unused
	 * @param arg0 Unused
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent arg0)
	{}

	/**
	 * Unused
	 * @param e Unused
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{}

	/**
	 * Unused
	 * @param arg0 Unused
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent arg0)
	{}

	/**
	 * Unused
	 * @param arg0 Unused
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentMoved(ComponentEvent arg0)
	{}

	/**
	 * Unused
	 * @param e Unused
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent e)
	{
		try
		{
			verticalSplitPane.setDividerLocation((int) (getSize().height / 1.6));
		}
		catch (Exception e2)
		{}
	}

	/**
	 * Unused
	 * @param arg0 Unused
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentShown(ComponentEvent arg0)
	{}

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
		System.exit(0);
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
