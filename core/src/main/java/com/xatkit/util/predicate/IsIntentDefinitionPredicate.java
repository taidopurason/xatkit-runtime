package com.xatkit.util.predicate;

import com.xatkit.execution.StateContext;
import com.xatkit.intent.IntentDefinition;
import com.xatkit.intent.RecognizedIntent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;

public class IsIntentDefinitionPredicate implements Predicate<StateContext> {

    @Getter
    private IntentDefinition intentDefinition;

    public IsIntentDefinitionPredicate(IntentDefinition intentDefinition) {
        this.intentDefinition = intentDefinition;
    }

    @Override
    public boolean test(StateContext stateContext) {
        RecognizedIntent recognizedIntent = stateContext.getIntent();
        if(nonNull(recognizedIntent)) {
            if(nonNull(recognizedIntent.getDefinition())) {
                /*
                 * TODO check equals works fine for IntentDefinition.
                 */
                return recognizedIntent.getDefinition().equals(this.intentDefinition);
            } else {
                throw new IllegalStateException(MessageFormat.format("The current {0}'s definition is null",
                        RecognizedIntent.class.getSimpleName()));
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Predicate<StateContext> and(@NotNull Predicate<? super StateContext> other) {
        return new AndPredicate<>(this, other);
    }

    @NotNull
    @Override
    public Predicate<StateContext> or(@NotNull Predicate<? super StateContext> other) {
        return new OrPredicate<>(this, other);
    }

    @NotNull
    @Override
    public Predicate<StateContext> negate() {
        return new NegatePredicate<>(this);
    }
}
