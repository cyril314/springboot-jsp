package com.fit.web;

import com.fit.common.Constants;
import com.fit.common.utils.DateUtil;
import com.fit.common.utils.StringUtil;
import com.fit.common.utils.SqliteUtil;
import com.fit.common.utils.security.Encodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@Slf4j
@RequestMapping({"/"})
public class LoginController {

    @Value("${identifyingCode}")
    private String identifyingCode;

    @RequestMapping(value = {"login"}, method = {RequestMethod.GET})
    public String login() {
        return "system/login";
    }

    @RequestMapping({"index"})
    public String index(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String username = (String) session.getAttribute(Constants.SESSION_USER_NAME);
        String databaseType = Constants.DATABASE_TYPE;
        request.setAttribute("username", username);
        request.setAttribute("databaseType", databaseType);
        return "system/index";
    }

    @RequestMapping(value = {"loginVaildate"}, method = {RequestMethod.POST})
    public String loginVaildate(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String captcha = request.getParameter("captcha").toLowerCase();
        username = Encodes.escapeHtml(username.trim());
        HttpSession session = request.getSession(true);
        String cap = (String) session.getAttribute("KAPTCHA_SESSION_KEY");
        if (username != "" && username != null) {
            if (identifyingCode.equals("1") && !captcha.equals(cap)) {
                request.setAttribute("message", "验证码错误！");
                return "system/login";
            } else {
                String sql1 = " select * from treesoft_users where username='" + username + "'";
                List list1 = SqliteUtil.executeSqliteQuery(sql1);
                if (list1.size() <= 0) {
                    request.setAttribute("message", "您输入的帐号或密码有误！");
                    return "system/login";
                } else {
                    String pas = (String) ((Map) list1.get(0)).get("password");
                    if (!pas.equals(StringUtil.MD5(password + "treesoft"))) {
                        request.setAttribute("message", "您输入的帐号或密码有误！");
                        return "system/login";
                    } else {
                        session.setAttribute(Constants.SESSION_USER_NAME, username);
                        log.info("TreeNMS login user {} ,time {}", username, DateUtil.getTime());
                        SqliteUtil.initDbConfig();
                        String databaseType = Constants.DATABASE_TYPE;
                        request.setAttribute("username", username);
                        request.setAttribute("databaseType", databaseType);
                        return "redirect:/index";
                    }
                }
            }
        } else {
            request.setAttribute("message", "请输入帐号！");
            return "system/login";
        }
    }

    @RequestMapping({"logout"})
    public String logout(HttpServletRequest request) {
        Enumeration em = request.getSession().getAttributeNames();

        while (em.hasMoreElements()) {
            request.getSession().removeAttribute(((String) em.nextElement()).toString());
        }

        request.getSession().invalidate();
        return "system/login";
    }
}