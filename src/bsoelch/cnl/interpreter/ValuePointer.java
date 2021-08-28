package bsoelch.cnl.interpreter;

import bsoelch.cnl.math.MathObject;
import org.jetbrains.annotations.NotNull;

interface ValuePointer extends Action {
    @NotNull
    MathObject getValue();

    @Override
    default boolean requiresArg() {
        return false;
    }

    @Override
    default void pushArg(ValuePointer arg) {
    }
}
