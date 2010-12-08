package org.joshy.sketch.property;

import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.util.u;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.SNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: May 14, 2010
 * Time: 8:41:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyManager {
    private Selection selection;

    public PropertyManager() {
        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>(){
            public void call(Selection.SelectionChangeEvent event) {
                selection = event.getSelection();
            }
        });

    }

    public boolean isClassAvailable(Class aClass) {
        if(selection == null) return false;
        if(selection.isEmpty()) return false;

        for(SNode item : selection.items()) {
            if(!aClass.isAssignableFrom(item.getClass())) {
                return false;
            }
        }
        return true;
    }

    public Property getProperty(String propName) {
        return new Property(propName,selection);
    }

    public static class Property {
        private String name;
        private Selection selection;

        public Property(String propName, Selection selection) {
            this.name = propName;
            this.selection = selection;
        }

        public double getDoubleValue() {
            SNode first = selection.items().iterator().next();

            try {
                Method method = getMethod();
                Object value = method.invoke(first);
                Double dval = (Double) value;
                double ddval = dval.doubleValue();
                return ddval;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0;
        }

        private Method getMethod() throws NoSuchMethodException {
            SNode first = selection.items().iterator().next();
            Method method = first.getClass().getMethod("get"+ name.substring(0,1).toUpperCase()+ name.substring(1));
            return method;
        }

        public void setValue(Object value) {
            SNode node = selection.items().iterator().next();
            try {
                String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
                Method method = null;
                for(Method m : node.getClass().getMethods()) {
                    if(m.getName().equals(methodName)) {
                        method = m;
                    }
                }

                for(SNode s : selection.items()) {
                    method.invoke(s,value);
                }
                //method.invoke(node,value);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public boolean hasSingleValue() {
            try {
                SNode first = selection.items().iterator().next();
                Method meth = getMethod();
                Object value = meth.invoke(first);
                for(SNode item : selection.items()) {
                    Object ival = meth.invoke(item);
                    //u.p("comparing: " + ival + " " + value);
                    if(value == null) return false;
                    if(!value.equals(ival)) {
                        return false;
                    }
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return true;
        }


        public Object getValue() {
            SNode first = selection.items().iterator().next();
            try {
                Method method = getMethod();
                Object value = method.invoke(first);
                return value;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
