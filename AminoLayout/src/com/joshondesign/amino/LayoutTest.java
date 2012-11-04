package com.joshondesign.amino;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.joshy.gfx.Core;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 11/3/12
 * Time: 7:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LayoutTest {
    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                try {
                    doit();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    private static void doit() throws Exception {
        File file = new File("AminoLayout/layoutest.xml");
        Panel panel = loadGUI(new FileInputStream(file));
        Stage stage = Stage.createStage();
        stage.setContent(panel);
        stage.centerOnScreen();
    }

    public static Panel loadGUI(InputStream in) throws Exception {
        Doc xml = XMLParser.parse(in);
        Elem layout = xml.xpathElement("/layout");
        Elem rootpanel = layout.withTag("anchorpanel").iterator().next();
        AnchorPanel panel = new AnchorPanel();
        for(Elem child : rootpanel.xpath("*")) {
            Node node = createNode(child.name());
            if(node == null) continue;
            setProperty(node,"text",child.attr("text"));
            setProperty(node,"translateX",child.attr("x"));
            setProperty(node,"translateY",child.attr("y"));
            setProperty(node,"prefWidth",child.attr("width"));
            setProperty(node,"prefHeight", child.attr("height"));
            setProperty(node,"enabled", child.attr("enabled"));
            setProperty(node,"id", child.attr("id"));
            setProperty(node,"selected", child.attr("selected"));
            AnchorPanel.Constraint c = parseConstraint(child);
            panel.add(node,c);
        }
        return panel;
    }

    private static AnchorPanel.Constraint parseConstraint(Elem child) {
        AnchorPanel.Constraint c = new AnchorPanel.Constraint();
        if(child.hasAttr("right")) {
            c.right = true;
            c.rightValue = Double.parseDouble(child.attr("right"));
        }
        if(child.hasAttr("left")) {
            c.left = true;
            c.leftValue = Double.parseDouble(child.attr("left"));
        }
        if(child.hasAttr("bottom")) {
            c.bottom = true;
            c.bottomValue = Double.parseDouble(child.attr("bottom"));
        }
        return c;
    }

    private static void setProperty(Node node, String name, String stringValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        u.p("---");
//        u.p("string value = " + stringValue);
        if(stringValue == null || stringValue.trim().length() < 1) return;
        String getterName = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
        Method getter = getMethod(node,getterName);
        if(getter == null) {
            getterName = "is"+name.substring(0,1).toUpperCase()+name.substring(1);
            getter = getMethod(node,getterName);
        }
        if(getter == null) {
//            return;
        }
//        u.p("getter = " + getter);
//        u.p("type = " + getter.getReturnType());

        Object value = convertToValueFromString(getter, stringValue);
        String setterName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
        Method setter = getMethod(node,setterName);
        setter.invoke(node,value);
        u.p("set the value " + value + " for the property " + name);
    }

    private static Object convertToValueFromString(Method getter, String stringValue) {
        Object value = null;
        if(getter.getReturnType() == double.class) {
            value = Double.parseDouble(stringValue);
        }
        if(getter.getReturnType().isAssignableFrom(boolean.class)) {
            value = Boolean.parseBoolean(stringValue);
        }
        if(getter.getReturnType().isAssignableFrom(String.class)) {
            value = stringValue;
        }
        return value;
    }

    private static Method getMethod(Node node, String name) {
        for(Method method : node.getClass().getMethods()) {
            if(method.getName().startsWith(name)) {
                return method;
            }
        }
        return null;
    }

    private static Node createNode(String name) {
        if(name.equals("button")) return new Button();
        if(name.equals("textbox")) return new Textbox();
        if(name.equals("checkbox")) return new Checkbox();
        if(name.equals("togglebutton")) return new Togglebutton("");
        if(name.equals("radiobutton")) return new Radiobutton("");
        if(name.equals("label")) return new Label("asdf");
        return null;
    }

}
