/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.customer.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author Karoline Silva
 */
@Component
@ConditionalOnProperty(
	havingValue = "false", matchIfMissing = true,
	name = "liferay.customer.okta.enabled"
)
public class OktaServiceStub implements OktaService {

	@Override
	public void addUserToApplication(
		String oktaApplicationId, String userEmail) {

		if (_log.isWarnEnabled()) {
			_log.warn(
				"[OktaServiceStub] addUserToApplication called with args " +
					oktaApplicationId + ", " + userEmail +
						" — Okta integration disabled.");
		}
	}

	@Override
	public String createApplication(String accountKey, String applicationName) {
		String stubOktaApplicationId = "STUB_cp-cloudnative-" + accountKey;

		if (_log.isWarnEnabled()) {
			_log.warn(
				"[OktaServiceStub] createApplication called with args " +
					accountKey + ", " + applicationName +
						" — Okta integration disabled. Returning: " +
							stubOktaApplicationId);
		}

		return stubOktaApplicationId;
	}

	@Override
	public void deactivateApplication(String oktaApplicationId) {
		if (_log.isWarnEnabled()) {
			_log.warn(
				"[OktaServiceStub] deactivateApplication called with args " +
					oktaApplicationId + " — Okta integration disabled.");
		}
	}

	@Override
	public void removeUserFromApplication(
		String oktaApplicationId, String userEmail) {

		if (_log.isWarnEnabled()) {
			_log.warn(
				"[OktaServiceStub] removeUserFromApplication called with args " +
					oktaApplicationId + ", " + userEmail +
						" — Okta integration disabled.");
		}
	}

	private static final Log _log = LogFactory.getLog(OktaServiceStub.class);

}
