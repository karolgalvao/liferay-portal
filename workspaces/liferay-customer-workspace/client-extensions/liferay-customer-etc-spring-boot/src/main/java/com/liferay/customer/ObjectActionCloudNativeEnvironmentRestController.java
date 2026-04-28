/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.customer;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import com.liferay.client.extension.util.spring.boot3.client.LiferayOAuth2AccessTokenManager;
import com.liferay.customer.service.OktaService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Karoline Silva
 */
@RequestMapping("/object/action/cloud-native-environment")
@RestController
public class ObjectActionCloudNativeEnvironmentRestController
	extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		try {
			if (!_isSystemUpdate(jwt)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}

			JSONObject jsonObject = new JSONObject(json);

			String objectActionTriggerKey = jsonObject.getString(
				"objectActionTriggerKey");

			if (!"onAfterAdd".equals(objectActionTriggerKey) &&
				!"onAfterDelete".equals(objectActionTriggerKey)) {

				throw new Exception(
					"Invalid objectActionTriggerKey: " +
						objectActionTriggerKey);
			}

			JSONObject cloudNativeEnvironmentJSONObject =
				jsonObject.getJSONObject(
					"objectEntryDTOCloudNativeEnvironment");

			long cloudNativeEnvironmentId =
				cloudNativeEnvironmentJSONObject.getLong("id");

			String accountKey = cloudNativeEnvironmentJSONObject.optString(
				"accountKey");

			String oktaApplicationId =
				cloudNativeEnvironmentJSONObject.optString("oktaApplicationId");

			if ("onAfterAdd".equals(objectActionTriggerKey)) {
				_handleOnAfterAdd(
					accountKey, cloudNativeEnvironmentId, oktaApplicationId);
			}
			else {
				_handleOnAfterDelete(oktaApplicationId);
			}
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			return new ResponseEntity<>(
				exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private String _getAuthorization() {
		return _liferayOAuth2AccessTokenManager.getAuthorization(
			"liferay-customer-etc-spring-boot-oahs");
	}

	private String _getSystemUserId() throws Exception {
		JSONObject systemUserJSONObject = new JSONObject(
			get(
				_getAuthorization(),
				UriComponentsBuilder.fromPath(
					"/o/headless-admin-user/v1.0/my-user-account"
				).build(
				).toUri()));

		return String.valueOf(systemUserJSONObject.getLong("id"));
	}

	private void _handleOnAfterAdd(
			String accountKey, long cloudNativeEnvironmentId,
			String oktaApplicationId)
		throws Exception {

		if ((oktaApplicationId != null) && !oktaApplicationId.isEmpty()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"CloudNativeEnvironment " + cloudNativeEnvironmentId +
						" already has oktaApplicationId " + oktaApplicationId +
							"; skipping creation.");
			}

			return;
		}

		String applicationName = _oktaAppNamePrefix + "-" + accountKey;

		String createdOktaApplicationId = _oktaService.createApplication(
			accountKey, applicationName);

		patch(
			_getAuthorization(),
			new JSONObject(
			).put(
				"oktaApplicationId", createdOktaApplicationId
			).toString(),
			UriComponentsBuilder.fromPath(
				"/o/c/cloudnativeenvironments/" + cloudNativeEnvironmentId
			).build(
			).toUri());
	}

	private void _handleOnAfterDelete(String oktaApplicationId)
		throws Exception {

		if ((oktaApplicationId == null) || oktaApplicationId.isEmpty()) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"onAfterDelete received no oktaApplicationId; skipping " +
						"deactivation.");
			}

			return;
		}

		_oktaService.deactivateApplication(oktaApplicationId);

		if (_log.isInfoEnabled()) {
			_log.info(
				"Deactivated Okta application " + oktaApplicationId + ".");
		}
	}

	private boolean _isSystemUpdate(Jwt jwt) throws Exception {
		return _getSystemUserId().equals(jwt.getSubject());
	}

	private static final Log _log = LogFactory.getLog(
		ObjectActionCloudNativeEnvironmentRestController.class);

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

	@Value("${liferay.customer.okta.app.name.prefix}")
	private String _oktaAppNamePrefix;

	@Autowired
	private OktaService _oktaService;

}
