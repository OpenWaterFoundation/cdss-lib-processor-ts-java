package rti.tscommandprocessor.commands.riverware;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.RiverWareTS;
import RTi.TS.TSEnsemble;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandSavesMultipleVersions;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the ReadRiverWare() command.
*/
public class ReadRiverWare_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider, CommandSavesMultipleVersions
{

protected final String _FALSE = "False";
protected final String _TRUE = "True";

protected final String _TimeSeries = "TimeSeries";
protected final String _TimeSeriesAndEnsembles = "TimeSeriesAndEnsembles";

/**
Private data members shared between the checkCommandParameter() and the 
runCommand() methods (prevent code duplication parsing dateTime strings).  
*/
private DateTime __InputStart = null;
private DateTime __InputEnd = null;

/**
List of time series read during discovery.  These are TS objects but with mainly the
metadata (TSIdent) filled in.
*/
private List<TS> __discoveryTSList = null;

/**
List of ensembles read during discovery.  These are TSEnsemble objects but with mainly the
metadata (identifier) filled in.
*/
private List<TSEnsemble> __discoveryEnsembleList = null;

/**
Constructor.
*/
public ReadRiverWare_Command ()
{
	super();
	setCommandName ( "ReadRiverWare" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{
    String routine = getClass().getName() + ".checkCommandParameters";
	String warning = "";
    String message;
    
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// Get the property values. 
	String InputFile = parameters.getValue("InputFile");
	String Output = parameters.getValue("Output");
	//String Units  = parameters.getValue("Units");
	String InputStart = parameters.getValue("InputStart");
	String InputEnd   = parameters.getValue("InputEnd");
	String Alias = parameters.getValue("Alias");
    
	// Require the alias to match historical behavior
	if ( (Alias == null) || Alias.equals("") ) {
	    message = "The Alias must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an alias." ) );
    }

    if (Alias != null && !Alias.equals("")) {
        if (Alias.indexOf(" ") > -1) {
            // do not allow spaces in the alias
            message = "The Alias value cannot contain any spaces.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Remove spaces from the alias." ) );
        }
    }

    if ( (InputFile == null) || (InputFile.length() == 0) ) {
        message = "The input file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing input file." ) );
    }
    else {
        String working_dir = null;
        try {
            // TODO SAM 2011-05-23 The below is higher overhead but deals with dynamic folder changes
            // Why doesn't it work?
            //working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (TSCommandProcessor)processor, this );
            //Message.printStatus(2, routine, "Working directory = \"" + working_dir + "\".");
            Object o = processor.getPropContents ( "WorkingDir" );
            // Working directory is available so use it...
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            Message.printWarning(3, routine, message );
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify an existing input file." ) );
        }
    
        try {
            Message.printStatus(2, routine, "Working directory is \"" + working_dir + "\"" );
            //String adjusted_path = 
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                    TSCommandProcessorUtil.expandParameterValue(processor,this,InputFile)));
        }
        catch ( Exception e ) {
            message = "The input file:\n" +
            "    \"" + InputFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that input file and working directory paths are compatible." ) );
        }
    }
    
    if ( Output != null && !Output.equals("") && !Output.equalsIgnoreCase(_TimeSeries) &&
        !Output.equalsIgnoreCase(_TimeSeriesAndEnsembles) ) {
        message = "Output (" + Output  + ") is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify Output as " + _TimeSeries + " (default) or " + _TimeSeriesAndEnsembles + ".") );
    }
    

    /*
	if ( Units != null ) {
		// Will check at run time
	}
	*/

	// InputStart
	if ((InputStart != null) && !InputStart.equals("")) {
		try {
			__InputStart = DateTime.parse(InputStart);
		} 
		catch (Exception e) {
            message = "The input start date/time \"" + InputStart + "\" is not valid.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid input start." ) );
		}
	}

	// InputEnd
	if ((InputEnd != null) && !InputEnd.equals("")) {
		try {
			__InputEnd = DateTime.parse(InputEnd);
		} 
		catch (Exception e) {
            message = "The input end date/time \"" + InputEnd + "\" is not valid.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid input end." ) );
		}
	}
	
    if ( (__InputStart != null) && (__InputEnd != null) ) {
        if ( __InputStart.getPrecision() != __InputEnd.getPrecision() ) {
            message = "The input start \"" + InputStart + "\" precision and input end \"" + InputEnd +
            "\" precision are not the same.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify consistent precision for input start and end." ) );
        }       
    }

	// Make sure __InputStart precedes __InputEnd
	if ( __InputStart != null && __InputEnd != null ) {
		if ( __InputStart.greaterThanOrEqualTo( __InputEnd ) ) {
            message = InputStart + " (" + __InputStart  + ") should be less than InputEnd (" + __InputEnd + ").";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify an input start less than the input end." ) );
		}
	}
    
	// Check for invalid parameters...
	List<String> valid_Vector = new Vector<String>();
    valid_Vector.add ( "Alias" );
    valid_Vector.add ( "InputFile" );
    valid_Vector.add ( "Output" );
    valid_Vector.add ( "InputStart" );
    valid_Vector.add ( "InputEnd" );
    valid_Vector.add ( "Units" );
    warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Create the list of ensembles from the list of time series.  The object name and slot name combinations make the unique
