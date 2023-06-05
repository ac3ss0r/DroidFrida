package acessor.droidfrida.js;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class JSMinifier {

	private static final int EOF = -1;
	private final PushbackReader in;
	private final Writer out;
	private int theA;
	private int theB;

	public static String minify(String jsCode) {
		StringReader reader = new StringReader(jsCode);
		StringWriter writer = new StringWriter();
		try {
			JSMinifier min = new JSMinifier(reader, writer);
			min.jsmin();
		}
		catch (Exception e) {
			System.err.println("Failed to minify");
		}
		return writer.toString().trim();
	}

	public JSMinifier(final Reader in, final Writer out) {
		this.in = new PushbackReader(in);
		this.out = out;
	}

	static boolean isAlphanum(final int c) {
		return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$'
			|| c == '\\' || c > 126);
	}

	int get() throws IOException {
		int c = in.read();
		if (c >= ' ' || c == '\n' || c == EOF) {
			return c;
		}
		if (c == '\r') {
			return '\n';
		}
		return ' ';
	}

	int peek() throws IOException {
		int lookaheadChar = in.read();
		in.unread(lookaheadChar);
		return lookaheadChar;
	}

	int next() throws IOException, UnterminatedCommentException {
		int c = get();
		if (c == '/') {
			switch (peek()) {
				case '/':
					for (;;) {
						c = get();
						if (c <= '\n') {
							return c;
						}
					}
				case '*':
					get();
					for (;;) {
						switch (get()) {
							case '*':
								if (peek() == '/') {
									get();
									return ' ';
								}
								break;
							case EOF:
								throw new UnterminatedCommentException();
						}
					}
				default:
					return c;
			}

		}
		return c;
	}

	void action(final int d) throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
	UnterminatedStringLiteralException {
		switch (d) {
			case 1:
				out.write(theA);
			case 2:
				theA = theB;

				if (theA == '\'' || theA == '"') {
					for (;;) {
						out.write(theA);
						theA = get();
						if (theA == theB) {
							break;
						}
						if (theA <= '\n') {
							throw new UnterminatedStringLiteralException();
						}
						if (theA == '\\') {
							out.write(theA);
							theA = get();
						}
					}
				}

			case 3:
				theB = next();
				if (theB == '/' && (theA == '(' || theA == ',' || theA == '=' || theA == ':' || theA == '[' || theA == '!'
					|| theA == '&' || theA == '|' || theA == '?' || theA == '{' || theA == '}' || theA == ';'
					|| theA == '\n')) {
					out.write(theA);
					out.write(theB);
					for (;;) {
						theA = get();
						if (theA == '/') {
							break;
						}
						else if (theA == '\\') {
							out.write(theA);
							theA = get();
						}
						else if (theA <= '\n') {
							throw new UnterminatedRegExpLiteralException();
						}
						out.write(theA);
					}
					theB = next();
				}
		}
	}

	public void jsmin() throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
	UnterminatedStringLiteralException {
		theA = '\n';
		action(3);
		while (theA != EOF) {
			switch (theA) {
				case ' ':
					if (isAlphanum(theB)) {
						action(1);
					}
					else {
						action(2);
					}
					break;
				case '\n':
					switch (theB) {
						case '{':
						case '[':
						case '(':
						case '+':
						case '-':
							action(1);
							break;
						case ' ':
							action(3);
							break;
						default:
							if (isAlphanum(theB)) {
								action(1);
							}
							else {
								action(2);
							}
					}
					break;
				default:
					switch (theB) {
						case ' ':
							if (isAlphanum(theA)) {
								action(1);
								break;
							}
							action(3);
							break;
						case '\n':
							switch (theA) {
								case '}':
								case ']':
								case ')':
								case '+':
								case '-':
								case '"':
								case '\'':
									action(1);
									break;
								default:
									if (isAlphanum(theA)) {
										action(1);
									}
									else {
										action(3);
									}
							}
							break;
						default:
							action(1);
							break;
					}
			}
		}
		out.flush();
	}

	class UnterminatedCommentException extends Exception {
		private static final long serialVersionUID = -2351947272352605042L;
	}

	class UnterminatedStringLiteralException extends Exception {
		private static final long serialVersionUID = -5055563551435617689L;
	}

	class UnterminatedRegExpLiteralException extends Exception {
		private static final long serialVersionUID = 2975193879980704494L;
	}

}
