package com.ccp.json.validations.fields.annotations;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldType;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldValidatorInterface;

/**
 * Tipo de campo customizado, apontado por {@code @CcpJsonFieldTypeCustom}: só considera compatível
 * o valor formado exclusivamente por vogais. Serve para provar que o engine instancia e consulta a
 * classe indicada na anotação.
 */
public class TipoCampoApenasVogais implements CcpJsonFieldType {

	public Predicate<CcpJsonRepresentation> evaluateCompatibleType(String fieldName) {
		return json -> {
			CcpFieldName ccpFieldName = new CcpFieldName(fieldName);
			String valor = json.getAsString(ccpFieldName);
			boolean apenasVogais = valor.matches("^[aeiouAEIOU]+$");
			return apenasVogais;
		};
	}

	public boolean hasRuleExplanation(Field field) {
		return true;
	}

	public CcpJsonFieldValidatorInterface[] getErrorTypes() {
		return new CcpJsonFieldValidatorInterface[0];
	}

	public String name() {
		String name = TipoCampoApenasVogais.class.getSimpleName();
		return name;
	}
}
