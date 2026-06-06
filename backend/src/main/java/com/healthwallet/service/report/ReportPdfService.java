package com.healthwallet.service.report;

import com.healthwallet.model.Report;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Responsabilidade única: renderizar o conteúdo de um relatório em PDF.
 * Mantém a geração de PDF isolada do ReportService (SRP), espelhando a
 * extração feita em VaccineAttachmentService.
 */
@Service
public class ReportPdfService {

    public byte[] generate(Report report) {
        String content = report.getObservations() != null ? report.getObservations() : "";

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font font = FontFactory.getFont(FontFactory.COURIER, 11);
            for (String line : content.split("\n", -1)) {
                // OpenPDF ignora parágrafos vazios; usa um espaço para preservar a quebra de linha
                document.add(new Paragraph(line.isEmpty() ? " " : line, font));
            }

            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF do relatório", e);
        }

        return out.toByteArray();
    }
}
