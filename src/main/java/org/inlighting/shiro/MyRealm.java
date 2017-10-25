package org.inlighting.shiro;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.inlighting.database.Service;
import org.inlighting.database.UserBean;
import org.inlighting.util.JWTUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MyRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LogManager.getLogger(MyRealm.class);

    private Service service;

    MyRealm() {
        service = new Service();
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }



    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = principals.toString();
        LOGGER.error("check permission: "+username);
        UserBean user = service.getUser(username);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(user.getRole());
        Set<String> permission = new HashSet<>(Arrays.asList(user.getPermission().split(",")));
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    /**
     * 进行用户名正确与否验证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        System.out.println("start realm");
        String username = (String) token.getPrincipal();
        UserBean user = service.getUser(username);
        if (user == null) {
            throw new AuthenticationException("username or password error");
        }
        return new SimpleAuthenticationInfo(user.getUsername(), JWTUtil.sign(username, user.getPassword()), "my_realm");
    }
}
