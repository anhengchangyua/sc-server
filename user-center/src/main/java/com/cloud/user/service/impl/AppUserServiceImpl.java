package com.cloud.user.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.cloud.common.utils.PageUtil;
import com.cloud.model.common.Page;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysPermission;
import com.cloud.model.user.SysRole;
import com.cloud.user.dao.AppUserDao;
import com.cloud.user.dao.UserRoleDao;
import com.cloud.user.service.AppUserService;
import com.cloud.user.service.SysPermissionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AppUserServiceImpl implements AppUserService {

	@Autowired
	private AppUserDao appUserDao;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private SysPermissionService sysPermissionService;
	@Autowired
	private UserRoleDao userRoleDao;

	@Override
	public void addAppUser(AppUser appUser) {
		AppUser user = appUserDao.findByUsername(appUser.getUsername());
		if (user != null) {
			throw new IllegalArgumentException("用户名已存在");
		}

		appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
		appUser.setEnabled(Boolean.TRUE);
		appUser.setCreateTime(new Date());
		appUser.setUpdateTime(appUser.getCreateTime());

		appUserDao.save(appUser);
		log.info("添加用户：{}", appUser);
	}

	@Override
	public void updateAppUser(AppUser appUser) {
		appUser.setUpdateTime(new Date());

		appUserDao.update(appUser);
		log.info("修改用户：{}", appUser);
	}

	@Transactional
	@Override
	public LoginAppUser findByUsername(String username) {
		AppUser appUser = appUserDao.findByUsername(username);
		if (appUser != null) {
			LoginAppUser loginAppUser = new LoginAppUser();
			BeanUtils.copyProperties(appUser, loginAppUser);

			Set<SysRole> sysRoles = userRoleDao.findRolesByUserId(appUser.getId());
			loginAppUser.setSysRoles(sysRoles);// 设置角色

			if (!CollectionUtils.isEmpty(sysRoles)) {
				Set<Long> roleIds = sysRoles.parallelStream().map(r -> r.getId()).collect(Collectors.toSet());
				Set<SysPermission> sysPermissions = sysPermissionService.findByRoleIds(roleIds);
				if (!CollectionUtils.isEmpty(sysPermissions)) {
					Set<String> permissions = sysPermissions.parallelStream().map(p -> p.getPermission())
							.collect(Collectors.toSet());

					loginAppUser.setPermissions(permissions);// 设置权限集合
				}

			}

			return loginAppUser;
		}

		return null;
	}

	@Override
	public AppUser findById(Long id) {
		return appUserDao.findById(id);
	}

	/**
	 * 给用户设置角色
	 */
	@Transactional
	@Override
	public void setRoleToUser(Long id, Set<Long> roleIds) {
		AppUser appUser = appUserDao.findById(id);
		if (appUser == null) {
			throw new IllegalArgumentException("用户不存在");
		}

		userRoleDao.deleteUserRole(id, null);
		if (!CollectionUtils.isEmpty(roleIds)) {
			roleIds.forEach(roleId -> {
				userRoleDao.saveUserRoles(id, roleId);
			});
		}

		log.info("修改用户：{}的角色，{}", appUser.getUsername(), roleIds);
	}

	@Override
	public void updatePassword(Long id, String oldPassword, String newPassword) {
		AppUser appUser = appUserDao.findById(id);
		if (StringUtils.isNoneBlank(oldPassword)) {
			if (!passwordEncoder.matches(oldPassword, appUser.getPassword())) {
				throw new IllegalArgumentException("旧密码错误");
			}
		}

		AppUser user = new AppUser();
		user.setId(id);
		user.setEnabled(appUser.isEnabled());
		user.setPassword(passwordEncoder.encode(newPassword));

		updateAppUser(user);
		log.info("修改密码：{}", user);
	}

	@Override
	public Page<AppUser> findUsers(Map<String, Object> params) {
		int total = appUserDao.count(params);
		List<AppUser> list = Collections.emptyList();
		if (total > 0) {
			PageUtil.pageParamConver(params);

			list = appUserDao.findData(params);
		}
		return new Page<>(total, list);
	}

	@Override
	public Set<SysRole> findRolesByUserId(Long userId) {
		return userRoleDao.findRolesByUserId(userId);
	}

}
