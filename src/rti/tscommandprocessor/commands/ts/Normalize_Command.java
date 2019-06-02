// Normalize_Command - This class initializes, checks, and runs the Normalize() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package rti.tscommandprocessor.commands.ts;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import rti.tscommandprocessor.core.TSListType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSUtil;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the Normalize() command.
*/
public class Normalize_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{
    
/**
Values for the MinValueMethod parameter.
*/
protected final String _MinFromTS = "MinFromTS";
protected final String _MinZero = "MinZero";

/**
List of time series read during discovery.  These are TS objects but with mainly the
metadata (TSIdent) filled in.
*/
private List<TS> __discoveryTSList = null;

/**
Constructor.
*/
public Normalize_Command ()
{	super();
	setCommandName ( "Normalize" );
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
{	String Alias = parameters.getValue ( "Alias" );
	String TSID = parameters.getValue ( "TSID" );
	String MinValue = parameters.getValue ( "MinValue" );
	String MaxValue = parameters.getValue ( "MaxValue" );
	String MinValueMethod = parameters.getValue ( "MinValueMethod" );
	String warning = "";
    String message;

    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	if ( (Alias == null) || Alias.equals("") ) {
        message = "The time series alias must be specified.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message,
                "Provide a time series alias when defining the command."));
	}
	if ( (TSID == null) || TSID.equals("") ) {
        message = "The time series identifier for the time series to normalize must be specified.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message,
                "Provide a time series identifier when defining the command."));
	}
	if ( (Alias != null) && !Alias.equals("") && (TSID != null) && !TSID.equals("") &&
	        Alias.equalsIgnoreCase(TSID) ) {
        message = "The alias and original time series are the same.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message,
                "Specify a different alias."));
	}
    if ( (MinValue == null) || MinValue.equals("") ) {
        message = "The minimum value must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum value as a number." ) );
    }
    else if ( !StringUtil.isDouble(MinValue) ) {
        message = "The minimum value " + MinValue + " is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum value as a number." ) );
    }
    
    if ( (MaxValue == null) || MaxValue.equals("") ) {
        message = "The maximum value must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the maximum value as a number." ) );
    }
    else if ( !StringUtil.isDouble(MaxValue) ) {
        message = "The maximum value " + MaxValue + " is not a number.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the maximum value as a number." ) );
    }

    if ( (MinValueMethod == null) || MinValueMethod.equals("") ) {
        message = "The minimum value method must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum value method as " + _MinFromTS + " or " + _MinZero ) );
    }
    else if ( !MinValueMethod.equalsIgnoreCase(_MinFromTS) && !MinValueMethod.equalsIgnoreCase(_MinZero) ) {
        message = "The minimum value method \"" + MinValueMethod + "\" is invalid.";
        warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(
                CommandStatusType.FAILURE, message,
                "Specify a method of " + _MinFromTS + " or " + _MinZero + "."));
    }
    
    // Check for invalid parameters...
    List<String> validList = new ArrayList<String>(5);
    validList.add ( "Alias" );
    validList.add ( "TSID" );
    validList.add ( "MinValue" );
    validList.add ( "MaxValue" );
    validList.add ( "MinValueMethod" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
    
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new Normalize_JDialog ( parent, this )).ok();
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
Classes that can be requested:  TS
*/
@SuppressWarnings("unchecked")
public <T> List<T> getObjectList ( Class<T> c )
{
    List<T> matchingDiscoveryTS = new Vector<T>();
    List<TS> discoveryTSList = getDiscoveryTSList ();
    if ( (discoveryTSList == null) || (discoveryTSList.size() == 0) ) {
        return matchingDiscoveryTS;
    }
    for ( TS datats : discoveryTSList ) {
        // Use the most generic for the base class...
        if ( (c == TS.class) || (c == datats.getClass()) ) {
            matchingDiscoveryTS.add((T)datats);
        }
    }
    return matchingDiscoveryTS;
}

