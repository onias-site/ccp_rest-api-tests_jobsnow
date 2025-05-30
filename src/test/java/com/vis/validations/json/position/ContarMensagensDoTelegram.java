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
import com.ccp.utils.CcpHashAlgorithm;

public class ContarMensagensDoTelegram {

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

	static int totalDeAceitos = 0;
	static int totalGeral = 0;
	
	public static void main(String[] args)  {
		File pasta = new File("C:\\jn\\chats");
		CcpFileDecorator arquivoDeSaida = new CcpStringDecorator("C:\\jn\\saida.html").file().reset();
		Set<String> hashes = new HashSet<>();
		File[] listFiles = pasta.listFiles();
		for (File arquivo : listFiles) {
			String absolutePath = arquivo.getAbsolutePath();
			List<String> lines1 = new CcpStringDecorator(absolutePath).file().getLines().stream()
					.map(line -> sanitizarLinha(line))
					.filter(x -> x.length() > 135)
					.collect(Collectors.toList());
			
			List<String> lines2 = lines1.stream().filter(x -> hashes.add(getHash(x))).collect(Collectors.toList());
			
			String nomeDoArquivo = arquivo.getName();
			double size1 = lines1.size();
			double size2 = lines2.size();
			int porcentagemDeRecusa = (int)(((size1 - size2)/size1) * 100);
			totalDeAceitos += size2;
			totalGeral += size1;
			String format = String.format("Arquivo: %s, linhas: %s, filtradas: %s, porcentagem de recusa: %s, total geral: %s", 
					nomeDoArquivo 
					,size1
					,size2
					,porcentagemDeRecusa
					,hashes.size()
					);
			for (String line : lines2) {
				arquivoDeSaida.append(line);
			}
//			CcpTimeDecorator.appendLog(format);
			System.out.println(format);
		}
//		CcpTimeDecorator.appendLog("Total geral: " + totalGeral + ". Total de aceitos: " + totalDeAceitos);
	}
	private static String sanitizarLinha(String line) {
		
		String trim = removerSujeiraGrossa(line);
		
		String cleaned = 
		new CcpStringDecorator(trim)
		.text()
		.removePieces("<", ">")
		.replace("http", " http")
		.removePieces(str -> str.toUpperCase().startsWith("HTTP"), " ")
		.content
		.trim()
		;
		
		boolean ehVagaDoJobsNow = ehVagaDoJobsNow(cleaned);
		
		String str = "#jnVaga";
		if(cleaned.contains(str)) {
			int jnVagaIndex = cleaned.indexOf(str);
			cleaned = cleaned.substring(jnVagaIndex+ str.length());
		}

		if(ehVagaDoJobsNow) {
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
	private static boolean ehVagaDoJobsNow(String cleaned) {
		String stripAccents = new CcpStringDecorator(cleaned).text().stripAccents().content;
		
		boolean naoTemPropaganda = stripAccents.toLowerCase().contains("propaganda numero") == false;
		
		if(naoTemPropaganda) {
			return false;
		}
		
		boolean naoTemDataDeExpiracao = stripAccents.toLowerCase().contains("esta vaga expira em") == false;
		
		if(naoTemDataDeExpiracao) {
			return false;
		}

		return true;
	}
	private static String removerSujeiraGrossa(String line) {
		String regexDasTagsHtml = "(<([a-z]+)(?![^>]*\\/>)[^>]*>)|(</([a-z]+)(?![^>]*\\/>)[^>]*>)";
		String regexDosEmojis = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
		String trim = line
				.replaceAll(regexDasTagsHtml, " ")
				.replaceAll(regexDosEmojis, " ")
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
		String delimitadores = "\\/|\\s|\n|\\:|\\,|\\;|\\!|\\?|\\[|\\]|\\{|\\}|\\<|\\>|\\=|\\(|\\)\\ |\\'|\\\"|\\`";
		String[] split = line.split(delimitadores);
		List<String> asList = Arrays.asList(split).stream().map(x -> transform(x)).filter(x -> x.length() > 2)
				.collect(Collectors.toList());
		TreeSet<String> treeSet = new TreeSet<String>(asList);
		String hash = new CcpStringDecorator(treeSet.toString()).hash().asString(CcpHashAlgorithm.SHA1);
		return hash;
	}
}