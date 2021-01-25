package me.koply.kcommando;

import me.koply.kcommando.internal.CommandType;

import java.lang.reflect.Method;
import java.util.HashMap;

public final class CommandToRun {

    public CommandToRun(Command clazz, String groupName, CommandType type, HashMap<String, MethodToRun> argumentMethods) {
        this.clazz = clazz;
        this.groupName = groupName;
        this.type = type;
        this.argumentMethods = argumentMethods;
    }

    private final Command clazz;
    private final String groupName;
    private final CommandType type;
    private final HashMap<String, MethodToRun> argumentMethods;

    public final Command getClazz() {
        return clazz;
    }

    public final String getGroupName() {
        return groupName;
    }

    public final CommandType getType() {
        return type;
    }

    public HashMap<String, MethodToRun> getArgumentMethods() {
        return argumentMethods;
    }

    public static final class MethodToRun {
        public MethodToRun(Method method, CommandType type) {
            this.method = method;
            this.type = type;
        }

        private final Method method;
        private final CommandType type;

        public Method getMethod() {
            return method;
        }

        public CommandType getType() {
            return type;
        }
    }
}