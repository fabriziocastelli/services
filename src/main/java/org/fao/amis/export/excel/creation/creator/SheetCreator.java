package org.fao.amis.export.excel.creation.creator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import org.apache.log4j.Logger;
import org.fao.amis.export.configuration.URLGetter;
import org.fao.amis.export.data.configurations.dataCreator.DataCreator;
import org.fao.amis.export.data.daoValue.DaoForecastValue;
import org.fao.amis.export.excel.creation.utils.AmisExcelUtils;
import org.fao.amis.export.excel.formula.bean.FormulaBean;
import org.fao.amis.export.excel.formula.configurator.ConfigurationReader;
import org.fao.amis.export.excel.formula.translator.CellMapper;


import java.util.*;

/**
 * Created by fabrizio on 11/10/14.
 */
public class SheetCreator {

    private static final Logger LOGGER = Logger.getLogger(SheetCreator.class);

    private String[] nationalCodes;

    private static String FORMULA_URL;

    private ConfigurationReader configurationReader;

    private String commodityChosen;

    private String toDeleteProva;

    private CellMapper cellMappers;

    private URLGetter urlGetter;

    public SheetCreator(){

        urlGetter = new URLGetter();

    }

    public int createSummary(int rowCounter, HSSFSheet sheet, HSSFWorkbook workbook, DataCreator dataCreator, String commodityLabel) {


        //Create Date Last Updated
        String commodity = commodityLabel;
        this.commodityChosen = commodityLabel;
        String season = dataCreator.getSeason();
        String dataSource = dataCreator.getDatasource();
        String country = dataCreator.getCountry();

        rowCounter = createHeadingRow(rowCounter, sheet, workbook, "COMMODITY: ", commodity);
        rowCounter = createHeadingRow(rowCounter, sheet, workbook, "SEASON: ", season);
        rowCounter = createHeadingRow(rowCounter, sheet, workbook, "COUNTRY: ", country);
        rowCounter = createHeadingRow(rowCounter, sheet, workbook, "DATASOURCE: ", dataSource);

        rowCounter = AmisExcelUtils.createEmptyRow(rowCounter, sheet, workbook);

        return rowCounter;
    }

    private int createHeadingRow(int rowCounter, HSSFSheet sheet, HSSFWorkbook workbook, String header, String headerValue) {
        HSSFRow row = sheet.createRow(rowCounter++);
        // LOGGER.info("----------- createHeadingRow .... START ");

        if (header != null && headerValue == null) {
            HSSFCell cell = row.createCell((short) 0);
            cell.setCellStyle(AmisExcelUtils.getBigBoldTextCellStyle(workbook, null));
            cell.setCellValue(header);

            row.createCell((short) 1).setCellValue("");
        } else {
            // LOGGER.info("----------- header  "+header);

            HSSFCell cell = row.createCell((short) 0);
            cell.setCellStyle(AmisExcelUtils.getRightAlignmentStyle(workbook));
            cell.setCellValue(header);

            cell = row.createCell((short) 1);
            cell.setCellStyle(AmisExcelUtils.getBoldTextCellStyle(workbook, null));
            cell.setCellValue(headerValue);
        }

        // LOGGER.info("----------- createHeadingRow .... END rowCounter =  "+rowCounter);

        return rowCounter;
    }

    public int createSheetTitle(int rowCounter, HSSFSheet sheet, HSSFWorkbook workbook) {
        HSSFRow row = sheet.createRow(rowCounter++);
        this.cellMappers = new CellMapper();
        String title = "amis commodity balance sheet";
        HSSFCell cell = row.createCell((short) 0);
        cell.setCellStyle(AmisExcelUtils.getBoldTextCellStyle(workbook, null));
        cell.setCellValue(title.toUpperCase());

        rowCounter = AmisExcelUtils.createEmptyRow(rowCounter, sheet, workbook);

        return rowCounter;
    }


    public int createHeadersGroup(int rowCounter, HSSFSheet sheet, HSSFWorkbook workbook,
                                  LinkedHashMap<String, LinkedHashMap<String, DaoForecastValue>> mapGroup, String type) {

        String title;


        if (type == "foodBalance") {
            title = "National Marketing Year (NMY):";
            this.nationalCodes = urlGetter.getOperandsFormulaNational();
            this.FORMULA_URL = urlGetter.getFormulaNational();
            this.configurationReader = new ConfigurationReader(FORMULA_URL);
        } else if (type == "international") {
            LOGGER.info("----------------------------------------");
            LOGGER.info("INTERMATIONAL START;;;;;;;;;;;;;");
            this.FORMULA_URL = null;

            title = "International Trade Year (ITY):";
        } else {

            title = "Other";
            this.nationalCodes = urlGetter.getOperandsFormulaOthers();
            this.FORMULA_URL = urlGetter.getFormulaOthers();
            this.configurationReader = new ConfigurationReader(FORMULA_URL);

        }

        int columnNumber = 0;

        HSSFRow row = sheet.createRow(rowCounter++);
        HSSFCell cell = row.createCell((short) columnNumber);
        cell.setCellStyle(AmisExcelUtils.getBoldTextCellStyle(workbook, null));
        cell.setCellValue(title);
        columnNumber++;

        Set<String> dates = mapGroup.keySet();

        for (String date : dates) {

            columnNumber = createHeadersValues(date, columnNumber, row, workbook, sheet);
            columnNumber++;
        }

        rowCounter++;

        return rowCounter;
    }

