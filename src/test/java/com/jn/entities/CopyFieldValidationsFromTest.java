package com.jn.entities;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Measures where the validation rules of each field of an entity come from.
 *
 * <p>The doubt recorded in {@code JnEntityLoginTokenRequestResend} was whether the engine would always
 * be copying the rules from {@code JnJsonCommonsFields}, regardless of what each field declares. The
 * fictitious entity {@link FakeEntityCopyFieldValidations} answers that with two fields with the same
 * names as the ones there and opposite origins, and the two proofs are symmetric:
 *
 * <ul>
 * <li>{@code email} accepts values that {@code JnJsonCommonsFields.email} would refuse — so the copy
 * does <b>not</b> happen where it was not asked for;</li>
 * <li>{@code password} refuses values that would pass if the field had no rule — so the copy
 * <b>does happen</b> where it was asked for.</li>
 * </ul>
 *
 * <p>Validation is triggered by {@code ENTITY.validateJson}, not by building the json by hand for the
 * engine: it is the {@code @CcpEntityFieldsValidator} decorator that is meant to be exercised, which is
 * what every entity save goes through.
 */
public class CopyFieldValidationsFromTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String REAL_EMAIL = "onias85@gmail.com";

	private static final String STRONG_PASSWORD = "Strong#12345";

	// ── email: what is written on the field itself applies ────────────────────

	/**
	 * Ten characters that do not form an e-mail. It passes the field's own rule (10 to 500) and would be
	 * refused by the regular expression of {@code JnJsonCommonsFields.email}.
	 */
	@Test
	public void emailAcceptsTextThatIsNotAnEmail() {
		this.shouldBeAccepted("abcdefghij", STRONG_PASSWORD);
	}

	/**
	 * Two hundred characters. It passes the own limit of 500 and would be refused by the limit of 100 of
	 * {@code JnJsonCommonsFields.email}. It proves the same thing as the test above, without depending on
	 * the regular expression.
	 */
	@Test
	public void emailAcceptsTextLongerThanTheCentralizerLimit() {
		String twoHundredCharacters = "a".repeat(200);
		this.shouldBeAccepted(twoHundredCharacters, STRONG_PASSWORD);
	}

	/** The field's own rule applies in full, not only in the part that loosens: three characters are too few. */
	@Test
	public void emailRefusesTextShorterThanItsOwnMinimum() {
		this.shouldBeRefused("abc", STRONG_PASSWORD, JnJsonCommonsFields.email.name());
	}

	/** And the own limit of 500 is enforced too. */
	@Test
	public void emailRefusesTextLongerThanItsOwnMaximum() {
		String fiveHundredAndOneCharacters = "a".repeat(501);
		this.shouldBeRefused(fiveHundredAndOneCharacters, STRONG_PASSWORD, JnJsonCommonsFields.email.name());
	}

	// ── password: what came from JnJsonCommonsFields applies ──────────────────

	/**
	 * The field declares no rule at all. Without the copy it would accept any text; refusing the weak
	 * password is what shows that the rule of {@code JnJsonCommonsFields.password} got here.
	 */
	@Test
	public void passwordRefusesWeakPassword() {
		this.shouldBeRefused(REAL_EMAIL, "weak", JnJsonCommonsFields.password.name());
	}

	/** And it accepts the password that satisfies that rule: uppercase, lowercase, digit, symbol and eight. */
	@Test
	public void passwordAcceptsStrongPassword() {
		this.shouldBeAccepted(REAL_EMAIL, STRONG_PASSWORD);
	}

	// ── and the real email passes both readings, so it proves nothing ─────────

	/**
	 * Recorded to make clear why the other tests use odd values: a real e-mail passes both the field's
	 * own rule and the centralizer's, and therefore does not tell one from the other.
	 */
	@Test
	public void realEmailPassesBothRulesAndSoDistinguishesNothing() {
		this.shouldBeAccepted(REAL_EMAIL, STRONG_PASSWORD);
	}

	// ── mechanics ─────────────────────────────────────────────────────────────

	private void shouldBeAccepted(String email, String password) {
		CcpJsonRepresentation json = this.json(email, password);
		FakeEntityCopyFieldValidations.ENTITY.validateJson(json);
	}

	private void shouldBeRefused(String email, String password, String expectedField) {

		CcpJsonRepresentation json = this.json(email, password);

		try {
			FakeEntityCopyFieldValidations.ENTITY.validateJson(json);
		} catch (CcpJsonValidationError e) {
			String message = e.getMessage();
			boolean pointsToTheRightField = message.contains(expectedField);
			assertTrue("the error should point to the field " + expectedField + ", but got: " + message, pointsToTheRightField);
			return;
		}
		fail("the json should have been refused because of the field " + expectedField);
	}

	private CcpJsonRepresentation json(String email, String password) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, email)
				.put(JnJsonCommonsFields.password, password);
		return json;
	}
}
