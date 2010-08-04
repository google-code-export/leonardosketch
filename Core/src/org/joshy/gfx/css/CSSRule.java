package org.joshy.gfx.css;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 28, 2010
 * Time: 4:44:14 PM
 * To change this template use File | Settings | File Templates.
 *
 * represents a single matcher with a set of property assignments within it
 * 
 */
public class CSSRule {
    public List<CSSMatcher> matchers = new ArrayList<CSSMatcher>();
    public List<CSSProperty> properties = new ArrayList<CSSProperty>();
}
