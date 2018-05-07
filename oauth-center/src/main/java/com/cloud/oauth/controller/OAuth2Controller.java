package com.cloud.oauth.controller;

import java.security.Principal;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.model.log.Log;
import com.cloud.model.log.constants.LogModule;
import com.cloud.oauth.feign.LogClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class OAuth2Controller {

	@Autowired
	private TokenStore tokenStore;

	/**
	 * 登陆用户信息
	 * 
	 * @param principal
	 * @return
	 */
	@GetMapping("/user-me")
	public Principal principal(Principal principal) {
		log.debug("user-me:{}", principal.getName());
		return principal;
	}

	/**
	 * 移除access_token和refresh_token
	 * 
	 * @param access_token
	 */
	@DeleteMapping(value = "/remove_token", params = "access_token")
	public void removeToken(Principal principal, String access_token) {
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(access_token);
		if (accessToken != null) {
			// 移除access_token
			tokenStore.removeAccessToken(accessToken);

			// 移除refresh_token
			tokenStore.removeRefreshToken(accessToken.getRefreshToken());

			saveLogoutLog(principal.getName());
		}
	}

	@Autowired
	private LogClient logClient;

	/**
	 * 退出日志
	 * 
	 * @param username
	 */
	private void saveLogoutLog(String username) {
		log.info("{}退出", username);
		// 异步
		CompletableFuture.runAsync(() -> {
			try {
				Log log = Log.builder().username(username).module(LogModule.LOGOUT).createTime(new Date()).build();
				logClient.save(log);
			} catch (Exception e) {
				// do nothing
			}

		});
	}

}
