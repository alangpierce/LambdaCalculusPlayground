package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import javax.annotation.Nullable;

public interface SlotControllerParent {
    void updateSlotExpression(@Nullable UserExpression userExpression);
    void handleChange();
}
