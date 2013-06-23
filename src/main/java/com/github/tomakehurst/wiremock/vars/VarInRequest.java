/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tomakehurst.wiremock.vars;

import java.util.Map;

/**
 *
 * @author martin
 */
public final class VarInRequest {

    private final boolean bodyMatchRegexp ;
    
    private final Map<String, String> varsValues ;
    
    public VarInRequest(boolean bodyMatchRegexp, Map<String, String> varsValues) {
        this.bodyMatchRegexp = bodyMatchRegexp ;
        this.varsValues = varsValues;
    }

    public boolean isBodyMatchRegexp() {
        return bodyMatchRegexp;
    }

    public Map<String, String> getVarsValues() {
        return varsValues;
    }
    
    
}
