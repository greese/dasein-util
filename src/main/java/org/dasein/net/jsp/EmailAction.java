/**
 * Copyright (C) 1998-2012 enStratus Networks Inc
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.net.jsp;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dasein.net.Email;

public class EmailAction extends HttpServlet {
	static private final long serialVersionUID = 3258126964249540401L;
	private String emailTarget = null;
    private String pagerTarget = null;
    private String redirect    = null;
    private String smtpHost    = null;
    
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
        emailTarget = cfg.getInitParameter("email-target");
        pagerTarget = cfg.getInitParameter("pager-target");
        if( emailTarget == null && pagerTarget == null ) {
            throw new ServletException("Either an email target or a pager " +
                                       "target must be specified (init " +
                                       "params 'email-target' and " +
                                       "'pager-target').");
        }
        smtpHost = cfg.getInitParameter("smtp-host");
        if( smtpHost == null ) {
            throw new ServletException("An SMTP host must be specified " +
                                       "(init param 'smtp-host').");
        }
        redirect = cfg.getInitParameter("redirect");
        if( redirect == null ) {
            throw new ServletException("A page to redirect to must be " +
                                       "specified (init param 'redirect').");
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        String email = req.getParameter("email");
        String name = req.getParameter("name");
        String content = req.getParameter("content");
        String subject = req.getParameter("subject");
        String pstr = req.getParameter("mode");
        Email mailer;
        String targ;

        if( content != null ) {
            content = content.trim();
        }
        if( content == null || content.length() < 1 ) {
            Exception e = new Exception("You must specify some content.");
            HashMap<String,Object> data = new HashMap<String,Object>();

            data.put("email", email);
            data.put("name", name);
            data.put("subject", subject);
            data.put("mode", pstr);
            req.getSession().setAttribute("formData", data);
            req.getSession().setAttribute("formError", e);
            resp.sendRedirect(redirect);
            return;
        }
        if( email == null ) {
            email = "Unknown";
        }
        else {
            email = email.trim();
            if( email.indexOf("\n") != -1 || email.indexOf("\r") != -1 ) {
                email = "Invalid Email";
            }
        }
        if( name == null ) {
            name = "Unknown Sender";
        }
        else {
            name = name.trim();
            if( name.indexOf("\n") != -1 || name.indexOf("\r") != -1 ) {
                name = "Invalid Name";
            }
        }
        if( subject == null ) {
            subject = "";
        }
        else {
            subject = subject.trim().replace('\n', '-').replace('\r', '-');
        }
        if( pstr == null || pstr.trim().equalsIgnoreCase("email") ) {
            if( emailTarget == null ) {
                throw new ServletException("No email target was configured.");
            }
            targ = emailTarget;
        }
        else {
            if( pagerTarget == null ) {
                throw new ServletException("No pager target was configured.");
            }
            targ = pagerTarget;
            if( content != null && content.length() > 109 ) {
                Exception e = new Exception("Pager messages are limited to " +
                                            "109 characters.");
                HashMap<String,Object> data = new HashMap<String,Object>();
                
                data.put("email", email);
                data.put("name", name);
                data.put("subject", subject);
                data.put("mode", pstr);
                req.getSession().setAttribute("formData", data);
                req.getSession().setAttribute("formError", e);
                resp.sendRedirect(redirect);
                return;
            }
        }
        mailer = new Email(targ, email, name, subject, content);
        try {
            mailer.send(smtpHost);
        }
        catch( IOException e ) {
            Exception err = new Exception("Could not connect to mail host.");
            HashMap<String,Object> data = new HashMap<String,Object>();
            
            data.put("email", email);
            data.put("name", name);
            data.put("subject", subject);
            data.put("mode", pstr);
            req.getSession().setAttribute("formData", data);
            req.getSession().setAttribute("formError", err);
            resp.sendRedirect(redirect);
            return;
        }
        if( redirect.indexOf("?") == -1 ) {
            resp.sendRedirect(redirect + "?done=true");
        }
        else {
            resp.sendRedirect(redirect + "&done=true");
        }
    }
}
