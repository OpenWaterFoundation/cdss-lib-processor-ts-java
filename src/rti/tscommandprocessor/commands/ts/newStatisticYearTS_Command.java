//------------------------------------------------------------------------------
// newStatisticYearTS - handle the TS Alias = newStatisticYearTS() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-09-08	Steven A. Malers, RTi	Initial version.  Copy and modify
//					copy().
// 2005-09-22	SAM, RTi		Add AllowMissingCount parameter.
// 2007-02-12	SAM, RTi		Remove direct dependence on TSCommandProcessor.
//					Clean up code based on Eclipse feedback.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package rti.tscommandprocessor.commands.ts;

import javax.swing.JFrame;

import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSAnalyst;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the newStatisticYearTS() command.
</p>
<p>The CommandProcessor must return the following properties:  TSResultsList.
</p>
*/
public class newStatisticYearTS_Command extends AbstractCommand
implements Command
{

/**
Constructor.
*/
public newStatisticYearTS_Command ()
{	super();
	setCommandName ( "newStatisticYearTS" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters (	PropList parameters, String command_tag,
					int warning_level )
throws InvalidCommandParameterException
{	String Alias = parameters.getValue ( "Alias" );
	String TSID = parameters.getValue ( "TSID" );
	String Statistic = parameters.getValue ( "Statistic" );
	String TestValue = parameters.getValue ( "TestValue" );
	String AllowMissingCount = parameters.getValue ( "AllowMissingCount" );
	String AnalysisStart = parameters.getValue ( "AnalysisStart" );
	String AnalysisEnd = parameters.getValue ( "AnalysisEnd" );
	String SearchStart = parameters.getValue ( "SearchStart" );
	String warning = "";

	if ( (Alias == null) || Alias.equals("") ) {
		warning += "\nThe time series alias must be specified.";
	}
	if ( (TSID == null) || TSID.equals("") ) {
		warning += "\nThe time series identifier must be specified.";
	}
	// REVISIT SAM 2005-08-29
	// Need to decide whether to check NewTSID - it might need to support
	// wildcards.
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
	if ( (Statistic == null) || Statistic.equals("") ) {
		warning += "\nThe statistic must be specified.";
	}
	if ( (TestValue != null) && !TestValue.equals("") ) {
		// If a test value is specified, for now make sure it is a
		// number.  It is possible that in the future it could be a
		// special value (date, etc.) but for now focus on numbers.
		if ( !StringUtil.isDouble(TestValue) ) {
			warning += "\nThe test value (" + TestValue +
					") is not a number.";
		}
	}
	// REVISIT SAM 2005-09-12
	// Need to evaluate whether the test value is needed, depending on the
	// statistic
	if ( (AllowMissingCount != null) && !AllowMissingCount.equals("") ) {
		if ( !StringUtil.isInteger(AllowMissingCount) ) {
			warning += "\nThe AllowMissingCount value (" +
				AllowMissingCount + ") is not an integer.";
		}
		else {	// Make sure it is an allowable value >= 0...
			if ( StringUtil.atoi(AllowMissingCount) < 0 ) {
				warning += "\nThe AllowMissingCount value (" +
				AllowMissingCount + ") must be >= 0.";
			}
		}
	}
	if (	(AnalysisStart != null) && !AnalysisStart.equals("") &&
		!AnalysisStart.equalsIgnoreCase("OutputStart") &&
		!AnalysisStart.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse(AnalysisStart);
		}
		catch ( Exception e ) {
			warning += 
				"\nThe Analysis start date \"" +
				AnalysisStart +
				"\" is not a valid date.\n"+
				"Specify a date or OutputStart.";
		}
	}
	if (	(AnalysisEnd != null) && !AnalysisEnd.equals("") &&
		!AnalysisEnd.equalsIgnoreCase("OutputStart") &&
		!AnalysisEnd.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse( AnalysisEnd );
		}
		catch ( Exception e ) {
			warning +=
				"\nThe Analysis end date \"" +
				AnalysisEnd +
				"\" is not a valid date.\n"+
				"Specify a date or OutputEnd.";
		}
	}
	if (	(SearchStart != null) && !SearchStart.equals("") &&
		!SearchStart.equalsIgnoreCase("OutputStart") &&
		!SearchStart.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse( SearchStart );
		}
		catch ( Exception e ) {
			warning +=
				"\nThe search start date \"" +
				SearchStart +
				"\" is not a valid date.\n"+
				"Specify a date or OutputEnd.";
		}
	}
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new newStatisticYearTS_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param command A string command to parse.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand (	String command, String command_tag,
				int warning_level )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_count = 0;
	String routine = "newStatisticYearTS.parseCommand", message;

	// Get the part of the command after the TS Alias =...
	int pos = command.indexOf ( "=" );
	if ( pos < 0 ) {
		message = "Syntax error in \"" + command +
			"\".  Expecting:  TS Alias = newStatisticYearTS(...)";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	String token0 = command.substring ( 0, pos ).trim();
	String token1 = command.substring ( pos + 1 ).trim();
	if ( (token0 == null) || (token1 == null) ) {
		message = "Syntax error in \"" + command +
			"\".  Expecting:  TS Alias = newStatisticYearTS(...)";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}

	Vector v = StringUtil.breakStringList ( token0, " ",
			StringUtil.DELIM_SKIP_BLANKS );
	if ( (v == null) || (v.size() != 2) ) {
		message = "Syntax error in \"" + command +
			"\".  Expecting:  TS Alias = newStatisticYearTS(...)";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	String Alias = (String)v.elementAt(1);
	Vector tokens = StringUtil.breakStringList ( token1,
		"()", StringUtil.DELIM_SKIP_BLANKS );
	if ( (tokens == null) || tokens.size() < 2 ) {
		// Must have at least the command name and its
		// parameters...
		message = "Syntax error in \"" + command +
			"\".  Not enough tokens.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	// Get the input needed to process the file...
	try {	 PropList parameters = PropList.parse ( Prop.SET_FROM_PERSISTENT,
			(String)tokens.elementAt(1), routine, "," );
		parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		parameters.set ( "Alias", Alias );
		parameters.setHowSet ( Prop.SET_UNKNOWN );
		setCommandParameters ( parameters );
	}
	catch ( Exception e ) {
		message = "Syntax error in \"" + command +
			"\".  Not enough tokens.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
}

/**
Run the commands:
<pre>
TS Alias = newStatisticYearTS(TSID="X",NewTSID="X",Statistic=X,TestValue=X,
AllowMissingCount=X,AnalysisStart="X",AnalysisEnd="X",SearchStart="X")
</pre>
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more
parameter values are invalid.
*/
public void runCommand ( String command_tag, int warning_level )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "newStatisticYearTS.runCommand", message;
	int warning_count = 0;
	int log_level = 3;	// Non-user warning level

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters ();
	CommandProcessor processor = getCommandProcessor();

	String Alias = parameters.getValue ( "Alias" );
	String TSID = parameters.getValue ( "TSID" );
	String NewTSID = parameters.getValue ( "NewTSID" );
	String Statistic = parameters.getValue ( "Statistic" );
	String TestValue = parameters.getValue ( "TestValue" );
	String AllowMissingCount = parameters.getValue ( "AllowMissingCount" );
	String AnalysisStart = parameters.getValue ( "AnalysisStart" );
	String AnalysisEnd = parameters.getValue ( "AnalysisEnd" );
	String SearchStart = parameters.getValue ( "SearchStart" );

	// Figure out the dates to use for the analysis...

	DateTime AnalysisStart_DateTime = null;
	DateTime AnalysisEnd_DateTime = null;
	try {
		if ( AnalysisStart != null ) {
			PropList request_params = new PropList ( "" );
			request_params.set ( "DateTime", AnalysisStart );
			CommandProcessorRequestResultsBean bean = null;
			try { bean =
				processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting AnalysisStart DateTime(DateTime=" +
				AnalysisStart + "\" from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
				throw new InvalidCommandParameterException ( message );
			}

			PropList bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for AnalysisStart DateTime(DateTime=" +
				AnalysisStart +	"\") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				throw new InvalidCommandParameterException ( message );
			}
			else {	AnalysisStart_DateTime = (DateTime)prop_contents;
			}
		}
		}
		catch ( Exception e ) {
			message = "AnalysisStart \"" + AnalysisStart + "\" is invalid.";
			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			throw new InvalidCommandParameterException ( message );
		}
		
		try {
		if ( AnalysisEnd != null ) {
			PropList request_params = new PropList ( "" );
			request_params.set ( "DateTime", AnalysisEnd );
			CommandProcessorRequestResultsBean bean = null;
			try { bean =
				processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting AnalysisEnd DateTime(DateTime=" +
				AnalysisEnd + "\" from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
				throw new InvalidCommandParameterException ( message );
			}

			PropList bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for AnalysisStart DateTime(DateTime=" +
				AnalysisStart +	"\") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				throw new InvalidCommandParameterException ( message );
			}
			else {	AnalysisEnd_DateTime = (DateTime)prop_contents;
			}
		}
		}
		catch ( Exception e ) {
			message = "AnalysisEnd \"" + AnalysisEnd + "\" is invalid.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			throw new InvalidCommandParameterException ( message );
		}
	
	// TODO SAM 2007-02-13 Need to enable SearchStart or remove
	/*DateTime SearchStart_DateTime = null;
	if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
		try {	// The following works with MM/DD and MM-DD
			SearchStart_DateTime =
				DateTime.parse ( SearchStart,
				DateTime.FORMAT_MM_SLASH_DD );
		}
		catch ( Exception e ) {
			message = "SearchStart \"" + SearchStart +
				"\" is invalid.  Expecting MM-DD or MM/DD";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
			throw new InvalidCommandParameterException ( message );
		}
	}*/

	// Get the time series to process.  The time series list is searched
	// backwards until the first match...

	PropList request_params = new PropList ( "" );
	request_params.set ( "CommandTag", command_tag );
	request_params.set ( "TSID", TSID );
	CommandProcessorRequestResultsBean bean = null;
	try { bean =
		processor.processRequest( "GetTimeSeriesForTSID", request_params);
	}
	catch ( Exception e ) {
		message = "Error requesting GetTimeSeriesForTSID(TSID=\"" + TSID +
		"\" from processor.";
		Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TS = bean_PropList.getContents ( "TS");
	TS ts = null;
	if ( o_TS == null ) {
		message = "Null TS requesting GetTimeSeriesForTSID(TSID=\"" + TSID +
		"\" from processor.";
		Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
	}
	else {
		ts = (TS)o_TS;
	}
	
	if ( ts == null ) {
		message = "Unable to find time series to analyze using TSID \""+
		TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
		throw new CommandWarningException ( message );
	}

	// Now process the time series...

	TS stats_ts = null;
	try {	TSAnalyst tsa = new TSAnalyst ();
		PropList tsa_props = new PropList ( "TSAnalyst" );
		if ( (NewTSID != null) && (NewTSID.length() > 0) ) {
			tsa_props.set ( "NewTSID", NewTSID );	// Optional
		}
		tsa_props.set ( "Statistic", Statistic );	// Required
		if ( (TestValue != null) && (TestValue.length() > 0) ) {
			tsa_props.set ( "TestValue", TestValue);// Optional
		}
		if (	(AllowMissingCount != null) &&
			(AllowMissingCount.length() > 0) ) {
			tsa_props.set ( "AllowMissingCount",
				AllowMissingCount);	// Optional
		}
		if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
			tsa_props.set ( "SearchStart", SearchStart);
								// Optional
		}
		stats_ts = tsa.createStatisticYearTS ( ts,
				AnalysisStart_DateTime, AnalysisEnd_DateTime,
				tsa_props );
		stats_ts.setAlias ( Alias );	// Do separate because setting
						// the NewTSID might cause the
						// alias set to fail below.
	}
	catch ( Exception e ) {
		message ="Unable to generate the statistic time series from \""+
			ts.getIdentifier() + "\".";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count),routine,message );
		Message.printWarning(3,routine,e);
	}

	// Update the data to the processor so that appropriate actions are
	// taken...

	Vector TSResultsList_Vector = null;
	try { Object o = processor.getPropContents( "TSResultsList" );
			TSResultsList_Vector = (Vector)o;
	}
	catch ( Exception e ){
		message = "Cannot get time series list to add new statistics time series.  Skipping.";
		Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
				command_tag, ++warning_count),
				routine,message);
	}
	if ( TSResultsList_Vector != null ) {
		TSResultsList_Vector.addElement ( stats_ts );
		try {	processor.setPropContents ( "TSResultsList", TSResultsList_Vector );
		}
		catch ( Exception e ){
			message = "Cannot set updated time series list.  Skipping.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
				command_tag, ++warning_count),
				routine,message);
		}
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag, ++warning_count),
			routine,message);
		throw new CommandWarningException ( message );
	}
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String Alias = props.getValue( "Alias" );
	String TSID = props.getValue( "TSID" );
	String NewTSID = props.getValue( "NewTSID" );
	String Statistic = props.getValue( "Statistic" );
	String TestValue = props.getValue( "TestValue" );
	String AllowMissingCount = props.getValue( "AllowMissingCount" );
	String AnalysisStart = props.getValue( "AnalysisStart" );
	String AnalysisEnd = props.getValue( "AnalysisEnd" );
	String SearchStart = props.getValue( "SearchStart" );
	StringBuffer b = new StringBuffer ();
	if ( (TSID != null) && (TSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TSID=\"" + TSID + "\"" );
	}
	if ( (NewTSID != null) && (NewTSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NewTSID=\"" + NewTSID + "\"" );
	}
	if ( (Statistic != null) && (Statistic.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Statistic=" + Statistic );
	}
	if ( (TestValue != null) && (TestValue.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TestValue=" + TestValue );
	}
	if ( (AllowMissingCount != null) && (AllowMissingCount.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AllowMissingCount=" + AllowMissingCount );
	}
	if ( (AnalysisStart != null) && (AnalysisStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AnalysisStart=\"" + AnalysisStart + "\"" );
	}
	if ( (AnalysisEnd != null) && (AnalysisEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AnalysisEnd=\"" + AnalysisEnd + "\"" );
	}
	if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SearchStart=\"" + SearchStart + "\"" );
	}
	return "TS " + Alias + " = " + getCommandName() + "("+ b.toString()+")";
}

}
