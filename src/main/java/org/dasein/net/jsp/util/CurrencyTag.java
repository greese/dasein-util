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

package org.dasein.net.jsp.util;

import java.util.Currency;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.net.jsp.ELParser;

public class CurrencyTag extends TagSupport {
    private static final long serialVersionUID = 4623231106195354388L;
    
    private String isoCode      = null;
    private Locale locale       = null;
    private String var          = null;
    private String varSymbol    = null;
    private String varException = null;
    
    public int doEndTag() throws JspException {
        try {
            Currency currency;
            
            if( locale == null ) {
                locale = pageContext.getRequest().getLocale();
                if( locale == null ) {
                    locale = Locale.getDefault();
                }
            }
            if( isoCode == null ) {
                currency = Currency.getInstance(locale);
            }
            else {
                currency = Currency.getInstance(isoCode);
            }
            if( var != null ) {
                pageContext.setAttribute(var, currency);
            }
            if( varSymbol != null ) {
                pageContext.setAttribute(varSymbol, currency == null ? null : currency.getSymbol(locale));
            }
            if( varException != null ) {
                pageContext.setAttribute(varException, null);
            }
        }
        catch( RuntimeException e ) {
            if( varException == null ) {
                throw new JspException(e);
            }
            pageContext.setAttribute(varException, e);
        }
        catch( Error e ) {
            if( varException == null ) {
                throw new JspException(e);
            }
            pageContext.setAttribute(varException, e);
        }
        finally {
            isoCode = null;
            locale = null;
            var = null;
            varSymbol = null;
            varException = null;
        }
        return EVAL_PAGE;        
    }
    
    public void setIsoCode(String c) throws JspException {
        isoCode = (new ELParser(c)).getStringValue(pageContext);
    }
    
    public void setLocale(String l) throws JspException {
        Object ob = (new ELParser(l)).getValue(pageContext);
        
        if( ob != null ) {
            if( ob instanceof Locale ) {
                locale = (Locale)ob;
            }
            else {
                String[] parts = ob.toString().split("_");
                
                if( parts.length == 3 ) {
                    locale = new Locale(parts[0], parts[1], parts[2]);
                }
                else if( parts.length == 2 ) {
                    locale = new Locale(parts[0], parts[1]);
                }
                else {
                    locale = new Locale(ob.toString());
                }
            }
        }        
    }
    
    public void setVar(String v) {
        var = v;
    }
    
    public void setVarException(String v) {
        varException = v;
    }
    
    public void setVarSymbol(String v) {
        varSymbol = v;
    }
}
