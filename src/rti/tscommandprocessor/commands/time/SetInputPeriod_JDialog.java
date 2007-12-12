package rti.tscommandprocessor.commands.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;

public class SetInputPeriod_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
private SimpleJButton	__cancel_JButton = null,// Cancel Button
			__ok_JButton = null;	// Ok Button
private JTextField	__InputStart_JTextField = null,// Dates for period.
			__InputEnd_JTextField = null;// Dates for period.
private SetInputPeriod_Command __command = null;	// Command to edit.
private JTextArea	__command_JTextArea = null;
private boolean		__error_wait = false;	// Is there an error that we
						// are waiting to be cleared up
						// or Cancel?
private boolean		__first_time = true;
private boolean		__ok = false;		// Indicates whether OK button
						// has been pressed.

/**
SetInputPeriod_JDialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public SetInputPeriod_JDialog ( JFrame parent, Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String InputStart = __InputStart_JTextField.getText();
	String InputEnd = __InputEnd_JTextField.getText();
	__error_wait = false;

	if ( InputStart.length() > 0 ) {
		props.set ( "InputStart", InputStart );
	}
	if ( InputEnd.length() > 0 ) {
		props.set ( "InputEnd", InputEnd );
	}
	try {	// This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String InputStart = __InputStart_JTextField.getText();
	String InputEnd = __InputEnd_JTextField.getText();
	__command.setCommandParameter ( "InputStart", InputStart );
	__command.setCommandParameter ( "InputEnd", InputEnd );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__cancel_JButton = null;
	__InputStart_JTextField = null;
	__InputEnd_JTextField = null;
	__command_JTextArea = null;
	__ok_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent Frame class instantiating this class.
@param title Dialog title.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (SetInputPeriod_Command)command;

	addWindowListener( this );

    Insets insetsTLBR = new Insets(7,2,7,2);
    Insets insetsMin = new Insets(0,2,0,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

	// Main contents...

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The input period constrains the period when reading data " +
		"from files and databases."),
		0, y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ("Use this command only "+
		"if a limited data period is necessary (e.g., to improve " +
		"performance)."),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel,new JLabel("Using a SetInputPeriod()"+
		" command may result in incomplete data being available for data filling."),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel ( "Enter date/times to a " +
		"precision appropriate for time series being read.  For " +
		"example:"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Year data:   YYYY"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Month data:   MM/YYYY or YYYY-MM"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Day data:     MM/DD/YYYY or YYYY-MM-DD"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Hour data:    MM/DD/YYYY HH or YYYY-MM-DD HH"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    Minute data:  MM/DD/YYYY HH:mm or YYYY-MM-DD HH:mm"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Special values are also recognized (for all precisions):"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToYear = the current date to year precision"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToMinute = the current date/time to minute " +
		"precision"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToMinute - 7Day = current date/time minus 7 days"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"    CurrentToMinute + 7Day = current date/time plus 7 days"),
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Leave blank to read all available data (default if " +
		"SetInputPeriod() command is not used)."), 
		0, ++y, 6, 1, 0, 0, insetsMin, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input period start:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputStart_JTextField = new JTextField ( 20 );
	__InputStart_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputStart_JTextField,
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Input period end:" ),
		0, ++y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputEnd_JTextField = new JTextField ( 20 );
	__InputEnd_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __InputEnd_JTextField,
		1, y, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea ( 4, 40 );
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add ( __ok_JButton );

	setTitle ( "Edit " + __command.getCommandName() + "() Command" );
	setResizable ( true );
        pack();
        JGUIUtil.center( this );
        super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	
}

public void keyReleased ( KeyEvent event )
{	refresh();	
}

public void keyTyped ( KeyEvent event ) {;}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String InputStart = "";
	String InputEnd = "";
	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		// Get the parameters from the command...
		InputStart = props.getValue ( "InputStart" );
		InputEnd = props.getValue ( "InputEnd" );
		if ( InputStart != null ) {
			__InputStart_JTextField.setText( InputStart );
		}
		if ( InputEnd != null ) {
			__InputEnd_JTextField.setText( InputEnd );
		}
	}
	// Regardless, reset the command from the fields...
	InputStart = __InputStart_JTextField.getText().trim();
	InputEnd = __InputEnd_JTextField.getText().trim();
	props = new PropList ( __command.getCommandName() );
	props.add ( "InputStart=" + InputStart );
	props.add ( "InputEnd=" + InputEnd );
	__command_JTextArea.setText(__command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{	__ok = ok;	// Save to be returned by ok()
	if ( ok ) {
		// Commit the changes...
		commitEdits ();
		if ( __error_wait ) {
			// Not ready to close out!
			return;
		}
	}
	// Now close out...
	setVisible( false );
	dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt )
{
}

public void windowClosed( WindowEvent evt )
{
}

public void windowDeactivated( WindowEvent evt )
{
}

public void windowDeiconified( WindowEvent evt )
{
}

public void windowIconified( WindowEvent evt )
{
}

public void windowOpened( WindowEvent evt )
{
}

} // end SetInputPeriod_JDialog
