/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.customer.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Karoline Silva
 */
@Component
@ConditionalOnProperty(
	havingValue = "true", name = "liferay.customer.okta.enabled"
)
public class OktaServiceImpl implements OktaService {

	@Override
	public void addUserToApplication(
			String oktaApplicationId, String userEmail)
		throws Exception {

		String oktaUserId = _resolveOktaUserId(userEmail);

		if (oktaUserId == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Okta user not found for email " + userEmail +
						"; skipping addUserToApplication.");
			}

			return;
		}

		_getWebClient(
		).post(
		).uri(
			"/api/v1/apps/" + oktaApplicationId + "/users"
		).bodyValue(
			new JSONObject(
			).put(
				"id", oktaUserId
			).toString()
		).retrieve(
		).bodyToMono(
			String.class
		).block();
	}

	@Override
	public String createApplication(String accountKey, String applicationName)
		throws Exception {

		String responseBody = _getWebClient(
		).post(
		).uri(
			"/api/v1/apps"
		).bodyValue(
			new JSONObject(
			).put(
				"label", applicationName
			).put(
				"name", "bookmark"
			).put(
				"settings",
				new JSONObject(
				).put(
					"app",
					new JSONObject(
					).put(
						"url", "https://liferay.com"
					)
				)
			).put(
				"signOnMode", "BOOKMARK"
			).toString()
		).retrieve(
		).bodyToMono(
			String.class
		).block();

		JSONObject responseJSONObject = new JSONObject(responseBody);

		return responseJSONObject.getString("id");
	}

	@Override
	public void deactivateApplication(String oktaApplicationId)
		throws Exception {

		_getWebClient(
		).post(
		).uri(
			"/api/v1/apps/" + oktaApplicationId + "/lifecycle/deactivate"
		).retrieve(
		).bodyToMono(
			String.class
		).block();
	}

	@Override
	public void removeUserFromApplication(
			String oktaApplicationId, String userEmail)
		throws Exception {

		String oktaUserId = _resolveOktaUserId(userEmail);

		if (oktaUserId == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Okta user not found for email " + userEmail +
						"; skipping removeUserFromApplication.");
			}

			return;
		}

		_getWebClient(
		).method(
			HttpMethod.DELETE
		).uri(
			"/api/v1/apps/" + oktaApplicationId + "/users/" + oktaUserId
		).retrieve(
		).toBodilessEntity(
		).block();
	}

	private WebClient _getWebClient() {
		return WebClient.builder(
		).baseUrl(
			_oktaBaseUrl
		).defaultHeader(
			HttpHeaders.AUTHORIZATION, "SSWS " + _oktaApiToken
		).defaultHeader(
			HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE
		).defaultHeader(
			HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
		).build();
	}

	private String _resolveOktaUserId(String email) throws Exception {
		String searchQuery = UriComponentsBuilder.fromPath(
			"/api/v1/users"
		).queryParam(
			"search", "profile.email eq \"" + email + "\""
		).build(
		).toUriString();

		String responseBody = _getWebClient(
		).get(
		).uri(
			searchQuery
		).retrieve(
		).bodyToMono(
			String.class
		).block();

		JSONArray usersJSONArray = new JSONArray(responseBody);

		if (usersJSONArray.length() == 0) {
			return null;
		}

		JSONObject userJSONObject = usersJSONArray.getJSONObject(0);

		return userJSONObject.getString("id");
	}

	private static final Log _log = LogFactory.getLog(OktaServiceImpl.class);

	@Value("${liferay.customer.okta.api.token}")
	private String _oktaApiToken;

	@Value("${liferay.customer.okta.base.url}")
	private String _oktaBaseUrl;

}
