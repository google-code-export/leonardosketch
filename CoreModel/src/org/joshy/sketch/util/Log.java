package org.joshy.sketch.util;

import org.joshy.gfx.util.u;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Dec 7, 2010
 * Time: 5:16:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Log {
    private static List<LogCollector> collectors = new ArrayList<LogCollector>();

    public static void info(Object ... values) {
        Throwable thr = new Throwable();
        thr.fillInStackTrace();
        StackTraceElement[] stack = thr.getStackTrace();
        LogEvent evt = new LogEvent("INFO",stack, values);
        evt.print();
        for(LogCollector c : collectors) {
            c.info(evt);
        }
    }

    public static void warning(Object ... values) {
        Throwable thr = new Throwable();
        thr.fillInStackTrace();
        StackTraceElement[] stack = thr.getStackTrace();
        LogEvent evt = new LogEvent("WARNING", stack, values);
        evt.print();
        for(LogCollector c : collectors) {
            c.warning(evt);
        }
    }

    public static void error(Object ... values) {
        Throwable thr = new Throwable();
        thr.fillInStackTrace();
        StackTraceElement[] stack = thr.getStackTrace();
        LogEvent evt = new LogEvent("ERROR", stack, values);
        evt.print();
        for(LogCollector c : collectors) {
            c.error(evt);
        }
    }

    public static void addCollector(LogCollector col) {
        collectors.add(col);
    }

    public static void removeCollector(LogCollector col) {
        collectors.remove(col);
    }

    public static class LogCollector {
        public boolean hasErrors() {
            return false;
        }

        public boolean hasWarnings() {
            return false;
        }

        public boolean hasInfo() {
            return false;
        }

        public void info(LogEvent evt) {

        }

        public void warning(LogEvent evt) {

        }

        public void error(LogEvent evt) {
            
        }

        public List<LogEvent> getEvents() {
            return new ArrayList<LogEvent>();
        }
    }

    public static class LogEvent {
        private Object[] values;
        private String clazz;
        private StackTraceElement[] stack;
        private String level;

        public LogEvent(String level, StackTraceElement[] stack, Object[] values) {
            this.level = level;
            clazz = stack[1].getClassName();
            this.stack = stack;
            this.values = values;
        }

        public void print() {
            u.pr(level+" == ");
            for(Object o : values) {
                u.pr(""+o);
                if(o instanceof Throwable) {
                    ((Throwable)o).printStackTrace();
                }
            }
            u.p("");
        }

        public String getReportingClass() {
            return this.clazz;
        }

        public String getLevel() {
            return level;
        }

        public Object[] getValues() {
            return values;
        }
    }
}
