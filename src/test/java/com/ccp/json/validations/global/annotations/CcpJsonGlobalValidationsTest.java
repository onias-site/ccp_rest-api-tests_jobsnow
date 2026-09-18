package com.ccp.json.validations.global.annotations;

import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Verifica os três atributos de {@code @CcpJsonGlobalValidations}, cada um com o comportamento que o
 * próprio nome anuncia:
 * <ul>
 * <li>{@code requiresAtLeastOne} — de cada grupo, ao menos um campo precisa estar no json;</li>
 * <li>{@code requiresAllOrNone} — de cada grupo, ou todos os campos estão no json, ou nenhum;</li>
 * <li>{@code customJsonValidators} — validadores de classe adicionais rodam junto com os dois de cima.</li>
 * </ul>
 * Os cenários espelham os usos reais em {@code VisEntityPosition.Fields} (os dois primeiros atributos
 * combinados) e em {@code VisEntityResume.Fields} (um único grupo de {@code requiresAtLeastOne}).
 */
public class CcpJsonGlobalValidationsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ------------------------------------------------------------------
	// requiresAtLeastOne
	// ------------------------------------------------------------------

	@Test
	public void requiresAtLeastOneComUmDosCamposDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneUmGrupo.maxClt, 9_000);
		ValidacaoGlobal.aceita(RegrasRequiresAtLeastOneUmGrupo.class, json);
	}

	@Test
	public void requiresAtLeastOneComOOutroCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneUmGrupo.maxPj, 12_000);
		ValidacaoGlobal.aceita(RegrasRequiresAtLeastOneUmGrupo.class, json);
	}

	@Test
	public void requiresAtLeastOneComTodosOsCamposDoGrupoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneUmGrupo.maxClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RegrasRequiresAtLeastOneUmGrupo.maxPj, 12_000);
		ValidacaoGlobal.aceita(RegrasRequiresAtLeastOneUmGrupo.class, json);
	}

	@Test
	public void requiresAtLeastOneSemNenhumCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneUmGrupo.titulo, "Desenvolvedor");
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAtLeastOneUmGrupo.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "maxClt");
		ValidacaoGlobal.contemMensagem(mensagens, "maxPj");
	}

	@Test
	public void requiresAtLeastOneComJsonVazioTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		ValidacaoGlobal.recusa(RegrasRequiresAtLeastOneUmGrupo.class, json);
	}

	@Test
	public void requiresAtLeastOneComDoisGruposAmbosSatisfeitosTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneDoisGrupos.maxClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RegrasRequiresAtLeastOneDoisGrupos.minClt, 5_000);
		ValidacaoGlobal.aceita(RegrasRequiresAtLeastOneDoisGrupos.class, json);
	}

	@Test
	public void requiresAtLeastOneComDoisGruposSatisfazendoApenasOPrimeiroTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneDoisGrupos.maxClt, 9_000);
		List<String> mensagens = this.recusaDoisGrupos(json);
		ValidacaoGlobal.contemMensagem(mensagens, "minClt");
	}

	private List<String> recusaDoisGrupos(CcpJsonRepresentation json) {
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAtLeastOneDoisGrupos.class, json);
		return mensagens;
	}

	@Test
	public void requiresAtLeastOneComDoisGruposSatisfazendoApenasOSegundoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneDoisGrupos.minPj, 5_000);
		List<String> mensagens = this.recusaDoisGrupos(json);
		ValidacaoGlobal.contemMensagem(mensagens, "maxClt");
	}

	@Test
	public void requiresAtLeastOneComGrupoMontadoPorDuasClassesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneUniaoDeClasses.sms, "11999999999");
		ValidacaoGlobal.aceita(RegrasRequiresAtLeastOneUniaoDeClasses.class, json);
	}

	@Test
	public void requiresAtLeastOneComGrupoMontadoPorDuasClassesSemNenhumCampoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAtLeastOneUniaoDeClasses.titulo, "Desenvolvedor");
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAtLeastOneUniaoDeClasses.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "telegram");
		ValidacaoGlobal.contemMensagem(mensagens, "sms");
	}

	// ------------------------------------------------------------------
	// requiresAllOrNone
	// ------------------------------------------------------------------

	@Test
	public void requiresAllOrNoneSemNenhumCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneUmGrupo.titulo, "Desenvolvedor");
		ValidacaoGlobal.aceita(RegrasRequiresAllOrNoneUmGrupo.class, json);
	}

	@Test
	public void requiresAllOrNoneComTodosOsCamposDoGrupoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneUmGrupo.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RegrasRequiresAllOrNoneUmGrupo.maxClt, 9_000);
		ValidacaoGlobal.aceita(RegrasRequiresAllOrNoneUmGrupo.class, json);
	}

	@Test
	public void requiresAllOrNoneComApenasOPrimeiroCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneUmGrupo.minClt, 5_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAllOrNoneUmGrupo.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "maxClt");
	}

	@Test
	public void requiresAllOrNoneComApenasOSegundoCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneUmGrupo.maxClt, 9_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAllOrNoneUmGrupo.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "minClt");
	}

	@Test
	public void requiresAllOrNoneComDoisGruposUmCompletoEOutroIntocadoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneDoisGrupos.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RegrasRequiresAllOrNoneDoisGrupos.maxClt, 9_000);
		ValidacaoGlobal.aceita(RegrasRequiresAllOrNoneDoisGrupos.class, json);
	}

	@Test
	public void requiresAllOrNoneComDoisGruposAmbosCompletosTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneDoisGrupos.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RegrasRequiresAllOrNoneDoisGrupos.maxClt, 9_000);
		CcpJsonRepresentation put3 = put2
				.put(RegrasRequiresAllOrNoneDoisGrupos.minPj, 7_000);
		CcpJsonRepresentation json = put3
				.put(RegrasRequiresAllOrNoneDoisGrupos.maxPj, 12_000);
		ValidacaoGlobal.aceita(RegrasRequiresAllOrNoneDoisGrupos.class, json);
	}

	@Test
	public void requiresAllOrNoneComDoisGruposUmCompletoEOutroPelaMetadeTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneDoisGrupos.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RegrasRequiresAllOrNoneDoisGrupos.maxClt, 9_000);
		CcpJsonRepresentation json = put2
				.put(RegrasRequiresAllOrNoneDoisGrupos.minPj, 7_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAllOrNoneDoisGrupos.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "maxPj");
	}

	@Test
	public void requiresAllOrNoneComDoisGruposAmbosPelaMetadeTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneDoisGrupos.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RegrasRequiresAllOrNoneDoisGrupos.minPj, 7_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAllOrNoneDoisGrupos.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "maxClt");
		ValidacaoGlobal.contemMensagem(mensagens, "maxPj");
	}

	@Test
	public void requiresAllOrNoneApontaOsCamposPresentesEOsFaltantesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasRequiresAllOrNoneUmGrupo.minClt, 5_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasRequiresAllOrNoneUmGrupo.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "contains the following fields");
		ValidacaoGlobal.contemMensagem(mensagens, "not contains the following fields");
	}

	// ------------------------------------------------------------------
	// customJsonValidators
	// ------------------------------------------------------------------

	@Test
	public void customJsonValidatorsSemErroTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasCustomJsonValidator.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RegrasCustomJsonValidator.maxClt, 9_000);
		ValidacaoGlobal.aceita(RegrasCustomJsonValidator.class, json);
	}

	@Test
	public void customJsonValidatorsComErroTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasCustomJsonValidator.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RegrasCustomJsonValidator.maxClt, 5_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasCustomJsonValidator.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, ValidadorFaixaSalarialCoerente.MENSAGEM);
	}

	@Test
	public void customJsonValidatorsNaoEhChamadoQuandoNaoTemComoAvaliarTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasCustomJsonValidator.minClt, 9_000);
		ValidacaoGlobal.aceita(RegrasCustomJsonValidator.class, json);
	}

	@Test
	public void customJsonValidatorsEmSerieAcumulamOsErrosTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasCustomJsonValidatoresEmSerie.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RegrasCustomJsonValidatoresEmSerie.maxClt, 5_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasCustomJsonValidatoresEmSerie.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, ValidadorFaixaSalarialCoerente.MENSAGEM);
		ValidacaoGlobal.contemMensagem(mensagens, ValidadorTituloObrigatorio.MENSAGEM);
	}

	@Test
	public void customJsonValidatorsEmSerieComApenasUmDelesAcusandoErroTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasCustomJsonValidatoresEmSerie.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RegrasCustomJsonValidatoresEmSerie.maxClt, 9_000);
		CcpJsonRepresentation json = put2
				.put(RegrasCustomJsonValidatoresEmSerie.titulo, "Desenvolvedor");
		ValidacaoGlobal.aceita(RegrasCustomJsonValidatoresEmSerie.class, json);
	}

	@Test
	public void customJsonValidatorsCriticoInterrompeOsSeguintesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasCustomJsonValidatorCritico.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, ValidadorCriticoDeContrato.MENSAGEM);
		ValidacaoGlobal.naoContemMensagem(mensagens, ValidadorTituloObrigatorio.MENSAGEM);
	}

	@Test
	public void customJsonValidatorsCriticoSemErroDeixaOsSeguintesRodaremTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasCustomJsonValidatorCritico.contrato, "clt");
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasCustomJsonValidatorCritico.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, ValidadorTituloObrigatorio.MENSAGEM);
	}

	// ------------------------------------------------------------------
	// atributos combinados e casos de borda da anotação
	// ------------------------------------------------------------------

	@Test
	public void atributosCombinadosComJsonValidoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasGlobalValidationsCombinadas.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RegrasGlobalValidationsCombinadas.maxClt, 9_000);
		ValidacaoGlobal.aceita(RegrasGlobalValidationsCombinadas.class, json);
	}

	@Test
	public void atributosCombinadosAcusamOsErrosDeTodosOsAtributosTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasGlobalValidationsCombinadas.minPj, 7_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasGlobalValidationsCombinadas.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, "It is missing one of them fields");
		ValidacaoGlobal.contemMensagem(mensagens, "maxPj");
	}

	@Test
	public void atributosCombinadosAcusamErroDoValidadorCustomizadoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RegrasGlobalValidationsCombinadas.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RegrasGlobalValidationsCombinadas.maxClt, 5_000);
		List<String> mensagens = ValidacaoGlobal.recusa(RegrasGlobalValidationsCombinadas.class, json);
		ValidacaoGlobal.contemMensagem(mensagens, ValidadorFaixaSalarialCoerente.MENSAGEM);
	}

	@Test
	public void anotacaoSemAtributosNaoCobraNadaTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasGlobalValidationsSemAtributos.minClt, 5_000);
		ValidacaoGlobal.aceita(RegrasGlobalValidationsSemAtributos.class, json);
	}

	@Test
	public void anotacaoSemAtributosAceitaAteJsonVazioTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		ValidacaoGlobal.aceita(RegrasGlobalValidationsSemAtributos.class, json);
	}

	// ------------------------------------------------------------------
	// explicacao das regras de cada atributo
	// ------------------------------------------------------------------

	@Test
	public void requiresAtLeastOneExplicaASuaRegraTest() {
		List<String> explicacoes = ValidacaoGlobal.explicacoesDasRegras(RegrasRequiresAtLeastOneUmGrupo.class);
		ValidacaoGlobal.contemMensagem(explicacoes, "one of this following fields");
		ValidacaoGlobal.contemMensagem(explicacoes, "maxClt");
		ValidacaoGlobal.contemMensagem(explicacoes, "maxPj");
	}

	@Test
	public void requiresAllOrNoneExplicaASuaRegraTest() {
		List<String> explicacoes = ValidacaoGlobal.explicacoesDasRegras(RegrasRequiresAllOrNoneUmGrupo.class);
		ValidacaoGlobal.contemMensagem(explicacoes, "all (or none)");
		ValidacaoGlobal.contemMensagem(explicacoes, "minClt");
		ValidacaoGlobal.contemMensagem(explicacoes, "maxClt");
	}

	@Test
	public void requiresAllOrNoneNaoExplicaOsGruposDoRequiresAtLeastOneTest() {
		List<String> explicacoes = ValidacaoGlobal.explicacoesDasRegras(RegrasRequiresAtLeastOneUmGrupo.class);
		ValidacaoGlobal.naoContemMensagem(explicacoes, "all (or none)");
	}

	@Test
	public void customJsonValidatorsExplicamSuasRegrasTest() {
		List<String> explicacoes = ValidacaoGlobal.explicacoesDasRegras(RegrasCustomJsonValidatoresEmSerie.class);
		ValidacaoGlobal.contemMensagem(explicacoes, ValidadorFaixaSalarialCoerente.MENSAGEM);
		ValidacaoGlobal.contemMensagem(explicacoes, ValidadorTituloObrigatorio.MENSAGEM);
	}

	@Test
	public void classeSemAnotacaoNaoCobraNadaTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RegrasSemGlobalValidations.minClt, 5_000);
		ValidacaoGlobal.aceita(RegrasSemGlobalValidations.class, json);
	}
}
