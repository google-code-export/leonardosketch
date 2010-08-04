package org.joshy.gfx;

import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.Skin;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.skin.*;
import org.joshy.gfx.util.u;
import org.joshy.gfx.util.x;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 18, 2010
 * Time: 1:58:25 PM
 * To change this template use File | Settings | File Templates.
 */

public class SkinManager {
    private static SkinManager _shared;

    private List<SkinMatchRule> matchRules = new ArrayList<SkinMatchRule>();

    private SkinManager() {
    }

    public static SkinManager getShared() {
        if(_shared == null) {
            _shared = new SkinManager();
        }
        return _shared;
    }

    public void parseStylesheet(URL url) throws Exception {
        Document doc = null;
        u.p("parsing stylesheet from url " + url);
        if(url == null) {
            doc = x.loadDocument(new File("assets/style.xml").toURI().toURL());
        } else {
            doc = x.loadDocument(url);
        }
        //doc.setDocumentURI("assets");
        for(Element ctrlE : x.xpathElements(doc, "/style/control")) {
//            u.p("classname in style = " + ctrlE.getAttribute("classname"));
            for(Element e : x.xpathElements(ctrlE, "part")) {
                String partName = e.getAttribute("name");
                String variant = null;
                if(e.hasAttribute("variant")) variant = e.getAttribute("variant");
//                u.p("Style: part = " + partName + " variant = " + variant);
                for(Element prop : x.xpathElements(e,"property")) {
                    String propName = prop.getAttribute("name");
                    String state = prop.getAttribute("state");
//                    u.p("Style:   property = " + propName + "   state = " + state);
                    for(Element val : x.echildren(prop)) {
//                        u.p("Style:     skin = " + val.getNodeName());
                        initSkin(ctrlE.getAttribute("classname"),  variant, partName, propName, state, val);
                    }
                }
            }
        }
    }

    public Skin getSkin(Class nodeClass, String variant, String part, String property, String state) {
        //u.p("getting skin for " + nodeClass.getName() + " variant = " + variant + " part = " + part + " property = " + property);

        //find a match of partname, variant, classname, state, and property
        for(SkinMatchRule rule : matchRules) {
            if(rule.partName.equals(part) && rule.getVariant().equals(variant)) {
                if(rule.classname.equals(nodeClass.getName())) {
                    if(rule.state.equals(state) && rule.property.equals(property)) {
                        return rule.skin;
                    }
                }
            }
        }
        //skip the state
        //match by partname, variant, classname and property
        for(SkinMatchRule rule : matchRules) {
            if(rule.partName.equals(part) && rule.getVariant().equals(variant)) {
                if(rule.classname.equals(nodeClass.getName())) {
                    if(rule.property.equals(property)) {
                        return rule.skin;
                    }
                }
            }
        }

        //skip variant
        //match by partname, classname, state, and property
        for(SkinMatchRule rule : matchRules) {
            if(rule.variant == "") {
                if(rule.partName.equals(part)) {
                    if(rule.classname.equals(nodeClass.getName())) {
                        if(rule.state.equals(state) && rule.property.equals(property)) {
                            return rule.skin;
                        }
                    }
                }
            }
        }

        //skip state
        //match by partname, classname, property
        for(SkinMatchRule rule : matchRules) {
            if(rule.partName.equals(part) && rule.variant == "") {
                if(rule.classname.equals(nodeClass.getName())) {
                    if(rule.property.equals(property)) {
                        return rule.skin;
                    }
                }
            }
        }

        //couldn't find anything for the class. go up to the super class
        if(nodeClass != Control.class) {
            return getSkin(nodeClass.getSuperclass(),variant,part,property,state);
        }
        return null;
    }

    public void installSkin(String classname, String variant, String partName, String propName, String state, Skin skin) {
        matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, skin));
    }

    public void installSkin(Class clazz, String partName, String propName, Skin skin) {
        matchRules.add(new SkinMatchRule(clazz.getName(), "", partName, propName, "", skin));
    }
    
    private void initSkin(String classname, String variant, String partName, String propName, String state, Element element) throws Exception {
        if("grid9".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new GridNineSkin(element)));
            return;
        }
        if("flatcolor".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new FlatColorSkin(element)));
            return;
        }
        if("shadow".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new ShadowSkin(element)));
            return;
        }
        if("insets".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new InsetsSkin(element)));
            return;
        }
        if("roundrect".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new RoundRectSkin(element)));
            return;
        }
        if("jogltext.font".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new FontSkin(element)));
            return;
        }
        if("image".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname, variant, partName, propName, state, new ImageSkin(element)));
            return;
        }
        if("noop".equals(element.getNodeName())) {
            matchRules.add(new SkinMatchRule(classname,variant,partName,propName,state,new NoopSkin()));
            return;
        }
        u.p("WARNING! found a skin kind that can't be initialized: " + element.getNodeName());
    }

    public Skin getSkin(Node node, String variant, String partName, String propName, String state, InsetsSkin aDefault) {
        Skin s = getSkin(node.getClass(), variant, partName, propName,state);
        if(s == null) {
            return aDefault;
        } else {
            return s;
        }
    }

    public FillSkin getFillSkin(Node node, String part, String property, String state, FillSkin defaultSkin) {
        Skin s = getSkin(node.getClass(), null, part, property, state);
        if(! (s instanceof FillSkin)) {
            return defaultSkin;
        }
        return (FillSkin) s;
    }

    public FontSkin getSkin(Node node, String variant, String part, String property, String state, FontSkin fallback) {
//        u.p("get skin called");
        Skin s = getSkin(node.getClass(), variant, part, property, state);
        //u.p("the returned skin = " + s);
        if(! (s instanceof FontSkin)) {
            //u.p("doing fallback");
            return fallback;
        }
        //u.p("doing real");
        return (FontSkin) s;
    }

    public Skin getSkin(Node node, String style, String part, String property, String state) {
        return getSkin(node.getClass(),style,part,property,state);
    }
    public Skin getSkin(Control control, String part, String property) {
        return getSkin(control.getClass(),null,part,property,null);
    }

    private class SkinMatchRule {
        private String partName;
        private String property;
        private String state;
        private Skin skin;
        private String classname;

        public String getVariant() {
            return variant;
        }

        private String variant;

        public SkinMatchRule(String classname, String variant, String partName, String propName, String state, Skin skin) {
            this.classname = classname;
            this.variant = variant;
            if(this.variant == null) this.variant = "";
            this.partName = partName;
            this.property = propName;
            this.state = state;
            this.skin = skin;
//            u.p("skin rule variant = " + variant);
        }

        @Override
        public String toString() {
            return "SkinMatchRule{" +
                    "partName='" + partName + '\'' +
                    ", property='" + property + '\'' +
                    ", state='" + state + '\'' +
                    ", skin=" + skin +
                    ", classname='" + classname + '\'' +
                    ", variant='" + variant + '\'' +
                    '}';
        }
    }
}
