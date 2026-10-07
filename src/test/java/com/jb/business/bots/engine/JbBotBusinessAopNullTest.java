package com.jb.business.bots.engine;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;
import org.junit.Test;

/**
 * Coverage of {@code CcpNullParameterAspect} over the default methods of {@code JbBotEngine.JbBotBusiness}. <p>
 * The implementations {@code Bot}, {@code BotCommand}, {@code BotCommandStep} and {@code CommonsBotCommandStep}
 * are {@code private static} inside {@code JbBotEngine}: they cannot be referenced outside the class that
 * declares them, so their overrides are not reachable by a test. The join points covered here are the ones of
 * the default implementations of the public interface. </p>
 */
public class JbBotBusinessAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private enum SampleFieldName implements CcpJsonFieldName {
		sample
	}

	/** Implementação mínima usada para alcançar os métodos default da interface. */


	private static JbBotBusiness bot() {
		return new BotBusinessForTest();
	}

	private static CcpSelectUnionAll unionAll() {
		return new CcpSelectUnionAll(new CcpJsonRepresentation[] { JSON },
				new java.util.ArrayList<CcpJsonRepresentation>(java.util.Arrays.asList(JSON)), ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isVisibleNullTest() {
		bot().isVisible(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hasPriorityNullTest() {
		bot().hasPriority(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBotNullTest() {
		bot().getBot(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getLoadedCommandNullTest() {
		bot().getLoadedCommand(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadLabelsWithLanguagesFilterValueNullTest() {
		bot().loadLabelsWithLanguages(null, unionAll(), ENTITY, SampleFieldName.sample);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadLabelsWithLanguagesResultNullTest() {
		bot().loadLabelsWithLanguages("filtro", null, ENTITY, SampleFieldName.sample);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadLabelsWithLanguagesEntityNullTest() {
		bot().loadLabelsWithLanguages("filtro", unionAll(), null, SampleFieldName.sample);
	}

	@Test(expected = CcpNullParameterException.class)
	public void loadLabelsWithLanguagesFilterFieldNullTest() {
		bot().loadLabelsWithLanguages("filtro", unionAll(), ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getExplanationNullTest() {
		bot().getExplanation(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hasExplanationNullTest() {
		bot().hasExplanation(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getIdentifierNullTest() {
		bot().getIdentifier(null);
	}
}
