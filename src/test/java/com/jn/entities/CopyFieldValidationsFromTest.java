package com.jn.entities;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Mede de onde vêm as regras de validação de cada campo de uma entidade.
 *
 * <p>A dúvida registrada em {@code JnEntityLoginTokenRequestResend} era se o motor estaria sempre
 * copiando as regras de {@code JnJsonCommonsFields}, independentemente do que cada campo declara. A
 * entidade fictícia {@link FakeEntityCopyFieldValidations} responde a isso com dois campos de mesmo
 * nome que os de lá e procedência oposta, e as duas provas são simétricas:
 *
 * <ul>
 * <li>o {@code email} aceita valores que {@code JnJsonCommonsFields.email} recusaria — logo a cópia
 * <b>não</b> acontece onde não foi pedida;</li>
 * <li>o {@code password} recusa valores que passariam se o campo estivesse sem regra — logo a cópia
 * <b>acontece</b> onde foi pedida.</li>
 * </ul>
 *
 * <p>A validação é disparada por {@code ENTITY.validateJson}, e não montando o json na mão para o
 * motor: é o decorator de {@code @CcpEntityFieldsValidator} que se quer exercitar, que é por onde
 * qualquer gravação de entidade passa.
 */
public class CopyFieldValidationsFromTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String EMAIL_DE_VERDADE = "onias85@gmail.com";

	private static final String SENHA_FORTE = "Senha#12345";

	// ── email: vale o que está escrito no próprio campo ───────────────────────

	/**
	 * Dez caracteres que não formam um e-mail. Passa pela regra própria do campo (10 a 500) e seria
	 * recusado pela expressão regular de {@code JnJsonCommonsFields.email}.
	 */
	@Test
	public void emailAceitaTextoQueNaoEhEmail() {
		this.deveSerAceito("abcdefghij", SENHA_FORTE);
	}

	/**
	 * Duzentos caracteres. Passa pelo limite próprio de 500 e seria recusado pelo limite de 100 de
	 * {@code JnJsonCommonsFields.email}. Prova a mesma coisa que o teste acima, sem depender da
	 * expressão regular.
	 */
	@Test
	public void emailAceitaTextoMaiorQueOLimiteDoCentralizador() {
		String duzentosCaracteres = "a".repeat(200);
		this.deveSerAceito(duzentosCaracteres, SENHA_FORTE);
	}

	/** A regra própria vale inteira, e não só na parte que afrouxa: três caracteres são poucos. */
	@Test
	public void emailRecusaTextoMenorQueOSeuProprioMinimo() {
		this.deveSerRecusado("abc", SENHA_FORTE, JnJsonCommonsFields.email.name());
	}

	/** E o limite próprio de 500 também é cobrado. */
	@Test
	public void emailRecusaTextoMaiorQueOSeuProprioMaximo() {
		String quinhentosEUmCaracteres = "a".repeat(501);
		this.deveSerRecusado(quinhentosEUmCaracteres, SENHA_FORTE, JnJsonCommonsFields.email.name());
	}

	// ── password: vale o que veio de JnJsonCommonsFields ──────────────────────

	/**
	 * O campo não declara regra nenhuma. Sem a cópia ele aceitaria qualquer texto; a recusa da senha
	 * fraca é o que mostra que a regra de {@code JnJsonCommonsFields.password} chegou até aqui.
	 */
	@Test
	public void passwordRecusaSenhaFraca() {
		this.deveSerRecusado(EMAIL_DE_VERDADE, "fraca", JnJsonCommonsFields.password.name());
	}

	/** E aceita a senha que satisfaz aquela regra: maiúscula, minúscula, dígito, símbolo e oito. */
	@Test
	public void passwordAceitaSenhaForte() {
		this.deveSerAceito(EMAIL_DE_VERDADE, SENHA_FORTE);
	}

	// ── e o email de verdade passa nas duas leituras, por isso não prova nada ──

	/**
	 * Registrado para deixar claro por que os demais testes usam valores esquisitos: um e-mail de
	 * verdade passa tanto pela regra própria quanto pela do centralizador, e portanto não distingue
	 * uma da outra.
	 */
	@Test
	public void emailDeVerdadePassaPelasDuasRegrasEPorIssoNaoDistingueNada() {
		this.deveSerAceito(EMAIL_DE_VERDADE, SENHA_FORTE);
	}

	// ── mecânica ──────────────────────────────────────────────────────────────

	private void deveSerAceito(String email, String password) {
		CcpJsonRepresentation json = this.json(email, password);
		FakeEntityCopyFieldValidations.ENTITY.validateJson(json);
	}

	private void deveSerRecusado(String email, String password, String campoEsperado) {

		CcpJsonRepresentation json = this.json(email, password);

		try {
			FakeEntityCopyFieldValidations.ENTITY.validateJson(json);
		} catch (CcpJsonValidationError e) {
			String mensagem = e.getMessage();
			boolean acusaOCampoCerto = mensagem.contains(campoEsperado);
			assertTrue("o erro deveria acusar o campo " + campoEsperado + ", mas veio: " + mensagem, acusaOCampoCerto);
			return;
		}
		fail("o json deveria ter sido recusado por causa do campo " + campoEsperado);
	}

	private CcpJsonRepresentation json(String email, String password) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, email)
				.put(JnJsonCommonsFields.password, password);
		return json;
	}
}
