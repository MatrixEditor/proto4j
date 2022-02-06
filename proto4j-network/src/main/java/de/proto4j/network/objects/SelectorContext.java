package de.proto4j.network.objects;//@date 04.02.2022

import de.proto4j.annotation.server.requests.selection.Selector;

import java.lang.reflect.Parameter;

public interface SelectorContext {

    boolean hasDefaultSelection();

    default Parameter[] getParameters() {
        return new Parameter[0];
    }

    default Selector getSelector() {
        return null;
    }

    static SelectorContext ofMethod(Parameter[] parameters) {
        return new SelectorContext() {
            @Override
            public boolean hasDefaultSelection() {
                return true;
            }

            @Override
            public Parameter[] getParameters() {
                return parameters;
            }
        };
    }

    static SelectorContext ofSelector(Selector s) {
        return new SelectorContext() {
            @Override
            public boolean hasDefaultSelection() {
                return false;
            }

            @Override
            public Selector getSelector() {
                return s;
            }
        };
    }
}
