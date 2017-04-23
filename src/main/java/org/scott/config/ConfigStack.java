package org.scott.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ognl.Ognl;
import ognl.OgnlException;

class OgnlExpression {
	private Object expression;

	public OgnlExpression(String expression) throws OgnlException {
	//	System.out.println(expression);
		this.expression = Ognl.parseExpression(expression);
	}
	
	public Object getValue(Map<Object, Object> context, Object root) throws OgnlException {
		//System.out.println("getValue '" + expression + "'");
		return Ognl.getValue(expression, context, root);
	}
	
}

class OgnlMap {
	private static final Pattern ognlExpr = Pattern.compile("\\Q%{\\E(.*)\\Q}\\E");
	public static Object put(Map<Object, Object> map, Object key, Object value) throws OgnlException {
		value = processValue(value);
		if (key != null && value != null) {
			if (key instanceof String) {
				String keyParts[] = ((String)key).split("\\.");
				if (keyParts != null && keyParts.length > 0) {
					Map<Object, Object> parent = map;
					for (int i=0; i<keyParts.length-1; i++) {
						try {
							Map<Object, Object> next = (Map<Object, Object>)parent.get(keyParts[i]);
							if (next == null) {
								parent.put(keyParts[i], next = new HashMap<Object, Object>());
							}
							parent = next;
						}
						catch(ClassCastException x) {
							throw new OgnlException("cannot create key '" + key + "' found non-map object '" + parent.get(keyParts[i]) + "'");
						}
					}
					return parent.put(keyParts[keyParts.length-1], value);
				}
			}
			else {
				return map.put(key, value);
			}
		}
		return null;
	}
	
	private static Object processValue(Object value) throws OgnlException {
		if (value != null && value instanceof String) {
			Matcher m = ognlExpr.matcher((String)value);
			if (m.find()) {
				return new OgnlExpression(m.group(1));
			}
		}
		return value;
		
	}
	
}

class OgnlStack extends HashMap<Object, Object>{
	private static final long serialVersionUID = 1L;
	private List<Map<Object, Object>> stack;
	private Map<Object, Object> context;
	
	public OgnlStack(List<Map<Object, Object>> stack, Map<Object, Object> context) {
		this.stack = stack;
		this.context = context;
	}

	public Object eval(Object key) throws OgnlException {
		OgnlExpression oe = new OgnlExpression(key.toString());
		return processValue(oe.getValue(context, this));
	}
	
	@Override
	public Object get(Object key) {
		for (Map<Object, Object> level: stack) {
			Object value = level.get(key);
			if (value != null) {
				return processValue(value);
			}
		}
		return null;
	}
	
	private Object processValue(Object value) {
		if (value != null && value instanceof OgnlExpression) {
			try {
				return ((OgnlExpression)value).getValue(context, this);
			} catch (OgnlException e) {
				throw new IllegalStateException(e);
			}
		}
		return value;
	}
}

class User {
	private String name;
	
	public User(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

public class ConfigStack {

	public static void main(String[] args) {
		try {
/*			Map<Object,Object> context = new HashMap<Object, Object>();
			context.put("base", "C://context");
			Map<Object,Object> root = new HashMap<Object, Object>();
			root.put("base", "C://root");
			System.out.println(Ognl.getValue("base + '/info'", context, root));
*/
			List<Map<Object, Object>> stack = new LinkedList<Map<Object,Object>>();
			stack.add(new HashMap<Object, Object>());
			stack.add(new HashMap<Object, Object>());
			stack.add(new HashMap<Object, Object>());
			OgnlMap.put(stack.get(0), "root.path", "C:/");
			OgnlMap.put(stack.get(1), "scott.path", "%{root.path + '/datalayout/' + user.name + '/' + query}");
			OgnlMap.put(stack.get(2), "query", "?type.now=shit.hot");
			OgnlMap.put(stack.get(2), "user", new User("johnny"));
			
			OgnlStack os = new OgnlStack(stack, new HashMap<Object, Object>());
			System.out.println(os.eval("scott.path"));
		
		
		
		
		} catch (OgnlException e) {
			e.printStackTrace();
		}
	}

}
