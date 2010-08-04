package org.joshy.gfx.css;

import org.joshy.gfx.util.u;
import org.parboiled.Parboiled;
import org.parboiled.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 31, 2010
 * Time: 6:53:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSSProcessor {
    public static void condense(org.parboiled.Node<?> node, CSSRuleSet set) {
        if("CSSRule".equals(node.getLabel())) {
            CSSRule rule = (CSSRule) node.getValue();
            set.rules.add(rule);
        }
        for(org.parboiled.Node<?> n : node.getChildren()) {
            condense(n,set);
        }
    }

    public static ParsingResult<?> parseCSS(InputStream css) throws IOException {
        String cssString = toString(css);
        try {
        CSSParser parser = Parboiled.createParser(CSSParser.class);
        //System.out.println("string = " + cssString);
        ParsingResult<?> result = ReportingParseRunner.run(parser.RuleSet(), cssString);
        //String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
        //System.out.println(parseTreePrintOut);
        //u.p("other value = " + result.parseTreeRoot.getLabel());
        return result;
        } catch(Throwable th) {
            u.printFullStackTrace(th);
            return null;
        }
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
}
