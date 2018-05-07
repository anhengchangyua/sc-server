package com.cloud.oauth.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import com.cloud.model.user.LoginAppUser;
import com.cloud.oauth.service.impl.RedisAuthorizationCodeServices;

/**
 * 授权服务器配置
 * 
 * @author jh
 *
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	/**
	 * 认证管理器
	 * 
	 * @see SecurityConfig 的authenticationManagerBean()
	 */
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;
	@Autowired
	private DataSource dataSource;
	/**
	 * 使用jwt或者redis
	 */
	@Value("${access_token.store-jwt:false}")
	private boolean storeWithJwt;
	@Autowired
	private RedisAuthorizationCodeServices redisAuthorizationCodeServices;

	/**
	 * 令牌存储
	 * 
	 * @return
	 */
	@Bean
	public TokenStore tokenStore() {
		if (storeWithJwt) {
			return new JwtTokenStore(accessTokenConverter());
		}
		return new RedisTokenStore(redisConnectionFactory);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(this.authenticationManager);
		endpoints.tokenStore(tokenStore());
		// 授权码模式下，code存储
//		endpoints.authorizationCodeServices(new JdbcAuthorizationCodeServices(dataSource));
		endpoints.authorizationCodeServices(redisAuthorizationCodeServices);
		if (storeWithJwt) {
			endpoints.accessTokenConverter(accessTokenConverter());
		}
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.allowFormAuthenticationForClients();
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//		clients.inMemory().withClient("system").secret("system")
//				.authorizedGrantTypes("password", "authorization_code", "refresh_token").scopes("app")
//				.accessTokenValiditySeconds(3600);

		clients.jdbc(dataSource);
	}

	/**
	 * Jwt资源令牌转换器
	 * 
	 * @return accessTokenConverter
	 */
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		return new JwtAccessTokenConverter() {

			/**
			 * 重写增强token的方法
			 * 
			 * @param accessToken
			 *            资源令牌
			 * @param authentication
			 *            认证
			 * @return 增强的OAuth2AccessToken对象
			 */
			@Override
			public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
				Object principal = authentication.getUserAuthentication().getPrincipal();
				if (principal instanceof LoginAppUser) {
					LoginAppUser loginAppUser = (LoginAppUser) principal;
					Map<String, Object> infoMap = new HashMap<>();
					infoMap.put("username", loginAppUser.getUsername());
					((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(infoMap);
				}

				return super.enhance(accessToken, authentication);
			}
		};
	}

}
