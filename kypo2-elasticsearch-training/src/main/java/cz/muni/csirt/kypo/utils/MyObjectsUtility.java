package cz.muni.csirt.kypo.utils;

/**
 * @author Pavel Šeda (441048)
 */
public final class MyObjectsUtility {

    private MyObjectsUtility() {
        throw new AssertionError("No java.util.MyObjectsUtility instances for you!");
    }

    public static String requireNonNullNonEmptyString(String obj) {
        if (obj == null)
            throw new NullPointerException();
        if (obj.isEmpty())
            throw new IllegalArgumentException();
        return obj;
    }

    public static String requireNonNullNonEmptyString(String obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        if (obj.isEmpty())
            throw new IllegalArgumentException(message);
        return obj;
    }

}
