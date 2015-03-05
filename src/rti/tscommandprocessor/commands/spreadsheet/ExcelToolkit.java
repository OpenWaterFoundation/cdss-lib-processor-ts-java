package rti.tscommandprocessor.commands.spreadsheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;

/**
Toolkit that contains useful Excel methods, for use with Apache POI Excel package.
*/
public class ExcelToolkit
{

/**
Construct an Excel toolkit instance.
*/
public ExcelToolkit()
{
    
}

/**
Create table columns from the first row of the area.
@param table new table that will have columns added
@param workbook the workbook being read
@param sheet the worksheet being read
@param area are being read into table
@param excelColumnNames indicate how to determine column names from the Excel worksheet
@param comment if non-null indicates character(s) that indicate comment lines
@param excelIntegerColumns names of columns that should be treated as integers, or null if none
@param excelDateTimeColumns names of columns that should be treated as dates, or null if none
@param precisionForFloats number of digits after decimal for double columns
@param readAllAsText if True, treat all data as text values
@param problems list of problems encountered during processing
@return the names of the output columns or null if cannot create
*/
/*
public String [] createTableColumns ( DataTable table, Workbook wb, Sheet sheet,
    AreaReference area, String excelColumnNames, String comment, String [] excelIntegerColumns,
    String [] excelDateTimeColumns, int precisionForFloats, boolean readAllAsText, List<String> problems )
{   String routine = "ReadTableFromExcel_Command.createTableColumns";
    Row dataRow; // First row of data
    Row headerRow = null; // Row containing column headings
    Cell cell;
    int iRow = area.getFirstCell().getRow();
    int firstDataRow = iRow; // Default before checking ExcelColumnNames parameter
    int rowEnd = area.getLastCell().getRow();
    if ( excelColumnNames.equalsIgnoreCase(_FirstRowInRange) ) {
        if ( comment == null ) {
            // Comments are not used so header is first row and first data row is next
            headerRow = sheet.getRow(iRow);
            firstDataRow = iRow + 1;
        }
        else {
            // Loop through first column cells.  If string and starts with comment, skip row
            boolean foundFirstDataRow = false;
            for ( ; iRow <= rowEnd; iRow++ ) {
                if ( rowIsComment ( sheet, iRow, comment ) ) {
                    continue;
                }
                else {
                    headerRow = sheet.getRow(iRow);
                    // Now find the first data row (could have more comments)
                    for ( ++iRow; iRow <= rowEnd; iRow++ ) {
                        if ( rowIsComment ( sheet, iRow, comment ) ) {
                            continue;
                        }
                        else {
                            foundFirstDataRow = true;
                            firstDataRow = iRow;
                            break;
                        }
                    }
                }
                if ( foundFirstDataRow ) {
                    break;
                }
            }
        }
    }
    else if ( excelColumnNames.equalsIgnoreCase(_RowBeforeRange) ) {
        // Loop backwards and skip comments
        for ( --iRow; iRow >= 0; iRow-- ) {
            if ( rowIsComment ( sheet, iRow, comment ) ) {
                continue;
            }
            else {
                headerRow = sheet.getRow(iRow);
                if ( headerRow == null ) {
                    problems.add ( "Specified ExcelColumnNames=" + _RowBeforeRange +
                        " but this results in row not on sheet.  Check address range." );
                    return null;
                }
            }
        }
    }
    setFirstDataRow(firstDataRow);
    Message.printStatus(2, routine, "Determined first data row to be [" + firstDataRow + "]");
    dataRow = sheet.getRow(firstDataRow);
    int colStart = area.getFirstCell().getCol();
    int colEnd = area.getLastCell().getCol();
    int columnIndex = -1;
    // First get the column names
    String [] columnNames = new String[colEnd - colStart + 1];
    int cellType;
    for ( int iCol = colStart; iCol <= colEnd; iCol++ ) {
        ++columnIndex;
        if ( excelColumnNames.equalsIgnoreCase(_ColumnN) ) {
            columnNames[columnIndex] = "Column" + (columnIndex + 1);
        }
        else {
            // Column names taken from header row - text value
            cell = headerRow.getCell(iCol);
            if ( cell == null ) {
                // Default...
                columnNames[columnIndex] = "Column" + (columnIndex + 1);
            }
            else {
                cellType = cell.getCellType();
                if ( cellType == Cell.CELL_TYPE_FORMULA ) {
                    // Have to evaluate the formula to get the result, which can be used as the column name
                    FormulaEvaluator formulaEval = wb.getCreationHelper().createFormulaEvaluator();
                    columnNames[columnIndex] = formulaEval.evaluate(cell).formatAsString();
                }
                else if ( (cellType == Cell.CELL_TYPE_BLANK) || (cellType == Cell.CELL_TYPE_ERROR) ) {
                 // Default...
                    columnNames[columnIndex] = "Column" + (columnIndex + 1);
                }
                else {
                    columnNames[columnIndex] = "" + cell;
                }
            }
        }
    }
    // Now loop through and determine the column data type from the data row
    // and add columns to the table
    columnIndex = -1;
    //CellStyle style = null;
    for ( int iCol = colStart; iCol <= colEnd; iCol++ ) {
        cell = dataRow.getCell(iCol);
        ++columnIndex;
        if ( readAllAsText ) {
            table.addField ( new TableField(TableField.DATA_TYPE_STRING, columnNames[columnIndex], -1, -1), null );
        }
        else {
            // See if the column name matches the integer columns
            boolean isInteger = false;
            if ( excelIntegerColumns != null ) {
                for ( int i = 0; i < excelIntegerColumns.length; i++ ) {
                    if ( columnNames[columnIndex].equalsIgnoreCase(excelIntegerColumns[i]) ) {
                        // Treat as an integer
                        isInteger = true;
                        break;
                    }
                }
            }
            boolean isDate = false;
            if ( excelDateTimeColumns != null ) {
                for ( int i = 0; i < excelDateTimeColumns.length; i++ ) {
                    if ( columnNames[columnIndex].equalsIgnoreCase(excelDateTimeColumns[i]) ) {
                        // Treat as a date
                        isDate = true;
                        break;
                    }
                }
            }
            // Interpret the first row cell types to determine column types
            if ( isInteger ) {
                Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_INT));
                table.addField ( new TableField(TableField.DATA_TYPE_INT, columnNames[columnIndex], -1, -1), null );
                continue;
            }
            else if ( isDate ) {
                Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_DATETIME));
                table.addField ( new TableField(TableField.DATA_TYPE_DATETIME, columnNames[columnIndex], -1, -1), null );
                continue;
            }
            cellType = Cell.CELL_TYPE_BLANK;
            if ( cell != null ) {
                cellType = cell.getCellType();
            }
            if ( (cell == null) || (cellType == Cell.CELL_TYPE_BLANK) || (cellType == Cell.CELL_TYPE_ERROR) ) {
                Message.printStatus(2,routine,"Table column [" + iCol + "] cell type from Excel is BLANK or ERROR.  " +
                    "Examining rows to find data to determine column type.");
                // Look forward to other rows to see if there is a non-null value that can be used to determine the type
                Row dataRow2;
                for ( int iRowSearch = (firstDataRow + 1); iRowSearch <= rowEnd; iRowSearch++ ) {
                    dataRow2 = sheet.getRow(iRowSearch);
                    if ( dataRow2 != null ) {
                        cell = dataRow2.getCell(iCol);
                        if ( cell != null ) {
                            cellType = cell.getCellType();
                            if ( (cellType == Cell.CELL_TYPE_STRING) || (cellType == Cell.CELL_TYPE_NUMERIC) ||
                                (cellType == Cell.CELL_TYPE_BOOLEAN) || (cellType == Cell.CELL_TYPE_FORMULA)) {
                                // Break out and interpret below
                                break;
                            }
                        }
                    }
                }
            }
            if ( cellType == Cell.CELL_TYPE_STRING ) {
                Message.printStatus(2,routine,"Table column [" + iCol + "] cell type from Excel is STRING." );
                Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_STRING));
                table.addField ( new TableField(TableField.DATA_TYPE_STRING, columnNames[columnIndex], -1, -1), null );
            }
            else if ( cellType == Cell.CELL_TYPE_NUMERIC ) {
                Message.printStatus(2,routine,"Table column [" + iCol + "] cell type from Excel is NUMERIC");
                if (DateUtil.isCellDateFormatted(cell)) {
                    // TODO SAM 2013-05-12 Evaluate whether to use DATA_TYPE_DATETIME
                    //table.addField ( new TableField(TableField.DATA_TYPE_STRING, columnNames[columnIndex], -1, -1), null );
                    Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_DATE));
                    table.addField ( new TableField(TableField.DATA_TYPE_DATE, columnNames[columnIndex], -1, -1), null );
                }
                else {
                    // TODO SAM 2013-02-26 Need to figure out the precision from formatting
                    // For now always set the column to a double with the precision from formatting
                    // Could default to integer for 0-precision but could guess wrong
                    //style = cell.getCellStyle();
                    //short format = style.getDataFormat();
                    //CellStyle style2 = wb.getCellStyleAt(format);
                    Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_DOUBLE));
                    table.addField ( new TableField(TableField.DATA_TYPE_DOUBLE, columnNames[columnIndex], -1, precisionForFloats), null );
                }
            }
            else if ( cellType == Cell.CELL_TYPE_BOOLEAN ) {
                Message.printStatus(2,routine,"Table column [" + iCol + "] cell type from Excel is BOOLEAN.  Treat as integer 0/1.");
                // Use integer for boolean
                Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_INT));
                table.addField ( new TableField(TableField.DATA_TYPE_INT, columnNames[columnIndex], -1, -1), null );
            }
            else if ( cellType == Cell.CELL_TYPE_FORMULA ) {
                Message.printStatus(2,routine,"Table column [" + iCol + "] cell type from Excel is FORMULA.  Treat as String.");
                Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_STRING));
                table.addField ( new TableField(TableField.DATA_TYPE_STRING, columnNames[columnIndex], -1, -1), null );
            }
            else {
                // Default is to treat as a string
                Message.printStatus(2,routine,"Table column [" + iCol + "] cell type from Excel (" + cellType + ") is unknown.  Treating as string.");
                Message.printStatus(2,routine,"Creating table column [" + iCol + "]=" + TableColumnType.valueOf(TableField.DATA_TYPE_STRING));
                table.addField ( new TableField(TableField.DATA_TYPE_STRING, columnNames[columnIndex], -1, -1), null );
            }
        }
    }
    return columnNames;
}
*/

/**
Get the array of cell ranges based on one of the input address methods.
@param wb the Excel workbook object
@param sheet the sheet in the workbook, read in entirety if no other address information is given
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name, treated as named range
@return null if no area reference can be determined
*/
public AreaReference getAreaReference ( Workbook wb, Sheet sheet, String excelAddress, String excelNamedRange, String excelTableName )
{   String routine = "ExcelToolkit.getAreaReference";
    if ( (excelTableName != null) && (excelTableName.length() > 0) ) {
        // Table name takes precedence as range name
        excelNamedRange = excelTableName;
    }
    // If sheet is specified but excelAddress, String excelNamedRange, String excelTableName are not,
    // read the entire sheet
    if ( ((excelAddress == null) || (excelAddress.length() == 0)) &&
        ((excelNamedRange == null) || (excelNamedRange.length() == 0)) ) {
        // Examine the sheet for blank columns/cells.  POI provides methods for the rows...
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        if ( Message.isDebugOn ) {
            Message.printDebug(1, routine, "Sheet firstRow=" + firstRow + ", lastRow=" + lastRow );
        }
        // ...but have to iterate through the rows as per:
        //  http://stackoverflow.com/questions/2194284/how-to-get-the-last-column-index-reading-excel-file
        Row row;
        int firstCol = -1;
        int lastCol = -1;
        int cellNum; // Index of cell in row (not column number?)
        int col;
        for ( int iRow = firstRow; iRow <= lastRow; iRow++ ) {
            row = sheet.getRow(iRow);
            if ( row == null ) {
                // TODO SAM 2013-06-28 Sometimes this happens with extra rows at the end of a worksheet?
                continue;
            }
            cellNum = row.getFirstCellNum(); // Not sure what this returns if no columns.  Assume -1
            if ( cellNum >= 0 ) {
                col = row.getCell(cellNum).getColumnIndex();
                if ( firstCol < 0 ) {
                    firstCol = col;
                }
                else {
                    firstCol = Math.min(firstCol, col);
                }
            }
            cellNum = row.getLastCellNum() - 1; // NOTE -1, as per API docs
            if ( cellNum >= 0 ) {
                col = row.getCell(cellNum).getColumnIndex();
                if ( lastCol < 0 ) {
                    lastCol = col;
                }
                else {
                    lastCol = Math.max(lastCol, col);
                }
            }
            if ( Message.isDebugOn ) {
                Message.printDebug(1, routine, "row " + iRow + ", firstCol=" + firstCol + ", lastCol=" + lastCol );
            }
        }
        // Return null if the any of the row column limits were not determined
        if ( (firstRow < 0) || (firstCol < 0) || (lastRow < 0) || (lastCol < 0) ) {
            return null;
        }
        else {
            return new AreaReference(new CellReference(firstRow,firstCol), new CellReference(lastRow,lastCol));
        }
    }
    if ( (excelAddress != null) && (excelAddress.length() > 0) ) {
        return new AreaReference(excelAddress);
    }
    else if ( (excelNamedRange != null) && (excelNamedRange.length() > 0) ) {
        int namedCellIdx = wb.getNameIndex(excelNamedRange);
        if ( namedCellIdx < 0 ) {
            Message.printWarning(3, routine, "Unable to get Excel internal index for named range \"" +
                excelNamedRange + "\"" );
            return null;
        }
        Name aNamedCell = wb.getNameAt(namedCellIdx);

        // Retrieve the cell at the named range and test its contents
        // Will get back one AreaReference for C10, and
        //  another for D12 to D14
        AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getRefersToFormula());
        // Can only handle one area
        if ( arefs.length != 1 ) {
            return null;
        }
        else {
            return arefs[0];
        }
    }
    else {
        return null;
    }
}

