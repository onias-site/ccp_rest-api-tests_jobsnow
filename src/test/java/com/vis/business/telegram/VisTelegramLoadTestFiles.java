package com.vis.business.telegram;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpFolderDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.vis.entities.VisEntityTelegramPosition;

/**
 * A folder of files exported from Telegram (HTML) and WhatsApp (text) for the tests of the load, with contacts that only
 * this run has, so its positions and recruiters never collide with the real ones. One HTML file has a position, the same
 * position repeated on the same day, a message with no contact and one with no skill; the WhatsApp file has one position;
 * a JSON file in the folder is not read.
 */
final class VisTelegramLoadTestFiles {

	static final String HTML_FILE = "messages.html";

	static final String WHATSAPP_FILE = "Conversa do WhatsApp.txt";

	static final String IGNORED_FILE = "notes.json";

	/** The month of the positions: an old one, already in the statistics of the real load. */
	static final String YEAR_MONTH = "201910";

	final String run;

	final String folderPath;

	final String telegramContact;

	final String noSkillContact;

	final String whatsAppContact;

	VisTelegramLoadTestFiles() {
		this.run = new CcpStringDecorator("abcdefghijklmnopqrstuvwxyz").text().generateToken(10).content.toLowerCase();
		this.folderPath = "c:\\logs\\vis\\telegram-load-test\\" + this.run;
		this.telegramContact = "vagas.telegram." + this.run + "@empresateste.com.br";
		this.noSkillContact = "rh.semhabilidade." + this.run + "@empresateste.com.br";
		this.whatsAppContact = "vagas.whatsapp." + this.run + "@empresateste.com.br";
	}

	private static String htmlMessage(String date, String text) {
		String message = "<div class=\"pull_right date details\" title=\"" + date + " 10:00:00 GMT-03:00\">x</div>"
				+ "<div class=\"text\">" + text + "</div>";
		return message;
	}

	String telegramPositionText() {
		String text = "Desenvolvedor Java Senior presencial em Sao Paulo. Envie o curriculo para " + this.telegramContact;
		return text;
	}

	String whatsAppPositionText() {
		String text = "Desenvolvedor Java Senior\r\n\r\n" + this.whatsAppContact;
		return text;
	}

	String html() {
		String position = this.telegramPositionText();
		String html = htmlMessage("15.10.2019", position)
				+ htmlMessage("15.10.2019", position)
				+ htmlMessage("15.10.2019", "Desenvolvedor Java Senior, vaga sem nenhum contato informado")
				+ htmlMessage("16.10.2019", "Procuramos pessoa animada e proativa. Contato: " + this.noSkillContact);
		return html;
	}

	String whatsApp() {
		String content = "16/10/2019 09:00 - Fulano adicionou Beltrano\r\n"
				+ "16/10/2019 10:00 - +55 11 99999-8888: " + this.whatsAppPositionText() + "\r\n";
		return content;
	}

	/** Writes the three files in a new folder. */
	void write() {
		CcpStringDecorator folderPathDecorator = new CcpStringDecorator(this.folderPath);
		CcpFolderDecorator folder = folderPathDecorator.folder();
		folder.createFolderIfNotExists();
		folder.writeInTheFile(HTML_FILE, this.html());
		folder.writeInTheFile(WHATSAPP_FILE, this.whatsApp());
		folder.writeInTheFile(IGNORED_FILE, "{}");
	}

	String pathOf(String fileName) {
		CcpFileDecorator file = this.fileOf(fileName);
		String path = file.getPath();
		return path;
	}

	CcpFileDecorator fileOf(String fileName) {
		CcpStringDecorator filePath = new CcpStringDecorator(this.folderPath + "\\" + fileName);
		CcpFileDecorator file = filePath.file();
		return file;
	}

	/**
	 * The two positions the load must write, read from the files by the same reader of the load.
	 * @return the positions, ready for the id
	 */
	List<CcpJsonRepresentation> expectedPositions() {
		VisSkillWordsInMemory skillWords = new VisSkillWordsInMemory();
		VisTelegramDdd regions = new VisTelegramDdd();
		VisBusinessReadTelegramPosition reader = new VisBusinessReadTelegramPosition(skillWords, regions);
		List<CcpJsonRepresentation> messages = new ArrayList<>();
		List<CcpJsonRepresentation> telegramMessages = VisTelegramMessages.read(this.html());
		messages.add(telegramMessages.get(0));
		List<CcpJsonRepresentation> whatsAppMessages = VisWhatsAppMessages.read(this.whatsApp());
		messages.addAll(whatsAppMessages);
		List<CcpJsonRepresentation> positions = new ArrayList<>();
		for (CcpJsonRepresentation message : messages) {
			CcpJsonRepresentation position = reader.execute(message);
			positions.add(position);
		}
		return positions;
	}

	/** Removes the positions of the run from the index and the files left in the folder. */
	void deletePositionsAndFiles() {
		List<CcpJsonRepresentation> positions = this.expectedPositions();
		for (CcpJsonRepresentation position : positions) {
			VisEntityTelegramPosition.ENTITY.delete(position);
		}
		String[] fileNames = {HTML_FILE, WHATSAPP_FILE, IGNORED_FILE};
		for (String fileName : fileNames) {
			CcpFileDecorator file = this.fileOf(fileName);
			boolean gone = false == file.exists();
			if (gone) {
				continue;
			}
			file.remove();
		}
	}

	/** Removes the positions, the files and the folder of the run. */
	void deleteEverything() {
		this.deletePositionsAndFiles();
		CcpStringDecorator folderPathDecorator = new CcpStringDecorator(this.folderPath);
		CcpFolderDecorator folder = folderPathDecorator.folder();
		boolean noFolder = false == folder.exists();
		if (noFolder) {
			return;
		}
		folder.remove();
	}
}
