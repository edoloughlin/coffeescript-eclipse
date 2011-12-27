package csep.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.Token;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parsetree.reconstr.Serializer;

import com.google.inject.Guice;

import csep.CoffeeScriptRuntimeModule;
import csep.parser.antlr.internal.InternalCoffeeScriptLexer;

/**
 * Used mostly for debugging
 * 
 * @author Adam Schmideg <adam@schmideg.net>
 */
public class Helper {
	public final static String INDENT = "  ";
	private static Serializer serializer;
	public final static Set<String> IGNORE_PROP_NAMES = new HashSet<String>(
			Arrays.asList(new String[] { "class", "operator" }));

	public static Serializer getSerializer() {
		if (serializer == null) {
			serializer = Guice.createInjector(new CoffeeScriptRuntimeModule())
					.getInstance(Serializer.class);
		}
		return serializer;
	}

	/**
	 * Create a human-friendly string representation of a parse element
	 * 
	 * @param eobj
	 * @return
	 */
	public static String stringify(EObject eobj) {
		return stringify(eobj, INDENT);
	}

	protected static String stringify(Object obj, String indent) {
		if (obj == null) {
			return "null";
		}
		StringBuffer buf = new StringBuffer();
		Class<?> clazz = obj.getClass();
		Map<String, Object> props = getProperties(obj);
		Object maybeOperator = props.get("operator");
		String name = clazz.getSimpleName();
		if (maybeOperator != null) {
			name += "" + maybeOperator;
		}
		buf.append(name + "\n");
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			Object child = entry.getValue();
			if (child == null)
				continue;
			if (IGNORE_PROP_NAMES.contains(entry.getKey()))
				continue;
			if (child instanceof List && ((List<?>) child).isEmpty())
				continue;
			buf.append(indent + entry.getKey() + ":");
			if (child instanceof EObject) {
				buf.append(" " + stringify((EObject) child, indent + INDENT));
			} else if (child instanceof List) {
				List<?> list = (List<?>) child;
				buf.append("\n");
				for (Object kid : list) {
					buf.append(indent + INDENT);
					buf.append(stringify(kid, indent + INDENT + INDENT));
				}
			} else {
				buf.append(" " + child + "\n");
			}

		}
		return buf.toString().replace("Impl", "");
	}

	/**
	 * Why do I have to write this function??!
	 */
	public static Map<String, Object> getProperties(Object obj) {
		Map<String, Object> props = new HashMap<String, Object>();
		for (Method m : obj.getClass().getMethods()) {
			int mod = m.getModifiers();
			if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)
					&& m.getParameterTypes().length == 0
					&& m.getName().startsWith("get")) {
				String name = m.getName().substring(3, 4).toLowerCase()
						+ m.getName().substring(4);
				Object value = null;
				try {
					value = m.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
				props.put(name, value);
			}
		}
		return props;
	}

	/**
	 * Given a class with static int fields, get the field name for an id
	 */
	@SuppressWarnings("rawtypes")
	public static String getFieldNameForId(Class clazz, int id) {
		for (Field f : clazz.getFields()) {
			try {
				Object fieldValue = f.get(null);
				if (fieldValue.equals(id)) {
					return f.getName();
				}
			} catch (Exception e) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Get only the token name and text
	 */
	public static String getNameAndText(Token token) {
		if (token == null) {
			return null;
		} 
		else {
			String value = token.getText();
			String name = Helper.getFieldNameForId(InternalCoffeeScriptLexer.class, token.getType());
			if (name == null)
				name = "<" + token.getType() + ">";
			if (name.startsWith("RULE_"))
				name = name.substring("RULE_".length());
			if (token.getType() == InternalCoffeeScriptLexer.RULE_TERMINATOR)
				value = "";
			return name + ":" + value;
		}
	}

	/**
	 * Check whether an indented block should start after <var>line</var>
	 * @param line
	 * @return if it's a special line
	 */
	public static boolean isBlockContainer(String line) {
		if (line.endsWith("="))
			return true;
		return false;
	}
	
}