/**
Create a format string for a floating point number.
@param precision number of digits after the decimal, can be 0.
@return format string for cell
*/
public String createFormatForFloat ( int precision )
{
    String format = "0.";
    if ( precision == 0 ) {
        // No decimal
        format= "0";
    }
    else {
        for ( int i = 0; i < precision; i++ ) {
            format += "0";
        }
    }
    return format;
}

/**
Is the row a comment?
@param sheet sheet being read
@param iRow row in sheet (0+)
@param comment if not null, character at start of row that indicates comment
*/
public boolean isRowComment ( Sheet sheet, int iRow, String comment )
{   if ( (comment == null) || comment.length() == 0 ) {
        return false;
    }
    Row dataRow = sheet.getRow(iRow);
    Cell cell = dataRow.getCell(0);
    if ( (cell != null) && (cell.getCellType() == Cell.CELL_TYPE_STRING) ) {
        String cellValue = cell.getStringCellValue();
        if ( (cellValue != null) && (cellValue.length() >= comment.length()) &&
            cellValue.substring(0,comment.length()).equals(comment) ) {
            return true;
        }
    }
    return false;
}

/**
Look up an Excel cell type, for messages.
@param cellType cell type from Apache POI Cell.getCellType()
@return cell type string (e.g., "NUMERIC") corresponding to cell type enumeration.
*/
public String lookupExcelCellType(int cellType)
{
    switch ( cellType ) {
        case Cell.CELL_TYPE_NUMERIC: return "NUMERIC"; // 0
        case Cell.CELL_TYPE_STRING: return "STRING"; // 1
        case Cell.CELL_TYPE_FORMULA: return "FORMULA"; // 2
        case Cell.CELL_TYPE_BLANK: return "BLANK"; // 3
        case Cell.CELL_TYPE_BOOLEAN: return "BOOLEAN"; // 4
        case Cell.CELL_TYPE_ERROR: return "ERROR"; // 5
        default: return "UNKNOWN";
    }
}

