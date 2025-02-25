package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.email.IEmailProvider;
import com.lanternsoftware.powermonitor.email.MailJetProvider;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.powermonitor.datamodel.EmailCredentials;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.email.EmailValidator;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.servlet.FreemarkerConfigUtil;
import com.lanternsoftware.util.servlet.FreemarkerServlet;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

@WebServlet("/resetPassword/*")
public class ResetPasswordServlet extends FreemarkerServlet {
    protected static final Logger LOG = LoggerFactory.getLogger(ResetPasswordServlet.class);
    protected static final Configuration CONFIG = FreemarkerConfigUtil.createConfig(ResetPasswordServlet.class, "/templates", 100);
    protected static final EmailCredentials credentials = DaoSerializer.parse(ResourceLoader.loadFileAsString(LanternFiles.CONFIG_PATH + "email.json"), EmailCredentials.class);
    protected static final IEmailProvider provider = new MailJetProvider();

    @Override
    protected Configuration getFreemarkerConfig() {
        return CONFIG;
    }

    @Override
    protected void doGet(HttpServletRequest _req, HttpServletResponse _resp) {
        String[] path = getPath(_req);
        String email = Globals.dao.getEmailForResetKey(CollectionUtils.get(path, 1));
        if (EmailValidator.getInstance().isValid(email)) {
            render(_resp, "passwordReset.ftl", model(_req, "key", path[1]));
        } else {
            render(_resp, "passwordResetMsg.ftl", model(_req, "msg", "This password reset code is no longer valid.  Please try sending a new code from the Lantern Power Monitor application."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest _req, HttpServletResponse _resp) {
        if (NullUtils.isEqual(_req.getContentType(), MediaType.APPLICATION_FORM_URLENCODED)) {
            String key = _req.getParameter("reset_key");
            String password = _req.getParameter("password");
            if (NullUtils.length(password) < 8) {
                render(_resp, "passwordReset.ftl", model(_req, "key", key).and("error", "Your password must be at least 8 characters."));
                return;
            }
            Globals.dao.resetPassword(key, password);
            render(_resp, "passwordResetMsg.ftl", model(_req, "msg", "Your password has been changed."));
        } else {
            DaoEntity payload = getRequestZipBson(_req);
            String email = DaoSerializer.getString(payload, "email");
            Account account = Globals.dao.getAccountByUsername(email);
            if ((account != null) && EmailValidator.getInstance().isValid(email)) {
                String key = Globals.dao.addPasswordResetKey(email);
                int status = provider.sendTextEmail(credentials, email, "Password Reset - Lantern Power Monitor", "Reset your password using this link:\n" + credentials.getServerUrlBase() + "resetPassword/" + key);
                zipBsonResponse(_resp, new DaoEntity("success", status == 200));
            }
            else
                _resp.setStatus(400);
        }
    }
}
