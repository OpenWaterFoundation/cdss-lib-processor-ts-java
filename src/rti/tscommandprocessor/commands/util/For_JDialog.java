package rti.tscommandprocessor.commands.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Editor dialog for the For() command.
*/
public class For_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton __cancel_JButton = null;
private SimpleJButton __ok_JButton = null;
private For_Command __command = null;
private JTextField __Name_JTextField = null;
private JTextField __IteratorProperty_JTextField = null;
private JTabbedPane __main_JTabbedPane = null;
private JTextArea __List_JTextArea = null;
private JTextField __SequenceStart_JTextField = null;
private JTextField __SequenceEnd_JTextField = null;
private JTextField __SequenceIncrement_JTextField = null;
private SimpleJComboBox __TableID_JComboBox = null;
private JTextField __TableColumn_JTextField = null;
private JTextArea __command_JTextArea = null;
private boolean __error_wait = false; // Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Indicates whether user pressed OK to close the dialog.

/**
Command dialog editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public For_JDialog ( JFrame parent, For_Command command, List<String> tableIDChoices )
{ 	super(parent, true);
	initialize ( parent, command, tableIDChoices );
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
{   // Put together a list of parameters to check...
    PropList props = new PropList ( "" );
    String Name = __Name_JTextField.getText().trim();
    String IteratorProperty = __IteratorProperty_JTextField.getText().trim();
    String List = __List_JTextArea.getText().trim().replace('\n', ' ').replace('\t', ' ');
    String SequenceStart = __SequenceStart_JTextField.getText().trim();
    String SequenceEnd = __SequenceEnd_JTextField.getText().trim();
    String SequenceIncrement = __SequenceIncrement_JTextField.getText().trim();
    String TableID = __TableID_JComboBox.getSelected();
    String TableColumn = __TableColumn_JTextField.getText().trim();
    if ( Name.length() > 0 ) {
        props.set ( "Name", Name );
    }
    if ( IteratorProperty.length() > 0 ) {
        props.set ( "IteratorProperty", IteratorProperty );
    }
    if ( List.length() > 0 ) {
        props.set ( "List", List );
    }
    if ( SequenceStart.length() > 0 ) {
        props.set ( "SequenceStart", SequenceStart );
    }
    if ( SequenceEnd.length() > 0 ) {
        props.set ( "SequenceEnd", SequenceEnd );
    }
    if ( SequenceIncrement.length() > 0 ) {
        props.set ( "SequenceIncrement", SequenceIncrement );
    }
    if ( TableID.length() > 0 ) {
        props.set ( "TableID", TableID );
    }
    if ( TableColumn.length() > 0 ) {
        props.set ( "TableColumn", TableColumn );
    }
    try {
        // This will warn the user...
        __command.checkCommandParameters ( props, null, 1 );
    }
    catch ( Exception e ) {
        // The warning would have been printed in the check code.
        __error_wait = true;
    }
}

/**
Commit the edits to the command.
*/
private void commitEdits ()
{   String Name = __Name_JTextField.getText().trim();
    String IteratorProperty = __IteratorProperty_JTextField.getText().trim();
    String List = __List_JTextArea.getText().trim().replace('\n', ' ').replace('\t', ' ');
    String SequenceStart = __SequenceStart_JTextField.getText().trim();
    String SequenceEnd = __SequenceEnd_JTextField.getText().trim();
    String SequenceIncrement = __SequenceIncrement_JTextField.getText().trim();
    String TableID = __TableID_JComboBox.getSelected();
    String TableColumn = __TableColumn_JTextField.getText().trim();
    __command.setCommandParameter ( "Name", Name );
    __command.setCommandParameter ( "IteratorProperty", IteratorProperty );
    __command.setCommandParameter ( "List", List );
    __command.setCommandParameter ( "SequenceStart", SequenceStart );
    __command.setCommandParameter ( "SequenceEnd", SequenceEnd );
    __command.setCommandParameter ( "SequenceIncrement", SequenceIncrement );
    __command.setCommandParameter ( "TableID", TableID );
    __command.setCommandParameter ( "TableColumn", TableColumn );
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, For_Command command, List<String> tableIDChoices )
{   __command = command;

	addWindowListener( this );

    Insets insetsNONE = new Insets(1,1,1,1);
    Insets insetsTLBR = new Insets(2,2,2,2);

	// Main panel...

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = -1;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "This command starts a \"for loop\", which repeatedly executes a block of commands while changing the " +
        "value of an iterator variable for each iteration."),
        0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "The iterator value is set as a processor property that can be accessed by other commands using " +
        "the ${Property} notation."),
        0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
        "Use an EndFor() command with matching name to indicate the end of the for loop."),
        0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++y, 7, 1, 0, 0, insetsNONE, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "For loop name:" ), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __Name_JTextField = new JTextField ( 20 );
    __Name_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __Name_JTextField,
        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel(
        "Required - the name will be matched against an EndFor() command name."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "For loop iterator property:" ), 
        0, ++y, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __IteratorProperty_JTextField = new JTextField (20);
    __IteratorProperty_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(main_JPanel, __IteratorProperty_JTextField,
        1, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel("Optional - name of iterator property for iteration (default=for loop name)."), 
        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    __main_JTabbedPane = new JTabbedPane ();
    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    // Panel for list iteration
    int yList = -1;
    JPanel list_JPanel = new JPanel();
    list_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "List", list_JPanel );
    
    JGUIUtil.addComponent(list_JPanel, new JLabel (
        "The for loop can iterate over a list of values, separated by commas.  Currently the values are treated as strings."),
        0, ++yList, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(list_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yList, 7, 1, 0, 0, insetsNONE, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(list_JPanel, new JLabel ( "List:" ), 
        0, ++yList, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __List_JTextArea = new JTextArea ( 4, 60 );
    __List_JTextArea.setLineWrap ( true );
    __List_JTextArea.setWrapStyleWord ( true );
    __List_JTextArea.addKeyListener(this);
    JGUIUtil.addComponent(list_JPanel, new JScrollPane(__List_JTextArea),
        1, yList, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(list_JPanel, new JLabel("Required - list of values for iterator."), 
        3, yList, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    // Panel to use sequence for iteration
    int ySeq = -1;
    JPanel seq_JPanel = new JPanel();
    seq_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Sequence", seq_JPanel );
    
    JGUIUtil.addComponent(seq_JPanel, new JLabel (
        "The for loop can iterate using a sequence of values, given a start, end, and increment."),
        0, ++ySeq, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(seq_JPanel, new JLabel (
        "The type of property is detected from the start as either integer or double-precision (has decimal point)."),
        0, ++ySeq, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(seq_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++ySeq, 7, 1, 0, 0, insetsNONE, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(seq_JPanel, new JLabel ( "Sequence start:" ), 
        0, ++ySeq, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SequenceStart_JTextField = new JTextField (10);
    __SequenceStart_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(seq_JPanel, __SequenceStart_JTextField,
        1, ySeq, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(seq_JPanel, new JLabel("Required - sequence start."), 
        3, ySeq, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(seq_JPanel, new JLabel ( "Sequence end:" ), 
        0, ++ySeq, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SequenceEnd_JTextField = new JTextField (10);
    __SequenceEnd_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(seq_JPanel, __SequenceEnd_JTextField,
        1, ySeq, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(seq_JPanel, new JLabel("Required - sequence end."), 
        3, ySeq, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(seq_JPanel, new JLabel ( "Sequence increment:" ), 
        0, ++ySeq, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __SequenceIncrement_JTextField = new JTextField (10);
    __SequenceIncrement_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(seq_JPanel, __SequenceIncrement_JTextField,
        1, ySeq, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(seq_JPanel, new JLabel("Optional - sequence increment (default=1)."), 
        3, ySeq, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
     
    // Panel to use table column for iteration
    int yTable = -1;
    JPanel table_JPanel = new JPanel();
    table_JPanel.setLayout( new GridBagLayout() );
    __main_JTabbedPane.addTab ( "Table", table_JPanel );
    
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "The for loop can iterate using the values from a table column."),
        0, ++yTable, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel (
        "If necessary, copy a subset of values from a table using CopyTable() and other table commands."),
        0, ++yTable, 7, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JSeparator (SwingConstants.HORIZONTAL),
        0, ++yTable, 7, 1, 0, 0, insetsNONE, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Table ID:" ), 
        0, ++yTable, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit
    __TableID_JComboBox.setToolTipText("Specify the table ID or use ${Property} notation");
    tableIDChoices.add(0,""); // Add blank to ignore table
    __TableID_JComboBox.setData ( tableIDChoices );
    __TableID_JComboBox.addItemListener ( this );
    JGUIUtil.addComponent(table_JPanel, __TableID_JComboBox,
        1, yTable, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel("Required - identifier for table."), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(table_JPanel, new JLabel ( "Table column:" ), 
        0, ++yTable, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __TableColumn_JTextField = new JTextField (20);
    __TableColumn_JTextField.addKeyListener(this);
    JGUIUtil.addComponent(table_JPanel, __TableColumn_JTextField,
        1, yTable, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(table_JPanel, new JLabel("Required - name of table column."), 
        3, yTable, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
		0, ++y, 1, 1, 0, 0, insetsNONE, GridBagConstraints.NONE, GridBagConstraints.WEST);
    __command_JTextArea = new JTextArea ( 4, 60 );
    __command_JTextArea.setLineWrap ( true );
    __command_JTextArea.setWrapStyleWord ( true );
    __command_JTextArea.setEditable ( false );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
		1, y, 6, 1, 1, 0, insetsNONE, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

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

	setTitle ( "Edit " + __command.getCommandName() + "() command" );
	setResizable ( true );
    pack();
    JGUIUtil.center( this );
    super.setVisible( true );
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
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( false );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

/**
Indicate if the user pressed OK (cancel otherwise).
*/
public boolean ok ()
{   return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{	String routine = getClass().getSimpleName() + ".refresh";
    String Name = "";
    String IteratorProperty = "";
    String List = "";
    String SequenceStart = "";
    String SequenceEnd = "";
    String SequenceIncrement = "";
	String TableID = "";
	String TableColumn = "";
	__error_wait = false;
	PropList props = __command.getCommandParameters();
	if ( __first_time ) {
		__first_time = false;
		Name = props.getValue( "Name" );
	    IteratorProperty = props.getValue( "IteratorProperty" );
	    List = props.getValue( "List" );
	    SequenceStart = props.getValue( "SequenceStart" );
	    SequenceEnd = props.getValue( "SequenceEnd" );
	    SequenceIncrement = props.getValue( "SequenceIncrement" );
		TableID = props.getValue( "TableID" );
		TableColumn = props.getValue( "TableColumn" );
		if ( Name != null ) {
		    __Name_JTextField.setText( Name );
		}
        if ( IteratorProperty != null ) {
            __IteratorProperty_JTextField.setText( IteratorProperty );
        }
        if ( List != null ) {
            __List_JTextArea.setText( List );
            __main_JTabbedPane.setSelectedIndex(0);
        }
		if ( SequenceStart != null ) {
		    __SequenceStart_JTextField.setText( SequenceStart );
		    __main_JTabbedPane.setSelectedIndex(1);
		}
		if ( SequenceEnd != null ) {
		    __SequenceEnd_JTextField.setText( SequenceEnd );
		}
		if ( SequenceIncrement != null ) {
		    __SequenceIncrement_JTextField.setText( SequenceIncrement );
		}
        if ( (TableID == null) || TableID.isEmpty() ) {
            // Select default...
            __TableID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TableID_JComboBox,TableID, JGUIUtil.NONE, null, null ) ) {
                __TableID_JComboBox.select ( TableID );
                __main_JTabbedPane.setSelectedIndex(2);
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTableID value \"" + TableID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( TableColumn != null ) {
            __TableColumn_JTextField.setText( TableColumn );
        }
	}
	// Regardless, reset the command from the fields...
	Name = __Name_JTextField.getText().trim();
    IteratorProperty = __IteratorProperty_JTextField.getText().trim();
    List = __List_JTextArea.getText().trim().replace('\n', ' ').replace('\t', ' ');
    SequenceStart = __SequenceStart_JTextField.getText().trim();
    SequenceEnd = __SequenceEnd_JTextField.getText().trim();
    SequenceIncrement = __SequenceIncrement_JTextField.getText().trim();
    TableID = __TableID_JComboBox.getSelected();
    TableColumn = __TableColumn_JTextField.getText().trim();
    props = new PropList ( __command.getCommandName() );
    props.add ( "Name=" + Name );
    props.add ( "IteratorProperty=" + IteratorProperty );
    props.add ( "List=" + List );
    props.add ( "SequenceStart=" + SequenceStart );
    props.add ( "SequenceEnd=" + SequenceEnd );
    props.add ( "SequenceIncrement=" + SequenceIncrement );
    props.set ( "TableID", TableID ); // May contain = so handle differently
    props.add ( "TableColumn=" + TableColumn );
    __command_JTextArea.setText( __command.toString(props) );
}

/**
React to the user response.
@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
*/
public void response ( boolean ok )
{   __ok = ok;
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

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}