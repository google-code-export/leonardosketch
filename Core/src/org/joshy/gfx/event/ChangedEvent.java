package org.joshy.gfx.event;

public class ChangedEvent extends Event {
    public static final EventType StringChanged = new EventType("StringChanged");
    public static final EventType DoubleChanged = new EventType("DoubleChanged");
    public static final EventType ColorChanged = new EventType("ColorChanged");
    public static final EventType IntegerChanged = new EventType("IntegerChanged");
    public static final EventType BooleanChanged = new EventType("BooleanChanged");
    private Object value;

    public ChangedEvent(EventType type, Object value, Object source) {
        super(type);
        this.value = value;
        this.source = source;
    }

    public Object getValue() {
        return value;
    }

    public boolean getBooleanValue() {
        return (Boolean)value;
    }
}
