package com.ccp.implementations.text.extractor.apache.tika;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.ccp.especifications.text.extractor.CcpTextExtractor;

/**
 * Implementação de {@code CcpTextExtractor} usando o Apache Tika com detecção automática de
 * formato. Recebe o conteúdo codificado em Base64, decodifica e extrai o texto puro via
 * {@code BodyContentHandler}.
 */
class ApacheTikaTextExtractor implements CcpTextExtractor {

	public String extractText(String content) {
		
		ParseContext context = new ParseContext();
		Detector detector = new DefaultDetector();
		Parser parser = new AutoDetectParser(detector);
		context.set(Parser.class, parser);
		Metadata metadata = new Metadata();
		
		try(InputStream is = this.getParameterAsByteArrayInputStream(content);OutputStream outputstream = new ByteArrayOutputStream();) {
			ContentHandler handler = new BodyContentHandler(outputstream);
			parser.parse(is, handler, metadata, context);
			String extractedText = outputstream.toString();
			return extractedText;
		} catch (Exception e) {
			CcpErrorApacheTikaExtraction ccpErrorApacheTikaExtraction = new CcpErrorApacheTikaExtraction(e);
			throw ccpErrorApacheTikaExtraction;
		}

	}
	
	private ByteArrayInputStream getParameterAsByteArrayInputStream(String content) {

		byte[] byteArray = this.getByteArrayFromBase64String(content);

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);

		return byteArrayInputStream;

	}

	private byte[] getByteArrayFromBase64String(String content) {
		String[] split = content.split(",");
		String str = split[0];
		boolean lengthMaior = split.length > 1;

		if (lengthMaior) {
			str = split[1];
		}

		String base64 = str;

		Decoder decoder = Base64.getDecoder();

		byte[] byteArray = decoder.decode(base64);
		return byteArray;
	}


	@SuppressWarnings("serial")
	private static class CcpErrorApacheTikaExtraction extends RuntimeException {
		private CcpErrorApacheTikaExtraction(Throwable cause) {
			super(cause);
		}
	}
}
