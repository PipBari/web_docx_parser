package ru.docxparser.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentComparisonService {

    public boolean compareDocuments(XWPFDocument doc1, XWPFDocument doc2) {
        List<String> doc1Fields = extractFields(doc1);
        List<String> doc2Fields = extractFields(doc2);
        return doc1Fields.equals(doc2Fields);
    }

    public List<String> extractFields(XWPFDocument doc) {
        List<String> fields = new ArrayList<>();

        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            fields.add(paragraph.getText());
        }

        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                String rowText = row.getTableCells().stream()
                        .map(cell -> cell.getText())
                        .collect(Collectors.joining(" | "));
                fields.add(rowText);
            }
        }

        return fields;
    }
}


