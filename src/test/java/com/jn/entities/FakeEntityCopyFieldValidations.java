package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityFieldsValidator(classReferenceWithTheFields = FakeEntityCopyFieldValidations.Fields.class)
/**
 * Entidade fictícia, existente só para os testes. Serve para medir de onde vêm as regras de validação
 * de cada campo, e por isso os dois campos que ela declara têm nome igual ao de campos de
 * {@code JnJsonCommonsFields}, mas procedência diferente:
 *
 * <ul>
 * <li>{@code email} — regras escritas nele mesmo, sem {@code @CcpJsonCopyFieldValidationsFrom}. As
 * regras daqui aceitam qualquer texto de 10 a 500 caracteres; as de {@code JnJsonCommonsFields.email}
 * exigem formato de e-mail e limitam a 100 caracteres. São incompatíveis de propósito: um valor que
 * passe aqui e seria recusado lá diz de onde a regra veio.</li>
 * <li>{@code password} — sem regra própria, e com {@code @CcpJsonCopyFieldValidationsFrom} apontando
 * para {@code JnJsonCommonsFields}. Se a cópia não acontecesse, o campo ficaria sem validação nenhuma
 * e aceitaria qualquer coisa; é a recusa de uma senha fraca que prova que ela aconteceu.</li>
 * </ul>
 *
 * <p>Não tem índice no banco nem entra na carga inicial: mora em {@code src/test}, e a criação das
 * entidades varre {@code jn_business_jobsnow}.
 */
public class FakeEntityCopyFieldValidations implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(FakeEntityCopyFieldValidations.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName {

		@CcpJsonFieldTypeString(minLength = 10, maxLength = 500)
		email,

		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		password
		;
	}
}
