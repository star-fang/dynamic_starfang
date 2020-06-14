package com.starfang.utilities;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


public class ClassSwitch implements Consumer<Object> {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void cSwitch(Object object, Case<?>... cases) {
        if( cases != null ) {
            for( Case<?> c : cases) {
                if( c.test(object)) {
                    c.accept(object);
                    break;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static final class Case<T> implements Predicate<Object>,
            Consumer<Object> {
        private final Class<T> type;
        private final Consumer<? super T> action;
        public Case( Class<T> type, Consumer<? super T> action ) {
            this.type = Objects.requireNonNull(type, "type");
            this.action = Objects.requireNonNull(action, "action");
        }

        @Override
        public void accept(Object o) {
            action.accept(type.cast(o));
        }

        @Override
        public boolean test(Object o) {
            return type.isInstance(o);
        }
    }

    private final List<Case<?>> cases;

    public ClassSwitch(Case<?>... cases) {
        if( cases == null ) {
            this.cases = Collections.emptyList();
        } else {
            List<Case<?>> list = new ArrayList<>(cases.length);
            for( Case<?> c : cases) {
                list.add(Objects.requireNonNull(c,"case"));
            }
            this.cases = Collections.unmodifiableList(list);
        }
    }

    public List<Case<?>> getCases() {
        return cases;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void accept(Object o) {
        for( Case<?> c : cases ) {
            if( c.test(o) ) {
                c.accept(o);
                break;
            }
        }
    }
}