ensemble identifiers.
@param tslist list of time series to process
@return list of TSEnsemble determined from the time series
*/
private List<TSEnsemble> createEnsembleList ( List<TS> tslist )
{
    List<TSEnsemble> ensembleList = new ArrayList<TSEnsemble>();
    List<String> objectNames = new ArrayList<String>();
    List<String> slotNames = new ArrayList<String>();
    String objectName, slotName;
    boolean found;
    for ( TS ts : tslist ) {
        objectName = ts.getLocation();
        slotName = ts.getDataType();
        found = false;
        for ( int i = 0; i < objectNames.size(); i++ ) {
            if ( objectNames.get(i).equalsIgnoreCase(objectName) && slotNames.get(i).equalsIgnoreCase(slotName)) {
                found = true;
                break;
            }
        }
        if ( !found ) {
            // Create an ensemble and save the names to check against the other time series
            objectNames.add(objectName);
            slotNames.add(slotName);
        }
    }
    // Now have a unique set of object name and slot name pairs.  Loop and create the ensembles
    List<TS> ensembleTS = null;
    for ( int i = 0; i < objectNames.size(); i++ ) {
        // Get the list of matching time series
        ensembleTS = new ArrayList<TS>();
        objectName = objectNames.get(i);
        slotName = slotNames.get(i);
        for ( TS ts : tslist ) {
            if ( ts.getLocation().equalsIgnoreCase(objectName) && ts.getDataType().equalsIgnoreCase(slotName) ) {
                ensembleTS.add(ts);
            }
        }
        ensembleList.add ( new TSEnsemble(objectName + "_" + slotName, objectName + "_" + slotName, ensembleTS));
    }
    return ensembleList;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	
	// The command will be modified if changed...
	return ( new ReadRiverWare_JDialog ( parent, this ) ).ok();
}

/**
Return the list of ensembles read in discovery phase.
*/
private List<TSEnsemble> getDiscoveryEnsembleList ()
{
    return __discoveryEnsembleList;
}

/**
Return the list of time series read in discovery phase.
*/
private List<TS> getDiscoveryTSList ()
{
    return __discoveryTSList;
}

/**
Return the list of data objects read by this object in discovery mode.
*/
public List getObjectList ( Class c )
{
	List<TS> discovery_TS_Vector = getDiscoveryTSList ();
	if ( c == TSEnsemble.class ) {
        return getDiscoveryEnsembleList();
    }
	// Time series
    if ( (discovery_TS_Vector == null) || (discovery_TS_Vector.size() == 0) ) {
        return null;
    }
    // Since all time series must be the same interval, check the class for the first one (e.g., MonthTS)
    TS datats = discovery_TS_Vector.get(0);
    // Use the most generic for the base class...
    if ( (c == TS.class) || (c == datats.getClass()) ) {
        return discovery_TS_Vector;
    }
    else {
        return null;
    }
}

