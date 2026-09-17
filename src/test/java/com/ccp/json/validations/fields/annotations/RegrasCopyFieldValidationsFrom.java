package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Campo sem nenhuma regra própria: todas vêm do campo homônimo de
 * {@code RegrasOrigemDasValidacoes}, por conta de {@code @CcpJsonCopyFieldValidationsFrom}.
 */
public enum RegrasCopyFieldValidationsFrom implements CcpJsonFieldName {

	@CcpJsonCopyFieldValidationsFrom(RegrasOrigemDasValidacoes.class)
	apelido,
	;
}
