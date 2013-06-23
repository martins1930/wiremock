package com.github.tomakehurst.wiremock.vars;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class Vars2RegexpTest {

    private final VarResolver expectedResolver;
    private final String bodyWithVars ; 
    private final List<String> inputVars ;

    public Vars2RegexpTest( 
                               VarResolver expectedResolver,
                               String bodyWithVars, 
                               List<String> inputVars) {
        
        this.expectedResolver = expectedResolver;
        this.bodyWithVars = bodyWithVars;
        this.inputVars = inputVars ;
    }
    
    @Parameters 
    public static Collection<Object[]> getTestParameters(){
        return Arrays.asList(new Object[][] {
            
            {
                // expected0
                createExpected(
                    "<content>(.*)</content>", 
                    newVarRegexp("varname", 1)
                        ), 
                
                //given
                "<content>${varname}</content>",  
                ImmutableList.of("varname")  
            } ,
            
            {
                // expected1
                createExpected("<content>${varname}</content>"), 
                
                //given
                "<content>${varname}</content>", 
                null  
            },
            
            {
                // expected2
                createExpected("<content>${varname}</content>"),                 
                
                //given
                "<content>${varname}</content>", 
                Collections.emptyList()
            },
            
            {
                // expected3
                createExpected(
                    " (.*) \\1 ", 
                    newVarRegexp("varname", 1)
                        ),                
                //given
                " ${varname} ${varname} ", 
                ImmutableList.of("varname")  
            },
            
            {
               // expected4                               
               createExpected(
                    " (.*) (.*) \\2 \\1 ", 
                    newVarRegexp("varname2", 1),
                    newVarRegexp("varname1", 2)
                        ),
                
                
                //given
                " ${varname2} ${varname1} ${varname1} ${varname2} ", 
                ImmutableList.of("varname1", "varname2")  
            },
            
            {
                // expected5                 
                createExpected("Hi, this is a text without vars!"),                 

                //given
                "Hi, this is a text without vars!", 
                ImmutableList.of("varname1")  
            },
            
            {
                // expected6                 
               createExpected(
                    ".*Hi, this is a text with (.*)!", 
                    newVarRegexp("varname1", 1)
                        ),
                
                //given
                ".*Hi, this is a text with ${varname1}!", 
                ImmutableList.of("varname1")  
            }         
                        
        }) ;
    }
    
    private static VarRegexp newVarRegexp(String varName, int groupIndex) {
        VarRegexp ret = new VarRegexp(varName, -1);
        ret.setGroupIndex(groupIndex);
        return ret ;
    }
    
    private static VarResolver createExpected(String expectedBodyWithRegexp, VarRegexp expectedVarRegexp) {
        return new VarResolver(expectedBodyWithRegexp, ImmutableList.of(expectedVarRegexp));
    }
    
    
    private static VarResolver createExpected(String expectedBodyWithRegexp) {
        return new VarResolver(expectedBodyWithRegexp, Collections.<VarRegexp>emptyList());
    }
    
    private static VarResolver createExpected(String expectedBodyWithRegexp, VarRegexp expectedVarRegexp1, VarRegexp expectedVarRegexp2) {
        return new VarResolver(expectedBodyWithRegexp, ImmutableList.of(expectedVarRegexp1, expectedVarRegexp2));
    }
    
    
    @Test
    public void testBodyWithVars2BodyWithVarsAsRegexp() {
        
        Var2Regexp var2Regexp = new Var2Regexp(bodyWithVars, inputVars);
        
        assertEquals(expectedResolver, var2Regexp.createVarResolver());
    }
   
    
}
