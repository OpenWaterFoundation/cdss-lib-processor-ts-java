package rti.tscommandprocessor.commands.table;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;

//import RTi.Util.GUI.DictionaryJDialog;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

public class InsertTableRow_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{

private boolean __error_wait = false; // To track errors
private boolean __first_time = true; // Indicate first time display
private JTextArea __command_JTextArea = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __InsertRow_JTextField = null;
private JTextField __InsertCount_JTextField = null;
// TODO SAM 2014-02-04 Later enable a way to set column values with a map
//private JTextArea __ColumnFilters_JTextArea = null;
//private JTextArea __ColumnValues_JTextArea = null;
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private InsertTableRow_Command __command = null;
//private JFrame __parent = null;
private boolean __ok = false;

/**
Command dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
@param tableIDChoices list of table identifiers to provide as choices
*/
public InsertTableRow_JDialog ( JFrame parent, InsertTableRow_Command command, List<String> tableIDChoices )
{	super(parent, true);
	initialize ( parent, command, tableIDChoices );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event)
{	Object o = event.getSource();

    if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			// Command has been edited...
			response ( true );
		}
	}
    /*
	else if ( event.getActionCommand().equalsIgnoreCase("EditColumnFilters") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnFilters = __ColumnFilters_JTextArea.getText().trim();
        String [] notes = { "Filter to indicate which rows should be modified",
            "Column to Filter - table column to check for values (currently can only be columns containing strings)",
            "Filter Value - value in columns to match, to determine rows to set values"
        };
        String columnFilters = (new DictionaryJDialog ( __parent, true, ColumnFilters, "Edit ColumnFilters Parameter",
            notes, "Column to Filter", "Filter Value (can include * wildcards)",10)).response();
        if ( columnFilters != null ) {
            __ColumnFilters_JTextArea.setText ( columnFilters );
            refresh();
        }
    }
    else if ( event.getActionCommand().equalsIgnoreCase("EditColumnValues") ) {
        // Edit the dictionary in the dialog.  It is OK for the string to be blank.
        String ColumnValues = __ColumnValues_JTextArea.getText().trim();
        String [] notes = { "Specify the column names and corresponding values to set." };
        String columnValues = (new DictionaryJDialog ( __parent, true, ColumnValues, "Edit ColumnValues Parameter",
            notes, "Column", "Value",10)).response();
        if ( columnValues != null ) {
            __ColumnValues_JTextArea.setText ( columnValues );
            refresh();
        }
    }
    */
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	// Put together a list of parameters to check...
	PropList props = new PropList ( "" );
	String TableID = __TableID_JComboBox.getSelected();
	String InsertRow = __InsertRow_JTextField.getText().trim();
	String InsertCount = __InsertCount_JTextField.getText().trim();
	//String ColumnFilters = __ColumnFilters_JTextArea.getText().trim().replace("\n"," ");
	//String ColumnValues = __ColumnValues_JTextArea.getText().trim().replace("\n"," ");
	__error_wait = false;

    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( InsertRow.length() > 0 ) {
        props.set ( "InsertRow", InsertRow );
    }
    if ( InsertCount.length() > 0 ) {
        props.set ( "InsertCount", InsertCount );
    }
    /*
    if ( ColumnFilters.length() > 0 ) {
        props.set ( "ColumnFilters", ColumnFilters );
    }
    if ( ColumnValues.length() > 0 ) {
        props.set ( "ColumnValues", ColumnValues );
    }
    */
	try {
	    // This will warn the user...
		__command.checkCommandParameters ( props, null, 1 );
	}
	catch ( Exception e ) {
        Message.printWarning(2,"", e);
		// The warning would have been printed in the check code.
		__error_wait = true;
	}
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{	String TableID = __TableID_JComboBox.getSelected();
    String InsertRow = __InsertRow_JTextField.getText().trim();
    String InsertCount = __InsertCount_JTextField.getText().trim();
    //String ColumnFilters = __ColumnFilters_JTextArea.getText().trim();
    //String ColumnValues = __ColumnValues_JTextArea.getText().trim();
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "InsertRow", InsertRow );
    __command.setCommandParameter ( "InsertCount", InsertCount );
	//__command.setCommandParameter ( "ColumnValues", ColumnValues );
	//__command.setCommandParameter ( "ColumnFilters", ColumnFilters );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit and possibly run.
