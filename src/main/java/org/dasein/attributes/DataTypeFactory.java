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

/* $Id: DataTypeFactory.java,v 1.7 2009/01/30 23:01:50 morgan Exp $ */
/* Copyright 2005-2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dasein.attributes.types.BooleanFactory;
import org.dasein.attributes.types.CurrencyFactory;
import org.dasein.attributes.types.DateFactory;
import org.dasein.attributes.types.DecimalFactory;
import org.dasein.attributes.types.NameValuePairFactory;
import org.dasein.attributes.types.NumberFactory;
import org.dasein.attributes.types.StringFactory;
import org.dasein.attributes.types.TimeFactory;
import org.dasein.util.Translation;
import org.dasein.util.Translator;

/**
 * <p>
 *   Manages attribute data types and access to custom data types.
 * </p>
 * <p>
 *   Last modified: $Date: 2009/01/30 23:01:50 $
 * </p>
 * @version $Revision: 1.7 $
 * @author George Reese
 */
public abstract class DataTypeFactory<V> implements Serializable {
    private static final long serialVersionUID = 963866399811619996L;

    /**
     * <p>
     *   A constraint helps narrow down the set of possible values associated with 
     *   the underlying data type. In general, a constraint indicates that the set of valid
     *   values for this data type is constrained by a value from another data type.
     * </p>
     * <p>
     *   Last modified: $Date: 2009/01/30 23:01:50 $
     * </p>
     * @version $Revision: 1.7 $
     * @author George Reese
     */
    static public class Constraint {
        /**
         * Are more constraints required to define the type?
         */
        private boolean     complete;
        /**
         * A name to identify this constraint.
         */
        private String      name;
        /**
         * The data type governing the constraint.
         */
        private DataType<?> type;

        public Constraint(String nom, DataType<?> t) {
            this(nom, t, true);
        }
        
        /**
         * Constructs a new constraint.
         * @param nom the name identifying the constraint
         * @param t the data type governing the constraint
         * @param cmpl true if the constraints are required for the type
         */
        public Constraint(String nom, DataType<?> t, boolean cmpl) {
            super();
            name = nom;
            type = t;
            complete = cmpl;
        }

        /**
         * @return true if the constraints are required for the type
         */
        public boolean isComplete() {
            return complete;
        }
        
        /**
         * @return the name of the constraint
         */
        public String getName() { return name; }
        
        /**
         * @return the data type governing the constraint
         */
        public DataType<?> getType() { return type; }
        
        /**
         * Makes a copy of this constraint with a different data type. When constraints
         * are first constructed, type parameters are generally unknown. This method
         * let's you refine the constraints as you learn more about the type info.
         * @param t the new data type
         * @return a copy of this constraint with the new data type
         */
        public Constraint newConstraint(DataType<?> t) {
            return new Constraint(name, t, complete);
        }
    }
    
    /**
     * A list of registered data types.
     */
    static private final HashMap<String,DataTypeFactory<?>> factories = new HashMap<String,DataTypeFactory<?>>();
    /**
     * The location of custom data type definitions.
     */
    static private final String CONFIG    = "/dasein/types.cfg";
    
