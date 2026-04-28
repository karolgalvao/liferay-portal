/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.customer.service;

/**
 * @author Karoline Silva
 */
public interface OktaService {

	String createApplication(String accountKey, String applicationName)
		throws Exception;

	void addUserToApplication(String oktaApplicationId, String userEmail)
		throws Exception;

	void deactivateApplication(String oktaApplicationId) throws Exception;

	void removeUserFromApplication(String oktaApplicationId, String userEmail)
		throws Exception;

}