*/
private void initialize ( JFrame parent, InsertTableRow_Command command, List<String> tableIDChoices )
{	//__parent = parent;
    __command = command;

	addWindowListener(this);

    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout(new GridBagLayout());
	getContentPane().add ("North", main_JPanel);
	int y = 0;

	JPanel paragraph = new JPanel();
	paragraph.setLayout(new GridBagLayout());
	int yy = -1;
    
   	JGUIUtil.addComponent(paragraph, new JLabel (
        "This command inserts 1+ rows into a table."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Currently only the insert row number can be specified as the insert position."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "In the future a row selection filter will be added to specify the insert position."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    JGUIUtil.addComponent(paragraph, new JLabel (
        "Currently only blank rows can be added but in the future specifying column values will be enabled."),
        0, ++yy, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JGUIUtil.addComponent(main_JPanel, paragraph,
		0, y, 7, 1, 0, 0, 5, 0, 10, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Table ID:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true );    // Allow edit
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    //__TableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
    JGUIUtil.addComponent(main_JPanel, __TableID_JComboBox,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel( "Required - original table."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel("Insert row:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertRow_JTextField = new JTextField ( "", 10 );
    __InsertRow_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InsertRow_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - row (1+) to insert before (default=at end)." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel("Insert count:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __InsertCount_JTextField = new JTextField ( "", 10 );
    __InsertCount_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InsertCount_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - number of rows to insert (default=1)." ),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    /*
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column filter(s):"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnFilters_JTextArea = new JTextArea (3,35);
    __ColumnFilters_JTextArea.setLineWrap ( true );
    __ColumnFilters_JTextArea.setWrapStyleWord ( true );
    __ColumnFilters_JTextArea.setToolTipText("ColumnName1:FilterPattern1,ColumnName2:FilterPattern2");
    __ColumnFilters_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnFilters_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Optional - select rows by matching column pattern (default=modify all rows)."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnFilters",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Column(s) and value(s) to set:"),
        0, ++y, 1, 2, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __ColumnValues_JTextArea = new JTextArea (6,35);
    __ColumnValues_JTextArea.setLineWrap ( true );
    __ColumnValues_JTextArea.setWrapStyleWord ( true );
    __ColumnValues_JTextArea.setToolTipText("Column1:Value1,Column2:Value2");
    __ColumnValues_JTextArea.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__ColumnValues_JTextArea),
        1, y, 2, 2, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ("Required - column(s) and associated value(s) to set."),
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent(main_JPanel, new SimpleJButton ("Edit","EditColumnValues",this),
        3, ++y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
        */

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Command:"), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__command_JTextArea = new JTextArea (4,40);
	__command_JTextArea.setLineWrap ( true );
	__command_JTextArea.setWrapStyleWord ( true );
	__command_JTextArea.setEditable (false);
	JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South JPanel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
 
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add (__cancel_JButton);
	__cancel_JButton.setToolTipText ( "Close window without saving changes." );
	__ok_JButton = new SimpleJButton("OK", this);
	button_JPanel.add (__ok_JButton);
	__ok_JButton.setToolTipText ( "Close window and save changes to command." );

	setTitle ( "Edit " + __command.getCommandName() + "() Command");
	setResizable (false);
    pack();
    JGUIUtil.center(this);
	refresh();	// Sets the __path_JButton status
    super.setVisible(true);
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged (ItemEvent e) {
	refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed (KeyEvent event) {
	int code = event.getKeyCode();

	if (code == KeyEvent.VK_ENTER) {
		refresh ();
		checkInput();
		if (!__error_wait) {
			response ( true );
		}
	}
}

public void keyReleased (KeyEvent event) {
	refresh();
}

public void keyTyped (KeyEvent event) {}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user canceled.
*/
public boolean ok ()
{	return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = getClass().getSimpleName() + ".refresh";
    String TableID = "";
    String InsertRow = "";
    String InsertCount = "";
    //String ColumnFilters = "";
    //String ColumnValues = "";
	PropList props = __command.getCommandParameters();
	if (__first_time) {
		__first_time = false;
        TableID = props.getValue ( "TableID" );
        InsertRow = props.getValue ( "InsertRow" );
        InsertCount = props.getValue ( "InsertCount" );
        //ColumnFilters = props.getValue ( "ColumnFilters" );
        //ColumnValues = props.getValue ( "ColumnValues" );
        if ( TableID == null ) {
            // Select default...
            __TableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TableID_JComboBox,TableID, JGUIUtil.NONE, null, null ) ) {
                __TableID_JComboBox.select ( TableID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTableID value \"" + TableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( InsertRow != null ) {
            __InsertRow_JTextField.setText ( InsertRow );
        }
        if ( InsertCount != null ) {
            __InsertCount_JTextField.setText ( InsertCount );
        }
        /*
        if ( ColumnFilters != null ) {
            __ColumnFilters_JTextArea.setText ( ColumnFilters );
        }
        if ( ColumnValues != null ) {
            __ColumnValues_JTextArea.setText ( ColumnValues );
        }
        */
	}
	// Regardless, reset the command from the fields...
	TableID = __TableID_JComboBox.getSelected();
    InsertRow = __InsertRow_JTextField.getText().trim();
    InsertCount = __InsertCount_JTextField.getText().trim();
	//ColumnFilters = __ColumnFilters_JTextArea.getText().trim();
	//ColumnValues = __ColumnValues_JTextArea.getText().trim();
	props = new PropList ( __command.getCommandName() );
    props.add ( "TableID=" + TableID );
    props.add ( "InsertRow=" + InsertRow );
    props.add ( "InsertCount=" + InsertCount );
    //props.add ( "ColumnFilters=" + ColumnFilters );
	//props.add ( "ColumnValues=" + ColumnValues );
	__command_JTextArea.setText( __command.toString ( props ) );
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
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
public void windowClosing(WindowEvent event) {
	response ( false );
}

// The following methods are all necessary because this class implements WindowListener
public void windowActivated(WindowEvent evt) {}
public void windowClosed(WindowEvent evt) {}
public void windowDeactivated(WindowEvent evt) {}
public void windowDeiconified(WindowEvent evt) {}
public void windowIconified(WindowEvent evt) {}
public void windowOpened(WindowEvent evt) {}

}