/**
Return an Area object given and address.
*/
public AreaReference parseAreaReferenceFromAddress ( Workbook wb, Sheet sheet, String address )
{
    // Define a named range.  First try to retrieve
    int nrid = wb.getNameIndex(address);
    AreaReference ar = null;
    if ( nrid >= 0 ) {
        // Have a named range
        Name nr = wb.getNameAt(nrid);
        ar = new AreaReference(nr.getRefersToFormula());
    }
    else {
        // Assume address is A1 notation
        ar = new AreaReference(address);
    }
    return ar;
}

/**
Read the table from an Excel worksheet and transfer to a row in the given table.
The column and matching cells are specified by the columnMap parameter.
@param table existing table to be appended to, or null to create a new table
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param columnCellMap a map indicating table column names and matching cells (named range or A1 notation) to read.  One row will
be added to the table.
@param excelIntegerCells cell addresses that should be treated as integers, or null if none
@param excelDateTimeCells cell addresses that should be treated as date/times, or null if none
@param numberPrecision digits after decimal for floating point numbers (can't yet determine from Excel)
@param readAllAsText if True, treat all data as text values
@param problems list of problems encountered during read, for formatted logging in calling code
@return a DataTable with the Excel contents. If an output table is provided, it is returned.
Currently a new table cannot be created.
*/
public DataTable readTableCells ( String workbookFile, String sheetName, boolean keepOpen,
    LinkedHashMap<String,String> columnCellMap,
    DataTable table, Hashtable<String,String>columnIncludeFiltersMap, String ifTableRowNotFound,
    // String [] excelIntegerCells, String [] excelDateTimeCells, int numberPrecision, boolean readAllAsText,
    List<String> problems )
