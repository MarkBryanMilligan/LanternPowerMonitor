package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.email.EmailValidator;
import com.lanternsoftware.util.servlet.FreemarkerConfigUtil;
import com.lanternsoftware.util.servlet.FreemarkerServlet;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@WebServlet("/resetPassword/*")
public class ResetPasswordServlet extends FreemarkerServlet {
    protected static final Logger LOG = LoggerFactory.getLogger(ResetPasswordServlet.class);
    protected static final Configuration CONFIG = FreemarkerConfigUtil.createConfig(ResetPasswordServlet.class, "/templates", 100);
    protected static final String api_key = ResourceLoader.loadFileAsString(LanternFiles.CONFIG_PATH + "sendgrid.txt");

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
            if (EmailValidator.getInstance().isValid(email)) {
                String key = Globals.dao.addPasswordResetKey(email);
                Email from = new Email("info@lanternsoftware.com");
                String subject = "Password Reset - Lantern Power Monitor";
                Email to = new Email(email);
                Content content = new Content("text/plain", "Reset your password using this link:\nhttps://lanternsoftware.com/currentmonitor/resetPassword/" + key);
                Mail mail = new Mail(from, subject, to, content);
                SendGrid sg = new SendGrid(api_key);
                Request request = new Request();
                try {
                    request.setMethod(Method.POST);
                    request.setEndpoint("mail/send");
                    request.setBody(mail.build());
                    Response response = sg.api(request);
                    zipBsonResponse(_resp, new DaoEntity("success", response.getStatusCode() == 200));
                } catch (IOException ex) {
                    LOG.error("Failed to send password reset email", ex);
                    _resp.setStatus(500);
                }
            }
            else
                _resp.setStatus(400);
        }
    }
}
