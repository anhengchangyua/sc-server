package com.cloud.oauth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cloud.model.user.LoginAppUser;
import com.cloud.oauth.feign.UserClient;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserClient userClient;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		LoginAppUser loginAppUser = userClient.findByUsername(username);
		if (loginAppUser == null) {
			throw new AuthenticationCredentialsNotFoundException("用户名不存在");
		} else if (!loginAppUser.isEnabled()) {
			throw new DisabledException("用户已作废");
		}

		return loginAppUser;
	}

}
