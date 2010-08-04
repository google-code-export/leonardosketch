package org.joshy.gfx.event;

import org.joshy.gfx.node.Node;
import org.joshy.gfx.util.u;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 19, 2010
 * Time: 10:10:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventBus {
    private Map<MatchRule,List<Callback>> callbacks = new HashMap<MatchRule,List<Callback>>();
    private static EventBus system;
    private Node pressedNode;

    public static EventBus getSystem() {
        if(system == null) {
            system = new EventBus();
        }
        return system;
    }

    protected EventBus() {
    }

    /*
        Listen to the event of type on the specified source. When event happens the
        callback will be called
     */
    public void addListener(Object source, Event.EventType type, Callback<? extends Event> callback) {
        //u.p("listener = " + callbacks);
        MatchRule rule = new MatchRule(source, type, Scope.Self);
        if(!callbacks.containsKey(rule)) {
            callbacks.put(rule,new ArrayList<Callback>());
        }
        callbacks.get(rule).add(callback);
    }
    public void addListener(Scope scope, Object source, Event.EventType type, Callback<? extends Event> callback) {
        MatchRule rule = new MatchRule(source, type, scope);
        if(!callbacks.containsKey(rule)) {
            callbacks.put(rule,new ArrayList<Callback>());
        }
        callbacks.get(rule).add(callback);
    }

    public void addListener(Event.EventType type, Callback<? extends Event> callback) {
        MatchRule rule = new MatchRule(null, type, Scope.Self);
        if(!callbacks.containsKey(rule)) {
            callbacks.put(rule,new ArrayList<Callback>());
        }
        callbacks.get(rule).add(callback);
    }

    //this is the public api. subclasses should override invokeLater and processEvents
    /* publish an event. all relevant listeners will be notified */
    public void publish(Event event) {
        invokeLater(event);
    }

    //subclass can override to implement true invokeLater functionality
    protected void invokeLater(Event event) {
        processEvents(event);
    }

    //process the specified event. subclasses can override
    protected void processEvents(Event event) {
        Map<MatchRule, List<Callback>> callbackMap = new HashMap<MatchRule, List<Callback>>();
        callbackMap.putAll(callbacks);
        for(MatchRule rule : callbackMap.keySet()) {
            if(rule.type.matches(event.getType())) {
//                u.p("checking rule: " + rule.type);
                if(rule.source != null & rule.source != event.getSource()) continue;
                for(Callback cb : callbacks.get(rule)) {
                    cb.call(event);
                }
                if(event.type == KeyEvent.KeyPressed) {
//                    u.p("it's a key event");
                    KeyEvent ke = (KeyEvent) event;
                    if(ke.isAccepted()) {
//                        u.p("it was accepted");
                    } else {
//                        u.p("it wasn't accepted");
                        processContainerScope(ke);
                    }
                }
                if(MouseEvent.MouseAll.matches(event.type)) {
                    processContainerScope(event);
                }
            }
        }
        event.finished();
    }

    //TODO: this method needs to be made recursive
    private void processContainerScope(Event event) {
        Map<MatchRule, List<Callback>> callbackMap = new HashMap<MatchRule, List<Callback>>();
        callbackMap.putAll(callbacks);
//        u.p("node = " + event.getSource());
        if(event.getSource() instanceof Node) {
//            u.p("doing the parent");
            Node node = (Node) event.getSource();
            Node parent = (Node) node.getParent();
//            u.p("Parent = " + parent);
            for(MatchRule rule : callbackMap.keySet()) {
                if(rule.scope == Scope.Container) {
//                    u.p("found a container rule: " + rule.type);
                    if(rule.source == parent && rule.type.matches(event.getType())) {
//                        u.p("matched the parent");
                        for(Callback cb : callbacks.get(rule)) {
                            cb.call(event);
                        }
                        if(event.type == KeyEvent.KeyPressed) {
//                            u.p("it's a key event");
                            KeyEvent ke = (KeyEvent) event;
                            if(ke.isAccepted()) {
//                                u.p("it was accepted");
                            } else {
//                                u.p("it wasn't accepted");
                                //processContainerScope(ke);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String ... args) {
        EventBus bus = EventBus.getSystem();
        Event.EventType type = MouseEvent.MousePressed;
        Node node = null;
        Callback<MouseEvent> cb = new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                u.p("got an event callback: " + event);
                u.p("class = " + event.getClass());
                u.p("type = " + event.getType());
                u.p("xy = " + event.getX() + " " + event.getY());
            }
        };

        //register for events
        bus.addListener(node, type, cb);

        //call an event
        MouseEvent me = new MouseEvent(MouseEvent.MousePressed, 22,33, node);
        bus.publish(me);
    }


    public static void setSystem(EventBus eventBus) {
        system = eventBus;
    }

    public void setPressedNode(Node pressedNode) {
        this.pressedNode = pressedNode;
    }

    public Node getPressedNode() {
        return pressedNode;
    }
}

class MatchRule {
    Object source;
    Event.EventType type;
    Scope scope;

    public MatchRule(Object source, Event.EventType type, Scope scope) {
        this.source = source;
        this.type = type;
        this.scope = scope;
    }
}