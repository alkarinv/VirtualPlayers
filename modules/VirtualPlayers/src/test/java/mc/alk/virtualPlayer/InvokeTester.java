package mc.alk.virtualPlayer;

import java.util.UUID;
import junit.framework.TestCase;

/**
 * Checks the class type for unknown parameters.
 * 
 * @author Nikolai
 */
public class InvokeTester extends TestCase {
    
    public InvokeTester(String testName) {
        super(testName);
    }
    
    public static void invoker(String methodName, Object... params) {
        System.out.println("params.getClass() = ");
        if (params == null) {
            } else {
                Class[] classParams = new Class[params.length];
                for (int index = 0; index < params.length; index = index + 1) {
                    classParams[index] = params[index].getClass();
                    System.out.println(classParams[index]);
                }
            }
    }
    
    public void testInvoke() {
        String param1 = "something";
        UUID param2 = UUID.randomUUID();
        invoker("someMethod", param1);
        invoker("someMethod", param2);
        invoker("someMethod", param1, param2);
        assertTrue(true);
    }
}
