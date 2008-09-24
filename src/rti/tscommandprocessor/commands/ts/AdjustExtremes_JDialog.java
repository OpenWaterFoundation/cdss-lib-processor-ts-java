package rti.tscommandprocessor.commands.ts;

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
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import rti.tscommandprocessor.core.TSListType;
import rti.tscommandprocessor.ui.CommandEditorUtil;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.Command;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Editor for the AdjustExtremes() command.
*/
public class AdjustExtremes_JDialog extends JDialog
implements ActionListener, ItemListener, KeyListener, WindowListener
{
private SimpleJButton	__cancel_JButton = null,// Cancel Button
			__ok_JButton = null;	// Ok Button
private SimpleJComboBox __TSList_JComboBox = null;
private JLabel __TSID_JLabel = null;
private SimpleJComboBox __TSID_JComboBox = null;
private JLabel __EnsembleID_JLabel = null;
private SimpleJComboBox __EnsembleID_JComboBox = null;
private AdjustExtremes_Command __command = null; // Command as Vector of String
private JTextArea __command_JTextArea=null;// Command as TextField
private SimpleJComboBox	__AdjustMethod_JComboBox = null;// Method to adjust.
private SimpleJComboBox	__ExtremeToAdjust_JComboBox = null;
private JTextField __ExtremeValue_JTextField=null;
private JTextField __MaxIntervals_JTextField = null;
private JTextField __AnalysisStart_JTextField = null;
private JTextField __AnalysisEnd_JTextField = null;
private boolean __error_wait = false;	// Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false;       // Indicates whether OK button has been pressed.

/**
Command editor dialog constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public AdjustExtremes_JDialog ( JFrame parent, Command command )
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
Check the GUI state to make sure that appropriate components are enabled/disabled.
*/
private void checkGUIState ()
{
    String TSList = __TSList_JComboBox.getSelected();
    if ( TSListType.ALL_MATCHING_TSID.equals(TSList) ||
            TSListType.FIRST_MATCHING_TSID.equals(TSList) ||
            TSListType.LAST_MATCHING_TSID.equals(TSList) ) {
        __TSID_JComboBox.setEnabled(true);
        __TSID_JLabel.setEnabled ( true );
    }
    else {
        __TSID_JComboBox.setEnabled(false);
        __TSID_JLabel.setEnabled ( false );
    }
    if ( TSListType.ENSEMBLE_ID.equals(TSList)) {
        __EnsembleID_JComboBox.setEnabled(true);
        __EnsembleID_JLabel.setEnabled ( true );
    }
    else {
        __EnsembleID_JComboBox.setEnabled(false);
        __EnsembleID_JLabel.setEnabled ( false );
    }
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{   // Put together a list of parameters to check...
    PropList parameters = new PropList ( "" );
    String TSList = __TSList_JComboBox.getSelected();
    String TSID = __TSID_JComboBox.getSelected();
    String EnsembleID = __EnsembleID_JComboBox.getSelected();
    String AdjustMethod = __AdjustMethod_JComboBox.getSelected();
    String ExtremeToAdjust = __ExtremeToAdjust_JComboBox.getSelected();
    String ExtremeValue = __ExtremeValue_JTextField.getText().trim();
    String MaxIntervals = __MaxIntervals_JTextField.getText().trim();
    String AnalysisStart = __AnalysisStart_JTextField.getText().trim();
    String AnalysisEnd = __AnalysisEnd_JTextField.getText().trim();
    
    __error_wait = false;

    if ( TSList.length() > 0 ) {
        parameters.set ( "TSList", TSList );
    }
    if ( TSID.length() > 0 ) {
        parameters.set ( "TSID", TSID );
    }
    if ( EnsembleID.length() > 0 ) {
        parameters.set ( "EnsembleID", EnsembleID );
    }
    if ( AdjustMethod.length() > 0 ) {
        parameters.set ( "AdjustMethod", AdjustMethod );
    }
    if ( ExtremeToAdjust.length() > 0 ) {
        parameters.set ( "ExtremeToAdjust", ExtremeToAdjust );
    }
    if ( ExtremeValue.length() > 0 ) {
        parameters.set ( "ExtremeValue", ExtremeValue );
    }
    if ( MaxIntervals.length() > 0 ) {
        parameters.set ( "MaxIntervals", MaxIntervals );
    }
    if ( AnalysisStart.length() > 0 ) {
        parameters.set ( "AnalysisStart", AnalysisStart );
    }
    if ( AnalysisEnd.length() > 0 ) {
        parameters.set ( "AnalysisEnd", AnalysisEnd );
    }
    try {   // This will warn the user...
        __command.checkCommandParameters ( parameters, null, 1 );
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
{   String TSList = __TSList_JComboBox.getSelected();
    String TSID = __TSID_JComboBox.getSelected();
    String EnsembleID = __EnsembleID_JComboBox.getSelected();   
    String AdjustMethod = __AdjustMethod_JComboBox.getSelected();
    String ExtremeToAdjust = __ExtremeToAdjust_JComboBox.getSelected();
    String ExtremeValue = __ExtremeValue_JTextField.getText().trim();
    String MaxIntervals = __MaxIntervals_JTextField.getText().trim();
    String AnalysisStart = __AnalysisStart_JTextField.getText().trim();
    String AnalysisEnd = __AnalysisEnd_JTextField.getText().trim();
    __command.setCommandParameter ( "TSList", TSList );
    __command.setCommandParameter ( "TSID", TSID );
    __command.setCommandParameter ( "EnsembleID", EnsembleID );
    __command.setCommandParameter ( "AdjustMethod", AdjustMethod );
    __command.setCommandParameter ( "ExtremeToAdjust", ExtremeToAdjust );
    __command.setCommandParameter ( "ExtremeValue", ExtremeValue );
    __command.setCommandParameter ( "MaxIntervals", MaxIntervals );
    __command.setCommandParameter ( "AnalysisStart", AnalysisStart );
    __command.setCommandParameter ( "AnalysisEnd", AnalysisEnd );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__TSID_JComboBox = null;
	__AdjustMethod_JComboBox = null;
	__ExtremeToAdjust_JComboBox = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__command = null;
	__ok_JButton = null;
	__ExtremeValue_JTextField = null;
	__MaxIntervals_JTextField = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (AdjustExtremes_Command)command;

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Adjust extreme data values by considering values to each side of extreme values."),
		0, y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"If the Extreme to Adjust is AdjustMinimum, values < Extreme Value are adjusted." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"If the Extreme to Adjust is AdjustMaximum, values > Extreme Value are adjusted." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The Average Adjust Method replaces the extreme and values on each side of the extreme"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"by the average of all values, preserving the total." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
/* TODO SAM Evaluate code
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The WeightedAverage Adjust Method replaces the extreme and" +
		" values on each side of the extreme"),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"by the average of all values, preserving the total." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
*/
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"The maximum intervals parameter indicates how many intervals on" +
		" each side of the extreme can be modified."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify dates with precision appropriate for the data." ),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    __TSList_JComboBox = new SimpleJComboBox(false);
    y = CommandEditorUtil.addTSListToEditorDialogPanel ( this, main_JPanel, __TSList_JComboBox, y );

    __TSID_JLabel = new JLabel ("TSID (for TSList=" + TSListType.ALL_MATCHING_TSID.toString() + "):");
    __TSID_JComboBox = new SimpleJComboBox ( true );  // Allow edits
    Vector tsids = TSCommandProcessorUtil.getTSIdentifiersNoInputFromCommandsBeforeCommand(
            (TSCommandProcessor)__command.getCommandProcessor(), __command );
    y = CommandEditorUtil.addTSIDToEditorDialogPanel ( this, this, main_JPanel, __TSID_JLabel, __TSID_JComboBox, tsids, y );
    
    __EnsembleID_JLabel = new JLabel ("EnsembleID (for TSList=" + TSListType.ENSEMBLE_ID.toString() + "):");
    __EnsembleID_JComboBox = new SimpleJComboBox ( true ); // Allow edits
    Vector EnsembleIDs = TSCommandProcessorUtil.getEnsembleIdentifiersFromCommandsBeforeCommand(
            (TSCommandProcessor)__command.getCommandProcessor(), __command );
    y = CommandEditorUtil.addEnsembleIDToEditorDialogPanel (
            this, this, main_JPanel, __EnsembleID_JLabel, __EnsembleID_JComboBox, EnsembleIDs, y );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Adjust method:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__AdjustMethod_JComboBox = new SimpleJComboBox ( false );
	__AdjustMethod_JComboBox.addItem ( __command._Average );
	//__method_JComboBox.addItem ( "WeightedAverage" );
	__AdjustMethod_JComboBox.select ( __command._Average );
        JGUIUtil.addComponent(main_JPanel, __AdjustMethod_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Extreme to adjust:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ExtremeToAdjust_JComboBox = new SimpleJComboBox ( false );
	__ExtremeToAdjust_JComboBox.addItem ( __command._AdjustMinimum );
	__ExtremeToAdjust_JComboBox.addItem ( __command._AdjustMaximum );
	__ExtremeToAdjust_JComboBox.select ( __command._AdjustMinimum );
        JGUIUtil.addComponent(main_JPanel, __ExtremeToAdjust_JComboBox,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Extreme value:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__ExtremeValue_JTextField = new JTextField ( "0", 10 );
	__ExtremeValue_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __ExtremeValue_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Required."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Maximum intervals:" ),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__MaxIntervals_JTextField = new JTextField ( "0", 10 );
	__MaxIntervals_JTextField.addKeyListener ( this );
        JGUIUtil.addComponent(main_JPanel, __MaxIntervals_JTextField,
		1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - default = 0, no limit."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ("Analysis start:"), 
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AnalysisStart_JTextField = new JTextField (20);
    __AnalysisStart_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AnalysisStart_JTextField,
        1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - default is all."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Analysis End:"), 
        0, ++y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __AnalysisEnd_JTextField = new JTextField (20);
    __AnalysisEnd_JTextField.addKeyListener (this);
    JGUIUtil.addComponent(main_JPanel, __AnalysisEnd_JTextField,
        1, y, 6, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - default is all."),
        3, y, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ), 
            0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __command_JTextArea = new JTextArea ( 4, 55 );
    __command_JTextArea.setLineWrap ( true );
    __command_JTextArea.setWrapStyleWord ( true );
    __command_JTextArea.setEditable ( false );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
        1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
    checkGUIState();
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
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{	checkGUIState();
    refresh();
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

    refresh ();
	if ( code == KeyEvent.VK_ENTER ) {
		checkInput();
		if ( !__error_wait ) {
			response ( false );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event )
{
}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok ()
{   return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{   String routine = "AdjustExtremes_JDialog.refresh";
    String TSList = "";
    String TSID = "";
    String EnsembleID = "";
    String AdjustMethod = "";
    String ExtremeToAdjust = "";
    String ExtremeValue = "";
    String MaxIntervals = "";
    String AnalysisStart = "";
    String AnalysisEnd = "";
    PropList props = __command.getCommandParameters();
    if ( __first_time ) {
        __first_time = false;
        // Get the parameters from the command...
        TSList = props.getValue ( "TSList" );
        TSID = props.getValue ( "TSID" );
        EnsembleID = props.getValue ( "EnsembleID" );
        AdjustMethod = props.getValue ( "AdjustMethod" );
        ExtremeToAdjust = props.getValue ( "ExtremeToAdjust" );
        ExtremeValue = props.getValue ( "ExtremeValue" );
        MaxIntervals = props.getValue ( "MaxIntervals" );
        AnalysisStart = props.getValue ( "SetStart" );
        AnalysisEnd = props.getValue ( "SetEnd" );
        if ( TSList == null ) {
            // Select default...
            __TSList_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __TSList_JComboBox,TSList, JGUIUtil.NONE, null, null ) ) {
                __TSList_JComboBox.select ( TSList );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nTSList value \"" + TSList +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if (    JGUIUtil.isSimpleJComboBoxItem( __TSID_JComboBox, TSID,
                JGUIUtil.NONE, null, null ) ) {
                __TSID_JComboBox.select ( TSID );
        }
        else {  // Automatically add to the list after the blank...
            if ( (TSID != null) && (TSID.length() > 0) ) {
                __TSID_JComboBox.insertItemAt ( TSID, 1 );
                // Select...
                __TSID_JComboBox.select ( TSID );
            }
            else {  // Select the blank...
                __TSID_JComboBox.select ( 0 );
            }
        }
        if ( EnsembleID == null ) {
            // Select default...
            __EnsembleID_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem( __EnsembleID_JComboBox,EnsembleID, JGUIUtil.NONE, null, null ) ) {
                __EnsembleID_JComboBox.select ( EnsembleID );
            }
            else {
                Message.printWarning ( 1, routine,
                "Existing command references an invalid\nEnsembleID value \"" + EnsembleID +
                "\".  Select a different value or Cancel.");
                __error_wait = true;
            }
        }
        if ( AdjustMethod == null ) {
            // Select default...
            __AdjustMethod_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem(
                __AdjustMethod_JComboBox,
                AdjustMethod, JGUIUtil.NONE, null, null ) ) {
                __AdjustMethod_JComboBox.select ( AdjustMethod );
            }
            else {  Message.printWarning ( 1,
                "adjustExtremes_JDialog.refresh",
                "Existing command uses an unrecognized\n"+
                "adjust method \"" + AdjustMethod +
                "\".  Select a recognized value." );
            }
        }
        if ( ExtremeToAdjust == null ) {
            // Select default...
            __ExtremeToAdjust_JComboBox.select ( 0 );
        }
        else {
            if ( JGUIUtil.isSimpleJComboBoxItem(
                __ExtremeToAdjust_JComboBox,
                ExtremeToAdjust, JGUIUtil.NONE, null, null ) ) {
                __ExtremeToAdjust_JComboBox.select ( ExtremeToAdjust );
            }
            else {  Message.printWarning ( 1,
                "adjustExtremes_JDialog.refresh",
                "Existing command uses an unrecognized\n"+
                "extreme to adjust \"" + ExtremeToAdjust +
                "\".  Select a recognized value." );
            }
        }
        if ( ExtremeValue != null ) {
            __ExtremeValue_JTextField.setText( ExtremeValue );
        }
        if ( MaxIntervals != null ) {
            __MaxIntervals_JTextField.setText( MaxIntervals );
        }
        if ( AnalysisStart != null ) {
            __AnalysisStart_JTextField.setText( AnalysisStart );
        }
        if ( AnalysisStart != null ) {
            __AnalysisStart_JTextField.setText( AnalysisStart );
        }
        if ( AnalysisStart != null ) {
            __AnalysisStart_JTextField.setText( AnalysisStart );
        }
        if ( AnalysisEnd != null ) {
            __AnalysisEnd_JTextField.setText( AnalysisEnd );
        }
    }
    // Regardless, reset the command from the fields...
    TSList = __TSList_JComboBox.getSelected();
    TSID = __TSID_JComboBox.getSelected();
    EnsembleID = __EnsembleID_JComboBox.getSelected();
    AdjustMethod = __AdjustMethod_JComboBox.getSelected();
    ExtremeToAdjust = __ExtremeToAdjust_JComboBox.getSelected();
    ExtremeValue = __ExtremeValue_JTextField.getText().trim();
    MaxIntervals = __MaxIntervals_JTextField.getText().trim();
    AnalysisStart = __AnalysisStart_JTextField.getText().trim();
    AnalysisEnd = __AnalysisEnd_JTextField.getText().trim();
    props = new PropList ( __command.getCommandName() );
    props.add ( "TSList=" + TSList );
    props.add ( "TSID=" + TSID );
    props.add ( "EnsembleID=" + EnsembleID );
    props.add ( "AdjustMethod=" + AdjustMethod );
    props.add ( "ExtremeToAdjust=" + ExtremeToAdjust );
    props.add ( "ExtremeValue=" + ExtremeValue );
    props.add ( "MaxIntervals=" + MaxIntervals );
    props.add ( "AnalysisStart=" + AnalysisStart );
    props.add ( "AnalysisEnd=" + AnalysisEnd );
    __command_JTextArea.setText( __command.toString ( props ) );
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{   __ok = ok;  // Save to be returned by ok()
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

} // end adjustExtremes_JDialog