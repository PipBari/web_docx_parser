package ru.docxparser.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentJsonComparisonService {

    private final DocumentComparisonService documentComparisonService;

    public DocumentJsonComparisonService(DocumentComparisonService documentComparisonService) {
        this.documentComparisonService = documentComparisonService;
    }

    public boolean compareDocumentWithJson(XWPFDocument document, MultipartFile jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> jsonMap = mapper.readValue(jsonFile.getInputStream(), Map.class);
        List<String> jsonFields = jsonMap.get("fields");
        List<String> documentFields = extractFieldsFromDocument(document);
        for (String field : jsonFields) {
            if (!documentFields.contains(field)) {
                return false;
            }
        }

        return true;
    }

    private List<String> extractFieldsFromDocument(XWPFDocument document) {
        List<String> fields = new ArrayList<>();

        if (!document.getParagraphs().isEmpty()) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String paragraphText = paragraph.getText().trim();
                String[] parts = paragraphText.split("\t");
                if (parts.length > 0) {
                    String key = parts[0].trim();
                    fields.add(key);
                }
            }
        }

        return fields;
    }
}
