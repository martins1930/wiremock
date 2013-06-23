/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tomakehurst.wiremock.vars;

import java.util.Objects;

/**
 *
 * @author martin
 */
public class VarRegexp {

    private final String varName;

    private final int indexInBody;

    private int groupIndex;

    public VarRegexp(String varName, int indexInBody) {
        this.varName = varName;
        this.indexInBody = indexInBody;
    }

    public String getVarName() {
        return varName;
    }

    public int getIndexInBody() {
        return indexInBody;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    @Override
    public String toString() {
        return "VarRegexp{" + "varName=" + varName + ", indexInBody=" + indexInBody + ", groupIndex=" + groupIndex + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.varName);
        hash = 19 * hash + this.groupIndex;
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
        final VarRegexp other = (VarRegexp) obj;
        if (!Objects.equals(this.varName, other.varName)) {
            return false;
        }
        if (this.groupIndex != other.groupIndex) {
            return false;
        }
        return true;
    }

    
    public static String formatVarWithPlaceHolder(String varName) {
        return "${" + varName + "}";
    }

    public static String formatVarWithPlaceHolderEscaped(String varName) {
        return "\\$\\{" + varName + "\\}";
    }
}
