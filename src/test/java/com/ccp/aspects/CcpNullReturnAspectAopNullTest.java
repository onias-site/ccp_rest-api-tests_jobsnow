package com.ccp.aspects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpAllowNullParameter;
import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Verifica o contrato dos dois aspectos em si — que o weaving está ativo neste módulo de testes e
 * que as anotações de dispensa são respeitadas.
 *
 * <p>
 * O {@code CcpNullReturnAspect} não pode ser exercitado contra o código de produção (nenhum método
 * de produção devolve {@code null}: se devolvesse, seria justamente o defeito que o aspecto
 * denuncia). Por isso os retornos nulos são produzidos aqui, por classes deste pacote — que também
 * está sob {@code com.ccp..} e portanto é interceptado pelos mesmos pointcuts.
 * </p>
 */
public class CcpNullReturnAspectAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Alvos de teste interceptados pelos aspectos por estarem em {@code com.ccp..}. */
	static class InterceptedTargets {

		String retornaNull() {
			return null;
		}

		List<String> retornaListaNula() {
			return null;
		}

		@CcpAllowNullReturn
		String retornaNullPermitido() {
			return null;
		}

		String recebeParametro(String value) {
			return value;
		}

		@CcpAllowNullParameter
		String recebeParametroNulavel(String value) {
			return String.valueOf(value);
		}

		void metodoVoidComRetornoImplicito() {
		}

		CcpJsonRepresentation retornaJson() {
			return CcpOtherConstants.EMPTY_JSON;
		}
	}

	private static InterceptedTargets alvo() {
		return new InterceptedTargets();
	}

	// ── CcpNullReturnAspect ───────────────────────────────────────────────────

	@Test(expected = CcpNullReturnException.class)
	public void retornoNuloDisparaExcecaoTest() {
		alvo().retornaNull();
	}

	@Test(expected = CcpNullReturnException.class)
	public void retornoDeColecaoNulaDisparaExcecaoTest() {
		alvo().retornaListaNula();
	}

	@Test
	public void retornoNuloAnotadoNaoDisparaExcecaoTest() {
		assertNull(alvo().retornaNullPermitido());
	}

	@Test
	public void metodoVoidNaoDisparaExcecaoTest() {
		alvo().metodoVoidComRetornoImplicito();
	}

	@Test
	public void retornoNaoNuloNaoDisparaExcecaoTest() {
		assertNotNull(alvo().retornaJson());
	}

	// ── CcpNullParameterAspect ────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void parametroNuloDisparaExcecaoTest() {
		alvo().recebeParametro(null);
	}

	@Test
	public void parametroNuloAnotadoNaoDisparaExcecaoTest() {
		assertEquals("null", alvo().recebeParametroNulavel(null));
	}

	@Test
	public void parametroPreenchidoNaoDisparaExcecaoTest() {
		assertEquals("ok", alvo().recebeParametro("ok"));
	}

	// ── mensagens das exceções ────────────────────────────────────────────────

	@Test
	public void mensagemDaExcecaoDeRetornoNuloTest() {
		try {
			alvo().retornaNull();
			org.junit.Assert.fail("deveria ter lançado CcpNullReturnException");
		} catch (CcpNullReturnException e) {
			String message = e.getMessage();
			assertNotNull(message);
			org.junit.Assert.assertTrue(message.contains("retornaNull"));
		}
	}

	@Test
	public void mensagemDaExcecaoDeParametroNuloTest() {
		try {
			alvo().recebeParametro(null);
			org.junit.Assert.fail("deveria ter lançado CcpNullParameterException");
		} catch (CcpNullParameterException e) {
			String message = e.getMessage();
			assertNotNull(message);
			org.junit.Assert.assertTrue(message.contains("recebeParametro"));
		}
	}
}
