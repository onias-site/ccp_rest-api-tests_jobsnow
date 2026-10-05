package com.vis.validations.json.position;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.hash.CcpHashAlgorithm;

public class CountTelegramMessages {

	public static String transform(String palavra) {
		String s = "";
		char[] charArray = new CcpStringDecorator(palavra).text().stripAccents().content.toUpperCase().toCharArray();

		for (char c : charArray) {
			if (c < '0') {
				continue;
			}
			if (c > 'Z') {
				continue;
			}
			s += c;
		}
		return s;
	}

	static int totalAccepted = 0;
	static int totalGeral = 0;
	
	public static void main(String[] args)  {
		File folder = new File("C:\\jn\\chats");
		CcpFileDecorator outputFile = new CcpStringDecorator("C:\\jn\\saida.html").file().reset();
		Set<String> hashes = new HashSet<>();
		File[] listFiles = folder.listFiles();
		for (File fileUnderTest : listFiles) {
			String absolutePath = fileUnderTest.getAbsolutePath();
			List<String> lines1 = new CcpStringDecorator(absolutePath).file().getLines().stream()
					.map(line -> sanitizeLine(line))
					.filter(x -> x.length() > 135)
					.collect(Collectors.toList());
			
			List<String> lines2 = lines1.stream().filter(x -> hashes.add(getHash(x))).collect(Collectors.toList());
			
			String fileName = fileUnderTest.getName();
			double size1 = lines1.size();
			double size2 = lines2.size();
			int refusalPercentage = (int)(((size1 - size2)/size1) * 100);
			totalAccepted += size2;
			totalGeral += size1;
			String format = String.format("Arquivo: %s, linhas: %s, filtradas: %s, porcentagem de recusa: %s, total geral: %s", 
					fileName 
					,size1
					,size2
					,refusalPercentage
					,hashes.size()
					);
			for (String line : lines2) {
				outputFile.append(line);
			}
//			CcpTimeDecorator.appendLog(format);
			System.out.println(format);
		}
//		CcpTimeDecorator.appendLog("Total geral: " + totalGeral + ". Total de aceitos: " + totalDeAceitos);
	}
	private static String sanitizeLine(String line) {
		
		String trim = removeCoarseDirt(line);
		
		String cleaned = 
		new CcpStringDecorator(trim)
		.text()
		.removePieces("<", ">")
		.replace("http", " http")
		.removePieces(str -> str.toUpperCase().startsWith("HTTP"), " ")
		.content
		.trim()
		;
		
		boolean isJobsNowPosition = isJobsNowPosition(cleaned);
		
		String str = "#jnVaga";
		if(cleaned.contains(str)) {
			int jnPositionIndex = cleaned.indexOf(str);
			cleaned = cleaned.substring(jnPositionIndex+ str.length());
		}

		if(isJobsNowPosition) {
			int indexOf = cleaned.indexOf("Esta vaga expira em");
			cleaned = cleaned.substring(0, indexOf);
		}
		if(cleaned.length() < 135) {
			return cleaned;
		}
		String substring = cleaned.substring(0, 6);
		try {
			Integer.valueOf(substring);
			return cleaned.substring(8);
		} catch (Exception e) {
			return cleaned;
		}
	}
	private static boolean isJobsNowPosition(String cleaned) {
		String stripAccents = new CcpStringDecorator(cleaned).text().stripAccents().content;
		
		boolean hasNoAdvertising = false == stripAccents.toLowerCase().contains("propaganda numero");
		
		if(hasNoAdvertising) {
			return false;
		}
		
		boolean hasNoExpirationDate = false == stripAccents.toLowerCase().contains("esta vaga expira em");
		
		if(hasNoExpirationDate) {
			return false;
		}

		return true;
	}
	private static String removeCoarseDirt(String line) {
		String htmlTagsRegex = "(<([a-z]+)(?![^>]*\\/>)[^>]*>)|(</([a-z]+)(?![^>]*\\/>)[^>]*>)";
		String emojisRegex = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
		String trim = line
				.replaceAll(htmlTagsRegex, " ")
				.replaceAll(emojisRegex, " ")
				.trim()
				.replace("•", " ")
				.replace("__", " ")
				.replace("---", " ")
				.replace("[PUBLIQUE ESTA VAGA NO LINKEDIN]", "")
				.replace("Forwarded message", " ")
				.trim()
				;
		return trim;
	}
	private static String getHash(String line) {
		String delimiters = "\\/|\\s|\n|\\:|\\,|\\;|\\!|\\?|\\[|\\]|\\{|\\}|\\<|\\>|\\=|\\(|\\)\\ |\\'|\\\"|\\`";
		String[] split = line.split(delimiters);
		List<String> asList = Arrays.asList(split).stream().map(x -> transform(x)).filter(x -> x.length() > 2)
				.collect(Collectors.toList());
		TreeSet<String> treeSet = new TreeSet<String>(asList);
		String hash = new CcpStringDecorator(treeSet.toString()).hash().asString(CcpHashAlgorithm.SHA1);
		return hash;
	}
}