throws FileNotFoundException, IOException
{   String routine = "ExcelToolkit.readTableCellsFromExcelFile";
    if ( table == null ) {
        table = new DataTable();
    }
    
    Workbook wb = null;
    InputStream inp = null;
    try {
        // See if an open workbook by the same name exists
        wb = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wb == null ) {
            try {
                inp = new FileInputStream(workbookFile);
            }
            catch ( IOException e ) {
                problems.add ( "Error opening workbook file \"" + workbookFile + "\" (" + e + ")." );
                return table;
            }
            try {
                wb = WorkbookFactory.create(inp);
            }
            catch ( InvalidFormatException e ) {
                problems.add ( "Error creating workbook object from \"" + workbookFile + "\" (" + e + ")." );
                return table;
            }
        }
        Sheet sheet = null;
        // TODO SAM 2013-02-22 In the future sheet may be determined from named address (e.g., named ranges
        // are global in workbook)
        if ( (sheetName == null) || (sheetName.length() == 0) ) {
            // Default is to use the first sheet
            sheet = wb.getSheetAt(0);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include any worksheets" );
                return table;
            }
        }
        else {
            sheet = wb.getSheet(sheetName);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include worksheet named \"" + sheetName + "\"" );
                return table;
            }
        }
        // First get the table column numbers for columns mentioned in the filter
        Enumeration keys = null;
        int [] columnNumbersToFilter = null;
        String [] columnFilterGlobs = null;
        int ikey = -1;
        String key = null;
        if ( columnIncludeFiltersMap != null ) {
            keys = columnIncludeFiltersMap.keys();
            columnNumbersToFilter = new int[columnIncludeFiltersMap.size()];
            columnFilterGlobs = new String[columnIncludeFiltersMap.size()];
            ikey = -1;
            key = null;
            while ( keys.hasMoreElements() ) {
                ++ikey;
                columnNumbersToFilter[ikey] = -1;
                try {
                    key = (String)keys.nextElement();
                    columnNumbersToFilter[ikey] = table.getFieldIndex(key);
                    if ( columnNumbersToFilter[ikey] < 0 ) {
                        problems.add ( "Filter column \"" + key + "\" not found in table.");
                    }
                    columnFilterGlobs[ikey] = (String)columnIncludeFiltersMap.get(key);
                    // Turn default globbing notation into internal Java regex notation
                    columnFilterGlobs[ikey] = columnFilterGlobs[ikey].replace("*", ".*").toUpperCase();
                }
                catch ( Exception e ) {
                    problems.add ( "Filter column \"" + key + "\" not found in table.");
                }
            }
        }
        // Similarly, get the table column numbers for column to cell map
        // Also set the cell styles based on the input table column types
        DataFormat [] cellFormats = new DataFormat[columnCellMap.size()];
        CellStyle [] cellStyles = new CellStyle[columnCellMap.size()];
        //keys = columnCellMap.keys();
        int [] columnNumbersToMap = new int[columnCellMap.size()];
        String [] columnNamesForCellMap = new String[columnCellMap.size()];
        String [] columnCellAddresses = new String[columnCellMap.size()];
        int [] tableColumnTypes = new int[columnCellMap.size()];
        int precision;
        ikey = -1;
        for ( Map.Entry<String,String> entry: columnCellMap.entrySet() ) {
            ++ikey;
            columnNumbersToMap[ikey] = -1;
            try {
                key = entry.getKey();
                //key = (String)keys.nextElement();
                columnNamesForCellMap[ikey] = key;
                columnNumbersToMap[ikey] = table.getFieldIndex(key);
                if ( columnNumbersToMap[ikey] < 0 ) {
                    problems.add ( "Column \"" + key + "\" for cell map not found in table." );
                }
                else {
                    // Create the styles for the data values
                    cellFormats[ikey] = wb.createDataFormat();
                    cellStyles[ikey] = wb.createCellStyle();
                    tableColumnTypes[ikey] = table.getFieldDataType(columnNumbersToMap[ikey]);
                    if ( (tableColumnTypes[ikey] == TableField.DATA_TYPE_FLOAT) || (tableColumnTypes[ikey] == TableField.DATA_TYPE_DOUBLE) ) {
                        precision = table.getFieldPrecision(columnNumbersToMap[ikey]);
                        if ( precision < 0 ) {
                            precision = 6;
                        }
                        String format = "0.";
                        for ( int i = 0; i < precision; i++ ) {
                            format += "0";
                        }
                        cellStyles[ikey].setDataFormat(cellFormats[ikey].getFormat(format));
                    }
                    else if ( (tableColumnTypes[ikey] == TableField.DATA_TYPE_INT) || (tableColumnTypes[ikey] == TableField.DATA_TYPE_LONG) ) {
                        String format = "0";
                        cellStyles[ikey].setDataFormat(cellFormats[ikey].getFormat(format));
                    }
                }
                columnCellAddresses[ikey] = (String)columnCellMap.get(key);
                Message.printStatus(2,routine,"ColumnCellMap[" + ikey + "]: Column[" + columnNumbersToMap[ikey] + "]=" + columnNamesForCellMap[ikey] + "\" Cell(address or name)=" + columnCellAddresses[ikey] );
            }
            catch ( Exception e ) {
                problems.add ( "Column \"" + key + "\" for cell map not found in table." );
            }
        }
        // Loop through the records in the table and hopefully find exactly one match
        boolean filterMatches;
        int icol;
        Object o;
        String s;
        TableRecord record = null;
        int irow = -1;
        int iRowOut = -1;
        for ( irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
            filterMatches = true;
            if ( (columnIncludeFiltersMap != null) && (columnNumbersToFilter.length > 0) ) {
                // Filters can be done on any columns so loop through to see if row matches before doing copy
                for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                    if ( columnNumbersToFilter[icol] < 0 ) {
                        filterMatches = false;
                        break;
                    }
                    try {
                        o = table.getFieldValue(irow, columnNumbersToFilter[icol]);
                        if ( o == null ) {
                            filterMatches = false;
                            break; // Don't include nulls when checking values
                        }
                        s = ("" + o).toUpperCase();
                        if ( !s.matches(columnFilterGlobs[icol]) ) {
                            // A filter did not match so don't copy the record
                            filterMatches = false;
                            break;
                        }
                    }
                    catch ( Exception e ) {
                        problems.add("Error getting table data for filter check [" + irow + "][" +
                            columnNumbersToFilter[icol] + "] (" + e + ").");
                        Message.printWarning(3, routine, "Error getting table data for [" + irow + "][" +
                            columnNumbersToFilter[icol] + "] (" + e + ")." );
                    }
                }
                if ( !filterMatches ) {
                    // Skip the record.
                    continue;
                }
                else {
                    if ( record != null ) {
                        problems.add("Column include filters result in > 1 matching record.  " +
                            "Cannot transfer data from Excel cells because table row match is ambiguous." );
                        return table;
                    }
                    else {
                        record = table.getRecord(irow);
                        iRowOut = irow;
                    }
                }
            }
        }
        if ( iRowOut < 0 ) {
            if ( ifTableRowNotFound.equalsIgnoreCase("Ignore") ) {
                // Don't do anything
                return table;
            }
            else if ( ifTableRowNotFound.equalsIgnoreCase("Append") ) {
                // Append a new row
                record = table.addRecord(table.emptyRecord());
                iRowOut = table.getNumberOfRecords() - 1;
                Message.printStatus(2, routine, "Appended row [" + iRowOut + "] to table since match not found.");
            }
            else if ( ifTableRowNotFound.equalsIgnoreCase("Warn") ) {
                problems.add ( "IfTableRowNotFoundWarning: unable to match single row in table for data." );
                return table;
            }
            else if ( ifTableRowNotFound.equalsIgnoreCase("Fail") ) {
                problems.add ( "IfTableRowNotFoundFailure: unable to match single row in table for data." );
                return table;
            }
        }
        else {
            Message.printStatus(2, routine, "Matched table row [" + iRowOut + "] to set data.");
        }
        // Loop through the cells to read and get the cells
        String cellAddress;
        AreaReference area = null;
        Row row;
        Cell cell;
        CellValue formulaCellValue = null; // Cell value after formula evaluation
        int cellType;
        String cellValueString;
        boolean cellValueBoolean;
        double cellValueDouble;
        boolean cellIsFormula; // Used to know when the evaluate cell formula to get output object
        Date cellValueDate;
        DateTime dt;
        int iColOut; // in the table
        ikey = -1;
        //keys = columnCellMap.keys();
        //while ( keys.hasMoreElements() ) {
        for ( Map.Entry<String,String> entry: columnCellMap.entrySet() ) {
            ++ikey;
            iColOut = columnNumbersToMap[ikey];
            //key = (String)keys.nextElement();
            key = entry.getKey();
            cellAddress = entry.getValue();
            Message.printStatus(2,routine,"Processing column \"" + key + "\", icolOut=" + iColOut + " cellAddress=\"" + cellAddress + "\"" );
            //cellAddress = (String)columnCellMap.get(key);
            if ( (cellAddress != null) && !cellAddress.equals("") ) {
                area = null;
                // First try to get a named range
                int namedCellIdx = wb.getNameIndex(cellAddress);
                if ( namedCellIdx < 0 ) {
                    // Must be an A1 address
                    area = new AreaReference(cellAddress);
                }
                else {
                    Name aNamedCell = wb.getNameAt(namedCellIdx);
                    // Retrieve the cell at the named range and test its contents
                    // Will get back one AreaReference for C10, and
                    //  another for D12 to D14
                    AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getRefersToFormula());
                    // Can only handle one area
                    if ( arefs.length != 1 ) {
                        continue;
                    }
                    else {
                        area = arefs[0];
                    }
                }
            }
            if ( area != null ) {
                // Get the cell for the area
                int rowStart = area.getFirstCell().getRow();
                int rowEnd = area.getLastCell().getRow();
                int colStart = area.getFirstCell().getCol();
                int colEnd = area.getLastCell().getCol();
                if ( (rowStart != rowEnd) || (colStart != colEnd) ) {
                    // Problem - can only read single cells
                    problems.add ( "Cell range " + cellAddress + " is not a single cell - cannot read." );
                }
                int iRow = rowStart;
                int iCol = colStart;
                // Read the row for the cell
                row = sheet.getRow(iRow);
                if ( row == null ) {
                    // Seems to happen at bottom of worksheets where there are extra junk rows
                    problems.add ( "Row is null for cell address " + cellAddress + " - cannot read cell - check case-sensitive spelling if a named range.");
                    continue;
                }
                // Read the cell object
                cell = row.getCell(iCol);
                if ( cell == null ) {
                    problems.add ( "Cell is null for cell address " + cellAddress + " - cannot read cell.");
                }
                // First get the data using the type indicated for the cell.  Then translate to
                // the appropriate type in the data table.  Handling at cell level is needed because
                // the Excel worksheet might have cell values that are mixed type in the column.
                // The checks are exhaustive, so list in the order that is most likely (string, double,
                // boolean, blank, error, formula).
                cellType = cell.getCellType();
                cellIsFormula = false;
                Message.printStatus(2,routine,"Cell type for \"" + cellAddress + "\" table column \"" + columnNamesForCellMap[ikey]+ "\" [" +
                    iColOut + "] is " + cellType + " " + lookupExcelCellType(cellType) );
                if ( iColOut < 0 ) {
                    Message.printStatus(2,routine,"Cell address \"" + cellAddress + "\" column is not in table - skipping." );
                    continue;
                }
                if ( cellType == Cell.CELL_TYPE_FORMULA ) {
                    // Have to evaluate the cell and get the value as the result
                    cellIsFormula = true;
                    try {
                        FormulaEvaluator formulaEval = wb.getCreationHelper().createFormulaEvaluator();
                        formulaCellValue = formulaEval.evaluate(cell);
                        // Reset cellType for following code
                        cellType = formulaCellValue.getCellType();
                        if ( Message.isDebugOn ) {
                            Message.printDebug(1, routine, "Detected formula, new cellType=" + cellType +
                                ", cell value=\"" + formulaCellValue + "\"" );
                        }
                    }
                    catch ( Exception e ) {
                        // Handle as an error in processing below.
                        problems.add ( "Error evaluating formula for row [" + iRow + "][" + iCol + "] \"" +
                            cell + "\" - setting to error cell type (" + e + ")");
                        cellType = Cell.CELL_TYPE_ERROR;
                    }
                }
                if ( cellType == Cell.CELL_TYPE_STRING ) {
                    if ( cellIsFormula ) {
                        cellValueString = formulaCellValue.getStringValue();
                    }
                    else {
                        cellValueString = cell.getStringCellValue();
                    }
                    Message.printStatus(2, routine, "Cell string value=\"" + cellValueString + "\"" );
                    if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_STRING ) {
                        // Just set
                        table.setFieldValue(iRowOut, iColOut, cellValueString, true);
                    }
                    else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DOUBLE ) {
                        // Parse to the double
                        try {
                            table.setFieldValue(iRowOut, iColOut, new Double(cellValueString), true);
                        }
                        catch ( NumberFormatException e ) {
                            // Set to NaN
                            table.setFieldValue(iRowOut, iColOut, Double.NaN, true);
                        }
                    }
                    else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_INT ) {
                        // Parse to the boolean
                        if ( cellValueString.equalsIgnoreCase("True") || cellValueString.equals("1") ) {
                            table.setFieldValue(iRowOut, iColOut, new Integer(1), true);
                        }
                        else {
                            // Set to null
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DATE ) {
                        // Try to parse to a date/time string
                        try {
                            dt = DateTime.parse(cellValueString);
                            table.setFieldValue(iRowOut, iColOut, dt.getDate(), true);
                        }
                        catch ( Exception e ) {
                            // Set to null
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DATETIME ) {
                        // Try to parse to a date/time string
                        try {
                            dt = DateTime.parse(cellValueString);
                            table.setFieldValue(iRowOut, iColOut, dt, true);
                        }
                        catch ( Exception e ) {
                            // Set to null
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else {
                        // Other cell types don't translate
                        table.setFieldValue(iRowOut, iColOut, null, true);
                    }
                }
                else if ( cellType == Cell.CELL_TYPE_NUMERIC ) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        if ( cellIsFormula ) {
                            // TODO SAM 2013-02-25 Does not seem to method to return date 
                            cellValueDate = null;
                        }
                        else {
                            cellValueDate = cell.getDateCellValue();
                        }
                        Message.printStatus(2, routine, "Cell date value=" + cellValueDate );
                        if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DATE ) {
                            // date to date
                            table.setFieldValue(iRowOut, iColOut, cellValueDate, true);
                        }
                        else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DATETIME ) {
                            // date to date/time
                            table.setFieldValue(iRowOut, iColOut, new DateTime(cellValueDate), true);
                        }
                        else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_STRING ) {
                            // date to string
                            try {
                                dt = new DateTime ( cellValueDate );
                                table.setFieldValue(iRowOut, iColOut, dt.toString(), true);
                            }
                            catch ( Exception e ) {
                                table.setFieldValue(iRowOut, iColOut, null, true);
                            }
                        }
                        else {
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                    else {
                        // Floating point value
                        if ( cellIsFormula ) {
                            cellValueDouble = formulaCellValue.getNumberValue();
                        }
                        else {
                            cellValueDouble = cell.getNumericCellValue();
                        }
                        Message.printStatus(2, routine, "Cell numeric value=" + cellValueDouble );
                        if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DOUBLE ) {
                            // Double to double
                            table.setFieldValue(iRowOut, iColOut, new Double(cellValueDouble), true);
                        }
                        else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_STRING ) {
                            // Double to string
                            table.setFieldValue(iRowOut, iColOut, "" + cellValueDouble, true);
                        }
                        else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_INT ) {
                            // Double to integer - use an offset to help make sure integer value is correct
                            if ( cellValueDouble >= 0.0 ) {
                                table.setFieldValue(iRowOut, iColOut, new Integer((int)(cellValueDouble + .0001)), true);
                            }
                            else {
                                table.setFieldValue(iRowOut, iColOut, new Integer((int)(cellValueDouble - .0001)), true);
                            }
                        }
                        else {
                            table.setFieldValue(iRowOut, iColOut, null, true);
                        }
                    }
                }
                else if ( cellType == Cell.CELL_TYPE_BOOLEAN ) {
                    if ( cellIsFormula ) {
                        cellValueBoolean = formulaCellValue.getBooleanValue();
                    }
                    else {
                        cellValueBoolean = cell.getBooleanCellValue();
                    }
                    Message.printStatus(2, routine, "Cell boolean value=" + cellValueBoolean );
                    if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_INT ) {
                        table.setFieldValue(iRowOut, iColOut, cellValueBoolean, true);
                    }
                    else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_STRING ) {
                        // Just set
                        table.setFieldValue(iRowOut, iColOut, "" + cellValueBoolean, true);
                    }
                    else if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_DOUBLE ) {
                        if ( cellValueBoolean ) {
                            table.setFieldValue(iRowOut, iColOut, new Double(1.0), true);
                        }
                        else {
                            table.setFieldValue(iRowOut, iColOut, new Double(0.0), true);
                        }
                    }
                    else {
                        // Not able to convert
                        table.setFieldValue(iRowOut, iColOut, null, true);
                    }
                }
                else if ( cellType == Cell.CELL_TYPE_BLANK ) {
                    // Null works for all object types.  If truly a blank string in text cell, use "" as text
                    if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_STRING ) {
                        table.setFieldValue(iRowOut, iColOut, "", true);
                    }
                    else {
                        table.setFieldValue(iRowOut, iColOut, null, true);
                    }
                    Message.printStatus(2, routine, "Cell is blank");
                }
                else if ( cellType == Cell.CELL_TYPE_ERROR ) {
                    if ( tableColumnTypes[ikey] == TableField.DATA_TYPE_STRING ) {
                        table.setFieldValue(iRowOut, iColOut, "", true);
                    }
                    else {
                        table.setFieldValue(iRowOut, iColOut, null, true);
                    }
                    Message.printStatus(2, routine, "Cell is error");
                }
                else {
                    table.setFieldValue(iRowOut, iColOut, null, true);
                    Message.printStatus(2, routine, "Cell type is unknown - setting null");
                }
            }
        }
    }
    catch ( Exception e ) {
        problems.add ( "Error reading workbook \"" + workbookFile + "\" (" + e + ")." );
        Message.printWarning(3,routine,e);
    }
    finally {
        // If keeping open skip because it will be written by a later command.
        if ( keepOpen ) {
            // Save the open workbook for other commands to use
            ExcelUtil.setOpenWorkbook(workbookFile,wb);
        }
        else {
            // Close the workbook and remove from the cache
            if ( inp != null ) {
                inp.close();
            }
            ExcelUtil.removeOpenWorkbook(workbookFile);
        }
    }
    return table;
}

