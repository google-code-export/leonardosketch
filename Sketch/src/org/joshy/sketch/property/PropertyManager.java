package org.joshy.sketch.property;

import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.SNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public boolean isPropertyNotNull(String shadow) {
        if(selection == null) return false;
        if(selection.isEmpty()) return false;
        Property property = getProperty(shadow);
        if(property.getValue() == null) return false;
        return true;
    }

    public static class Property {
        private String name;
        private ArrayList<SNode> items;

        public Property(String propName, Selection selection) {
            this.name = propName;
            items = new ArrayList<SNode>();
            for(SNode item : selection.items()) {
                items.add(item);
            }
        }

        public double getDoubleValue() {
            SNode first = items.iterator().next();

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

        public boolean getBooleanValue() {
            SNode first = items.iterator().next();

            try {
                Method method = getBooleanMethod();
                Object value = method.invoke(first);
                Boolean dval = (Boolean) value;
                boolean ddval = dval.booleanValue();
                return ddval;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }

        private Method getMethod() throws NoSuchMethodException {
            SNode first = items.iterator().next();
            try {
                Method method = first.getClass().getMethod("get"+ name.substring(0,1).toUpperCase()+ name.substring(1));
                return method;
            } catch (NoSuchMethodException ex) {
                Method method = first.getClass().getMethod("is"+ name.substring(0,1).toUpperCase()+ name.substring(1));
                return method;
            }
        }
        private Method getBooleanMethod() throws NoSuchMethodException {
            SNode first = items.iterator().next();
            Method method = first.getClass().getMethod("is"+ name.substring(0,1).toUpperCase()+ name.substring(1));
            return method;
        }

        public void setValue(Object value) {
            SNode node = items.iterator().next();
            try {
                String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
                Method method = null;
                for(Method m : node.getClass().getMethods()) {
                    if(m.getName().equals(methodName)) {
                        method = m;
                    }
                }

                if(method == null) {
                    throw new Exception("Method: " + methodName + " not found on object " + node.getClass().getName());
                }
                for(SNode s : items) {
                    method.invoke(s,value);
                }
                //method.invoke(node,value);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void setValue(SNode node, Object o) {
            try {
                String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
                Method method = null;
                for(Method m : node.getClass().getMethods()) {
                    if(m.getName().equals(methodName)) {
                        method = m;
                    }
                }

                if(method == null) {
                    throw new Exception("Method: " + methodName + " not found on object " + node.getClass().getName());
                }
                method.invoke(node,o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean hasSingleValue() {
            try {
                SNode first = items.iterator().next();
                Method meth = getMethod();
                Object value = meth.invoke(first);
                for(SNode item : items) {
                    Object ival = meth.invoke(item);
                    //u.p("comparing: " + ival + " " + value);
                    if(value == null) return false;
                    if(!value.equals(ival)) {
                        return false;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }


        public Object getValue() {
            SNode first = items.iterator().next();
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
        public Map<SNode, Object> getValues() {
            Map<SNode, Object> values = new HashMap<SNode, Object>();
            for(SNode node : items) {
                try {
                    Method method = getMethod();
                    Object value = method.invoke(node);
                    values.put(node, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return values;
        }

    }
}
