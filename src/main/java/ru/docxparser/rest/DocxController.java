package ru.docxparser.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.docxparser.service.DocumentComparisonService;
import ru.docxparser.service.DocumentJsonComparisonService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class DocxController {

    private final DocumentComparisonService documentComparisonService;
    private final DocumentJsonComparisonService documentJsonComparisonService;
    private final ResourceLoader resourceLoader;

    @Autowired
    public DocxController(DocumentComparisonService documentComparisonService,
                          DocumentJsonComparisonService documentJsonComparisonService,
                          ResourceLoader resourceLoader) {
        this.documentComparisonService = documentComparisonService;
        this.documentJsonComparisonService = documentJsonComparisonService;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/compareWithJson")
    public String compareWithJsonPage() {
        return "compareWithJson";
    }

    @GetMapping("/generateJson")
    public String generateJsonPage() {
        return "generateJson";
    }

    @PostMapping("/compare")
    public String compareDocuments(@RequestParam("file1") MultipartFile file1,
                                   @RequestParam("file2") MultipartFile file2,
                                   Model model) throws IOException {
        XWPFDocument doc1 = new XWPFDocument(file1.getInputStream());
        XWPFDocument doc2 = new XWPFDocument(file2.getInputStream());

        boolean areEqual = documentComparisonService.compareDocuments(doc1, doc2);
        model.addAttribute("areEqual", areEqual);
        return "result";
    }

    @PostMapping("/compareWithJson")
    public String compareDocumentWithJson(@RequestParam("file") MultipartFile file,
                                          @RequestParam("configFile") MultipartFile configFile,
                                          Model model) throws IOException {
        XWPFDocument doc = new XWPFDocument(file.getInputStream());
        boolean areEqual = documentJsonComparisonService.compareDocumentWithJson(doc, configFile);

        if (!areEqual) {
            model.addAttribute("areEqual", false);
            model.addAttribute("message", "Документ не содержит все необходимые поля.");
            return "result";
        }

        model.addAttribute("areEqual", true);
        return "result";
    }

    @PostMapping("/generateJson")
    public String generateJson(@RequestParam("fields") List<String> fields, Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Map<String, List<String>> config = Map.of("fields", fields);
        String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);

        File file = new File("generated_config.json");
        System.out.println("JSON content: \n" + jsonContent);
        System.out.println("Saving JSON to: " + file.getAbsolutePath());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonContent);
        }

        model.addAttribute("message", "Конфигурационный файл успешно создан!");
        model.addAttribute("jsonContent", jsonContent);
        return "generateJsonResult";
    }

    @GetMapping("/downloadJson")
    public ResponseEntity<Resource> downloadJson() throws IOException {
        File file = new File("generated_config.json");
        Resource resource = resourceLoader.getResource("file:" + file.getAbsolutePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated_config.json")
                .body(resource);
    }
}


