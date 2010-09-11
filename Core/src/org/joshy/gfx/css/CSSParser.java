package org.joshy.gfx.css;

import org.joshy.gfx.css.values.*;
import org.joshy.gfx.css.values.StringValue;
import org.parboiled.*;
import org.parboiled.annotations.DontLabel;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.support.Var;

import java.util.ArrayList;
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
        final Var<List<CSSMatcher>> matcher = new Var<List<CSSMatcher>>();
        final Var properties = new Var();
        return Sequence(
                //a set of match expressions
                MatchExpression(),
                matcher.set(new ArrayList()),
                matcher.get().add((CSSMatcher) this.value("MatchExpression")),
                ZeroOrMore(Sequence(
                        Optional(Spacing()),
                        ',',
                        Optional(Spacing()),
                        MatchExpression(),
                        matcher.get().add((CSSMatcher) this.value("MatchExpression")),
                        Optional(Spacing()))),
                Spacing(),
                LWING,
                ZeroOrMore(PropertyRule()),
                properties.set(this.nodes("ZeroOrMore")),
                RWING,
                new CSSRuleAction(matcher,properties)
        );
    }

    public Rule MatchExpression() {
        final Var<String> elem = new Var<String>();
        final Var<String> pseudo = new Var<String>();
        final Var<String> id = new Var<String>();
        final Var<String> cssClass = new Var<String>();
        return Sequence(
                FirstOf(
                    //#foo : match id
                    Sequence(Hash(),Sequence(Letter(),ZeroOrMore(LetterOrDigit())),
                            toString, id.set((String)value())),
                    //foo : match element name
                    Sequence(Sequence(Letter(), ZeroOrMore(LetterOrDigit())),
                            toString,  elem.set((String) value())),
                    //* : match all
                    Sequence(LetterOrStar(),
                            toString,elem.set((String)value())),
                    //.foo : match css class
                    Sequence(Period(),Sequence(LetterOrDash(),ZeroOrMore(LetterOrDigitOrDash())),
                            toString, cssClass.set((String)value()))
                )

                //pseudo class
                ,Optional(Sequence(':',Sequence(OneOrMore(Letter()),toString,pseudo.set((String) value()))))
                //turn into a match expression
                ,new MatchExpressionAction(elem, pseudo,id, cssClass)
                );
    }

    public Rule PropertyRule() {
        final Var<String> propName = new Var<String>();
        final Var propValue = new Var();
        return FirstOf(
                //margin shortcut
                Sequence(
                    Spacing(),
                    "margin",
                    Spacing(),
                    COLON,
                    Spacing(),
                    OneOrMore(
                        Sequence(OneOrMore(Number()),FirstOf("px","pt"),Spacing())
                    ),toString,propValue.set(value()),
                    Spacing(),
                    SEMICOLON,
                    new InsetsRuleAction("margin","",propValue)
                ),
                //padding shortcut
                Sequence(
                    Spacing(),
                    "padding",
                    Spacing(),
                    COLON,
                    Spacing(),
                    OneOrMore(
                        Sequence(OneOrMore(Number()),FirstOf("px","pt"),Spacing())
                    ),toString,propValue.set(value()),
                    Spacing(),
                    SEMICOLON,
                    new InsetsRuleAction("padding","",propValue)
                ),
                //border-width shortcut
                Sequence(
                    Spacing(),
                    "border-width",
                    Spacing(),
                    COLON,
                    Spacing(),
                    OneOrMore(
                        Sequence(OneOrMore(Number()),FirstOf("px","pt"),Spacing())
                    ),toString,propValue.set(value()),
                    Spacing(),
                    SEMICOLON,
                    new InsetsRuleAction("border","-width",propValue)
                ),
                //other property name
                Sequence(
                    Spacing(),
                    PropertyName(),propName.set((String)value()),
                    Spacing(),
                    COLON,
                    Spacing(),
                    PropertyValue(),propValue.set(value()),
                    Spacing(),
                    SEMICOLON
                    ,new PropertyRuleAction(propName,propValue)
                )
        );
    }

    public Action toString = new ToStringAction();
    
    public Rule PropertyName() {
        return Sequence(OneOrMore(LetterOrDigitOrDash()), toString);
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
        final Var<String> textShadowColor = new Var<String>();
        final Var<String> shadowXoff = new Var<String>();
        final Var<String> shadowYoff = new Var<String>();
        final Var<String> blurRadius = new Var<String>();
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
            //text shadows
            Sequence(Sequence(
                    HexValue(),textShadowColor.set(lastText()),Spacing(),
                    PixelValue(),shadowXoff.set(lastText()),Spacing(),
                    PixelValue(),shadowYoff.set(lastText()),Spacing(),
                    PixelValue(),blurRadius.set(lastText()),Spacing()),
                    new TextShadowAction(textShadowColor,shadowXoff,shadowYoff,blurRadius)
                    ),
            //image URL
            Sequence(Sequence("url(",OneOrMore(URLChar()),")"), new ImageURLAction()),
                //color constants like 'red', 'green', and 'blue'
            ColorConstant(),
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

    public Rule PixelValue() {
        return Sequence(Optional("-"),OneOrMore(Number()),"px");
    }

    public Rule WordCharOrSpace() {
        //word chars and spaces
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '#',' ');
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
        return Sequence(Sequence(Ch('#'),ZeroOrMore(FirstOf(CharRange('0','9'),CharRange('a','f'),CharRange('A','Z')))),
                new Action() {
                    public boolean run(Context context) {
                        context.setNodeValue(new ColorValue(context.getPrevText()));
                        return true;
                    }
                }
                );
    }

    public Rule ColorConstant() {
        return Sequence(FirstOf(
                "red","green","blue",
                "black","white"), new Action() {
            public boolean run(Context context) {
                context.setNodeValue(new ColorValue(context.getPrevText()));
                return true;
            }
        });
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
    public Rule Hash() {
        return CharSet('#');
    }
    
    public Rule Period() {
        return CharSet('.');
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
    public Rule LetterOrDigitOrDash() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_','-');
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

    public static class ImageURLAction implements Action {
        public boolean run(Context context) {
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
        private final Var<List<CSSMatcher>> matcher;
        private Var properties;

        public CSSRuleAction(Var<List<CSSMatcher>> matcher, Var properties) {
            this.matcher = matcher;
            this.properties = properties;
        }

        public boolean run(Context context) {
            CSSRule rule = new CSSRule();
            rule.matchers.addAll((Collection<? extends CSSMatcher>) matcher.get());
            for(Object n : context.getLastNode().getChildren()) {
                Object value = ((Node)n).getValue();
                if(value instanceof CSSProperty) {
                    rule.addProperty((CSSProperty) value);
                }
                if(value instanceof CSSPropertySet) {
                    CSSPropertySet set = (CSSPropertySet) value;
                    for(CSSProperty prop : set.getProps()) {
                        rule.addProperty(prop);
                    }
                }
            }
            set(rule);
            return true;
        }
    }

    public class MatchExpressionAction implements Action {
        private final Var<String> elem;
        private final Var<String> pseudo;
        private final Var<String> id;
        private final Var<String> cssClass;

        public MatchExpressionAction(Var<String> elem, Var<String> pseudo, Var<String> id, Var<String> cssClass) {
            this.elem = elem;
            this.pseudo = pseudo;
            this.id = id;
            this.cssClass = cssClass;
        }

        public boolean run(Context context) {
            CSSMatcher match = new CSSMatcher();
            match.element = elem.get();
            match.pseudo = pseudo.get();
            match.id = id.get();
            if(cssClass.get() != null) {
                match.classes.add(cssClass.get());
            }
            /*p("--------");
            p("elem = " + elem.get());
            p("pseudo = " + pseudo.get());
            p("id = " + id.get());
            p("class = " + cssClass.get());*/
            set(match);
            return true;
        }
    }

    public class PropertyRuleAction implements Action<String> {
        private Var<String> propName;
        private Var propValue;

        public PropertyRuleAction(Var<String> propName, Var propValue) {
            this.propName = propName;
            this.propValue = propValue;
        }

        public boolean run(Context context) {
//            p("prop name = " + propName.get());
//            p("prop value = " + propValue.get());
            CSSProperty cpa = new CSSProperty();
            cpa.name = propName.get();
            cpa.value = (BaseValue) propValue.get();
            context.setNodeValue(cpa);
            set(cpa);
            return true;
        }
    }
                                                    
    public static class ToStringAction implements Action<String> {
        public boolean run(Context<String> context) {
            context.setNodeValue(context.getPrevText());
            return true;
        }
    }

    public static class TextShadowAction implements Action {
        private Var<String> color;
        private Var<String> xoff;
        private Var<String> yoff;
        private Var<String> radius;

        public TextShadowAction(Var<String> textShadowColor, Var<String> shadowXoff, Var<String> shadowYoff, Var<String> blurRadius) {
            this.color = textShadowColor;
            this.xoff = shadowXoff;
            this.yoff = shadowYoff;
            this.radius = blurRadius;
        }

        public boolean run(Context context) {
            context.setNodeValue(new ShadowValue(color.get(), xoff.get(),yoff.get(), radius.get()));
            return true;
        }
    }

    public class InsetsRuleAction implements Action {
        private Var propValue;
        private String prefix;
        private String suffix;

        public InsetsRuleAction(String prefix, String suffix, Var propValue) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.propValue = propValue;
        }

        public boolean run(Context context) {
            String[] parts = (""+propValue.get()).split(" ");

            CSSProperty right = new CSSProperty();
            CSSProperty left = new CSSProperty();
            CSSProperty top = new CSSProperty();
            CSSProperty bottom = new CSSProperty();
            right.name = prefix+"-right"+suffix;
            left.name = prefix+"-left"+suffix;
            top.name = prefix+"-top"+suffix;
            bottom.name = prefix+"-bottom"+suffix;
            if(parts.length == 1) {
                top.value = new IntegerPixelValue(parts[0]);
                right.value = new IntegerPixelValue(parts[0]);
                bottom.value = new IntegerPixelValue(parts[0]);
                left.value = new IntegerPixelValue(parts[0]);
            }
            if(parts.length == 2) {
                top.value = new IntegerPixelValue(parts[0]);
                bottom.value = new IntegerPixelValue(parts[0]);
                right.value = new IntegerPixelValue(parts[1]);
                left.value = new IntegerPixelValue(parts[1]);
            }
            if(parts.length == 3) {
                top.value = new IntegerPixelValue(parts[0]);
                right.value = new IntegerPixelValue(parts[1]);
                left.value = new IntegerPixelValue(parts[1]);
                bottom.value = new IntegerPixelValue(parts[2]);
            }
            if(parts.length == 4) {
                top.value = new IntegerPixelValue(parts[0]);
                right.value = new IntegerPixelValue(parts[1]);
                bottom.value = new IntegerPixelValue(parts[2]);
                left.value = new IntegerPixelValue(parts[3]);
            }


            CSSPropertySet set = new CSSPropertySet();
            set.add(right,left,top,bottom);
            context.setNodeValue(set);
            set(set);
            return true;
        }
    }
}


