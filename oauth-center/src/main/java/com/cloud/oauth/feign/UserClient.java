package com.cloud.oauth.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cloud.model.user.LoginAppUser;

@FeignClient("user-center")
public interface UserClient {

	@GetMapping(value = "/users-anon/internal", params = "username")
	LoginAppUser findByUsername(@RequestParam("username") String username);
}