/**
Set a cell to blank.  If necessary a new row will be created.
@param sheet worksheet to write to
@param row row (0+)
@param col column (0+)
@param s string to write
@return the Cell instance being modified
*/
public Cell setCellBlank ( Sheet sheet, int row, int col )
{
	Row wbRowColumnNames = sheet.getRow(row);
	// If it does not exist, create it
	if ( wbRowColumnNames == null ) {
	    wbRowColumnNames = sheet.createRow(row);
	}
	Cell wbCell = wbRowColumnNames.getCell(col);
	if ( wbCell == null ) {
	    wbCell = wbRowColumnNames.createCell(col);
	}
	wbCell.setCellType(Cell.CELL_TYPE_BLANK);
	return wbCell;
}

/**
Write a single float cell value.  If necessary a new row will be created.
@param sheet worksheet to write to
@param row row (0+)
@param col column (0+)
@param d string to write
@param style cell style to set (or null to not set style)
@return the Cell instance being modified
*/
public Cell setCellValue ( Sheet sheet, int row, int col, double d, CellStyle style )
{
	Row wbRowColumnNames = sheet.getRow(row);
	// If it does not exist, create it
	if ( wbRowColumnNames == null ) {
	    wbRowColumnNames = sheet.createRow(row);
	}
	Cell wbCell = wbRowColumnNames.getCell(col);
	if ( wbCell == null ) {
	    wbCell = wbRowColumnNames.createCell(col);
	}
	wbCell.setCellValue(d);
	if ( style != null ) {
		wbCell.setCellStyle(style);
	}
	return wbCell;
}

