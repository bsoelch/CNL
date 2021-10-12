package bsoelch.cnl.interpreter;

import bsoelch.cnl.math.MathObject;

interface ValuePointer extends Action {
    /**Value of this ValuePointer (non-null)*/
    MathObject getValue();

    @Override
    default boolean requiresArg() {
        return false;
    }

    @Override
    default void pushArg(ValuePointer arg) {
    }
}
