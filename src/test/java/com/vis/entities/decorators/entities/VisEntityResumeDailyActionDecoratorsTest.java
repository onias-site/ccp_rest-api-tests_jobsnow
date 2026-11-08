package com.vis.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.vis.entities.VisEntityResumeDailyAction;
import com.vis.json.fields.validation.VisResumeDailyActions;

/**
 * vis_resume_daily_action: {@code @CcpEntityCache(3600)}, daily disposable, transformer and validator. Each action of the
 * candidate over the saved resume (inactivation, reactivation, exclusion and update) once a day, since 2026-11-07.
 */
public class VisEntityResumeDailyActionDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return VisEntityResumeDailyAction.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		CcpJsonRepresentation key = VisEntityResumeDailyAction.getKey(this.email, VisResumeDailyActions.update);
		return key;
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.daily);
	}
}
