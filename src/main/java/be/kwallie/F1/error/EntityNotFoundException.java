package be.kwallie.F1.error;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entity, UUID reference) {
        this(entity, reference.toString());
    }

    public EntityNotFoundException(Class<?> entity, String reference) {
        super(String.format("Unable to find entity '%s' by reference '%s'", entity.getSimpleName(), reference));
    }
}
