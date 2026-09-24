package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityContactUs;
import com.jn.entities.JnEntityContactUsIgnored;
import com.jn.entities.JnEntityJobsnowPenddingError;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.entities.VisEntityDeniedViewToCompany;
import com.vis.entities.VisEntityPosition;
import com.vis.entities.VisEntityResume;

/**
 * Cobre o {@code deleteAnyWhere} de todas as entidades anotadas com {@code @CcpEntityTwin}. A promessa
 * do método é apagar o registro esteja ele onde estiver, então cada entidade é exercitada duas vezes:
 * com o registro na entidade principal e com o registro na gêmea. Nos dois casos o método tem que
 * devolver que o registro existia e tem que deixar os dois índices sem ele.
 *
 * <p>Cada execução usa um e-mail novo, derivado do relógio. As entidades têm o e-mail na chave
 * primária, então isso dá um cenário limpo sem depender de faxina, e evita que as mensagens de texto
 * fixo — a que avisa o suporte sobre ticket pendente, por exemplo — sejam recusadas como repetição da
 * execução anterior.
 *
 * <p>O banco é o Elasticsearch de verdade. Telegram e provedor de email são dublês, porque gravar
 * algumas destas entidades dispara aviso ao suporte ou envio ao usuário.
 */
public class DeleteAnyWhereEmEntidadesTwinTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> new MensageiroSilencioso();
		CcpInstanceProvider<CcpEmailSender> email = () -> new CaixaDeEmailSilenciosa();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				new CcpApacheTikaTextExtractor(),
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private final String email = "twin" + System.currentTimeMillis() + "@jobsnow.com";

	// ── entidades sem efeito colateral na gravação ────────────────────────────

	@Test
	public void contactUs() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(JnJsonCommonsFields.subjectType, "duvida")
				.put(JnJsonCommonsFields.subject, "assunto de teste")
				.put(JnJsonInstantMessengerFields.chatId, 751717896L)
				.put(JnJsonCommonsFields.sender, "devs.jobsnow@gmail.com");

		this.deveApagarDosDoisIndices(JnEntityContactUs.ENTITY, registro);
	}

	@Test
	public void contactUsIgnored() {
		this.deveApagarDosDoisIndices(JnEntityContactUsIgnored.ENTITY, this.comEmail());
	}

	@Test
	public void jobsnowPenddingError() {
		CcpJsonRepresentation registro = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityJobsnowPenddingError.Fields.stackTraceHash, this.email)
				.put(JnEntityJobsnowPenddingError.Fields.type, "java.lang.RuntimeException")
				.put(JnJsonCommonsFields.message, "erro de teste");

		this.deveApagarDosDoisIndices(JnEntityJobsnowPenddingError.ENTITY, registro);
	}

	@Test
	public void loginPassword() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(JnJsonCommonsFields.password, "Senha#12345");

		this.deveApagarDosDoisIndices(JnEntityLoginPassword.ENTITY, registro);
	}

	@Test
	public void loginSessionValidation() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(JnEntityLoginSessionValidation.Fields.token, "TOKEN123")
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				.put(JnJsonCommonsFields.userAgent, "junit");

		this.deveApagarDosDoisIndices(JnEntityLoginSessionValidation.ENTITY, registro);
	}

	@Test
	public void deniedViewToCompany() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(VisEntityDeniedViewToCompany.Fields.domain, "jobsnow.com")
				.put(VisEntityDeniedViewToCompany.Fields.reasonType, "teste")
				.put(VisEntityDeniedViewToCompany.Fields.reasonText, "motivo de teste");

		this.deveApagarDosDoisIndices(VisEntityDeniedViewToCompany.ENTITY, registro);
	}

	// ── entidades que avisam alguém ao gravar ─────────────────────────────────

	@Test
	public void loginToken() {
		this.deveApagarDosDoisIndices(JnEntityLoginToken.ENTITY, this.comEmail());
	}

	@Test
	public void loginTokenRequestResend() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		this.deveApagarDosDoisIndices(JnEntityLoginTokenRequestResend.ENTITY, registro);
	}

	@Test
	public void loginTokenRequestUnlock() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);

		this.deveApagarDosDoisIndices(JnEntityLoginTokenRequestUnlock.ENTITY, registro);
	}

	// ── entidades com muitos campos obrigatórios ─────────────────────────────

	@Test
	public void position() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(VisEntityPosition.Fields.channel, Arrays.asList("email"))
				.put(VisEntityPosition.Fields.contactChannel, "selecao@jobsnow.com")
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.description, "vaga usada para testar o deleteAnyWhere")
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.expireDate, this.hoje(30))
				.put(VisEntityPosition.Fields.frequency, "daily")
				.put(VisEntityPosition.Fields.requiredSkill, Arrays.asList("java"))
				.put(VisEntityPosition.Fields.seniority, "SR")
				.put(VisEntityPosition.Fields.sortFields, Arrays.asList("seniority"))
				.put(VisEntityPosition.Fields.title, "vaga de teste para exercitar o delete any where")
				.put(VisEntityPosition.Fields.showSalaryExpectation, true)
				.put(VisEntityPosition.Fields.minClt, 2_000)
				.put(VisEntityPosition.Fields.maxClt, 9_000);

		this.deveApagarDosDoisIndices(VisEntityPosition.ENTITY, registro);
	}

	@Test
	public void resume() {
		CcpJsonRepresentation registro = this.comEmail()
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "desenvolvedor")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, this.hoje(5 * 365))
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.clt, 3_000);

		this.deveApagarDosDoisIndices(VisEntityResume.ENTITY, registro);
	}

	// ── o que se espera de todo deleteAnyWhere ───────────────────────────────

	/**
	 * Exercita os dois cenários possíveis. Em cada um, a gravação é feita pelo lado onde o registro deve
	 * nascer — e gravar de um lado apaga do outro, que é o contrato da entidade gêmea, então a montagem
	 * do segundo cenário não precisa desfazer o primeiro.
	 */
	private void deveApagarDosDoisIndices(CcpEntity entidade, CcpJsonRepresentation registro) {

		CcpEntity gemea = entidade.getTwinEntity();

		String principal = this.nomeDoIndice(entidade);
		String secundaria = this.nomeDoIndice(gemea);

		entidade.save(registro);
		assertTrue("nao gravou em " + principal, entidade.exists(registro));

		boolean existiaNaPrincipal = entidade.deleteAnyWhere(registro);

		assertTrue("deleteAnyWhere negou que o registro existia em " + principal, existiaNaPrincipal);
		assertFalse("sobrou registro em " + principal, entidade.exists(registro));
		assertFalse("sobrou registro em " + secundaria, gemea.exists(registro));

		gemea.save(registro);
		assertTrue("nao gravou em " + secundaria, gemea.exists(registro));

		boolean existiaNaGemea = entidade.deleteAnyWhere(registro);

		assertTrue("deleteAnyWhere negou que o registro existia em " + secundaria, existiaNaGemea);
		assertFalse("sobrou registro em " + secundaria, gemea.exists(registro));
		assertFalse("sobrou registro em " + principal, entidade.exists(registro));
	}

	private String nomeDoIndice(CcpEntity entidade) {
		CcpEntityMetaData entityMetaData = entidade.getEntityMetaData();
		return entityMetaData.entityName;
	}

	private CcpJsonRepresentation comEmail() {
		CcpJsonRepresentation registro = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, this.email);
		return registro;
	}

	/**
	 * Timestamp de quantos dias atrás. As validações de tempo destas entidades pedem um momento anterior
	 * ao de agora, dentro de uma janela em anos.
	 *
	 * <p>Vai como texto porque a validação exige que o valor seja um inteiro longo, e número posto no
	 * json chega nela como {@code Double} — em notação científica, que ela não reconhece.
	 */
	private String hoje(int diasAtras) {
		long umDia = 24L * 60L * 60L * 1000L;
		long agora = System.currentTimeMillis();
		long instante = agora - (diasAtras * umDia);
		String comoTexto = String.valueOf(instante);
		return comoTexto;
	}
}
