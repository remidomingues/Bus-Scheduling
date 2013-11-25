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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Modal frame, use to enter a location coordinates when this one is not found
 */
public class LocationNotFoundFrame extends JDialog implements ActionListener
{
	/** Default serial version ID */
	private static final long serialVersionUID = 1L;
	
	//Warning message
	/** Warning label */
	private JTextArea taWarning;
	
	
	//Latitude informations
	/** Latitude label */
	private JLabel lbLatitude = new JLabel("Latitude");

	/** Latitude text field */
	private JTextField tfLatitude = new JTextField();

	/** Panel used for latitude input */
	private JPanel plLatitude = new JPanel(new FlowLayout());
	
	
	//Longitude informations
	/** Longitude label */
	private JLabel lbLongitude = new JLabel("Longitude");

	/** Latitude text field */
	private JTextField tfLongitude = new JTextField();

	/** Panel used for longitude input */
	private JPanel plLongitude = new JPanel(new FlowLayout());

	
	//Buttons
	/** OK button */
	private JButton btOk = new JButton("Ok");

	/** Cancel button */
	private JButton btCancel = new JButton("Cancel");
	

	/** false if the ok button were pressed, false else */
	private boolean isCanceled = true;
	
	/** Latitude */
	private float latitude;
	
	/** Longitude */
	private float longitude;
	
	/**
	 * Constructor
	 * @param parent Parent frame
	 * @param message Message to print
	 */
	public LocationNotFoundFrame(ProcessFrame parent, String message)
	{
		super(parent, true);
		
		taWarning = new JTextArea(message);
		taWarning.setEditable(false);
		taWarning.setBackground(new Color(238, 238, 238));
		
		tfLatitude.setPreferredSize(new Dimension(100, 25));
		plLatitude.add(lbLatitude);
		plLatitude.add(tfLatitude);

		tfLongitude.setPreferredSize(new Dimension(100, 25));
		plLongitude.add(lbLongitude);
		plLongitude.add(tfLongitude);
		
		JPanel plMainPanel = new JPanel(new GridLayout(3, 1));
		plMainPanel.add(taWarning);
		plMainPanel.add(plLatitude);
		plMainPanel.add(plLongitude);
		plMainPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

		btOk.setSize(new Dimension(50, 25));
		btOk.addActionListener(this);
		btOk.setFocusable(false);
		btCancel.setSize(new Dimension(50, 25));
		btCancel.addActionListener(this);
		btCancel.setFocusable(false);
		
		JPanel plButtons = new JPanel(new FlowLayout());
		plButtons.add(btOk);
		plButtons.add(btCancel);

		// Main frame
		this.add(plMainPanel, BorderLayout.CENTER);
		this.add(plButtons, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setMinimumSize(new Dimension(400, 175));
		this.setLocationRelativeTo(getParent());
		this.setResizable(false);
		this.setTitle("Cuidado");
		this.setVisible(true);
	}

	/**
	 * 
	 * @param e 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btCancel)
			this.dispose();
		else if(e.getSource() == btOk)
		{
			if(tfLatitude.getText().isEmpty() || tfLongitude.getText().isEmpty())
			{
				JOptionPane.showOptionDialog(getParent(),
						"Um campo obrigatório falta.", "Cuidado",
						JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				return;
			}
			try
			{
				latitude = Float.parseFloat(tfLatitude.getText());
				longitude = Float.parseFloat(tfLongitude.getText());
			}
			catch(NumberFormatException e1)
			{
				JOptionPane.showOptionDialog(getParent(),
						"Um campo e errado.", "Cuidado",
						JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				return;
			}
			isCanceled = false;
			this.dispose();
		}
	}

	/**
	 * Return the isCanceled value
	 * @return The isCanceled value
	 */
	public boolean isCanceled()
	{
		return isCanceled;
	}

	/**
	 * Return the latitude value
	 * @return The latitude value
	 */
	public float getLatitude()
	{
		return latitude;
	}

	/**
	 * Return the longitude value
	 * @return The longitude value
	 */
	public float getLongitude()
	{
		return longitude;
	}
	
}
