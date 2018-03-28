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

package org.dasein.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {
    public class Record {
        private ArrayList<String> columns = new ArrayList<String>();
        private String            line    = null;
        private CSVParser         parser  = null;
        
        private Record(CSVParser parser, String l) {
            line = l;
            this.parser = parser;
            parse();
        }
        
        public List<String> getColumns() {
            return columns;
        }
        
        private String getQuoted() {
            String col;
            int idx;
            
            if( line.equals(parser.getEncloser()) ) {
                line = null;
                return "";
            }
            line = line.substring(parser.getEncloser().length());
            idx = line.indexOf(parser.getEncloser());
            if( idx == -1 ) {
                return line.trim();
            }
            col = line.substring(0,idx);
            if( idx == (line.length()-parser.getEncloser().length()) ) {
                line= null;
            }
            else {
                line = line.substring(idx+parser.getEncloser().length());
                col = col + getUnquoted();
            }
            return col;
        }
        
        private String getUnquoted() {
            if( line.trim().equals("") ) {
                line = null;
                return "";
            }
            if( line.startsWith(parser.getDelimiter()) ) {
                if( line.equals(parser.getDelimiter()) ) {
                    line = "";
                }
                else {
                    line = line.substring(parser.getDelimiter().length());
                }
                return "";
            }
            else {
                int e = line.indexOf(parser.getEncloser());
                int d = line.indexOf(parser.getDelimiter());
                String col;
                
                if( e == -1 && d == -1 ) {
                    col = line;
                    line = null;
                    return col;
                }
                if( (e < d || d == -1) && e != -1 ) {
                    if( e == 0 ) {
                        return getQuoted();
                    }
                    else {
                        col = line.substring(0, e);
                        line = line.substring(e);
                        return col + getQuoted();
                    }
                }
                if( (d < e || e == -1) && d != -1 ) {
                    if( d == 0 ) {
                        if( !line.equals(parser.getDelimiter()) ) {
                            line = line.substring(parser.getDelimiter().length());
                        }
                        else {
                            line = "";
                        }
                        return "";
                    }
                    else {
                        col = line.substring(0, d);
                        if( d == (line.length()-parser.getDelimiter().length()) ) {
                            line = "";
                        }
                        else {
                            line = line.substring(d + parser.getDelimiter().length());
                        }
                        return col;
                    }
                }
                throw new RuntimeException("This cannot happen.");
            }
        }
        
        private String next() {
            String col;
            
            if( line == null ) {
                return null;
            }
            if( line.startsWith(parser.getDelimiter()) ) {
                if( line.equals(parser.getDelimiter()) ) {
                    line = "";
                }
                else {
                    line = line.substring(parser.getDelimiter().length());
                }
                return "";
            }
            if( line.startsWith(encloser) ) {
                col = getQuoted().trim();
            }
            else {
                col = getUnquoted().trim();
            }
            return col;
        }
        
        private void parse() {
            String column;
            
            while( (column = next()) != null ) {
                columns.add(column);
            }
        }
    }

    private String         delimiter = ",";
    private String         encloser  = "\"";
    private boolean        eof       = false;
    private File           file      = null;
    private BufferedReader reader    = null;
    
    public CSVParser(File f) {
        file = f;
    }
    
    public CSVParser(File f, String d, String e) {
        file = f;
        if( d != null ) {
            delimiter = d;
        }
        if( e != null ) {
            encloser = e;
        }
    }
    
    public CSVParser(String fname) {
        file = new File(fname);
    }
    
    public CSVParser(String fname, String d, String e) {
        file = new File(fname);
        if( d != null ) {
            delimiter = d;
        }
        if( e != null ) {
            encloser = e;
        }
    }    
    
    public String getDelimiter() {
        return delimiter;
    }
    
    public String getEncloser() {
        return encloser;
    }
    
    public Record parse(String line) {
        return new Record(this, line);
    }
    
    public Record next() throws IOException {
        String line;
        
        if( eof ) {
            return null;
        }
        if( reader == null ) {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }
        do {
            line = reader.readLine();
            if( line == null ) {
                eof = true;
                return null;
            }
            line = line.trim();
        } while( line.equals("") || line.startsWith("#") );
        return parse(line);
    }
    
    static public void main(String ... args) {
        try {
            CSVParser parser = new CSVParser(args[0]);
            System.out.println(parser.getDelimiter());
            System.out.println(parser.getEncloser());
            Record header = parser.next();
            Record current;
            System.out.println(header.getColumns());
            if( header == null ) {
                System.out.println("Empty file.");
            }
            while( (current = parser.next()) != null ) {
            	
                for(int i=0; i<header.columns.size(); i++) {
                	if (current.columns.size() <= i) {
                		continue;
                	}
                    String val = current.columns.get(i);
                    
                    System.out.println(header.columns.get(i) + ": " + val);
                }
            }
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
     }
}
