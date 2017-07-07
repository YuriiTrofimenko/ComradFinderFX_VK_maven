/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.tyaa.comradfinder.model.VKCandidate;
import org.tyaa.comradfinder.modules.interfaces.IResultSaver;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * Поиск кандидатов на приглашение с сохранением в таблицу Excel
 * 
 * @author Yurii
 */
public class ExcelSaver implements IResultSaver {
    
    private final String FILE_PATH_BASE =
        "ExcellResults/TodayResult_";
    private Calendar mCalendar;

    @Override
    public boolean saveCandidates(List<VKCandidate> _candidatesList
        , String _filePath) throws IOException {
        
        boolean result = false;
        
        try (Workbook book = new XSSFWorkbook()) {
            
            //Создаем лист Excel
            Sheet sheet = book.createSheet("Кандидаты на приглашение в сообщество");
            
            // Строка заголовка
            Row mainHeaderRow = sheet.createRow(0);
            
            CellStyle rowStyle = book.createCellStyle();
            rowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            rowStyle.setFillPattern(CellStyle.SOLID_FOREGROUND); 
            
            Cell candidates = mainHeaderRow.createCell(0);
            candidates.setCellValue("Кандидаты");
            
            Cell currentdate = mainHeaderRow.createCell(1);
            
            DataFormat format = book.createDataFormat();
            CellStyle dateStyle = book.createCellStyle();
            dateStyle.setDataFormat(format.getFormat("dd.mm.yyyy"));
            
            currentdate.setCellStyle(dateStyle);
            
            currentdate.setCellValue(Date.from(Instant.now()));
            
            //Строка заголовков колонок
            Row headersRow = sheet.createRow(1);
            
            Cell idHeader = headersRow.createCell(0);
            idHeader.setCellValue("ID");
            idHeader.setCellStyle(rowStyle);
            
            Cell nameHeader = headersRow.createCell(1);
            nameHeader.setCellValue("Имя");
            nameHeader.setCellStyle(rowStyle);
            
            Cell scoreHeader = headersRow.createCell(2);
            scoreHeader.setCellValue("Баллы");
            scoreHeader.setCellStyle(rowStyle);
            
            //Строки данных
            int dataRowIdx = 2;
            for(VKCandidate vKCandidate : _candidatesList) {
                
                Row dataRow = sheet.createRow(dataRowIdx);
                
                Cell idData = dataRow.createCell(0);
                idData.setCellValue(vKCandidate.getUID());
                
                Cell nameData = dataRow.createCell(1);
                nameData.setCellValue(
                    vKCandidate.getFirstName()
                        + " "
                        + vKCandidate.getLastName()
                );
                
                Cell scoreData = dataRow.createCell(2);
                scoreData.setCellValue(vKCandidate.getScore());
                
                dataRowIdx++;
            }
            
            // Меняем размер столбцов
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            
            // Записываем книгу в файл
            
            try (FileOutputStream fileOutputStream =
                new FileOutputStream(_filePath)) {
                
                book.write(fileOutputStream);
                result = true;
                
            } catch(Exception ex){
            
                result = false;
            }
        }
        
        return result;
    }

    @Override
    public boolean saveCandidates(List<VKCandidate> _candidatesList) throws IOException
    {
        
        mCalendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        File directory1 = new File(String.valueOf("ExcellResults"));
        if (!directory1.exists()){
            directory1.mkdir();
        }

        /*File directory2 = new File(String.valueOf("C:/VKParserDoc/ExcellResults"));
        if (!directory2.exists()){
            directory2.mkdir();
        }*/
        
        return saveCandidates(_candidatesList
            , FILE_PATH_BASE
                + simpleDateFormat.format(mCalendar.getTime())
                + ".xlsx"
        );
    }
}
