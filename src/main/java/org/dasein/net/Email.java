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

package org.dasein.net;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class Email {
    static public final int SMTP = 25;
    
    private String addressee  = null;
    private String content    = null;
    private String sender     = null;
    private String senderName = null;
    private String subject    = null;
    
    public Email(String to, String from, String nom, String sub, String cnt) {
        super();
        addressee = to;
        sender = from;
        senderName = nom;
        subject = sub;
        content = cnt;
    }

    public void send(String svr) throws IOException {
        PrintStream out = null;
        Socket s = null;

        try {
            s = new Socket(svr, SMTP);
            out = new PrintStream(s.getOutputStream());
            out.println("HELO " + InetAddress.getLocalHost().getHostAddress() +
                        "\r\n");
            out.flush();
            out.print("MAIL FROM: " + sender + "\r\n");
            out.flush();
            out.print("RCPT TO: " + addressee + "\r\n");
            out.flush();
            out.print("DATA\r\n");
            out.flush();
            out.print("From: " + senderName + "<" + sender + ">\r\n");
            out.flush();
            out.print("To: Page George <" + addressee + ">\r\n");
            out.flush();
            if( subject != null && subject.length() > 0 ) {
                out.print("Subject: " + subject + "\r\n");
            }
            out.print("X-pager: George's pager\r\n");
            out.print("\r\n");
            out.print(content);
            out.print("\r\n.\r\n");
            out.flush();
            out.print("QUIT\r\n");
            out.flush();
        }
        finally {
            if( out != null ) {
                out.close();
            }
            try {
                if( s != null ) {
                    s.close();
                }
            }
            catch( IOException e ) { // ignore
            }
        }
    }
}
