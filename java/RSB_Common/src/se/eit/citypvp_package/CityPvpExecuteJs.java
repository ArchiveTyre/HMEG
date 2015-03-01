/*
package se.eit.citypvp_package;
import javax.script.*;
public class CityPvpExecuteJs {
	public static int getBlock ()
	ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");

    // JavaScript code in a String
    String script = "function hello(name) { print('Hello, ' + name); }";
    // evaluate script
    engine.eval(script);

    // javax.script.Invocable is an optional interface.
    // Check whether your script engine implements or not!
    // Note that the JavaScript engine implements Invocable interface.
    Invocable inv = (Invocable) engine;

    // invoke the global function named "hello"
    inv.invokeFunction("hello", "Scripting!!" );
}
}
*/