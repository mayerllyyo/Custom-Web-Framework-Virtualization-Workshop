package edu.eci.arep;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

public class InvokeMain {

    public static void main(String... args) {
        try {
            Class<?> c = Class.forName(args[0]);

            Class<?>[] argTypes = new Class<?>[]{ String[].class };
            Method main = c.getDeclaredMethod("main", argTypes);

            String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);

            Logger.getLogger(InvokeMain.class.getName()).info("invoking " + c.getName() + ".main()");

            main.invoke(null, (Object) mainArgs);

        } catch (ClassNotFoundException x) {
            x.printStackTrace();
        } catch (NoSuchMethodException x) {
            x.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}