/**
Write a single float cell value.  If necessary a new row will be created.
@param sheet worksheet to write to
@param row row (0+)
@param col column (0+)
@param f string to write
@param style cell style to set (or null to not set style)
@return the Cell instance being modified
*/
public Cell setCellValue ( Sheet sheet, int row, int col, float f, CellStyle style )
{
	Row wbRowColumnNames = sheet.getRow(row);
	// If it does not exist, create it
	if ( wbRowColumnNames == null ) {
	    wbRowColumnNames = sheet.createRow(row);
	}
	Cell wbCell = wbRowColumnNames.getCell(col);
	if ( wbCell == null ) {
	    wbCell = wbRowColumnNames.createCell(col);
	}
	wbCell.setCellValue(f);
	if ( style != null ) {
		wbCell.setCellStyle(style);
	}
	return wbCell;
}

/**
Write a single string cell value.  If necessary a new row will be created.
@param sheet worksheet to write to
@param row row (0+)
@param col column (0+)
@param s string to write
@return the Cell instance being modified
*/
public Cell setCellValue ( Sheet sheet, int row, int col, String s )
{
	Row wbRowColumnNames = sheet.getRow(row);
	// If it does not exist, create it
	if ( wbRowColumnNames == null ) {
	    wbRowColumnNames = sheet.createRow(row);
	}
	Cell wbCell = wbRowColumnNames.getCell(col);
	if ( wbCell == null ) {
	    wbCell = wbRowColumnNames.createCell(col);
	}
	wbCell.setCellValue(s);
	return wbCell;
}

/**
Set a comment string.
@param wb the workbook being updated
@param sheet the worksheet being updated
@param cell the cell for which to set the comment
*/
public void setComment ( Workbook wb, Sheet sheet, Cell cell, String commentString, String author )
{
	// When the comment box is visible, have it show in a 1x3 space
	CreationHelper factory = wb.getCreationHelper();
    ClientAnchor anchor = factory.createClientAnchor();
    anchor.setCol1(cell.getColumnIndex());
    anchor.setCol2(cell.getColumnIndex()+1);
    Row row = cell.getRow();
    anchor.setRow1(row.getRowNum());
    anchor.setRow2(row.getRowNum()+3);

    // Create the comment and set the text+author
    Drawing drawing = sheet.createDrawingPatriarch();
    Comment comment = drawing.createCellComment(anchor);
    RichTextString str = factory.createRichTextString(commentString);
    comment.setString(str);
    comment.setAuthor(author);

    // Assign the comment to the cell
    cell.setCellComment(comment);
}