/**
Parse the command string into a PropList of parameters.  This method currently
supports old syntax and new parameter-based syntax.
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_level = 2;
	String routine = "Normalize_Command.parseCommand", message;

    if ( !command.trim().toUpperCase().startsWith("TS") ) {
        // New style syntax using simple parameter=value notation
        super.parseCommand(command);
    }
    else {
    	// Get the part of the command after the TS Alias =...
    	int pos = command.indexOf ( "=" );
    	if ( pos < 0 ) {
    		message = "Syntax error in \"" + command + "\".  Expecting:  TS Alias = Normalize(...)";
    		Message.printWarning ( warning_level, routine, message);
    		throw new InvalidCommandSyntaxException ( message );
    	}
    	String token0 = command.substring ( 0, pos ).trim();
    	String token1 = command.substring ( pos + 1 ).trim();
    	if ( (token0 == null) || (token1 == null) ) {
    		message = "Syntax error in \"" + command + "\".  Expecting:  TS Alias = Normalize(...)";
    		Message.printWarning ( warning_level, routine, message);
    		throw new InvalidCommandSyntaxException ( message );
    	}
    	List<String> v = StringUtil.breakStringList ( token0, " ", StringUtil.DELIM_SKIP_BLANKS );
        if ( v == null ) {
            message = "Syntax error in \"" + command +
            "\".  Expecting:  TS Alias = Normalize(TSID,MinValueMethod,MinValue,MaxValue)";
            Message.printWarning ( warning_level, routine, message);
            throw new InvalidCommandSyntaxException ( message );
        }
        String Alias = v.get(1);
        String TSID = null;
        String MinValue = null;
        String MaxValue = null;
        String MinValueMethod = null;
    	if ( (token1.indexOf('=') < 0) && !token1.endsWith("()") ) {
    		// No parameters have = in them...
    		// TODO SAM 2009-09-22 This whole block of code needs to be
    		// removed as soon as commands have been migrated to the new syntax.
    		//
    		// Old syntax without named parameters.
    
    		v = StringUtil.breakStringList ( token1,"(),",
    		        StringUtil.DELIM_SKIP_BLANKS|StringUtil.DELIM_ALLOW_STRINGS );
    		if ( (v == null) || v.size() != 5 ) {
    			message = "Syntax error in \"" + command + "\".  Expecting:  TS Alias = " +
    					"Normalize(TSID,MinValueMethod,MinValue,MaxValue";
    			Message.printWarning ( warning_level, routine, message);
    			throw new InvalidCommandSyntaxException ( message );
    		}
            // TSID is the only parameter
            TSID = v.get(1);
            MinValueMethod = v.get(2);
            MinValue = v.get(3);
            MaxValue = v.get(4);
     	}
    	else {
            // Current syntax...
            super.parseCommand( token1 );
    	}
        
        // Set parameters and new defaults...
    
        PropList parameters = getCommandParameters();
        parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
        if ( Alias.length() > 0 ) {
            parameters.set ( "Alias", Alias );
        }
        // Reset using above information
        if ( (TSID != null) && (TSID.length() > 0) ) {
            parameters.set ( "TSID", TSID );
        }
        if ( (MinValue != null) && (MinValue.length() > 0) ) {
            parameters.set ( "MinValue", MinValue );
        }
        if ( (MaxValue != null) && (MaxValue.length() > 0) ) {
            parameters.set ( "MaxValue", MaxValue );
        }
        if ( (MinValueMethod != null) && (MinValueMethod.length() > 0) ) {
            parameters.set ( "MinValueMethod", MinValueMethod );
        }
        parameters.setHowSet ( Prop.SET_UNKNOWN );
    }
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
public void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "Normalize_Command.runCommand", message;
	int warning_count = 0;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int log_level = 3;	// Level for non-user messages

	// Make sure there are time series available to operate on...

	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTSList ( null );
    }
	
	String Alias = parameters.getValue ( "Alias" );
	String TSID = parameters.getValue ( "TSID" );
	String MinValue = parameters.getValue ( "MinValue" );
	double MinValue_double = Double.parseDouble(MinValue);
	String MaxValue = parameters.getValue ( "MaxValue" );
	double MaxValue_double = Double.parseDouble(MaxValue);
	String MinValueMethod = parameters.getValue ( "MinValueMethod" );
	boolean MinValueMethod_MinFromTS_boolean = true;
	if ( MinValueMethod.equalsIgnoreCase(_MinZero) ) {
	    MinValueMethod_MinFromTS_boolean = false;
	}

	// Get the time series to process.  The time series list is searched
	// backwards until the first match...

	TS ts = null;
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        // Get the discovery time series list from all time series above this command
        String TSList = "" + TSListType.LAST_MATCHING_TSID;
        List<TS> tslist = TSCommandProcessorUtil.getDiscoveryTSFromCommandsBeforeCommand(
            (TSCommandProcessor)processor, this, TSList, TSID, null, null );
        if ( (tslist != null) && (tslist.size() > 0) ) {
            ts = tslist.get(0);
        }
    }
    else if ( commandPhase == CommandPhaseType.RUN ) {
    	try {
    	    PropList request_params = new PropList ( "" );
    			request_params.set ( "CommandTag", command_tag );
    			request_params.set ( "TSID", TSID );
    			CommandProcessorRequestResultsBean bean = null;
    			try {
    			    bean = processor.processRequest( "GetTimeSeriesForTSID", request_params);
    			}
    			catch ( Exception e ) {
    				message = "Error requesting GetTimeSeriesForTSID(TSID=\"" + TSID + "\") from processor.";
    				Message.printWarning(log_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
    				Message.printWarning(log_level, routine, e );
                    status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Report the problem to software support." ) );
    			}
    			PropList bean_PropList = bean.getResultsPropList();
    			Object o_TS = bean_PropList.getContents ( "TS");
    			if ( o_TS == null ) {
    				message = "Null TS requesting GetTimeSeriesForTSID(TSID=\"" + TSID +
    				"\") from processor.";
    				Message.printWarning(log_level,
    				MessageUtil.formatMessageTag( command_tag, ++warning_count),
    				routine, message );
                    status.addToLog ( commandPhase,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Verify the time series identifier.  A previous error may also cause this problem." ) );
    			}
    			else {
    				ts = (TS)o_TS;
    			}
    	}
    	catch ( Exception e ) {
    		ts = null;
    	}
    }
	if ( ts == null ) {
		message = "Unable to find time series to normalize using TSID \"" + TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify the time series identifier.  A previous error may also cause this problem." ) );
		throw new CommandWarningException ( message );
	}
	
	// Now process the time series...

	TS tsnew = null;
	try {
	    // Make a copy of the found time series...
        tsnew = (TS)ts.clone();
        if ( commandPhase == CommandPhaseType.RUN ) {
            TSUtil.normalize ( tsnew, MinValueMethod_MinFromTS_boolean, MinValue_double, MaxValue_double );
        }
        if ( (Alias != null) && !Alias.equals("") ) {
            String alias = TSCommandProcessorUtil.expandTimeSeriesMetadataString(
                processor, tsnew, Alias, status, commandPhase);
            tsnew.setAlias ( alias );
        }
	}
	catch ( Exception e ) {
		message = "Unexpected error trying to normalize time series \""+ ts.getIdentifier() + "\".";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count),routine,message );
		Message.printWarning(3,routine,e);
        status.addToLog ( commandPhase,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Check the log file - report the problem to software support." ) );
	}

    // Update the data to the processor so that appropriate actions are taken...
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        // Just want time series headers initialized
        List<TS> discoveryTSList = new Vector<TS>();
        discoveryTSList.add ( tsnew );
        setDiscoveryTSList ( discoveryTSList );
    }
    else if ( commandPhase == CommandPhaseType.RUN ) {
        TSCommandProcessorUtil.appendTimeSeriesToResultsList(processor, this, tsnew );
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag, ++warning_count),
			routine,message);
		throw new CommandWarningException ( message );
	}
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the list of time series read in discovery phase.
@param discoveryTSList list of time series created in discovery mode
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
	String Alias = props.getValue( "Alias" );
	String TSID = props.getValue( "TSID" );
	String MinValue = props.getValue( "MinValue" );
	String MaxValue = props.getValue( "MaxValue" );
	String MinValueMethod = props.getValue( "MinValueMethod" );
	StringBuffer b = new StringBuffer ();
	if ( (TSID != null) && (TSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TSID=\"" + TSID + "\"" );
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
    if ( (MinValueMethod != null) && (MinValueMethod.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MinValueMethod=" + MinValueMethod );
    }
	if ( (MinValue != null) && (MinValue.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "MinValue=" + MinValue );
	}
    if ( (MaxValue != null) && (MaxValue.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "MaxValue=" + MaxValue );
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