/**
Parse the command string into a PropList of parameters.
@param commandString A string command to parse.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings (recommended is 2).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String commandString )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_level = 2;
	String routine = "ReadRiverWare_Command.parseCommand", message;
	
    if ( !commandString.trim().toUpperCase().startsWith("TS") ) {
        // New style syntax using simple parameter=value notation
        super.parseCommand(commandString);
    }
    else {
        String Alias = null;
    	String InputFile = null;
    	String InputStart = null;
    	String InputEnd = null;
    	String Units = null;
        if (StringUtil.startsWithIgnoreCase(commandString, "TS ")) {
            // There is an alias specified.  Extract the alias from the full command.
            int index = commandString.indexOf("=");
            Alias = StringUtil.getToken ( commandString, " =", StringUtil.DELIM_SKIP_BLANKS, 1);
            if ( (StringUtil.patternCount(commandString,"=") >=2) || commandString.endsWith("()") ) {
                // New syntax, can be blank parameter list.  Extract command name and parameters to parse
                super.parseCommand ( commandString.substring(index + 1).trim() );
            }
            else {
                // Parse the old command...
            	List<String> tokens = StringUtil.breakStringList ( commandString.substring(index + 1),
                    "(,)", StringUtil.DELIM_ALLOW_STRINGS );
                if ( tokens.size() != 5 ) {
                    message = "Invalid syntax for legacy command \"" + commandString +
                    "\".  Expecting TS Alias = ReadRiverWare(InputFile,Units,InputStart,InputEnd).";
                    Message.printWarning ( warning_level, routine, message);
                    CommandStatus status = getCommandStatus();
                    status.addToLog ( CommandPhaseType.INITIALIZATION,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Verify command syntax or edit with command editor." ) );
                    throw new InvalidCommandSyntaxException ( message );
                }
                InputFile = (tokens.get(1)).trim();
                Units = (tokens.get(2)).trim();
                InputStart = (tokens.get(3)).trim();
                InputEnd = (tokens.get(4)).trim();
                PropList parameters = getCommandParameters();
                parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
                parameters.set ( "InputFile", InputFile );
                parameters.set ( "InputStart", InputStart );
                parameters.set ( "InputEnd", InputEnd );
                parameters.set ( "Units", Units );
                parameters.setHowSet ( Prop.SET_UNKNOWN );
                setCommandParameters ( parameters );
            }
        }
        
        PropList parameters = getCommandParameters();
        parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
        parameters.set ( "Alias", Alias );
        parameters.setHowSet ( Prop.SET_UNKNOWN );
        setCommandParameters ( parameters );
     
     	// The following is for backwards compatibility with old commands files.
    	if ( (parameters.getValue("InputStart") != null) && parameters.getValue("InputStart").equals("*") ) {
    	    // Reset to more recent blank default
    	    parameters.set("InputStart","");
    	}
        if ( (parameters.getValue("InputEnd") != null) && parameters.getValue("InputEnd").equals("*") ) {
            // Reset to more recent blank default
            parameters.set("InputEnd","");
        }
        if ( (parameters.getValue("Units") != null) && parameters.getValue("Units").equals("*") ) {
            // Reset to more recent blank default
            parameters.set("Units","");
        }
    }
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number The number of the command being run.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType command_phase )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = "ReadRiverWare_Command.runCommand", message;
	int warning_level = 2;
    int log_level = 3;
	String command_tag = "" + command_number;
	int warning_count = 0;
	    
    // Get and clear the status and clear the run log...
    
    CommandStatus status = getCommandStatus();
    status.clearLog(command_phase);
    boolean readData = true;
    if ( command_phase == CommandPhaseType.DISCOVERY ){
        readData = false;
        setDiscoveryTSList ( new ArrayList<TS>() );
        setDiscoveryEnsembleList ( new ArrayList<TSEnsemble>() );
    }
    CommandProcessor processor = getCommandProcessor();

	// Get the command properties not already stored as members.
	PropList parameters = getCommandParameters();
	String InputFile = parameters.getValue("InputFile");
	String Output = parameters.getValue("Output");
	if ( (Output == null) || Output.equals("") ) {
	    Output = _TimeSeries; // default
	}
	String Units = parameters.getValue("Units");
	String InputStart = parameters.getValue("InputStart");
	String InputEnd = parameters.getValue("InputEnd");
	String Alias = parameters.getValue("Alias");
    
    DateTime InputStart_DateTime = null;
    DateTime InputEnd_DateTime = null;
    if ( (InputStart != null) && (InputStart.length() != 0) ) {
        try {
        PropList request_params = new PropList ( "" );
        request_params.set ( "DateTime", InputStart );
        CommandProcessorRequestResultsBean bean = null;
        try {
            bean = processor.processRequest( "DateTime", request_params);
        }
        catch ( Exception e ) {
            message = "Error requesting InputStart DateTime(DateTime=" + InputStart + ") from processor.";
            Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
            status.addToLog ( command_phase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Report the problem to software support." ) );
            throw new InvalidCommandParameterException ( message );
        }

        PropList bean_PropList = bean.getResultsPropList();
        Object prop_contents = bean_PropList.getContents ( "DateTime" );
        if ( prop_contents == null ) {
            message = "Null value for InputStart DateTime(DateTime=" + InputStart + ") returned from processor.";
            Message.printWarning(log_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( command_phase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify that the specified date/time is valid." ) );
            throw new InvalidCommandParameterException ( message );
        }
        else {  InputStart_DateTime = (DateTime)prop_contents;
        }
    }
    catch ( Exception e ) {
        message = "InputStart \"" + InputStart + "\" is invalid.";
        Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
        status.addToLog ( command_phase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify a valid date/time for the input start, " +
                        "or InputStart for the global input start." ) );
        throw new InvalidCommandParameterException ( message );
    }
    }
    else {
        // Get the global input start from the processor...
        try {
            Object o = processor.getPropContents ( "InputStart" );
                if ( o != null ) {
                    InputStart_DateTime = (DateTime)o;
                }
        }
        catch ( Exception e ) {
            message = "Error requesting the global InputStart from processor.";
            Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
            status.addToLog ( command_phase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Report the problem to software support." ) );
            throw new InvalidCommandParameterException ( message );
        }
    }
    
    if ( (InputEnd != null) && (InputEnd.length() != 0) ) {
        try {
            PropList request_params = new PropList ( "" );
            request_params.set ( "DateTime", InputEnd );
            CommandProcessorRequestResultsBean bean = null;
            try {
                bean = processor.processRequest( "DateTime", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting InputEnd DateTime(DateTime=" + InputEnd + ") from processor.";
                Message.printWarning(log_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                status.addToLog ( command_phase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report problem to software support." ) );
                throw new InvalidCommandParameterException ( message );
            }

            PropList bean_PropList = bean.getResultsPropList();
            Object prop_contents = bean_PropList.getContents ( "DateTime" );
            if ( prop_contents == null ) {
                message = "Null value for InputEnd DateTime(DateTime=" + InputEnd +  ") returned from processor.";
                Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
                status.addToLog ( command_phase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the end date/time is valid." ) );
                throw new InvalidCommandParameterException ( message );
            }
            else {
                InputEnd_DateTime = (DateTime)prop_contents;
            }
        }
        catch ( Exception e ) {
            message = "InputEnd \"" + InputEnd + "\" is invalid.";
            Message.printWarning(warning_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count),
                    routine, message );
            status.addToLog ( command_phase,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid date/time for the input end, " +
                            "or InputEnd for the global input start." ) );
            throw new InvalidCommandParameterException ( message );
        }
        }
        else {
            // Get from the processor...
            try {
                Object o = processor.getPropContents ( "InputEnd" );
                    if ( o != null ) {
                        InputEnd_DateTime = (DateTime)o;
                    }
            }
            catch ( Exception e ) {
                message = "Error requesting the global InputEnd from processor.";
                Message.printWarning(log_level,
                        MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                status.addToLog ( command_phase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Report problem to software support." ) );
            }
    }

	// Read the file.
    List<TS> tslist = new ArrayList<TS>();
    List<TSEnsemble> ensembleList = new ArrayList<TSEnsemble>();
    String InputFile_full = InputFile;
    boolean isRdf = false;
	try {
        InputFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,InputFile)));
        if ( !IOUtil.fileExists(InputFile_full)) {
            message = "The RiverWare file \"" + InputFile_full + "\" does not exist.";
            status.addToLog(command_phase,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message,"Verify that the filename is correct."));
        }
        else {
            isRdf = RiverWareTS.isRiverWareFile ( InputFile_full, true );
            if ( isRdf ) {
                // Read multiple time series from the file
                Message.printStatus(2, routine, "Reading RiverWare RDF file \"" + InputFile_full + "\"" );
                tslist = RiverWareTS.readTimeSeriesListFromRdf ( InputFile_full, InputStart_DateTime, InputEnd_DateTime, Units, readData );
                if ( Output.equalsIgnoreCase(_TimeSeriesAndEnsembles) ) {
                    // Create a list of ensemble from the time series list
                    ensembleList = createEnsembleList ( tslist );
                }
            }
            else {
                // Read a single time series
                TS ts = RiverWareTS.readTimeSeries ( InputFile_full, InputStart_DateTime, InputEnd_DateTime, Units, readData );
                if ( ts == null ) {
                    message = "Unable to read time series from RiverWare file \"" + InputFile_full + "\".";
                    status.addToLog(command_phase,
                            new CommandLogRecord(
                                    CommandStatusType.FAILURE, message,"See the log file."));
                }
                else {
                    tslist.add ( ts );
                }
            }
            if ( (Alias != null) && !Alias.equals("") ) {
                for ( TS ts2 : tslist ) {
                    String alias = TSCommandProcessorUtil.expandTimeSeriesMetadataString(
                        processor, ts2, Alias, status, command_phase);
                    ts2.setAlias ( alias );
                }
            }
        }
	} 
	catch ( Exception e ) {
		message = "Unexpected error reading RiverWare file. \"" + InputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count ),routine, message );
		Message.printWarning ( 3, routine, e );
        status.addToLog(command_phase,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message,"Check the log file for details."));
		throw new CommandException ( message );
	}
    
    int size = 1;
    Message.printStatus ( 2, routine, "Read " + size + " RiverWare time series." );

    if ( command_phase == CommandPhaseType.RUN ) {
        if ( tslist.size() > 0 ) {
            // Further process the time series...
            // This makes sure the period is at least as long as the output period...
            int wc = TSCommandProcessorUtil.processTimeSeriesListAfterRead( processor, this, tslist );
            if ( wc > 0 ) {
                message = "Error post-processing RiverWare time series after read.";
                Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag,
                    ++warning_count), routine, message );
                    status.addToLog ( command_phase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Report the problem to software support." ) );
                throw new CommandException ( message );
            }
    
            // Now add the list in the processor...
            
            int wc2 = TSCommandProcessorUtil.appendTimeSeriesListToResultsList ( processor, this, tslist );
            if ( wc2 > 0 ) {
                message = "Error adding RiverWare time series after read.";
                Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag,
                    ++warning_count), routine, message );
                    status.addToLog ( command_phase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Report the problem to software support." ) );
                throw new CommandException ( message );
            }
        }
        if ( Output.equalsIgnoreCase(_TimeSeriesAndEnsembles) ) {
            for ( TSEnsemble ensemble : ensembleList ) {
                TSCommandProcessorUtil.appendEnsembleToResultsEnsembleList(processor, this, ensemble );
            }
        }
    }
    else if ( command_phase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTSList ( tslist );
        if ( Output.equalsIgnoreCase(_TimeSeriesAndEnsembles) ) {
            setDiscoveryEnsembleList ( ensembleList );
        }
    }

	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count ), routine, message );
		throw new CommandWarningException ( message );
	}
    
    status.refreshPhaseSeverity(command_phase,CommandStatusType.SUCCESS);
}

/**
Set the list of ensembles read in discovery phase.
*/
private void setDiscoveryEnsembleList ( List<TSEnsemble> discoveryEnsembleList )
{
    __discoveryEnsembleList = discoveryEnsembleList;
}

