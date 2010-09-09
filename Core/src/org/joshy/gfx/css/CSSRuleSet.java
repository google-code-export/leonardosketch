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
    private static final boolean DEBUG = false;
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }

    public CSSRuleSet() {
        
    }

    public void append(CSSRuleSet set) {
        rules.addAll(set.rules);
    }

    public String findStringValue(String elem, String propName) {
        return findMatchingRule(new CSSMatcher(elem),propName).value.asString();
    }
    public String findStringValue(CSSMatcher matcher, String propName) {
        CSSProperty property = findMatchingRule(matcher, propName);
        if(property == null) return null;
        return property.value.asString();
    }

    public CSSProperty findMatchingRule(CSSMatcher elem, String propName) {
        p("----- looking for property: " + propName);
        p("on element: " + elem);
        List<CSSRule> rulescopy = new ArrayList<CSSRule>();
        rulescopy.addAll(rules);
        Collections.reverse(rulescopy);
        for(CSSRule rule : rulescopy) {
            for(CSSMatcher matcher : rule.matchers) {
                p("checking matcher: " + matcher);
                if(matches(matcher,elem)) {
                    for(CSSProperty prop : rule.getProperties()) {
                        if(prop.name.equals(propName)) {
                            p("found property: " + propName);
                            return prop;
                        }
                    }
                    p("didn't find property: " + propName + ". trying again");
                }
            }
        }
        return null;
    }

    private boolean matches(CSSMatcher matcher, CSSMatcher elem) {

        //match pseudo class
        if(matcher.pseudo != null) {
            if(matcher.pseudo.equals(elem.pseudo) && matcher.element.equals(elem.element)) {
                p("matched pseudo on: " + elem);
                return true;
            }
        }

        if(matcher.id != null) {
           p("checking id: " + matcher.id + " vs " + elem.id);
            if(matcher.id.equals(elem.id)) {
                p("matched id on: " + elem);
                return true;
            }
        }

        if(matcher.element != null) {
            if(matcher.element.equals(elem.element) && matcher.pseudo == null) {
                p("matched element on: " + elem);
                return true;
            }
        }

        for(String c1 : matcher.classes) {
            for(String c2 : elem.classes) {
                p("checking css class: " + c1 + " vs " + c2);
                if(c1.equals(c2)) {
                    p("matched css class on: " + elem);
                    return true;
                }
            }
        }

        if("*".equals(matcher.element)) {
            p("Matched * on: " + elem);
            return true;
        }
        return false;
    }

    public int findIntegerValue(String elemName, String propName) {
        CSSProperty prop = findMatchingRule(new CSSMatcher(elemName), propName);
        if(prop == null) return 0;
        int v = ((IntegerPixelValue)prop.value).getValue();
//        u.p("Prop value " + v);
        return v;
    }

    public int findColorValue(CSSMatcher matcher, String propName) {
        CSSProperty prop = findMatchingRule(matcher,propName);
        if(prop == null) {
            System.out.println("Couldn't find property " + propName + " for " + matcher);
            return 0;
        }
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

    public int findIntegerValue(CSSMatcher matcher, String propName) {
        p("--------");
        CSSProperty prop = findMatchingRule(matcher,propName);
        if(prop == null) return -1;
        return ((IntegerPixelValue)prop.value).getValue();
    }
}
