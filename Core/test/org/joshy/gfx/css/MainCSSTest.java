package org.joshy.gfx.css;

import org.joshy.gfx.Core;
import org.joshy.gfx.css.values.BaseValue;
import org.joshy.gfx.css.values.ShadowValue;
import org.joshy.gfx.css.values.StringListValue;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.util.u;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 28, 2010
 * Time: 9:37:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainCSSTest {
    private CSSRuleSet set;

    @Before
    public void setUp() throws Exception {
        Core.setTesting(true);
        Core.init();
        InputStream css = MainCSSTest.class.getResourceAsStream("test1.css");
        ParsingResult<?> result = parseCSS(css);
        set = new CSSRuleSet();
        condense(result.parseTreeRoot,set);
    }

    @Test
    public void basicTests() throws IOException {

        //basic matching
        assertTrue(set.findStringValue("button","color").equals("ff00aa"));
        //fallback to *
        assertTrue(set.findStringValue("label","color").equals("ff00aa"));
        //parsing a pixel value
        assertTrue(set.findIntegerValue("button","border")==3);

        //get the url
        CSSMatcher matcher = new CSSMatcher("button");
        assertTrue(set.findURIValue(matcher,"icon").toString().endsWith("png"));
        //get the icon position:
        assertTrue("left".equals(set.findStringValue("button","icon-position")));

        //background-color: transparent
        assertTrue("transparent".equals(set.findStringValue("transptest","background-color")));
        assertTrue(!"transparent".equals(set.findStringValue("transptest","color")));
        //background-color: red turns into #ff0000

        //fonts
        BaseValue fontList = set.findValue(matcher, "font-family");
        assertTrue(fontList instanceof StringListValue);
        u.p(((StringListValue)fontList).getList());
        assertTrue(((StringListValue)fontList).getList().contains("serif"));
        assertTrue("normal".equals(set.findStringValue("label","font-weight")));
        assertTrue("bold".equals(set.findStringValue("button","font-weight")));
        assertTrue(set.findIntegerValue(matcher,"font-size") == 12);

        //shadow
        BaseValue shadow = set.findValue(new CSSMatcher("shadowtest"), "text-shadow");
        assertTrue(shadow instanceof ShadowValue);
        ShadowValue sv = (ShadowValue)shadow;
        assertTrue(sv.getXoffset()==3);
        assertTrue(sv.getYoffset()==2);
        assertTrue(sv.getBlurRadius()==4);


        //tests for id and class matching
        CSSMatcher idMatcher = new CSSMatcher();
        idMatcher.id = "idmatch1";
        //test an ID search
        assertTrue(set.findIntegerValue(idMatcher,"dummy-prop-name") == 87);

        //match by class
        CSSMatcher classMatcher = new CSSMatcher();
        classMatcher.classes.add("classmatch1");
        assertTrue(set.findIntegerValue(classMatcher,"dummy-prop-name") == 88);

        //match by element against a rule with multiple elements
        CSSMatcher multiElementMatcher = new CSSMatcher();
        multiElementMatcher.element = "e1";
        assertTrue(set.findIntegerValue(multiElementMatcher,"border")==3);
        multiElementMatcher.element = "e2";
        assertTrue(set.findIntegerValue(multiElementMatcher,"border")==3);


        idMatcher = new CSSMatcher();
        idMatcher.id = "hex_test";
        assertTrue(set.findColorValue(idMatcher,"background-color") == 0x00ff00ff);

        //match by element and class
//        classMatcher.element = "Button";
//        classMatcher.cssClass = "classmatch2";
//        assertTrue(set.findIntegerValue(classMatcher,"margin") == 1);
//        assertTrue(set.findIntegerValue(classMatcher,"padding") == 8);

//        marginTests(set);
//        paddingTests(set);
//        borderTests(set);
//
//        advancedClassTests(set);
    }

    @Test
    public void advancedClassTests() {
        Button button = new Button();
        button.getCSSClasses().add("class1");
        CSSMatcher matcher = new CSSMatcher(button);
        assertTrue(set.findIntegerValue(matcher,"dummy-prop")==1);
        button.getCSSClasses().add("class2");
        assertTrue(set.findIntegerValue(new CSSMatcher(button),"dummy-prop")==3);
        assertTrue(set.findIntegerValue(new CSSMatcher(button),"dummy-prop2")==10);
    }

    @Test
    public void marginTests() {
        CSSMatcher m = new CSSMatcher();
        m.id = "margin_test_1";
        assertTrue(set.findIntegerValue(m,"margin-top")==1);
        assertTrue(set.findIntegerValue(m,"margin-right")==3);
        assertTrue(set.findIntegerValue(m,"margin-bottom")==5);
        assertTrue(set.findIntegerValue(m,"margin-left")==7);

        m.id = "margin_test_2";
        assertTrue(set.findIntegerValue(m,"margin-top")==1);
        assertTrue(set.findIntegerValue(m,"margin-right")==3);
        assertTrue(set.findIntegerValue(m,"margin-bottom")==5);
        assertTrue(set.findIntegerValue(m,"margin-left")==7);

        m.id = "margin_test_3";
        assertTrue(set.findIntegerValue(m,"margin-top")==9);
        assertTrue(set.findIntegerValue(m,"margin-right")==9);
        assertTrue(set.findIntegerValue(m,"margin-bottom")==9);
        assertTrue(set.findIntegerValue(m,"margin-left")==9);

        m.id = "margin_test_4";
        assertTrue(set.findIntegerValue(m,"margin-top")==10);
        assertTrue(set.findIntegerValue(m,"margin-right")==11);
        assertTrue(set.findIntegerValue(m,"margin-bottom")==10);
        assertTrue(set.findIntegerValue(m,"margin-left")==11);

        m.id = "margin_test_5";
        assertTrue(set.findIntegerValue(m,"margin-top")==10);
        assertTrue(set.findIntegerValue(m,"margin-right")==11);
        assertTrue(set.findIntegerValue(m,"margin-bottom")==12);
        assertTrue(set.findIntegerValue(m,"margin-left")==11);
    }


    @Test
    public void paddingTests() {
        CSSMatcher m = new CSSMatcher();
        m.id = "padding_test_1";
        assertTrue(set.findIntegerValue(m,"padding-top")==1);
        assertTrue(set.findIntegerValue(m,"padding-right")==3);
        assertTrue(set.findIntegerValue(m,"padding-bottom")==5);
        assertTrue(set.findIntegerValue(m,"padding-left")==7);

        m.id = "padding_test_2";
        assertTrue(set.findIntegerValue(m,"padding-top")==1);
        assertTrue(set.findIntegerValue(m,"padding-right")==3);
        assertTrue(set.findIntegerValue(m,"padding-bottom")==5);
        assertTrue(set.findIntegerValue(m,"padding-left")==7);

        m.id = "padding_test_3";
        assertTrue(set.findIntegerValue(m,"padding-top")==9);
        assertTrue(set.findIntegerValue(m,"padding-right")==9);
        assertTrue(set.findIntegerValue(m,"padding-bottom")==9);
        assertTrue(set.findIntegerValue(m,"padding-left")==9);

        m.id = "padding_test_4";
        assertTrue(set.findIntegerValue(m,"padding-top")==10);
        assertTrue(set.findIntegerValue(m,"padding-right")==11);
        assertTrue(set.findIntegerValue(m,"padding-bottom")==10);
        assertTrue(set.findIntegerValue(m,"padding-left")==11);

        m.id = "padding_test_5";
        assertTrue(set.findIntegerValue(m,"padding-top")==10);
        assertTrue(set.findIntegerValue(m,"padding-right")==11);
        assertTrue(set.findIntegerValue(m,"padding-bottom")==12);
        assertTrue(set.findIntegerValue(m,"padding-left")==11);
    }

    @Test
    public void borderTests() {
        CSSMatcher m = new CSSMatcher();
        m.id = "border_test_1";
        assertTrue(set.findIntegerValue(m,"border-top-width")==1);
        assertTrue(set.findIntegerValue(m,"border-right-width")==3);
        assertTrue(set.findIntegerValue(m,"border-bottom-width")==5);
        assertTrue(set.findIntegerValue(m,"border-left-width")==7);

        m.id = "border_test_2";
        assertTrue(set.findIntegerValue(m,"border-top-width")==1);
        assertTrue(set.findIntegerValue(m,"border-right-width")==3);
        assertTrue(set.findIntegerValue(m,"border-bottom-width")==5);
        assertTrue(set.findIntegerValue(m,"border-left-width")==7);

        m.id = "border_test_3";
        assertTrue(set.findIntegerValue(m,"border-top-width")==9);
        assertTrue(set.findIntegerValue(m,"border-right-width")==9);
        assertTrue(set.findIntegerValue(m,"border-bottom-width")==9);
        assertTrue(set.findIntegerValue(m,"border-left-width")==9);

        m.id = "border_test_4";
        assertTrue(set.findIntegerValue(m,"border-top-width")==10);
        assertTrue(set.findIntegerValue(m,"border-right-width")==11);
        assertTrue(set.findIntegerValue(m,"border-bottom-width")==10);
        assertTrue(set.findIntegerValue(m,"border-left-width")==11);

        m.id = "border_test_5";
        assertTrue(set.findIntegerValue(m,"border-top-width")==10);
        assertTrue(set.findIntegerValue(m,"border-right-width")==11);
        assertTrue(set.findIntegerValue(m,"border-bottom-width")==12);
        assertTrue(set.findIntegerValue(m,"border-left-width")==11);
    }

    @Test
    public void constantTests() {
        CSSMatcher matcher = new CSSMatcher();
        matcher.id = "constant_color_1";
        //test for red
        assertTrue(set.findColorValue(matcher,"prop1")== 0x00ff0000);
        assertTrue(set.findColorValue(matcher,"prop2")== 0x0000ff00);
        assertTrue(set.findColorValue(matcher,"prop3")== 0x000000ff);
    }

    /* -------------- support -------------- */
    private static void condense(Node<?> node, CSSRuleSet set) {
        if(node == null) return;
        if("CSSRule".equals(node.getLabel())) {
            CSSRule rule = (CSSRule) node.getValue();
            set.rules.add(rule);
        }
        for(Node<?> n : node.getChildren()) {
            condense(n,set);
        }
    }

    private static ParsingResult<?> parseCSS(InputStream css) throws IOException {
        String cssString = toString(css);
        CSSParser parser = Parboiled.createParser(CSSParser.class);
        //System.out.println("string = " + cssString);
        ParsingResult<?> result = ReportingParseRunner.run(parser.RuleSet(), cssString);
        String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
        //System.out.println(parseTreePrintOut);
        //u.p("other value = " + result.parseTreeRoot.getLabel());
        return result;
    }

    private static String toString(InputStream css) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while(true) {
            int n = css.read(buff);
            if(n < 0) break;
            out.write(buff,0,n);
        }
        css.close();
        out.close();
        return new String(out.toByteArray());
    }

    @After
    public void tearDown() throws Exception {
    }

}