/**
Set the list of time series read in discovery phase.
*/
private void setDiscoveryTSList ( List<TS> discoveryTSList )
{
    __discoveryTSList = discoveryTSList;
}

/**
Return the string representation of the command.
@param props parameters for the command
*/
public String toString ( PropList props )
{
    return toString ( props, 10 );
}

/**
Return the string representation of the command.
@param props parameters for the command
@param majorVersion the major version for software - if less than 10, the "TS Alias = " notation is used,
allowing command files to be saved for older software.
*/
public String toString ( PropList props, int majorVersion )
{   if ( props == null ) {
        if ( majorVersion < 10 ) {
            return "TS Alias = " + getCommandName() + "()";
        }
        else {
            return getCommandName() + "()";
        }
    }

	String Alias = props.getValue("Alias");
	String InputFile = props.getValue("InputFile" );
	String Output = props.getValue("Output" );
	String Units = props.getValue("Units");
	String InputStart = props.getValue("InputStart");
	String InputEnd = props.getValue("InputEnd");

	StringBuffer b = new StringBuffer ();

	// Input File
	if ((InputFile != null) && (InputFile.length() > 0)) {
		b.append("InputFile=\"" + InputFile + "\"");
	}
    if ((Output != null) && (Output.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("Output=" + Output );
    }
    if ( majorVersion >= 10 ) {
        // Add as a parameter
        if ( (Alias != null) && (Alias.length() > 0) ) {
            if ( b.length() > 0 ) {
                b.append ( "," );
            }
            b.append ( "Alias=\"" + Alias + "\"" );
        }
    }
	// Units
	if ((Units != null) && (Units.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("Units=\"" + Units + "\"");
	}

	// Input Start
	if ((InputStart != null) && (InputStart.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("InputStart=\"" + InputStart + "\"");
	}

	// Input End
	if ((InputEnd != null) && (InputEnd.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("InputEnd=\"" + InputEnd + "\"");
	}
    if ( majorVersion < 10 ) {
        // Old syntax...
        if ( (Alias == null) || Alias.equals("") ) {
            Alias = "Alias";
        }
        return "TS " + Alias + " = " + getCommandName() + "("+ b.toString()+")";
    }
    else {
        return getCommandName() + "("+ b.toString()+")";
    }
}

}