/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tomakehurst.wiremock.vars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author martin
 */
public class VarResolver {

    public final String bodyWithRegexp ;
    public final List<VarRegexp> varAsRegexp;
    
    public VarResolver(String bodyWithRegexp, List<VarRegexp> varAsRegexp) {
        this.bodyWithRegexp = bodyWithRegexp;
        this.varAsRegexp = varAsRegexp;
    }
    
    public VarInRequest resolve(String bodyAsString) {
        
        Map<String, String> varsValues = new HashMap<String, String>();
        boolean bodyMatchRegexp = false ;
        
        final Pattern pattern = Pattern.compile(bodyWithRegexp);
      
        final Matcher matcher = pattern.matcher(bodyAsString);
        
        if (matcher.find()) { 
            bodyMatchRegexp = true ;
            for (VarRegexp varRegexp : varAsRegexp) {
                varsValues.put(varRegexp.getVarName(), matcher.group(varRegexp.getGroupIndex())) ;
            }
        }
        
        return new VarInRequest(bodyMatchRegexp, varsValues);
    }


    
    @Override
    public String toString() {
        return "VarResolver{" + "bodyWithRegexp=" + bodyWithRegexp + ", varAsRegexp=" + varAsRegexp + '}';
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.bodyWithRegexp);
        hash = 79 * hash + Objects.hashCode(this.varAsRegexp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VarResolver other = (VarResolver) obj;
        if (!Objects.equals(this.bodyWithRegexp, other.bodyWithRegexp)) {
            return false;
        }
        if (!Objects.equals(this.varAsRegexp, other.varAsRegexp)) {
            return false;
        }
        return true;
    }


}
