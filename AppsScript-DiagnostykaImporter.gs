var filesToImportFolder = "<YOUR_GOOGLE_DRIVE_FOLDER_ID>";
var sheetName = "<YOUR_SHEET_NAME>";
var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(sheetName).activate();
var firstImport = true;

function getFiles() {
  // get the CSV files in the specified Drive folder
  var files = DriveApp.getFolderById(filesToImportFolder).getFiles();
  sheet.clearContents();
  // loop through the files
  while (files.hasNext()) {
    // get the file
    var file = files.next();
    if (file.getName().endsWith(".csv")) {
      // parse the CSV
      var data = Utilities.parseCsv(file.getBlob().getDataAsString(), ";");
      // pass the file and the parsed data to the import function
      importData(file, data);
    }
  }
  changeDotsToCommasIn('D2:D');
  changeDotsToCommasIn('F2:F');
  changeDotsToCommasIn('G2:G');
}

function changeDotsToCommasIn(range) {
  var values = sheet.getRange(range).getValues();
  replaceInSheet(values, /(\d)\.(\d)/g, '$1,$2');
  sheet.getRange(range).setValues(values);
}

function importData(file, data) {
  var lastRow = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(sheetName).getLastRow();
  if (!firstImport) {
    var header = data.shift(); // Now the data don't have the header
  }
  firstImport = false;
  sheet.getRange(lastRow + 1, 1, data.length, 1).setValue(file.getName());
  sheet.getRange(lastRow + 1, 2, data.length, data[0].length).setValues(data);
}

function replaceInSheet(values, to_replace, replace_with) {
  //loop over the rows in the array
  for(var row in values){
    //use Array.map to execute a replace call on each of the cells in the row.
    var replaced_values = values[row].map(function(original_value) {
      return original_value.toString().replace(to_replace,replace_with);
    });

    //replace the original row values with the replaced values
    values[row] = replaced_values;
  }
}