package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeNestedJson} valida o json interno pela classe indicada em
 * {@code jsonValidation} e se respeita a proibição de json interno vazio.
 */
public class CcpJsonFieldTypeNestedJsonTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeNestedJson business = new BusinessFieldTypeNestedJson();

	private CcpJsonRepresentation json(RulesFieldTypeNestedJson field, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, valor);
		return json;
	}

	@Test
	public void jsonInternoValidoTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON.put(RulesNestedAddress.rua, "Rua das Flores");
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.endereco, interno);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void jsonInternoComCampoObrigatorioAusenteTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.endereco, interno);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.endereco, CcpJsonFieldTypeError.nestedJson);
	}

	@Test
	public void jsonInternoComCampoForaDaRegraTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON.put(RulesNestedAddress.rua, "ab");
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.endereco, interno);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.endereco, CcpJsonFieldTypeError.nestedJson);
	}

	@Test
	public void jsonInternoVazioRecusadoTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.naoAceitaVazio, interno);
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.naoAceitaVazio, CcpJsonFieldTypeError.emptyJson);
	}

	@Test
	public void jsonInternoVazioAceitoTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.aceitaVazio, interno);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void valorQueNaoEhJsonTest() {
		CcpJsonRepresentation json = this.json(RulesFieldTypeNestedJson.aceitaVazio, "abc");
		FieldValidation.refuses(this.business, json, RulesFieldTypeNestedJson.aceitaVazio, CcpJsonFieldError.incompatibleType);
	}
}