/**
Write a list of values to table cells in an Excel worksheet.
@param table the table to output
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param columnIncludeFiltersMap a map indicating patters for column values, to exclude rows
@param columnCellMap a map indicating cell address and contents to write
@param cellFormatExcel if true, retain the Excel cell formats, if false set formatting to match table column types
@param keepOpen if true, keep the Excel file open, if false close after processing
@param problems list of problems encountered during read, for formatted logging in calling code
*/
public void writeTableCells ( DataTable table, String workbookFile, String sheetName,
    Hashtable columnIncludeFiltersMap,
    Hashtable columnCellMap, boolean cellFormatExcel, boolean keepOpen, List<String> problems )
throws FileNotFoundException, IOException
{   String routine = getClass().getSimpleName() + ".writeTableCells", message;
    
    Workbook wb = null;
    InputStream inp = null;
    try {
        // See if an open workbook by the same name exists
        wb = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wb == null ) {
            // Workbook is not open in memory so Open the file
            try {
                inp = new FileInputStream(workbookFile);
            }
            catch ( IOException e ) {
                problems.add ( "Error opening workbook file \"" + workbookFile + "\" (" + e + ")." );
                return;
            }
            try {
                // Open an existing workbook if it exists...
                wb = WorkbookFactory.create(inp);
            }
            catch ( InvalidFormatException e ) {
                problems.add ( "Error creating workbook object from \"" + workbookFile + "\" (" + e + ")." );
                return;
            }
            finally {
                // Close the sheet because will need to write it below and close
                if ( !keepOpen ) {
                    inp.close();
                }
            }
        }
        Sheet sheet = null;
        // TODO SAM 2013-02-22 In the future sheet may be determined from named address (e.g., named ranges
        // are global in workbook)
        if ( (sheetName == null) || (sheetName.length() == 0) ) {
            // Default is to use the first sheet
            sheet = wb.getSheetAt(0);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include any worksheets" );
                return;
            }
        }
        else {
            sheet = wb.getSheet(sheetName);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include worksheet named \"" + sheetName + "\"" );
                return;
            }
        }
        // First get the table column numbers for columns mentioned in the filter
        Enumeration keys = columnIncludeFiltersMap.keys();
        int [] columnNumbersToFilter = new int[columnIncludeFiltersMap.size()];
        String [] columnFilterGlobs = new String[columnIncludeFiltersMap.size()];
        int ikey = -1;
        String key = null;
        while ( keys.hasMoreElements() ) {
            ++ikey;
            columnNumbersToFilter[ikey] = -1;
            try {
                key = (String)keys.nextElement();
                columnNumbersToFilter[ikey] = table.getFieldIndex(key);
                if ( columnNumbersToFilter[ikey] < 0 ) {
                    problems.add ( "Filter column \"" + key + "\" not found in table.");
                }
                columnFilterGlobs[ikey] = (String)columnIncludeFiltersMap.get(key);
                // Turn default globbing notation into internal Java regex notation
                columnFilterGlobs[ikey] = columnFilterGlobs[ikey].replace("*", ".*").toUpperCase();
            }
            catch ( Exception e ) {
                problems.add ( "Filter column \"" + key + "\" not found in table.");
            }
        }
        // Similarly, get the table column numbers for column to cell map
        // Also set the cell styles based on the input table column types
        DataFormat [] cellFormats = new DataFormat[columnCellMap.size()];
        CellStyle [] cellStyles = new CellStyle[columnCellMap.size()];
        keys = columnCellMap.keys();
        int [] columnNumbersToMap = new int[columnCellMap.size()];
        String [] columnNumbersForCellMap = new String[columnCellMap.size()];
        String [] columnCellAddresses = new String[columnCellMap.size()];
        int tableFieldType;
        int precision;
        ikey = -1;
        while ( keys.hasMoreElements() ) {
            ++ikey;
            columnNumbersToMap[ikey] = -1;
            try {
                key = (String)keys.nextElement();
                columnNumbersForCellMap[ikey] = key;
                columnNumbersToMap[ikey] = table.getFieldIndex(key);
                if ( columnNumbersToMap[ikey] < 0 ) {
                    problems.add ( "Column \"" + key + "\" for cell map not found in table." );
                }
                else {
                    // Create the styles for the data values
                    cellFormats[ikey] = wb.createDataFormat();
                    cellStyles[ikey] = wb.createCellStyle();
                    tableFieldType = table.getFieldDataType(columnNumbersToMap[ikey]);
                    if ( (tableFieldType == TableField.DATA_TYPE_FLOAT) || (tableFieldType == TableField.DATA_TYPE_DOUBLE) ) {
                        precision = table.getFieldPrecision(columnNumbersToMap[ikey]);
                        if ( precision < 0 ) {
                            precision = 6;
                        }
                        String format = "0.";
                        for ( int i = 0; i < precision; i++ ) {
                            format += "0";
                        }
                        cellStyles[ikey].setDataFormat(cellFormats[ikey].getFormat(format));
                    }
                    else if ( (tableFieldType == TableField.DATA_TYPE_INT) || (tableFieldType == TableField.DATA_TYPE_LONG) ) {
                        String format = "0";
                        cellStyles[ikey].setDataFormat(cellFormats[ikey].getFormat(format));
                    }
                }
                columnCellAddresses[ikey] = (String)columnCellMap.get(key);
            }
            catch ( Exception e ) {
                problems.add ( "Column \"" + key + "\" for cell map not found in table." );
            }
        }
        // Loop through the records in the table and hopefully find exactly one match
        boolean filterMatches;
        int icol;
        Object o;
        String s;
        TableRecord record = null;
        int irow = -1, rowMatched = -1;;
        for ( irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
            filterMatches = true;
            if ( columnNumbersToFilter.length > 0 ) {
                // Filters can be done on any columns so loop through to see if row matches before doing copy
                for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                    if ( columnNumbersToFilter[icol] < 0 ) {
                        filterMatches = false;
                        break;
                    }
                    try {
                        o = table.getFieldValue(irow, columnNumbersToFilter[icol]);
                        if ( o == null ) {
                            filterMatches = false;
                            break; // Don't include nulls when checking values
                        }
                        s = ("" + o).toUpperCase();
                        if ( !s.matches(columnFilterGlobs[icol]) ) {
                            // A filter did not match so don't copy the record
                            filterMatches = false;
                            break;
                        }
                    }
                    catch ( Exception e ) {
                        problems.add("Error getting table data for filter check [" + irow + "][" +
                            columnNumbersToFilter[icol] + "] (" + e + ").");
                        Message.printWarning(3, routine, "Error getting table data for [" + irow + "][" +
                            columnNumbersToFilter[icol] + "] (" + e + ")." );
                    }
                }
                if ( !filterMatches ) {
                    // Skip the record.
                    continue;
                }
                else {
                    if ( record != null ) {
                        problems.add("Column include filters result in > 1 matching record.  " +
                            "Cannot transfer data to Excel cells because will overwrite the same addresses." );
                        return;
                    }
                    else {
                        record = table.getRecord(irow);
                        rowMatched = irow;
                    }
                }
            }
        }
        if ( record == null ) {
            problems.add("Column include filters result in no matching records.  Cannot transfer data to Excel cells." );
            return;
        }
        // Loop through the columns in the column to cell map and output to Excel
        // Write the table data
        Object fieldValue;
        Double fieldValueDouble;
        Float fieldValueFloat;
        Integer fieldValueInteger;
        Long fieldValueLong;
        String cellString;
        int colOut, rowOut;
        boolean writeAllAsText = false;
        CellStyle cellStyleOrig = null;
        for ( icol = 0; icol < columnNumbersToMap.length; icol++ ) {
            fieldValue = table.getFieldValue(rowMatched, columnNumbersForCellMap[icol]);
            // Get the cell address
            AreaReference area = parseAreaReferenceFromAddress(wb,sheet,columnCellAddresses[icol]);
            if ( area == null ) {
                problems.add ( "Unable to get worksheet area reference from address information (empty worksheet?)." );
                return;
            }
            else {
                colOut = area.getFirstCell().getCol();
                rowOut = area.getFirstCell().getRow();
            }
            Message.printStatus(2,routine,"Writing table column \"" + columnNumbersForCellMap[icol] + "\" row=[" + rowMatched +
                "] object= \"" + fieldValue + "\" col=[" + columnNumbersToMap[icol] + "] to address \"" +
                columnCellAddresses[icol] + "\" row=[" + rowOut + "] col=[" + colOut + "]");
            // First try to get an existing row
            Row wbRow = sheet.getRow(rowOut);
            // If it does not exist, create it
            if ( wbRow == null ) {
                wbRow = sheet.createRow(rowOut);
            }
            // Write the row value to the cell address, taking care of the object type
            // First try to get an existing cell
            Cell wbCell = wbRow.getCell(colOut);
            if ( wbCell == null ) {
                wbCell = wbRow.createCell(colOut);
            }
            // Existing stile is retained if cellFormatExcel=true (for some reason need to reset after setting values)
            cellStyleOrig = wbCell.getCellStyle();
            try {
                tableFieldType = table.getFieldDataType(columnNumbersToMap[icol]);
                precision = table.getFieldPrecision(columnNumbersToMap[icol]);
                if ( tableFieldType == TableField.DATA_TYPE_STRING ) {
                    if ( fieldValue == null ) {
                        cellString = ""; // Strings are OK as blanks for string cells
                        //wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                        wbCell.setCellValue("");
                    }
                    else {
                        wbCell.setCellValue((String)fieldValue);
                    }
                }
                else if ( tableFieldType == TableField.DATA_TYPE_FLOAT ) {
                    if ( fieldValue == null ) {
                        wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                    }
                    else {
                        fieldValueFloat = (Float)fieldValue;
                        if ( fieldValueFloat.isNaN() ) {
                            //cellString = NaNValue;
                            //wbCell.setCellValue((Float)null);
                            wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                        }
                        else {
                            if ( writeAllAsText ) {
                                if ( precision > 0 ) {
                                    // Format according to the precision if floating point
                                    cellString = StringUtil.formatString(fieldValue,"%." + precision + "f");
                                }
                                else {
                                    // Use default formatting.
                                    cellString = "" + fieldValue;
                                }
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueFloat);
                            }
                        }
                    }
                }
                else if ( tableFieldType == TableField.DATA_TYPE_DOUBLE ) {
                    if ( fieldValue == null ) {
                        wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                    }
                    else {
                        fieldValueDouble = (Double)fieldValue;
                        if ( fieldValueDouble.isNaN() ) {
                            //cellString = NaNValue;
                            //wbCell.setCellValue(cellString);
                            wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                        }
                        else {
                            if ( writeAllAsText ) {
                                if ( precision > 0 ) {
                                    // Format according to the precision if floating point
                                    cellString = StringUtil.formatString(fieldValue,"%." + precision + "f");
                                }
                                else {
                                    // Use default formatting.
                                    cellString = "" + fieldValue;
                                }
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueDouble);
                            }
                        }
                    }
                }
                else if ( tableFieldType == TableField.DATA_TYPE_INT ) {
                    if ( fieldValue == null ) {
                        wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                    }
                    else {
                        fieldValueInteger = (Integer)fieldValue;
                        if ( writeAllAsText ) {
                            cellString = "" + fieldValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            wbCell.setCellValue(fieldValueInteger);
                        }
                    }
                }
                else if ( tableFieldType == TableField.DATA_TYPE_LONG ) {
                    if ( fieldValue == null ) {
                        wbCell.setCellType(Cell.CELL_TYPE_BLANK);
                    }
                    else {
                        fieldValueLong = (Long)fieldValue;
                        if ( writeAllAsText ) {
                            cellString = "" + fieldValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            wbCell.setCellValue(fieldValueLong);
                        }
                    }
                }
                else {
                    // TODO SAM 2014-02-05 Add support for DateTime, Date, and other table types
                    // Use default formatting.
                    if ( fieldValue == null ) {
                        // TODO SAM 2014-01-21 Need to handle as blanks in output, if user indicates to do so
                        cellString = "";
                    }
                    else {
                        cellString = "" + fieldValue;
                    }
                    wbCell.setCellValue(cellString);
                }
                if ( cellFormatExcel ) {
                    // The cell format is set to the original - style seems to get reset when calling setCellValue()
                    // so reset to the original here.
                    wbCell.setCellStyle(cellStyleOrig);
                }
                else {
                    // Set to the style based on the column type in the table
                    wbCell.setCellStyle(cellStyles[icol]);
                }
            }
            catch ( Exception e ) {
                // Log but let the output continue
                message = "Unexpected error writing table row=[" + rowOut + "] col=[" +
                    columnNumbersToMap[icol] + "] (" + e + ").";
                Message.printWarning(3, routine, message );
                problems.add(message);
                Message.printWarning(3, routine, e);
            }
        }
    }
    catch ( Exception e ) {
        message = "Error writing to workbook \"" + workbookFile + "\" (" + e + ").";
        problems.add ( message );
        Message.printWarning(3,routine,message);
        Message.printWarning(3,routine,e);
    }
    finally {
        // Now write the workbook and close.  If keeping open skip because it will be written by a later command.
        if ( keepOpen ) {
            // Save the open workbook for other commands to use
            ExcelUtil.setOpenWorkbook(workbookFile,wb);
        }
        else {
            // Close the workbook and remove from the cache
            wb.setForceFormulaRecalculation(true); // Will cause Excel to recalculate formulas when it opens
            FileOutputStream fout = new FileOutputStream(workbookFile);
            wb.write(fout);
            fout.close();
            ExcelUtil.removeOpenWorkbook(workbookFile);
        }
    }
}

}