    static {
        try {
            DataTypeFactory<?> factory;
            
            // load the built-in types
            factory = new BooleanFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new CurrencyFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new DateFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new DecimalFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new NumberFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new StringFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new NameValuePairFactory();
            factories.put(factory.getTypeName(), factory);
            factory = new TimeFactory();
            factories.put(factory.getTypeName(), factory);
            // load any custom types
            try {
                InputStream is = DataTypeFactory.class.getResourceAsStream(CONFIG);
                if( is != null ) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    
                    while( (line = reader.readLine()) != null ) {
                        //System.out.println(line);
                        line = line.trim();
                        if( line.length() < 1 ) {
                            continue;
                        }
                        if( line.startsWith("#") ) {
                            continue;
                        }
                        try {
                            Class<DataTypeFactory<?>> cls = getClassFor(line);
                            
                            factory = cls.newInstance();
                            factories.put(factory.getTypeName(), factory);                    
                        }
                        catch( Exception e ) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
        catch( Throwable t ) {
            t.printStackTrace();
        }
    }
    
    /**
     * This little bizarre method lets me stick on a warning suppression that you cannot
     * add (at least in Eclipse) to static initializers.
     * @param line the line containing a class name
     * @return the class named by the specified string
     * @throws ClassNotFoundException no such class exists
     */
    @SuppressWarnings("unchecked")
    static private Class<DataTypeFactory<?>> getClassFor(String line) throws ClassNotFoundException {
        return (Class<DataTypeFactory<?>>)Class.forName(line);
    }
    
    /**
     * Provides access to the data type factory associated with the specified type name.
     * @param tname the name of the data type for the desired factory
     * @return the factory representing the specified type name
     * @throws InvalidAttributeException when an unknown type is sought out
     */
    static public DataTypeFactory<?> getInstance(String tname) {
        DataTypeFactory<?> factory = factories.get(tname);
        
        if( factory == null ) {
            throw new InvalidAttributeException("Unknown type name: " + tname);
        }
        return factory;
    }

    /**
     * Lists all types known to the system.
     * @return a collection of all type factories
     */
    static public Collection<DataTypeFactory<?>> getTypes() {
        ArrayList<DataTypeFactory<?>> list = new ArrayList<DataTypeFactory<?>>();
        
        for( Map.Entry<String,DataTypeFactory<?>> entry : factories.entrySet() ) {
            list.add(entry.getValue());
        }
        return list;
    }
    
    /**
     * Constructs a new data type factory instance.
     */
    public DataTypeFactory() {
        super();
    }

    /**
     * <p>
     * Provides the next constrain on this data type based on provided type info. If no
     * further constraints exist, it should return <code>null</code>. A user interface
     * will repeatedly make calls to this method to help narrow down a type definition.
     * The first call will pass in no type info and should be identitical
     * to {@link #getConstraints()}<code>.iterator().next().</code> Subsequent calls
     * will pass in selections based on the previous constraint.
     * </p>
     * <p>
     * You may override this method to customize its functionality (such as, offer 
     * conditional constraints based on prior input). For example of overriding with
     * conditional constraints, 
     * see {@link org.dasein.attributes.types.StringFactory#getConstraint(String[])}.
     * </p>
     * <p>
     * An example from <a href="http://simplicis.valtira.com">Simplicis CMS</a> is a
     * <code>media</code> data type. It has as constraints a <code>media type</code>
     * (such as an image, video, document, etc.) and a <code>media group</code> (a user
     * defined categorization for media assets that holds the type information for
     * media objects). In the CMS, when a page has an attribute of type <code>media</code>,
     * only media assets matching the type and group of the attribute are presented in
     * the choice box for the content owner. The UI can do this because of the constraints.
     * </p>
     * <p>
     * When the programmer designs the page type, they need to identify which type and group
     * should be included in the choice box. Simplicis has a UI that enables the programmer
     * to manually specify data types (as a result, Simplicis programmers can add new data
     * types without having to alter the Simplicis UI). To accomplish this, the UI needs
     * to prompt for first the media type, and then only those media groups matching the
     * selected media type.
     * </p>
     * <ol>
     * <li>It calls this method first with null to get the first constraint.</li>
     * <li>Using the data type from that constraint, it prompts the user for
     * all media types.</li>
     * <li>When the user selects a type from the choice box, the UI then calls this
     * method again with the {@link #getStringValue(Object)} of the selected type</li>
     * <li>This method should then return a constraint with the data type for
     * <code>media group</code> with the specified type info as a type parameter.</li>
     * <li>The user interface prompts the user for a media group of the specified type.</li>
     * </ul>
     * @param typeInfo the string values from previous constraints
     * @return the next constraint in the list of constraints with the data type modified to reflect the specified type info
     */
    public Constraint getConstraint(String ... typeInfo) {
        Collection<Constraint> constraints = getConstraints();
        Constraint constraint = null;
        DataType<?> t;
        int i;
        
        if( constraints.size() < 1 ) {
            return null;
        }
        if( typeInfo == null || typeInfo.length < 1 ) {
            return constraints.iterator().next();
        }
        if( typeInfo.length >= constraints.size() ) {
            return null;
        }
        i = 1;
        for( Constraint c : constraints ) {
            if( i++ == (typeInfo.length+1) ) {
                constraint = c;
                break;
            }
        }
        if( constraint == null ) { // should never happen
            return null;
        }
        t = constraint.getType();
        return constraint.newConstraint(DataTypeFactory.getInstance(t.getName()).getType(t.isMultiLingual(), t.isMultiValued(), t.isRequired(), typeInfo)); 
    }
    
    /**
     * Implementing classes should override this method to define any governing
     * constraints. A constraint is simply another data type that will govern values of
     * this type. Imagine, for example, a <code>MediaAttribute</code> data type that had values
     * of a specific media type. For one attribute, &quot;images&quot, only <code>Media</code>
     * objects that are images are allowed. For another attribute, &quot;documents&quot;, 
     * only documents are allowed. You would thus have a <code>MediaTypeAttribute</code>
     * data type that allowed media types (image, document, etc). That data type, in turn,
     * would be a constraint on your <code>MediaAttribute</code> data type. 
     * @return a list of constraints on this data type
     */
    public Collection<Constraint> getConstraints() {
        return new ArrayList<Constraint>(0);
    }
    
    /**
     * Provides a multi-lingual display name for this data type.
     * @return the display name for the data type
     */
    public abstract Translator<String> getDisplayName();
    
    /**
     * Provides a display value for an instance of this data type. If the value is a 
     * multi-lingual {@link Translator}, then this method will provide the display value
     * for the default system locale as defined in {@link java.util.Locale#getDefault()}.
     * @param ob the value for which a display value is being sought
     * @return the display value for the specified value
     */
    public String getDisplayValue(Object ob) {
        if( ob == null ) {
            return "";
        }
        if( ob instanceof Translator ) {
            return getDisplayValue(Locale.getDefault(), ob);
        }
        return ob.toString();
    }
    
    /**
     * Provides a display version of the specified value translated for the target locale.
     * @param loc the locale for which the display should be translated
     * @param ob the target value
     * @return a display version of the specified value
     */
    public String getDisplayValue(Locale loc, Object ob) {
        if( ob == null ) {
            return "";
        }
        if( ob instanceof Translator ) {
            @SuppressWarnings("rawtypes") Translation trans = ((Translator)ob).get(loc);
            
            if( trans == null ) {
                return null;
            }
            return getDisplayValue(trans.getData());
        }
        else {
            return getDisplayValue(ob);
        }
    }
    
    /**
     * Converts the specified value to a string representation for storage.
     * @param ob the object to be converted
     * @return a string representation for storage
     */
    public String getStringValue(Object ob) {
        if( ob == null ) {
            return "";
        }
        return ob.toString();
    }
    
    /**
     * @return the name of this data type
     */
    public abstract String getTypeName();

    /**
     * Provides access to the underlying data type object that governs this data type. The 
     * configurable rules for the data type are provided as parameters.
     * @param ml is the data type multi-lingual?
     * @param mv is the data type multi-valued?
     * @param req is a value for the data type required?
     * @param typeArgs arbitrary parameters with meaning dependent on the underlying type
     * @return a data type object for the specified type
     */
    public abstract DataType<V> getType(boolean ml, boolean mv, boolean req, String... typeArgs);

    /**
     * Provides access to the underlying data type object that governs this data type. The
     * configurable rules for the data type are provided as parameters.
     * @param grp the group of the data type.
     * @param idx the index of the data type.
     * @param ml is the data type multi-lingual?
     * @param mv is the data type multi-valued?
     * @param req is a value for the data type required?
     * @param typeArgs arbitrary parameters with meaning dependent on the underlying type
     * @return a data type object for the specified type
     */
    public abstract DataType<V> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... typeArgs);
}
