
package info.freelibrary.jiiify;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * A test class for the message codes.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class MessageCodesTest {

    @Test
    public void test() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            InstantiationException {
        for (final Field field : MessageCodes.class.getDeclaredFields()) {
            assertEquals(field.getName(), ((String) field.get(null)).replace('-', '_'));
        }
    }

}
