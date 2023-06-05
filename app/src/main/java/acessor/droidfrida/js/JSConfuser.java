package acessor.droidfrida.js;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSConfuser {

	/*
	 * This was made using regexps only and should be rewritten to per-char
	 * processing eventually. The regexp implementation is temprorary and doesn't
	 * allow to actually analyze the code structure
	 */

	public static void main(String[] args) {
		String testcode = "function areaOfCircle(e){return Math.PI*e*e}function perimeterOfCircle(e){return 2*Math.PI*e}function volumeOfCube(e){return e*e*e}let radius=5;console.log(\"Area of circle with radius \"+radius+\" is: \"+areaOfCircle(radius)),radius=10,console.log(\"Perimeter of circle with radius \"+radius+\" is: \"+perimeterOfCircle(radius));let side=4;console.log(\"Volume of cube with side \"+side+\" is: \"+volumeOfCube(side));";
		System.out.println(JSConfuser.obfuscate(testcode));
	}

	private static final Random RANDOM = new Random();
	private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String STRDECRYPTOR = "function rot13(t){let r=\"\";for(let e=0;e<t.length;e++){let o=t.charCodeAt(e);o>=97&&o<=122?o=97+(o-97+13)%26:o>=65&&o<=90&&(o=65+(o-65+13)%26),r+=String.fromCharCode(o)}return r}function decryptString(t,r){let e=\"\";for(let o=0;o<t.length;o++)e+=String.fromCharCode(t.charCodeAt(o)-256*r);return rot13(e)}";
	private static final String ANTIDEBUG = "eval(\"function ultrasex(){debugger;}(async()=>{setInterval(()=>{ultrasex()},1e3)})();\");";

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?:let|var|const) ([a-zA-Z0-9]+)(?: |=)");
	private static final Pattern STRING_PATTERN = Pattern.compile("((?:\"|')(.*?[^\\\\])(?:\"|'))"); // strings
	private static final Pattern INTEGER_PATTERN = Pattern.compile("(?:[^a-zA-Z0-9])(\\d+)(?:[^a-zA-Z0-9])");
	private static final Pattern METHOD_PATTERN = Pattern.compile("function ([a-zA-Z0-9]+)\\("); // functions
	private static final Pattern CALL_PATTERN = Pattern.compile("[a-zA-Z0-9\\]](\\.[a-zA-Z0-9]+)\\(.*?\\)");
	// private static final Pattern STATIC_PATTERN = Pattern.compile("(?:[\\n
	// ({=]+)((?!this)[a-zA-Z0-9]+?)[\\.\\[]"); // static

	private StringBuffer sb;

	public JSConfuser(String jsCode) {
		this.sb = new StringBuffer();
		this.sb.append(jsCode);
	}

	public static String obfuscate(String jsCode) {
		JSConfuser confuser = new JSConfuser(JSMinifier.minify(jsCode));
		return confuser.obfuscate();
	}

	/*
	 * This is tricky, the obfuscation is text-based so some of the protections may
	 * mess up the others if used not in correct order. Just keep that in mind
	 */
	public String obfuscate() {

		appendModule(ANTIDEBUG);

		applyBlockReplacement(STRING_PATTERN, new MatchEvent() {
				@Override
				public String onMatch(Matcher matcher, String jsCode) {
					String block = jsCode.substring(matcher.start(), matcher.end());
					if (matcher.group(1).length() > 0) {
						int shift = getRandomInt(1, 10);
						String data = "decryptString(\"" + encryptString(matcher.group(2), shift) + "\"," + shift + ")";
						return block.replace(matcher.group(1), data);
					}
					else
						return block;
				}
			});

		applyBlockReplacement(CALL_PATTERN, new MatchEvent() {
				@Override
				public String onMatch(Matcher matcher, String jsCode) {
					String block = jsCode.substring(matcher.start(), matcher.end());
					int shift = getRandomInt(1, 10);
					String data = "[decryptString(\"" + encryptString(matcher.group(1).substring(1), shift) + "\", " + shift
						+ ")]";
					return block.replace(matcher.group(1), data);
				}
			});

		appendModule(STRDECRYPTOR);

		applyGlobalReplacement(VARIABLE_PATTERN, new MatchEvent() {
				@Override
				public String onMatch(Matcher matcher, String jsCode) {
					if (matcher.group(1).length() > 2)
						return jsCode.replaceAll(matcher.group(1), genRandomName());
					else
						return jsCode;
				}
			});

		applyGlobalReplacement(METHOD_PATTERN, new MatchEvent() {
				@Override
				public String onMatch(Matcher matcher, String jsCode) {
					if (matcher.group(1).length() > 2)
						return jsCode.replaceAll(matcher.group(1), genRandomName());
					else
						return jsCode;
				}
			});

		/*
		 applyBlockReplacement(INTEGER_PATTERN, new MatchEvent() {
		 @Override
		 public String onMatch(Matcher matcher, String jsCode) {
		 Integer value = Integer.parseInt(matcher.group(1));
		 String block = jsCode.substring(matcher.start(), matcher.end());
		 if (value > 0) {
		 return block.replace(matcher.group(1), generateExpression(value, 2));
		 } else
		 return block;
		 }
		 });*/

		return this.sb.toString();
	}

	public void appendModule(String jsCode) {
		sb.insert(0, jsCode);
	}

	public void applyBlockReplacement(Pattern pattern, MatchEvent event) {
		String jsCode = sb.toString();
		sb.setLength(0);
		Matcher matcher = pattern.matcher(jsCode);
		while (matcher.find()) {
			matcher.appendReplacement(sb, event.onMatch(matcher, jsCode));
		}
		matcher.appendTail(sb);
		jsCode = sb.toString();
	}

	public void applyGlobalReplacement(Pattern pattern, MatchEvent event) {
		String jsCode = sb.toString();
		sb.setLength(0);
		Matcher matcher = pattern.matcher(jsCode);
		while (matcher.find()) {
			jsCode = event.onMatch(matcher, jsCode);
		}
		sb.append(jsCode);
	}

	class MatchEvent {
		public String onMatch(Matcher matcher, String jsCode) {
			return jsCode.substring(matcher.start(), matcher.end());
		}
	}

	public static String encryptString(String input, int key) {
		String rotten = rot13(input);
		StringBuilder encrypted = new StringBuilder();
		for (int i = 0; i < rotten.length(); i++) {
			encrypted.append((char) (rotten.charAt(i) + (key * 256)));
		}
		return encrypted.toString();
	}

	public static String rot13(String input) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c >= 'a' && c <= 'z') {
				c = (char) ('a' + (c - 'a' + 13) % 26);
			}
			else if (c >= 'A' && c <= 'Z') {
				c = (char) ('A' + (c - 'A' + 13) % 26);
			}
			output.append(c);
		}
		return output.toString();
	}

	private static String generateExpression(int target, int depth) {
		if (depth == 0 | target < 1) {
			return String.valueOf(target);
		}
		char operator = getRandomOperator();
		int subTarget = getRandomInt(1, Math.abs(target));
		switch (operator) {
			case '+':
				return "(" + generateExpression(target - subTarget, depth - 1) + operator + subTarget + ")";
			case '-':
				return "(" + generateExpression(target + subTarget, depth - 1) + operator + subTarget + ")";
			case '*':
				if (target % subTarget == 0) {
					return "(" + generateExpression(target / subTarget, depth - 1) + operator + subTarget + ")";
				}
				else {
					return generateExpression(target, depth - 1);
				}
			case '/':
				return "(" + generateExpression(target * subTarget, depth - 1) + operator + subTarget + ")";
			default:
				return String.valueOf(target);
		}
	}

	private static char getRandomOperator() {
		char[] operators = { '+', '-', '*', '/' };
		return operators[RANDOM.nextInt(operators.length)];
	}

	private static int getRandomInt(int min, int max) {
		return RANDOM.nextInt((max - min) + 1) + min;
	}

	private static String genRandomName() {
		StringBuilder name = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			name.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
		}
		return name.toString();
	}
}
