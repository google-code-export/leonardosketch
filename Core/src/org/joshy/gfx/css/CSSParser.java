package org.joshy.gfx.css;

import org.joshy.gfx.css.values.*;
import org.joshy.gfx.css.values.StringValue;
import org.parboiled.*;
import org.parboiled.annotations.DontLabel;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.support.Var;

import java.util.Collection;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 28, 2010
 * Time: 2:43:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSSParser extends BaseParser<Object> {
    public Rule RuleSet() {
        return Sequence(
                CSSRule(),
                ZeroOrMore(CSSRule())
        );
    }

    public Rule CSSRule() {
        final Var matcher = new Var();
        return Sequence(
                //a set of match expressions
                OneOrMore(Sequence(
                        MatchExpression(),
                        Optional(Spacing()),
                        Optional(','),
                        Optional(Spacing()))),
                matcher.set(this.values("OneOrMore/Sequence/MatchExpression")),
                
                Spacing(),
                LWING,
                ZeroOrMore(PropertyRule()),
                RWING,
                new CSSRuleAction(matcher)
        );
    }

    public Rule MatchExpression() {
        final Var<String> elem = new Var<String>();
        final Var<String> pseudo = new Var<String>();
        return Sequence(
                //element name
                Sequence(Sequence(LetterOrStar(), ZeroOrMore(LetterOrDigit())),toString,elem.set((String) value()),
                //pseudo class
                Optional(Sequence(':',Sequence(OneOrMore(Letter()),toString,pseudo.set((String) value()))))
                ),
                new MatchExpressionAction(elem, pseudo)
                );
    }

    public Rule PropertyRule() {
        return Sequence(
                PropertyName(),
                COLON,
                PropertyValue(),
                SEMICOLON,
                new PropertyRuleAction()
        );
    }

    public Action toString = new ToStringAction();
    
    public Rule PropertyName() {
        return Sequence(OneOrMore(LetterOrDash()), toString);
    }

    public static class LinearGradientAction implements Action {
        private Var<String> pos1;
        private Var<String> pos2;
        private Var stops;

        public LinearGradientAction(Var<String> pos1, Var<String> pos2, Var stops) {
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.stops = stops;
        }

        public boolean run(Context context) {
//                            u.p("pos 1 = " + pos1.get());
//                            u.p("stops = " + stops.get());
//                            u.p(""+context.getNodeByPath("GradientStop"));

            LinearGradientValue grad = new LinearGradientValue(
                    pos1.get(),
                    pos2.get());
            grad.addStops((List<GradientStopValue>)stops.get());
            context.setNodeValue(grad);
            return true;
        }
    }

    public Rule PropertyValue() {
        final Var<String> pos1 = new Var<String>();
        final Var<String> pos2 = new Var<String>();
        final Var stops = new Var();
        return FirstOf(
            //gradients
            Sequence(Sequence(String("linear-gradient("),
                    //the gradient position
                    FirstOf("left","center","right"),pos1.set(lastText()),Spacing(),
                    FirstOf("bottom","top","center"),pos2.set(lastText()),Spacing(),
                    //the stops
                    ZeroOrMore(Sequence(",",GradientStop())),stops.set(values("ZeroOrMore/Sequence/GradientStop")),
                    ")"),
                    new LinearGradientAction(pos1,pos2,stops)
                    ),
            //image URL
            Sequence(Sequence("url(",OneOrMore(URLChar()),")"), new ImageURLAction()),
                //hex color values: #abc067
            HexValue(),
                //pixel values: 90px
            Sequence(Sequence(OneOrMore(Number()),FirstOf("px","pt")),
                    new PixelValueAction()
                    ),
                //a comma separated set of string values (mainly for font-family)
                Sequence(Sequence(OneOrMore(WordCharOrSpace()),ZeroOrMore(Sequence(',',OneOrMore(WordCharOrSpace())))),
                        new StringListAction()
                        ),

                //plain string values
            Sequence(OneOrMore(Letter()),
                    new PlainStringValueAction()
                    )


        );
    }

    public Rule WordCharOrSpace() {
        //word chars and spaces
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '#',' ');
    }

    public static class GradientStopAction implements Action {
        private Var<String> hex;
        private Var<String> percentage;

        public GradientStopAction(Var<String> hex, Var<String> percentage) {
            this.hex = hex;
            this.percentage = percentage;
        }

        public boolean run(Context context) {
            context.setNodeValue(new GradientStopValue(hex.get(),percentage.get()));
            return true;
        }
    }

    public Rule GradientStop() {
        final Var<String> hex = new Var<String>();
        final Var<String> percentage = new Var<String>();
        return Sequence(Sequence(
                Spacing(),HexValue(),hex.set(lastText()),
                Spacing(),Number(),percentage.set(lastText()),"%"),new GradientStopAction(hex,percentage)
        );
    }

    public Rule HexValue() {
        return Sequence(Sequence(Ch('#'),ZeroOrMore(FirstOf(CharRange('0','9'),CharRange('a','f')))),
                new Action() {
                    public boolean run(Context context) {
                        context.setNodeValue(new ColorValue(context.getPrevText()));
                        return true;
                    }
                }
                );
    }


    /* common low level reusable rules */
    public final Rule LWING = Terminal("{");
    public final Rule RWING = Terminal("}");
    public final Rule COLON = Terminal(":");
    public final Rule SEMICOLON = Terminal(";");

    @SuppressNode
    @DontLabel
    public Rule Terminal(String string) {
        return Sequence(string, Spacing()).label('\'' + string + '\'');
    }

    @SuppressNode
    public Rule Spacing() {
        return ZeroOrMore(FirstOf(

                // whitespace
                OneOrMore(CharSet(" \t\r\n\f")),

                // traditional comment
                Sequence("/*", ZeroOrMore(Sequence(TestNot("*/"), Any())), "*/"),

                // end of line comment
                Sequence(
                        "//",
                        ZeroOrMore(Sequence(TestNot(CharSet("\r\n")), Any())),
                        FirstOf("\r\n", '\r', '\n', Eoi())
                )
        ));
    }

    public Rule Number() {
        return OneOrMore(CharRange('0', '9'));
    }
    @SuppressNode
    public Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');
    }
    @SuppressNode
    public Rule LetterOrDash() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '-');
    }
    @SuppressNode
    public Rule LetterOrDigit() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_');
    }
    @SuppressNode
    public Rule LetterOrStar() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '*' );
    }
    @SuppressNode
    public Rule LetterOrDigitOrHash() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '#');
    }
    @SuppressNode
    public Rule URLChar() {
        return FirstOf(Letter(),'/','.');
    }

    private static void p(String s) {
        System.out.println(s);
    }

    public static class ImageURLAction implements Action {
        public boolean run(Context context) {
            //u.p("matched a url: " + context.getPrevText());
            try {
                context.setNodeValue(new URLValue(context.getPrevText()));
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return true;
        }
    }

    public static class PixelValueAction implements Action {
        public boolean run(Context context) {
            context.setNodeValue(new IntegerPixelValue(context.getPrevText()));
            return true;
        }
    }

    public static class StringListAction implements Action {
        public boolean run(Context context) {
            String str = context.getPrevText();
            if(str.contains(",")) {
                context.setNodeValue(new StringListValue(str.split(",")));
            } else {
                context.setNodeValue(new StringValue(context.getPrevText()));
            }
            return true;
        }
    }

    public static class PlainStringValueAction implements Action {
        public boolean run(Context context) {
            context.setNodeValue(new StringValue(context.getPrevText()));
            return true;
        }
    }

    public class CSSRuleAction implements Action {
        private final Var matcher;

        public CSSRuleAction(Var matcher) {
            this.matcher = matcher;
        }

        public boolean run(Context context) {
//                        p("value = " + matcher.get());
            CSSRule rule = new CSSRule();
            rule.matchers.addAll((Collection<? extends CSSMatcher>) matcher.get());
            //rule.matcher = (CSSMatcher) ((List)matcher.get()).get(0);
            Node rules = context.getNodeByPath("ZeroOrMore");
            for(Object n : rules.getChildren()) {
                rule.properties.add((CSSProperty) ((Node)n).getValue());
            }
            set(rule);
            return true;
        }
    }

    public class MatchExpressionAction implements Action {
        private final Var<String> elem;
        private final Var<String> pseudo;

        public MatchExpressionAction(Var<String> elem, Var<String> pseudo) {
            this.elem = elem;
            this.pseudo = pseudo;
        }

        public boolean run(Context context) {
            CSSMatcher match = new CSSMatcher();
            match.element = elem.get();
            match.pseudo = pseudo.get();
//                        p("elem = " + elem.get());
//                        p("pseudo = " + pseudo.get());
            set(match);
            return true;
        }
    }

    public class PropertyRuleAction implements Action<String> {
        public boolean run(Context<String> context) {
//                        System.out.println("go//t a rule!: " + prevText() + " value = " + context.getNodeValue() + " " + context.getPrevValue());
//                        p("pn = " + context.getNodeByPath("PropertyName").getValue());
//                        p("pv = " + context.getNodeByPath("PropertyValue").getValue());
            CSSProperty cpa = new CSSProperty();
            cpa.name = context.getNodeByPath("PropertyName").getValue();
            Object v = context.getNodeByPath("PropertyValue").getValue();
            cpa.value = (BaseValue) v;
            set(cpa);
            //context.setNodeValue(cpa);
            return true;
        }
    }

    public static class ToStringAction implements Action<String> {
        public boolean run(Context<String> context) {
            context.setNodeValue(context.getPrevText());
            return true;
        }
    }
}


