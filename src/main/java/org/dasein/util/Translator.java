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

/* $Id: Translator.java,v 1.6 2007/11/15 21:18:57 greese Exp $ */
/* Copyright © 2003 George Reese, All Rights Reserved */
package org.dasein.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * <br/>
 * Last modified: $Date
 * @version $Revision: 1.6 $
 * @author George Reese (http://george.reese.name)
 */
public class Translator<T> extends PseudoMap<Locale,Translation<T>> implements DelegatedComparable, Serializable {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3258417231141483574L;

    static public Locale parseLocale(String iso) {
        String ctry, lang;
        int i;

        if( iso == null ) {
            return Locale.getDefault();
        }
        i = iso.indexOf("_");
        if( i != 2 ) {
            if( i == -1 ) {
                return new Locale(iso);
            }
            else {
                throw new InvalidLocaleException(iso);
            }
        }
        lang = iso.substring(0, 2);
        ctry = iso.substring(3);
        i = ctry.indexOf("_");
        if( i != -1 ) {
            if( i != 2 ) {
                throw new InvalidLocaleException(iso);
            }
            else {
                // this system ignores variants
                ctry = ctry.substring(0, 1);
            }
        }
        return new Locale(lang, ctry);
    }
    
    static public Map<String,Translator<String>> load(String fromXmlResource) throws IOException, SAXException, ParserConfigurationException {
        InputStream input = Translator.class.getResourceAsStream(fromXmlResource);
        HashMap<String,Translator<String>> translations = new HashMap<String,Translator<String>>();
        
        if( input == null ) {
            return translations;
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        StringBuffer sb = new StringBuffer();
        String line;
            
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();
            
        ByteArrayInputStream bas = new ByteArrayInputStream(sb.toString().getBytes());
            
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.parse(bas);
        bas.close();
        
        NodeList matches = doc.getElementsByTagName("translator");
        for( int i=0; i<matches.getLength(); i++ ) {
            Node node = matches.item(i);
            Node keyAttr = node.getAttributes().getNamedItem("key");

            HashMap<Locale,String> translated = new HashMap<Locale,String>();
            String key = keyAttr.getNodeValue();
            NodeList list = node.getChildNodes();
            
            for( int j=0; j<list.getLength(); j++ ) {
                Node t = list.item(j);
                
                if( t.getNodeName().equals("translation") ) {
                    Locale loc = parseLocale(t.getAttributes().getNamedItem("locale").getNodeValue());
                    String val = t.getFirstChild().getNodeValue();
                    
                    translated.put(loc, val);
                }
            }
            translations.put(key, new Translator<String>(translated));
        }
        return translations;
    }
    
    private String                                     attributeKey;
    private HashMap<String,Map<String,Translation<T>>> translations;
    private String                                     resourceBundleName;
    
    private Translator() {
        super();
        translations = new HashMap<String,Map<String,Translation<T>>>();
    }
    
    public Translator(String rscName, String attr) {
        super();
        attributeKey = attr;
        resourceBundleName = rscName;
    }
    
    public Translator(Map<Locale,T> trans) {
        this();
        for( Locale loc: trans.keySet() ) {
            store(loc, trans.get(loc));
        }
    }   

    public Translator(Locale loc, T msg) {
        this();
        store(loc, msg);
    }

    public boolean containsKey(Object key) {
        if( key instanceof String ) {
            return translations.containsKey(key);
        }
        else if( key instanceof Locale ) {
            String lang = ((Locale)key).getLanguage();

            return translations.containsKey(lang);
        }
        else {
            return false;
        }
    }

    public boolean containsValue(Object val) {
        for( String lang : translations.keySet() ) {
            Map<String,Translation<T>> vals = translations.get(lang);

            if( vals.containsValue(val) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies the current translator into a new translator object. Even if this translator is resource bundle based,
     * the copy will not be.
     * @return a copy of the current translator
     */
    public Translator<T> copy() {
        Translator<T> trans = new Translator<T>();

        if( isBundleBased() ) {
            for( Translation<T> t : values() ) {
                Locale loc = t.getLocale();
                
                trans.store(loc, t.getData());
            }
        }
        else {
            for( String lang : translations.keySet() ) {
                Map<String,Translation<T>> map = translations.get(lang);
                
                for( String ctry : map.keySet() ) {
                    trans.store(lang, ctry, map.get(ctry));
                }
            }
        }
        return trans;
    }
    
    public Set<Entry<Locale,Translation<T>>> entrySet() {
        Map<Locale,Translation<T>> tmp = new HashMap<Locale,Translation<T>>();

        if( isBundleBased() ) {
            for( Translation<T> t : values() ) {
                tmp.put(t.getLocale(), t);
            }
        }
        else {
            for (Map<String,Translation<T>> m : translations.values()) {
                for (Translation<T> t : m.values()) {
                    tmp.put(t.getLocale(), t);
                }
            }
        }
        return tmp.entrySet();
    }

    public boolean equals(Object ob) {
        Translator<?> other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (Translator<?>)ob;
        if( isBundleBased() ) {
            if( !other.isBundleBased() ) {
                return false;
            }
            return (resourceBundleName.equals(other.resourceBundleName) && attributeKey.equals(other.attributeKey));
        }
        else {
            if( other.isBundleBased() ) {
                return false;
            }
            return translations.equals(other.translations);
        }
    }
    
    public Translation<T> get(Object key) {
        if( key instanceof String ) {
            return getTranslation(new Locale((String)key));
        }
        else if( key instanceof Locale ) {
            return getTranslation((Locale)key);
        }
        return null;
    }
    
    public Translation<T> get(Collection<Locale> locales) {
        return getTranslation(locales);
    }

    public Translation<T> getAnyTranslation() {
        if( isBundleBased() ) {
            return getBundleTranslation(Locale.getDefault());
        }
        for( Entry<String,Map<String,Translation<T>>> entry : translations.entrySet() ) {
            Map<String,Translation<T>> ctrys = entry.getValue();
            
            if( ctrys.size() < 1 ) {
                continue;
            }
            if( ctrys.containsKey(null) ) {
                return ctrys.get(null);
            }
            else {
                String ctry = ctrys.keySet().iterator().next();
                
                return ctrys.get(ctry);
            }
        }
        // this should never be reached
        return null;
    }
    
    public Translation<T> getBestTranslation(String lang) {
        if( !isBundleBased() ) {
            Map<String, Translation<T>> map;
            
            if( !translations.containsKey(lang) ) {
                return null;
            }
            map = translations.get(lang);
            if( map.size() < 1 ) {
                return null;
            }
            return map.get(map.keySet().iterator());
        }
        else {
            return getBundleTranslation(new Locale(lang));
        }
    }
    
    @SuppressWarnings("unchecked")
    public Translation<T> getBundleTranslation(Locale loc) {
        ResourceBundle bundle = ResourceBundle.getBundle(resourceBundleName, loc);
        T val = (T)bundle.getObject(attributeKey);
        
        return new Translation<T>(bundle.getLocale(), val);
    }
    
    public Translation<T> getBestTranslation(String lang, String ctry) {
        return getBestTranslation(new Locale(lang, ctry));
    }
    
    public Translation<T> getBestTranslation(Locale loc) {
        if( isBundleBased() ) {
            return getBundleTranslation(loc);
        }
        else {
            Map<String, Translation<T>> map;
            String lang = loc.getLanguage();
            String ctry = loc.getCountry();
            
            if( !translations.containsKey(lang) ) {
                return null;
            }
            map = translations.get(lang);
            if( map.size() < 1 ) {
                return null;
            }
            if( map.containsKey(ctry) ) {
                return map.get(ctry);
            }
            else {
                for( Translation<T> t : map.values() ) {
                    if( t != null ) {
                        return t;
                    }
                }
                return null;
            }
        }
    }
   
    public Object getDelegate(Locale loc) {
        return getTranslation(loc).getData();
    }
    
    @SuppressWarnings("unchecked")
    public Translation<T> getExactTranslation(Locale loc) {
        if( isBundleBased() ) {
            ResourceBundle bundle = ResourceBundle.getBundle(resourceBundleName, loc);
            
            if( !loc.equals(bundle.getLocale()) ) {
                return null;
            }
            return new Translation<T>(bundle.getLocale(), (T)bundle.getObject(attributeKey));
        }
        else {
            String lang = loc.getLanguage();
            String ctry = loc.getCountry();
            Map<String, Translation<T>> map;
            
            if( !translations.containsKey(lang) ) {
                return null;
            }
            map = translations.get(lang);
            if( map.containsKey(ctry) ) {
                return map.get(ctry);
            }
            if( ctry != null && map.containsKey(null) ) {
                return map.get(null);
            }
            else if( (ctry == null) || (map.size() == 1) ) {
                return map.get(map.keySet().iterator().next());
            }
            return null;
        }
    }

    public Translation<T> getTranslation(Locale loc) {
        Translation<T> trans = getExactTranslation(loc);

        if( trans == null ) {
            trans = getBestTranslation(loc);
            if( trans == null ) {
                if( !loc.equals(Locale.getDefault()) ) {
                    trans = getBestTranslation(Locale.getDefault());
                    if( trans == null ) {
                        trans = getAnyTranslation();      
                    }
                }
            }
        }
        return trans;
    }
    
    public Translation<T> getTranslation(Collection<Locale> locales) {
        Translation<T> trans;

        if( !isBundleBased() && translations.size() < 1 ) {
            return null;
        }
        for( Locale loc : locales ) {
            trans = getExactTranslation(loc);
            if( trans != null ) {
                for( Locale l: locales ) {
                    if( l.equals(loc) ) {
                        return trans;
                    }
                    if( !l.getLanguage().equals(loc.getLanguage()) ) {
                        Translation<T> tmp = getBestTranslation(l);

                        if( tmp != null ) {
                            return tmp;
                        }
                    }
                }
                return trans;
            }
        }
        for( Locale loc : locales ) {
            trans = getBestTranslation(loc);
            if( trans != null ) {
                return trans;
            }
        }
        trans = getBestTranslation(Locale.getDefault());
        if( trans != null ) {
            return trans;
        }
        else {
            return getAnyTranslation();
        }
    }

    public Translation<T> getTranslation(String lang, String ctry) {
        return getTranslation(new Locale(lang, ctry));
    }
    
    private transient int hashCode = -1;
    
    public int hashCode() {
        if( hashCode == -1 ) {
            String tmp;
            
            if( translations == null ) {
                tmp = "null:";
            }
            else {
                tmp = String.valueOf(translations.hashCode()) + ":";
            }
            tmp = tmp + (resourceBundleName == null ? "null" : resourceBundleName) + ":";
            tmp = tmp + (attributeKey == null ? "null" : attributeKey);
            hashCode = tmp.hashCode();
        }
        return hashCode;
    }

    public boolean isBundleBased() {
        return (resourceBundleName != null);
    }
    
    public boolean isEmpty() {
        if( isBundleBased() ) {
            ResourceBundle bundle = ResourceBundle.getBundle(resourceBundleName, Locale.getDefault());
            
            return (bundle.getObject(attributeKey) == null);
        }
        else {
            return translations.isEmpty();
        }
    }

    private transient Set<Locale> locales = null;
    
    public Set<Locale> keySet() {
        if( isBundleBased() ) {
            if( locales == null ) {
                TreeSet<Locale> matches = new TreeSet<Locale>();
                
                for( Locale loc : Locale.getAvailableLocales() ) {
                    ResourceBundle b = ResourceBundle.getBundle(resourceBundleName, loc);
                    
                    if( loc.equals(b.getLocale()) ) {
                        matches.add(loc);
                    }
                }
                locales = matches;
            }
            return locales;
        }
        else {
            HashSet<Locale> locales = new HashSet<Locale>();
            
            for( String lang : translations.keySet() ) {
                Map<String, Translation<T>> map = translations.get(lang);
                
                for( String ctry : map.keySet() ) {
                    if( ctry == null ) {
                        locales.add(new Locale(lang));
                    }
                    else {
                        locales.add(new Locale(lang, ctry));
                    }
                }
            }
            return locales;
        }
    }

    public Iterator<String> countries(String lang) {
        if( isBundleBased() ) {
            TreeSet<String> countries = new TreeSet<String>();
            
            for( Locale loc : keySet() ) {
                if( loc.getLanguage().equals(lang) ) {
                    countries.add(loc.getCountry());
                }
            }
            return countries.iterator();
        }
        else {
            Map<String, Translation<T>> map = translations.get(lang);

            return map.keySet().iterator();
        }
    }
    
    public Iterator<String> languages() {
        if( isBundleBased() ) {
            TreeSet<String> languages = new TreeSet<String>();
            
            for( Locale loc : keySet() ) {
                languages.add(loc.getLanguage());
            }
            return languages.iterator();
        }
        else {
            return translations.keySet().iterator();
        }
    }

    public Translator<T> newTranslator(Translator<T> t) {
        Translator<T> trans = copy();
        
        if( t.isBundleBased() ) {
            Iterator<String> langs = t.languages();
            
            while( langs.hasNext() ) {
                String lang = langs.next();
                Iterator<String> ctrys;
                
                ctrys = t.countries(lang);
                while( ctrys.hasNext() ) {
                    String ctry = ctrys.next();
                    Locale loc;
                    
                    if( ctry == null ) {
                        loc = new Locale(lang);
                    }
                    else {
                        loc = new Locale(lang, ctry);
                    }
                    trans.store(lang, ctry, t.getExactTranslation(loc));
                }
            }
        }
        else {
            for( String lang : t.translations.keySet() ) {
                Map<String,Translation<T>> map = t.translations.get(lang);
                
                for( String ctry : map.keySet() ) {
                    trans.store(lang, ctry, map.get(ctry));
                }
            }
        }
        return trans;
    }

    public Translator<T> newTranslator(Locale loc, T val) {
        Translator<T> trans = copy();
        
        trans.store(loc, val);
        return trans;
    }
    
    public Translator<T> newTranslator(Map<String,Map<String,T>> additional) {
        Translator<T> trans = copy();
        
        for( String lang : additional.keySet() ) {
            Map<String,T> map = additional.get(lang);
            
            for( String ctry : map.keySet() ) {
                trans.store(lang, ctry, map.get(ctry));
            }
        }
        return trans;
    }
    
    public int size() {
        int sz = 0;

        if( isBundleBased() ) {
            Iterator<String> langs = languages();
            
            while( langs.hasNext() ) {
                String lang = langs.next();
                Iterator<String> ctrys;
                
                ctrys = countries(lang);
                while( ctrys.hasNext() ) {
                    sz++;
                }
            }
        }
        else {
            for( String lang : translations.keySet() ) {
                Map<String, Translation<T>> vals = translations.get(lang);
    
                sz += vals.size();
            }
        }
        return sz;
    }

    private void store(Locale loc, T item) {
        store(loc.getLanguage(), loc.getCountry(), new Translation<T>(loc, item));
    }
    
    private void store(String lang, String ctry, T item) {
        Locale loc;
        
        if( ctry == null ) {
            loc = new Locale(lang);
        }
        else {
            loc = new Locale(lang, ctry);
        }
        store(lang, ctry, new Translation<T>(loc, item));
    }
    
    private void store(String lang, String ctry, Translation<T> item) {
        if( isBundleBased() ) {
            throw new RuntimeException("Cannot store in resource bundles.");
        }
        else {
            Map<String,Translation<T>> map;
    
            if( translations.containsKey(lang) ) {
                map = translations.get(lang);
            }
            else {
                map = new HashMap<String,Translation<T>>();
                translations.put(lang, map);
            }
            map.put(ctry, item);
        }
    }

    public Map<Locale,? extends Object> toMap() {
        if( isBundleBased() ) {
            HashMap<Locale,Object> map = new HashMap<Locale,Object>();
            Iterator<String> langs = languages();
            
            while( langs.hasNext() ) {
                String lang = langs.next();
                Iterator<String> ctrys;
                
                ctrys = countries(lang);
                while( ctrys.hasNext() ) {
                    String ctry = ctrys.next();
                    Locale loc;
                    
                    if( ctry == null ) {
                        loc = new Locale(lang);
                    }
                    else {
                        loc = new Locale(lang, ctry);
                    }
                    map.put(loc, getExactTranslation(loc).getData());
                }
            }
            return map;
        }
        else {
            HashMap<Locale,Object> tmp = new HashMap<Locale,Object>();
            
            for( String lang : translations.keySet() ) {
                Map<String,Translation<T>> map = translations.get(lang);
                
                for( String ctry : map.keySet() ) {
                    Translation<T> trans;
                    Locale loc;
                    
                    if( ctry == null ) {
                        loc = new Locale(lang);
                    }
                    else {
                        loc = new Locale(lang, ctry);
                    }
                    trans = getExactTranslation(loc);
                    tmp.put(loc, trans.getData());
                }
            }
            return tmp;
        }
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append("[");
        if( isBundleBased() ) {
            Iterator<String> langs = languages();
            
            while( langs.hasNext() ) {
                String lang = langs.next();
                Iterator<String> ctrys;
                
                ctrys = countries(lang);
                while( ctrys.hasNext() ) {
                    String ctry = ctrys.next();
                    Translation<T> trans;
                    Locale loc;
                    
                    if( ctry == null ) {
                        loc = new Locale(lang);
                    }
                    else {
                        loc = new Locale(lang, ctry);
                    }
                    trans = getExactTranslation(loc);
                    buff.append(lang);
                    buff.append("_");
                    buff.append(ctry);
                    buff.append(":");
                    buff.append(trans.toString());
                    buff.append(",");
                }
            }
        }
        else {
            for( String lang : translations.keySet() ) {
                Map<String,Translation<T>> map = translations.get(lang);
                
                for( String ctry : map.keySet() ) {
                    Translation<T> trans;
                    Locale loc;
                    
                    if( ctry == null ) {
                        loc = new Locale(lang);
                    }
                    else {
                        loc = new Locale(lang, ctry);
                    }
                    trans = getExactTranslation(loc);
                    buff.append(lang);
                    buff.append("_");
                    buff.append(ctry);
                    buff.append(":");
                    buff.append(trans.toString());
                    buff.append(",");
                }
            }
        }
        buff.append("]");
        return buff.toString();
    }

    public Collection<Translation<T>> values() {
        ArrayList<Translation<T>> values = new ArrayList<Translation<T>>();
        
        if( isBundleBased() ) {
            Iterator<String> langs = languages();
            
            while( langs.hasNext() ) {
                String lang = langs.next();
                Iterator<String> ctrys;
                
                ctrys = countries(lang);
                while( ctrys.hasNext() ) {
                    String ctry = ctrys.next();
                    Locale loc;
                    
                    if( ctry == null ) {
                        loc = new Locale(lang);
                    }
                    else {
                        loc = new Locale(lang, ctry);
                    }
                    values.add(getExactTranslation(loc));
                }
            }
        }
        else {
            for( String lang : translations.keySet() ) {
                Map<String,Translation<T>> map = translations.get(lang);
    
                values.addAll(map.values());
            }
        }
        return values;
    }
}
