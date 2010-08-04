package org.joshy.gfx.css;

import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.ColorValue;
import org.joshy.gfx.css.values.IntegerPixelValue;
import org.joshy.gfx.css.values.URLValue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**  Represents an entire set of CSS rules. Usually it maps to a single .css file.
 */
public class CSSRuleSet {
    public List<CSSRule> rules = new ArrayList<CSSRule>();
    private URI baseURI;

    public CSSRuleSet() {
        
    }

    public CSSProperty findMatchingRule(CSSMatcher elem, String propName) {
        List<CSSRule> rulescopy = new ArrayList<CSSRule>();
        rulescopy.addAll(rules);
        Collections.reverse(rulescopy);
        for(CSSRule rule : rulescopy) {
            for(CSSMatcher matcher : rule.matchers) {
                if(matches(matcher,elem)) {
                    for(CSSProperty prop : rule.properties) {
                        if(prop.name.equals(propName)) {
                            return prop;
                        }
                    }
                }
            }
        }
        return null;
    }
    public String findStringValue(String elem, String propName) {
        return findMatchingRule(new CSSMatcher(elem),propName).value.asString();
    }
    public String findStringValue(CSSMatcher matcher, String propName) {
        CSSProperty property = findMatchingRule(matcher, propName);
        if(property == null) return null;
        return property.value.asString();
    }

    private boolean matches(CSSMatcher matcher, CSSMatcher elem) {
        if(matcher.pseudo != null) {
            if(matcher.pseudo.equals(elem.pseudo) && matcher.element.equals(elem.element)) {
                return true;
            }
        }
        if(matcher.element.equals(elem.element) && matcher.pseudo == null) return true;
        if(matcher.element.equals("*")) return true;
        return false;
    }

    public int findIntegerValue(String elemName, String propName) {
        CSSProperty prop = findMatchingRule(new CSSMatcher(elemName), propName);
        if(prop == null) return 0;
        return ((IntegerPixelValue)prop.value).getValue();
    }

    public int findColorValue(CSSMatcher matcher, String propName) {
        CSSProperty prop = findMatchingRule(matcher,propName);
        return ((ColorValue)prop.value).getValue();
    }

    public BaseValue findValue(CSSMatcher matcher, String propName) {
        CSSProperty prop = findMatchingRule(matcher,propName);
        if(prop == null) return null;
        return prop.value;
    }

    public URI findURIValue(CSSMatcher matcher, String propName) {
        CSSProperty prop = findMatchingRule(matcher,propName);
        if(prop == null) return null;
        return ((URLValue)prop.value).getValue();
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI uri) {
        baseURI = uri;
    }
}