    private int createHeadersValues(String date, int columnNumber, HSSFRow row, HSSFWorkbook workbook, HSSFSheet sheet) {

        String flags = "Forecasting Methodology";
        String notes = "Notes";


        row.setHeight((short) (3*260));
        HSSFCell cell = row.createCell((short) columnNumber);
        cell.setCellStyle(AmisExcelUtils.getBlueCellStyle(workbook));
        cell.setCellValue(date);
        sheet.autoSizeColumn(columnNumber);


        columnNumber++;

        HSSFCell cell2 = row.createCell((short) columnNumber);
        cell2.setCellStyle(AmisExcelUtils.getBlueCellStyle(workbook));
        cell2.setCellValue(flags);
        sheet.autoSizeColumn(columnNumber);


        columnNumber++;

        HSSFCell cell3 = row.createCell((short) columnNumber);
        cell3.setCellStyle(AmisExcelUtils.getBlueCellStyle(workbook));
        cell3.setCellValue(notes);
        sheet.autoSizeColumn(columnNumber);


        columnNumber++;

        return columnNumber;
    }

    public int createDataTableGroup(int rowCounter, HSSFSheet sheet, HSSFWorkbook workbook,
                                    HashMap<Integer, String> elements,
                                    LinkedHashMap<String, LinkedHashMap<String,
                                            DaoForecastValue>> foodBalanceResults) {

        List<String> datesList = null;
        Set<Integer> codes = elements.keySet();


        for (int code : codes) {

            int columnNumber = 0;

            HSSFRow row = sheet.createRow(rowCounter++);

            HSSFCell cell = row.createCell((short) columnNumber);
            cell.setCellStyle(AmisExcelUtils.getGreyCellStyle(workbook));
            cell.setCellValue(elements.get(code));
            sheet.autoSizeColumn(columnNumber);

            columnNumber++;

            Set<String> dates = foodBalanceResults.keySet();

            int length = dates.size();

            int i=0;

            datesList = new LinkedList<String>();

            for (String date : dates) {
                columnNumber = fillForecastElements(columnNumber,row,workbook,foodBalanceResults.get(date),code,sheet,date);
                columnNumber++;
                i++;
                datesList.add(date);
            }

        }

        if(this.FORMULA_URL != null) {
            for (String date : datesList) {
                LinkedList<FormulaBean> formulaBeans = this.configurationReader.getFormulas(this.nationalCodes);
                handleFormulas(formulaBeans, sheet, workbook, date);
            }
        }

        return rowCounter;
    }

