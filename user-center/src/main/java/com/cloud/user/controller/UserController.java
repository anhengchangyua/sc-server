package com.cloud.user.controller;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloud.common.utils.AppUserUtil;
import com.cloud.model.common.Page;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysRole;
import com.cloud.model.user.constants.UserType;
import com.cloud.user.service.AppUserService;

@RestController
public class UserController {

    @Autowired
    private AppUserService appUserService;

    /**
     * 当前登录用户 LoginAppUser
     *
     * @return
     */
    @GetMapping("/users/current")
    public LoginAppUser getLoginAppUser() {
        return AppUserUtil.getLoginAppUser();
    }

    //登录时用到的
    @GetMapping(value = "/users-anon/internal", params = "username")
    public LoginAppUser findByUsername(String username) {
        return appUserService.findByUsername(username);
    }

    /**
     * 用户查询
     *
     * @param params
     * @return
     */
    @PreAuthorize("hasAuthority('back:user:query')")
    @GetMapping("/users")
    public Page<AppUser> findUsers(@RequestParam Map<String, Object> params) {
        return appUserService.findUsers(params);
    }

    @PreAuthorize("hasAuthority('back:user:query')")
    @GetMapping("/users/{id}")
    public AppUser findUserById(@PathVariable Long id) {
        return appUserService.findById(id);
    }

    /**
     * 添加用户
     *
     * @param appUser
     * @return
     */
    @PostMapping("/users-anon/register")
    public AppUser register(@RequestBody AppUser appUser) {
        if (StringUtils.isBlank(appUser.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (StringUtils.isBlank(appUser.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (StringUtils.isBlank(appUser.getNickname())) {
            appUser.setNickname(appUser.getUsername());
        }

        if (StringUtils.isBlank(appUser.getType())) {
            appUser.setType(UserType.APP.name());
        }

        appUserService.addAppUser(appUser);

        return appUser;
    }

    /**
     * 修改自己的个人信息
     *
     * @param appUser
     * @return
     */
    @LogAnnotation(module = LogModule.UPDATE_ME)
    @PutMapping("/users/me")
    public AppUser updateMe(@RequestBody AppUser appUser) {
        AppUser user = AppUserUtil.getLoginAppUser();
        appUser.setId(user.getId());
        appUser.setEnabled(user.isEnabled());

        appUserService.updateAppUser(appUser);

        return appUser;
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     */
    @LogAnnotation(module = LogModule.UPDATE_PASSWORD)
    @PutMapping(value = "/users/password", params = {"oldPassword", "newPassword"})
    public void updatePassword(String oldPassword, String newPassword) {
        if (StringUtils.isBlank(oldPassword)) {
            throw new IllegalArgumentException("旧密码不能为空");
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new IllegalArgumentException("新密码不能为空");
        }

        AppUser user = AppUserUtil.getLoginAppUser();
        appUserService.updatePassword(user.getId(), oldPassword, newPassword);
    }

    /**
     * 管理后台，给用户重置密码
     *
     * @param id
     * @param newPassword
     */
    @LogAnnotation(module = LogModule.RESET_PASSWORD)
    @PreAuthorize("hasAuthority('back:user:password')")
    @PutMapping(value = "/users/{id}/password", params = {"newPassword"})
    public void resetPassword(@PathVariable Long id, String newPassword) {
        appUserService.updatePassword(id, null, newPassword);
    }

    /**
     * 管理后台修改用户
     *
     * @param appUser
     */
    @LogAnnotation(module = LogModule.UPDATE_USER)
    @PreAuthorize("hasAuthority('back:user:update')")
    @PutMapping("/users")
    public void updateAppUser(@RequestBody AppUser appUser) {
        appUserService.updateAppUser(appUser);
    }

    /**
     * 管理后台给用户分配角色
     *
     * @param id
     * @param roleIds
     */
    @LogAnnotation(module = LogModule.SET_ROLE)
    @PreAuthorize("hasAuthority('back:user:role:set')")
    @PostMapping("/users/{id}/roles")
    public void setRoleToUser(@PathVariable Long id, @RequestBody Set<Long> roleIds) {
        appUserService.setRoleToUser(id, roleIds);
    }

    /**
     * 获取用户的角色
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasAnyAuthority('back:user:role:set','user:role:byuid')")
    @GetMapping("/users/{id}/roles")
    public Set<SysRole> findRolesByUserId(@PathVariable Long id) {
        return appUserService.findRolesByUserId(id);
    }

}
