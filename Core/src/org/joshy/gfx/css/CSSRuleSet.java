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

    public String findStringValue(String elem, String propName) {
        return findMatchingRule(new CSSMatcher(elem),propName).value.asString();
    }
    public String findStringValue(CSSMatcher matcher, String propName) {
        CSSProperty property = findMatchingRule(matcher, propName);
        if(property == null) return null;
        return property.value.asString();
    }

    public CSSProperty findMatchingRule(CSSMatcher elem, String propName) {
        List<CSSRule> rulescopy = new ArrayList<CSSRule>();
        rulescopy.addAll(rules);
        Collections.reverse(rulescopy);
        for(CSSRule rule : rulescopy) {
            for(CSSMatcher matcher : rule.matchers) {
                //u.p("checking matcher: " + matcher);
                if(matches(matcher,elem)) {
                    for(CSSProperty prop : rule.getProperties()) {
                        if(prop.name.equals(propName)) {
                            //u.p("found property: " + propName);
                            return prop;
                        }
                    }
                    //u.p("didn't find property: " + propName + ". trying again");
                }
            }
        }
        return null;
    }

    private boolean matches(CSSMatcher matcher, CSSMatcher elem) {

        //match pseudo class
        if(matcher.pseudo != null) {
            if(matcher.pseudo.equals(elem.pseudo) && matcher.element.equals(elem.element)) {
//                u.p("matched pseudo on: " + elem);
                return true;
            }
        }

        if(matcher.id != null) {
//            u.p("checking id: " + matcher.id + " vs " + elem.id);
            if(matcher.id.equals(elem.id)) {
//                u.p("matched id on: " + elem);
                return true;
            }
        }

        if(matcher.element != null) {
            if(matcher.element.equals(elem.element) && matcher.pseudo == null) {
//                u.p("matched element on: " + elem);
                return true;
            }
        }

        if(matcher.cssClass != null) {
//            u.p("checking class: " + matcher.cssClass + " vs " + elem.cssClass);
            if(matcher.cssClass.equals(elem.cssClass)) {
//                u.p("matched css class on: " + elem);
                return true;
            }
        }

        if("*".equals(matcher.element)) {
//            u.p("Matched * on: " + elem);
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
        CSSProperty prop = findMatchingRule(matcher,propName);
        if(prop == null) return -1;
        return ((IntegerPixelValue)prop.value).getValue();
    }
}
