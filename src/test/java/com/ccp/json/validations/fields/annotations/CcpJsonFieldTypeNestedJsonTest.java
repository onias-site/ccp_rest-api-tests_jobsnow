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

	private final NegocioFieldTypeNestedJson negocio = new NegocioFieldTypeNestedJson();

	private CcpJsonRepresentation json(RegrasFieldTypeNestedJson campo, Object valor) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, valor);
		return json;
	}

	@Test
	public void jsonInternoValidoTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON.put(RegrasEnderecoAninhado.rua, "Rua das Flores");
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNestedJson.endereco, interno);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void jsonInternoComCampoObrigatorioAusenteTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNestedJson.endereco, interno);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNestedJson.endereco, CcpJsonFieldTypeError.nestedJson);
	}

	@Test
	public void jsonInternoComCampoForaDaRegraTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON.put(RegrasEnderecoAninhado.rua, "ab");
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNestedJson.endereco, interno);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNestedJson.endereco, CcpJsonFieldTypeError.nestedJson);
	}

	@Test
	public void jsonInternoVazioRecusadoTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNestedJson.naoAceitaVazio, interno);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNestedJson.naoAceitaVazio, CcpJsonFieldTypeError.emptyJson);
	}

	@Test
	public void jsonInternoVazioAceitoTest() {
		CcpJsonRepresentation interno = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNestedJson.aceitaVazio, interno);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void valorQueNaoEhJsonTest() {
		CcpJsonRepresentation json = this.json(RegrasFieldTypeNestedJson.aceitaVazio, "abc");
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeNestedJson.aceitaVazio, CcpJsonFieldError.incompatibleType);
	}
}
