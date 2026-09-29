package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;

/**
 * Dublê de provedor de e-mail que não envia nada, mas guarda os destinatários de cada envio. Serve
 * para provar que o decorator de envio ao usuário mandou (ou deixou de mandar) o e-mail.
 */
public class CountingEmailSender implements CcpEmailSender {

	private static final List<String> sentRecipients = Collections.synchronizedList(new ArrayList<>());

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		List<String> asList = Arrays.asList(recipients);
		sentRecipients.addAll(asList);
		return CcpOtherConstants.EMPTY_JSON;
	}

	/** Quantos e-mails já foram endereçados ao destinatário informado. */
	public static long howManyFor(String destinatario) {
		synchronized (sentRecipients) {
			long quantos = sentRecipients.stream().filter(x -> destinatario.equalsIgnoreCase(x)).count();
			return quantos;
		}
	}
}
