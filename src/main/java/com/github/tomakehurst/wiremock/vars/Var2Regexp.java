/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tomakehurst.wiremock.vars;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author martin
 */
public class Var2Regexp {

    private final String bodyWithVars ;
    private final List<String> inputVars;
    
    public Var2Regexp(String bodyWithVars, List<String> inputVars) {
        this.bodyWithVars = bodyWithVars ;
        this.inputVars = inputVars ;
    }

    public VarResolver createVarResolver() {
        
        List<VarRegexp> varAsRegexp = getInputVarsAsVarRegexp();
        
        orderVarsByFirstOccurrenceInBody(varAsRegexp);
        
        setGroupIndexToVarRegexp(varAsRegexp);
        
        String bodyWithRegexp = convertBodyWithVarToBodyWithRegexp(varAsRegexp);
        
        return new VarResolver(bodyWithRegexp, varAsRegexp);
    }

    private List<VarRegexp> getInputVarsAsVarRegexp() {
        List<VarRegexp> varAsRegexp = new LinkedList<VarRegexp>();
        if (inputVars!=null) {
            for (String varIter : inputVars) {

                int indexInBodyOfVarIter = bodyWithVars.indexOf(VarRegexp.formatVarWithPlaceHolder(varIter));
                if (indexInBodyOfVarIter!=-1) {
                    varAsRegexp.add(new VarRegexp(varIter, indexInBodyOfVarIter));     
                }
            }
        }
        return varAsRegexp;
    }

    private void orderVarsByFirstOccurrenceInBody(List<VarRegexp> varAsRegexp) {
        Collections.sort(varAsRegexp, new Comparator<VarRegexp>() {

            @Override
            public int compare(VarRegexp o1, VarRegexp o2) {
                return Integer.compare(o1.getIndexInBody(), o2.getIndexInBody()) ;
            }
        });
    }

    private void setGroupIndexToVarRegexp(List<VarRegexp> varAsRegexp) {
        int currentGroupIndex = 0 ;
        for (VarRegexp varRegexp : varAsRegexp) {
            varRegexp.setGroupIndex(++currentGroupIndex);
        }
    }

    private String convertBodyWithVarToBodyWithRegexp(List<VarRegexp> varAsRegexp) {
        String bodyWithVarsRegexp = new String(bodyWithVars) ;
        for (VarRegexp varRegexp : varAsRegexp) {
            bodyWithVarsRegexp = bodyWithVarsRegexp.replaceFirst(VarRegexp.formatVarWithPlaceHolderEscaped(varRegexp.getVarName()), "(.*)");
            bodyWithVarsRegexp = bodyWithVarsRegexp.replaceAll(VarRegexp.formatVarWithPlaceHolderEscaped(varRegexp.getVarName()), "\\\\"+varRegexp.getGroupIndex());
        }
        return bodyWithVarsRegexp;
    }
    
}
