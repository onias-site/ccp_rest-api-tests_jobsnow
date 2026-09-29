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
				.put(RulesRequiresAtLeastOneOneGroup.maxClt, 9_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneComOOutroCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.maxPj, 12_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneComTodosOsCamposDoGrupoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.maxClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAtLeastOneOneGroup.maxPj, 12_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneSemNenhumCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneOneGroup.titulo, "Desenvolvedor");
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAtLeastOneOneGroup.class, json);
		GlobalValidation.containsMessage(mensagens, "maxClt");
		GlobalValidation.containsMessage(mensagens, "maxPj");
	}

	@Test
	public void requiresAtLeastOneComJsonVazioTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		GlobalValidation.refuses(RulesRequiresAtLeastOneOneGroup.class, json);
	}

	@Test
	public void requiresAtLeastOneComDoisGruposAmbosSatisfeitosTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneTwoGroups.maxClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAtLeastOneTwoGroups.minClt, 5_000);
		GlobalValidation.accepts(RulesRequiresAtLeastOneTwoGroups.class, json);
	}

	@Test
	public void requiresAtLeastOneComDoisGruposSatisfazendoApenasOPrimeiroTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneTwoGroups.maxClt, 9_000);
		List<String> mensagens = this.recusaDoisGrupos(json);
		GlobalValidation.containsMessage(mensagens, "minClt");
	}

	private List<String> recusaDoisGrupos(CcpJsonRepresentation json) {
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAtLeastOneTwoGroups.class, json);
		return mensagens;
	}

	@Test
	public void requiresAtLeastOneComDoisGruposSatisfazendoApenasOSegundoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneTwoGroups.minPj, 5_000);
		List<String> mensagens = this.recusaDoisGrupos(json);
		GlobalValidation.containsMessage(mensagens, "maxClt");
	}

	@Test
	public void requiresAtLeastOneComGrupoMontadoPorDuasClassesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneUnionOfClasses.sms, "11999999999");
		GlobalValidation.accepts(RulesRequiresAtLeastOneUnionOfClasses.class, json);
	}

	@Test
	public void requiresAtLeastOneComGrupoMontadoPorDuasClassesSemNenhumCampoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAtLeastOneUnionOfClasses.titulo, "Desenvolvedor");
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAtLeastOneUnionOfClasses.class, json);
		GlobalValidation.containsMessage(mensagens, "telegram");
		GlobalValidation.containsMessage(mensagens, "sms");
	}

	// ------------------------------------------------------------------
	// requiresAllOrNone
	// ------------------------------------------------------------------

	@Test
	public void requiresAllOrNoneSemNenhumCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.titulo, "Desenvolvedor");
		GlobalValidation.accepts(RulesRequiresAllOrNoneOneGroup.class, json);
	}

	@Test
	public void requiresAllOrNoneComTodosOsCamposDoGrupoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAllOrNoneOneGroup.maxClt, 9_000);
		GlobalValidation.accepts(RulesRequiresAllOrNoneOneGroup.class, json);
	}

	@Test
	public void requiresAllOrNoneComApenasOPrimeiroCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.minClt, 5_000);
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAllOrNoneOneGroup.class, json);
		GlobalValidation.containsMessage(mensagens, "maxClt");
	}

	@Test
	public void requiresAllOrNoneComApenasOSegundoCampoDoGrupoTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.maxClt, 9_000);
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAllOrNoneOneGroup.class, json);
		GlobalValidation.containsMessage(mensagens, "minClt");
	}

	@Test
	public void requiresAllOrNoneComDoisGruposUmCompletoEOutroIntocadoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAllOrNoneTwoGroups.maxClt, 9_000);
		GlobalValidation.accepts(RulesRequiresAllOrNoneTwoGroups.class, json);
	}

	@Test
	public void requiresAllOrNoneComDoisGruposAmbosCompletosTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RulesRequiresAllOrNoneTwoGroups.maxClt, 9_000);
		CcpJsonRepresentation put3 = put2
				.put(RulesRequiresAllOrNoneTwoGroups.minPj, 7_000);
		CcpJsonRepresentation json = put3
				.put(RulesRequiresAllOrNoneTwoGroups.maxPj, 12_000);
		GlobalValidation.accepts(RulesRequiresAllOrNoneTwoGroups.class, json);
	}

	@Test
	public void requiresAllOrNoneComDoisGruposUmCompletoEOutroPelaMetadeTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RulesRequiresAllOrNoneTwoGroups.maxClt, 9_000);
		CcpJsonRepresentation json = put2
				.put(RulesRequiresAllOrNoneTwoGroups.minPj, 7_000);
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAllOrNoneTwoGroups.class, json);
		GlobalValidation.containsMessage(mensagens, "maxPj");
	}

	@Test
	public void requiresAllOrNoneComDoisGruposAmbosPelaMetadeTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneTwoGroups.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesRequiresAllOrNoneTwoGroups.minPj, 7_000);
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAllOrNoneTwoGroups.class, json);
		GlobalValidation.containsMessage(mensagens, "maxClt");
		GlobalValidation.containsMessage(mensagens, "maxPj");
	}

	@Test
	public void requiresAllOrNoneApontaOsCamposPresentesEOsFaltantesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesRequiresAllOrNoneOneGroup.minClt, 5_000);
		List<String> mensagens = GlobalValidation.refuses(RulesRequiresAllOrNoneOneGroup.class, json);
		GlobalValidation.containsMessage(mensagens, "contains the following fields");
		GlobalValidation.containsMessage(mensagens, "not contains the following fields");
	}

	// ------------------------------------------------------------------
	// customJsonValidators
	// ------------------------------------------------------------------

	@Test
	public void customJsonValidatorsSemErroTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCustomJsonValidator.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesCustomJsonValidator.maxClt, 9_000);
		GlobalValidation.accepts(RulesCustomJsonValidator.class, json);
	}

	@Test
	public void customJsonValidatorsComErroTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCustomJsonValidator.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesCustomJsonValidator.maxClt, 5_000);
		List<String> mensagens = GlobalValidation.refuses(RulesCustomJsonValidator.class, json);
		GlobalValidation.containsMessage(mensagens, ConsistentSalaryRangeValidator.MESSAGE);
	}

	@Test
	public void customJsonValidatorsNaoEhChamadoQuandoNaoTemComoAvaliarTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesCustomJsonValidator.minClt, 9_000);
		GlobalValidation.accepts(RulesCustomJsonValidator.class, json);
	}

	@Test
	public void customJsonValidatorsEmSerieAcumulamOsErrosTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesChainedCustomJsonValidators.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesChainedCustomJsonValidators.maxClt, 5_000);
		List<String> mensagens = GlobalValidation.refuses(RulesChainedCustomJsonValidators.class, json);
		GlobalValidation.containsMessage(mensagens, ConsistentSalaryRangeValidator.MESSAGE);
		GlobalValidation.containsMessage(mensagens, RequiredTitleValidator.MESSAGE);
	}

	@Test
	public void customJsonValidatorsEmSerieComApenasUmDelesAcusandoErroTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesChainedCustomJsonValidators.minClt, 5_000);
		CcpJsonRepresentation put2 = put
				.put(RulesChainedCustomJsonValidators.maxClt, 9_000);
		CcpJsonRepresentation json = put2
				.put(RulesChainedCustomJsonValidators.titulo, "Desenvolvedor");
		GlobalValidation.accepts(RulesChainedCustomJsonValidators.class, json);
	}

	@Test
	public void customJsonValidatorsCriticoInterrompeOsSeguintesTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		List<String> mensagens = GlobalValidation.refuses(RulesCriticalCustomJsonValidator.class, json);
		GlobalValidation.containsMessage(mensagens, CriticalContractValidator.MESSAGE);
		GlobalValidation.doesNotContainMessage(mensagens, RequiredTitleValidator.MESSAGE);
	}

	@Test
	public void customJsonValidatorsCriticoSemErroDeixaOsSeguintesRodaremTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesCriticalCustomJsonValidator.contrato, "clt");
		List<String> mensagens = GlobalValidation.refuses(RulesCriticalCustomJsonValidator.class, json);
		GlobalValidation.containsMessage(mensagens, RequiredTitleValidator.MESSAGE);
	}

	// ------------------------------------------------------------------
	// atributos combinados e casos de borda da anotação
	// ------------------------------------------------------------------

	@Test
	public void atributosCombinadosComJsonValidoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCombinedGlobalValidations.minClt, 5_000);
		CcpJsonRepresentation json = put
				.put(RulesCombinedGlobalValidations.maxClt, 9_000);
		GlobalValidation.accepts(RulesCombinedGlobalValidations.class, json);
	}

	@Test
	public void atributosCombinadosAcusamOsErrosDeTodosOsAtributosTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesCombinedGlobalValidations.minPj, 7_000);
		List<String> mensagens = GlobalValidation.refuses(RulesCombinedGlobalValidations.class, json);
		GlobalValidation.containsMessage(mensagens, "It is missing one of them fields");
		GlobalValidation.containsMessage(mensagens, "maxPj");
	}

	@Test
	public void atributosCombinadosAcusamErroDoValidadorCustomizadoTest() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(RulesCombinedGlobalValidations.minClt, 9_000);
		CcpJsonRepresentation json = put
				.put(RulesCombinedGlobalValidations.maxClt, 5_000);
		List<String> mensagens = GlobalValidation.refuses(RulesCombinedGlobalValidations.class, json);
		GlobalValidation.containsMessage(mensagens, ConsistentSalaryRangeValidator.MESSAGE);
	}

	@Test
	public void anotacaoSemAtributosNaoCobraNadaTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesGlobalValidationsWithoutAttributes.minClt, 5_000);
		GlobalValidation.accepts(RulesGlobalValidationsWithoutAttributes.class, json);
	}

	@Test
	public void anotacaoSemAtributosAceitaAteJsonVazioTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		GlobalValidation.accepts(RulesGlobalValidationsWithoutAttributes.class, json);
	}

	// ------------------------------------------------------------------
	// explicacao das regras de cada atributo
	// ------------------------------------------------------------------

	@Test
	public void requiresAtLeastOneExplicaASuaRegraTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesRequiresAtLeastOneOneGroup.class);
		GlobalValidation.containsMessage(explicacoes, "one of this following fields");
		GlobalValidation.containsMessage(explicacoes, "maxClt");
		GlobalValidation.containsMessage(explicacoes, "maxPj");
	}

	@Test
	public void requiresAllOrNoneExplicaASuaRegraTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesRequiresAllOrNoneOneGroup.class);
		GlobalValidation.containsMessage(explicacoes, "all (or none)");
		GlobalValidation.containsMessage(explicacoes, "minClt");
		GlobalValidation.containsMessage(explicacoes, "maxClt");
	}

	@Test
	public void requiresAllOrNoneNaoExplicaOsGruposDoRequiresAtLeastOneTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesRequiresAtLeastOneOneGroup.class);
		GlobalValidation.doesNotContainMessage(explicacoes, "all (or none)");
	}

	@Test
	public void customJsonValidatorsExplicamSuasRegrasTest() {
		List<String> explicacoes = GlobalValidation.rulesExplanations(RulesChainedCustomJsonValidators.class);
		GlobalValidation.containsMessage(explicacoes, ConsistentSalaryRangeValidator.MESSAGE);
		GlobalValidation.containsMessage(explicacoes, RequiredTitleValidator.MESSAGE);
	}

	@Test
	public void classeSemAnotacaoNaoCobraNadaTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(RulesWithoutGlobalValidations.minClt, 5_000);
		GlobalValidation.accepts(RulesWithoutGlobalValidations.class, json);
	}
}