    private int fillForecastElements(int columnNumber, HSSFRow row, HSSFWorkbook workbook, LinkedHashMap<String,
            DaoForecastValue> elements, int code, HSSFSheet sheet, String date) {


        DaoForecastValue forecast = elements.get("" + code);


        if(forecast != null) {
            // value

            int value =  (int)forecast.getValue();
            HSSFCell cell = row.createCell((short) columnNumber);
            cell.setCellStyle(AmisExcelUtils.getBasicCellStyle(workbook));
            if(value == -1){
                cell.setCellValue("");
            }else {
                cell.setCellValue(value);
            }
            String indexLetter = CellReference.convertNumToColString(columnNumber) + ""+(cell.getRowIndex()+1);
            cellMappers.putData(date,""+code,"value",indexLetter);

            columnNumber++;

            // flags
            HSSFCell cell1 = row.createCell((short) columnNumber);
            cell1.setCellStyle(AmisExcelUtils.getBasicCellStyle(workbook));
            cell1.setCellValue(forecast.getFlags());

            String indexLetter1 = CellReference.convertNumToColString(columnNumber) + ""+(cell.getRowIndex()+1);
            cellMappers.putData(date, "" + code, "flags", indexLetter1);

            columnNumber++;

            // notes
            HSSFCell cell2 = row.createCell((short) columnNumber);
            cell2.setCellStyle(AmisExcelUtils.getBasicCellStyle(workbook));
            cell2.setCellValue(forecast.getNotes());
            sheet.autoSizeColumn(columnNumber);

            String indexLetter2 = CellReference.convertNumToColString(columnNumber) + ""+(cell.getRowIndex()+1);
            cellMappers.putData(date, "" + code, "notes", indexLetter2);

            columnNumber++;
        }
        else
        {

            HSSFCell cell = row.createCell((short) columnNumber);
            cell.setCellStyle(AmisExcelUtils.getBasicCellStyle(workbook));
            cell.setCellValue("");

            String indexLetter = CellReference.convertNumToColString(columnNumber) + ""+(cell.getRowIndex()+1);
            cellMappers.putData(date, "" + code, "value", indexLetter);

            columnNumber++;

            // flags
            HSSFCell cell1 = row.createCell((short) columnNumber);
            cell1.setCellStyle(AmisExcelUtils.getBasicCellStyle(workbook));
            cell1.setCellValue("");

            String indexLetter1 = CellReference.convertNumToColString(columnNumber) + ""+(cell.getRowIndex()+1);
            cellMappers.putData(date, "" + code, "flags", indexLetter1);

            columnNumber++;

            // notes
            HSSFCell cell2 = row.createCell((short) columnNumber);
            cell2.setCellStyle(AmisExcelUtils.getBasicCellStyle(workbook));
            cell2.setCellValue("");

            String indexLetter2 = CellReference.convertNumToColString(columnNumber) + ""+(cell.getRowIndex()+1);
            cellMappers.putData(date, "" + code, "notes", indexLetter2);

            columnNumber++;

        }

        return columnNumber;
    }


    private void handleFormulas(LinkedList<FormulaBean> formulaBeans, HSSFSheet sheet, HSSFWorkbook wb, String date){

        LinkedHashMap<String,String> mapper = cellMappers.getMapCells();

        LOGGER.info("CELLMAPPERS");
        LOGGER.info(cellMappers);



        for(FormulaBean formulaBean: formulaBeans){
            String codeOperand = formulaBean.getOperand();
            LOGGER.info("codeOperand");
            LOGGER.info(codeOperand);

            String operandCodeValue = mapper.get(date+"*"+codeOperand+"*"+"value");
            LOGGER.info(operandCodeValue);

            String operandCodeFlags = mapper.get(date+"*"+codeOperand+"*"+"flags");
            LOGGER.info(operandCodeFlags);


            LinkedList<String> addendumsCodes = new LinkedList<String>();
            String operator = formulaBean.getOperator();
            LOGGER.info(operator);


            for (String addendum: formulaBean.getAddendums()){
                addendumsCodes.add(mapper.get(date+"*"+addendum+"*"+"value"));
            }
            LOGGER.info(addendumsCodes.toString());
            makeFormula(operandCodeFlags, operandCodeValue, operator, addendumsCodes, sheet);

        }
    }

    private void makeFormula(String operandCodeFlags, String operandCodeValue,
                             String operator,  LinkedList<String> addendumsCodes,
                             HSSFSheet sheet){

        LOGGER.info("StartedMAKE FORMULA");

        Cell operandCellValue = null;
        Cell operandCellFlag = null;


        CellReference referenceValue = new CellReference(operandCodeValue);
        Row rowIndex = sheet.getRow(referenceValue.getRow());
        if (rowIndex != null) {
            operandCellValue = rowIndex.getCell(referenceValue.getCol());
        }

        String formula="";

        int lengthAddendums = addendumsCodes.size();

        boolean operandsExist = true;

        int i= 0;
        for(String addendumCode: addendumsCodes){

            if(addendumCode.equals("B19")){
                System.out.println("vedemo!");
            }

            CellReference tempReference = new CellReference(operandCodeValue);
            Row tempRow= sheet.getRow(tempReference.getRow());
            Cell tempCell = tempRow.getCell(tempReference.getCol());

            if(tempCell.getCellType() == 1){
                System.out.println("qui!!!");
                System.out.println(tempCell.getStringCellValue());
               operandsExist = false;
               break;
            }


            formula+=addendumCode;


            if(i != lengthAddendums-1){
                formula+=operator;
            }
            i++;
        }

        if(operandsExist) {
            operandCellValue.setCellFormula(formula);

            // set the flag to C


            CellReference referenceFlags = new CellReference(operandCodeFlags);
            Row rowIndexFlag = sheet.getRow(referenceFlags.getRow());
            if (rowIndexFlag != null) {
                operandCellFlag = rowIndexFlag.getCell(referenceFlags.getCol());
            }

            operandCellFlag.setCellValue("C");
        }

    }


}

