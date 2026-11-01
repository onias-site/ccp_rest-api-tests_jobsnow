package com.vis.business.telegram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntityTelegramPosition;
import com.vis.json.transformers.VisSeniorityType;

/** The rules that turn a message of Telegram into a position, none of which reads the database. */
public class VisTelegramTextRulesTest {

	@BeforeClass
	public static void loadDependencies() {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static String normalized(String text) {
		return VisTelegramPositionText.normalize(text);
	}

	@Test
	public void theDateOfEachMessageIsTheOnePrintedAndAMessageWithNoTextIsLeftOut() {
		String html = "<div class=\"pull_right date details\" title=\"29.04.2021 14:00:51 GMT-03:00\">x</div>"
				+ "<div class=\"text\">Dev <strong>Java</strong><br>a &amp; b</div>"
				+ "<div class=\"pull_right date details\" title=\"30.04.2021 10:00:00 GMT-03:00\">x</div>"
				+ "<div class=\"media\">only a photo</div>"
				+ "<div class=\"pull_right date details\" title=\"01.05.2021 10:00:00 GMT-03:00\">x</div>"
				+ "<div class=\"text\"><a href=\"https://empresa.com/vaga\">clique</a></div>";
		List<CcpJsonRepresentation> messages = VisTelegramMessages.read(html);
		assertEquals(2, messages.size());
		CcpJsonRepresentation first = messages.get(0);
		assertEquals("29042021", first.getAsString(JnJsonCommonsFields.disposablePeriod));
		assertEquals("202104", first.getAsString(VisEntityTelegramPosition.Fields.yearMonth));
		assertTrue(first.getAsString(CcpJsonCommonsFields.text).contains("Java"));
		assertTrue(first.getAsString(CcpJsonCommonsFields.text).contains("a & b"));
		assertTrue(messages.get(1).getAsString(CcpJsonCommonsFields.text).contains("https://empresa.com/vaga"));
	}

	@Test
	public void aMessageOfWhatsAppLosesItsAuthorAndIsHomeOffice() {
		String content = "21/11/2025 11:27 - G313 Rapha adicionou Rogerio\r\n"
				+ "21/11/2025 11:57 - +55 11 99999-8888: Dev Java\r\n\r\nrh@empresa.com\r\n"
				+ "22/11/2025 09:00 - \u200e~ Ana: Dev PHP remoto: fale comigo\r\n";
		List<CcpJsonRepresentation> messages = VisWhatsAppMessages.read(content);
		assertEquals(2, messages.size());
		CcpJsonRepresentation first = messages.get(0);
		assertEquals("Dev Java\r\n\r\nrh@empresa.com", first.getAsString(CcpJsonCommonsFields.text));
		assertEquals("21112025", first.getAsString(JnJsonCommonsFields.disposablePeriod));
		assertEquals("202511", first.getAsString(VisEntityTelegramPosition.Fields.yearMonth));
		assertEquals(Integer.valueOf(10), first.getAsIntegerNumber(VisEntityTelegramPosition.Fields.ddd));
		assertEquals("Dev PHP remoto: fale comigo", messages.get(1).getAsString(CcpJsonCommonsFields.text));
	}

	@Test
	public void theHeaderGoesBeforeTheFooterIsLookedFor() {
		String text = "[ENCAMINHE ESTA VAGA EM TODOS OS GRUPOS]\nQuer que eu envie seu curriculo?\n[vaga]\nDev Java\nrh@empresa.com\nVaga Publicada por Deia";
		assertEquals("Dev Java\nrh@empresa.com", VisTelegramPositionText.cut(text));
	}

	@Test
	public void theWarningIsAHeaderAndTheMisspelledHashtagAFooter() {
		String text = "Avisos:\n1) Envio teu curriculo\n2) Se esta vaga nao te serviu, encaminhe-a!!!\nDev PHP\n#jnPopaganda\nCurso de Java";
		assertEquals("Dev PHP", VisTelegramPositionText.cut(text));
	}

	@Test
	public void theHashtagOfThePositionIsAHeader() {
		assertEquals("Dev Go", VisTelegramPositionText.cut("Canal X #jnVaga Dev Go #jnDica nada"));
	}

	@Test
	public void theLinesOfTheChannelsLeaveTheirEmailBehind() {
		String text = "#jnVaga\n[PUBLIQUE ESTA VAGA NO LINKEDIN]\n[RECRUTADOR, RECEBA CURRICULOS, ENVIE E-MAIL PARA dono@canal.com]\nDev Java\nsem contato";
		String position = VisTelegramPositionText.cut(text);
		assertFalse(position.contains("dono@canal.com"));
		assertTrue(position.contains("Dev Java"));
	}

	@Test
	public void aMessageThatIsOnlyAFooterComesOutEmpty() {
		assertEquals("", VisTelegramPositionText.cut("Divulgue conosco a custo zero!"));
	}

	@Test
	public void anEmailWinsOverALink() {
		assertEquals("rh@empresa.com", VisTelegramContact.find("veja https://empresa.com/vaga ou rh@empresa.com"));
	}

	@Test
	public void theLinksOfTelegramAndOfTheSocialNetworksAreNoContact() {
		assertEquals("https://empresa.com/vaga", VisTelegramContact.find("https://t.me/canal https://youtube.com/x https://empresa.com/vaga."));
	}

	@Test
	public void aLinkOfWhatsAppComesAfterTheOtherLinks() {
		assertEquals("https://empresa.com/vaga", VisTelegramContact.find("https://wa.me/5511999998888 https://empresa.com/vaga"));
		assertEquals("https://wa.me/5511999998888", VisTelegramContact.find("chame https://wa.me/5511999998888"));
	}

	@Test
	public void aNumberToldAsWhatsAppKeepsOnlyItsDigits() {
		assertEquals("11999998888", VisTelegramContact.find("Whatsapp: (11) 99999-8888"));
		assertEquals("11999998888", VisTelegramContact.find("(11) 99999-8888 no whats"));
	}

	@Test
	public void aPhoneIsTheLastContact() {
		assertEquals("1133334444", VisTelegramContact.find("ligue 11 3333-4444"));
	}

	@Test
	public void aTextWithNoContactComesOutEmpty() {
		assertEquals("", VisTelegramContact.find("Dev Java, 5 anos"));
	}

	@Test
	public void theLinksDoNotReachTheSearchOfTheSkills() {
		assertFalse(VisTelegramContact.removeLinks("Dev https://empresa.com/java-e-php").contains("java"));
	}

	@Test
	public void theFirstSeniorityMentionedWins() {
		assertEquals(VisSeniorityType.pleno, VisTelegramSeniority.find(normalized("Analista PL/SR")));
		assertEquals(VisSeniorityType.senior, VisTelegramSeniority.find(normalized("Dev Sênior ou pleno")));
		assertEquals(VisSeniorityType.estag, VisTelegramSeniority.find(normalized("Vaga de estágio")));
		assertEquals(VisSeniorityType.especialista, VisTelegramSeniority.find(normalized("Especialista em dados")));
	}

	@Test
	public void aMarkBetweenTwoWordsDoesNotGlueThem() {
		assertEquals(VisSeniorityType.senior, VisTelegramSeniority.find(normalized("Vaga:Sênior")));
		assertEquals(VisSeniorityType.junior, VisTelegramSeniority.find(normalized("Dev|Jr")));
		assertEquals(10, new VisTelegramDdd().find(normalized("Trabalho:remoto")));
	}

	@Test
	public void plSqlIsNoSeniority() {
		assertEquals(VisSeniorityType.unknown, VisTelegramSeniority.find(normalized("Dev PL/SQL e PL SQL")));
	}

	@Test
	public void aRemotePositionIsHomeOffice() {
		assertEquals(10, new VisTelegramDdd().find(normalized("Dev Java, 100% remoto, empresa em Barueri/SP")));
	}

	@Test
	public void aHybridPositionHasTheCityOfTheState() {
		assertEquals(11, new VisTelegramDdd().find(normalized("Modelo híbrido em Barueri/SP")));
		assertEquals(19, new VisTelegramDdd().find(normalized("Vaga em Campinas - SP")));
		assertEquals(31, new VisTelegramDdd().find(normalized("Atuação em Belo Horizonte (MG)")));
		assertEquals(11, new VisTelegramDdd().find(normalized("Local: Osasco \u2013 SP")));
	}

	@Test
	public void theNameOfACityMayBeOnlyTheEndOfTheWordsBeforeTheState() {
		assertEquals(13, new VisTelegramDdd().find(normalized("Vaga para atuar em Santos/SP")));
	}

	@Test
	public void aCityWithAnApostropheIsFound() {
		assertEquals(19, new VisTelegramDdd().find(normalized("Vaga em Santa Bárbara d'Oeste/SP")));
	}

	@Test
	public void aCommonWordOnlyTellsACityWithItsState() {
		assertEquals(0, new VisTelegramDdd().find(normalized("Vaga, tratar com Santos")));
	}

	@Test
	public void aBigCityNeedsNoState() {
		assertEquals(41, new VisTelegramDdd().find(normalized("Vaga presencial em Curitiba")));
		assertEquals(11, new VisTelegramDdd().find(normalized("Escritório em Alphaville")));
	}

	@Test
	public void aRemotePositionWithAHybridMentionAndNoCityIsHomeOffice() {
		assertEquals(10, new VisTelegramDdd().find(normalized("Remoto ou híbrido")));
	}

	@Test
	public void aTextWithNoPlaceHasNoDdd() {
		assertEquals(0, new VisTelegramDdd().find(normalized("Dev Java")));
	}

	private static CcpJsonRepresentation skill(int occurrences, int childOcurrences, int ranking) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisTelegramLoadFields.occurrences, occurrences)
				.put(VisEntitySkill.Fields.childOcurrences, childOcurrences)
				.put(VisEntitySkill.Fields.ranking, ranking);
	}

	@Test
	public void theRankingGoesByOccurrencesThenChildOcurrencesThenTheCurrentRanking() {
		VisSkillRankingComparator comparator = VisSkillRankingComparator.INSTANCE;
		assertTrue(comparator.compare(skill(5, 0, 9), skill(4, 99, 1)) < 0);
		assertTrue(comparator.compare(skill(5, 7, 9), skill(5, 3, 1)) < 0);
		assertTrue(comparator.compare(skill(5, 7, 1), skill(5, 7, 9)) < 0);
	}
}
