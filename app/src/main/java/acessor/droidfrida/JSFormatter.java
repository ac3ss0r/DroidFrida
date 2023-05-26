package acessor.droidfrida;

public class JSFormatter {

  /*
   * JavaScript formatter by ac3ss0r (per-char processing)
   * 
   * Project homepage - https://github.com/ac3ss0r/jshift
   */

  public static void main(String[] args) {
	String testcode = "function tjcHsmAp(n,o){return LZFxABAq(\"\\u0045\",(((((0+2)-1)*1)/1)+1))==o?Math[LZFxABAq(\"wtzsi\", ((((286-49)-87)/10)/3))](((((0+1)*1)*8)+1)*n/(((51-23)/4)-2)+32):Math[LZFxABAq(\"tqwpf\", ((((0+2)*6)/3)/2))](((0+1)+4)*(n-(((13+41)-24)+2))/(((((3/1)-1)-1)*1)+8))}console[LZFxABAq(\"orj\", ((((110/10)-5)/2)/1))](tjcHsmAp((((1*3)+1)+6)));;function LZFxABAq(e,t){let r=\"\";for(let o=0;o<e.length;o++){let a=e.charCodeAt(o);if(uCyQkFXL(a)){let e=JZDTxRod(a)?\"a\".charCodeAt(0):\"A\".charCodeAt(0);r+=String.fromCharCode((a-e-t+(((((3+7)+68)-23)-20)-9))%(((0+1)+12)*2)+e)}else r+=e[o]}return r}function uCyQkFXL(e){return e>=\"A\".charCodeAt(0)&&e<=\"Z\".charCodeAt(0)||e>=\"a\".charCodeAt(0)&&e<=\"z\".charCodeAt(0)}function JZDTxRod(e){return e>=\"a\".charCodeAt(0)&&e<=\"z\".charCodeAt(0)};function AeqfIToO(){}(async()=>{setInterval((()=>{AeqfIToO()}),1e3)})();\n";
	System.out.print(JSFormatter.formatJSCode(testcode));
  }

  private String jsCode;
  private StringBuilder sb;
  private int indentSize = 4;

  // expressions, comments and strings should be skipped
  boolean inString = false;
  boolean inComment = false;
  boolean inExpression = false; // while, for

  // the "deepness" of ( and { brackets
  int expressionLayer = 0;
  int indentLevel = 0;

  public JSFormatter(String jsCode, int indent) {
	this.sb = new StringBuilder();
	this.indentSize = indent;
	this.jsCode = jsCode;
  }

  public static String formatJSCode(String jsCode) {
	JSFormatter formatter = new JSFormatter(jsCode, 4);
	return formatter.format();
  }

  public String format() {
	sb.setLength(0); // reset sb
	jsCode = jsCode.replace("\t", repeat(" ", indentSize)); // tabs to space
	// per-char code processing
	for (int i = 0; i < jsCode.length(); i++) {
	  char c = jsCode.charAt(i);

	  if (inComment || inString) {
		sb.append(c);
		if (c == '\n')
		  inComment = false;
		if (c == '"' && jsCode.charAt(i - 1) != '\\')
		  inString = false;
	  } else {
		if (peek(jsCode, "function", i))
		  sb.append("\n");
		if (peek(jsCode, "for", i) || peek(jsCode, "while", i))
		  inExpression = true;
		switch (c) {
		  case '{':
			indentLevel++;
			sb.append("{\n");
			appendIndent(indentLevel);
			break;
		  case '}':
			indentLevel--;
			sb.append("\n");
			appendIndent(indentLevel);
			sb.append("}\n");
			appendIndent(indentLevel);
			break;
		  case '(':
			if (inExpression)
			  expressionLayer++;
			sb.append('(');
			break;
		  case ')':
			if (inExpression)
			  expressionLayer--;
			if (expressionLayer == 0)
			  inExpression = false;
			sb.append(')');
			break;
		  case ';':
			if (jsCode.charAt(i - 1) != ';')// we don't want double ;
			  sb.append(";");
			if (!inExpression) {
			  sb.append('\n');
			  appendIndent(indentLevel);
			}
			break;
		  case '\n':
			sb.append("\n");
			appendIndent(indentLevel);
			break;
		  case '"':
			sb.append('"');
			inString = true;
			break;
		  case '/':
			if (jsCode.charAt(i + 1) == '/') {
			  inComment = true;
			  sb.append("//");
			  i++;
			} else if (jsCode.charAt(i + 1) == '*') {
			  inComment = true;
			  sb.append("/*");
			  i++;
			}
			break;
		  case ' ':
			if (jsCode.charAt(i - 1) != ' ')
			  sb.append(' ');
		  default:
			sb.append(c);
			break;
		}
	  }
	}
	return sb.toString();
  }

  // peek into the code and check strings equality
  public boolean peek(String str, String val, int index) {
	if (val.length() > str.length() - index)
	  return false;
	for (int i = 0; i < val.length(); i++) {
	  if (str.charAt(index + i) != val.charAt(i))
		return false;
	}
	return true;
  }

  private String repeat(String str, int count) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < count; i++)
	  sb.append(str);
	return sb.toString();
  }

  private void appendIndent(int level) {
	sb.append(repeat(" ", indentSize * level));
  }
}
