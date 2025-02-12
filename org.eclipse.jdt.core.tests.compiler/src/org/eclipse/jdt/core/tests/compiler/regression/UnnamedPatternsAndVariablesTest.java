/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class UnnamedPatternsAndVariablesTest extends AbstractBatchCompilerTest {

	private static String[] JAVAC_OPTIONS = new String[] { "--enable-preview" };

	public static Test suite() {
		return buildMinimalComplianceTestSuite(UnnamedPatternsAndVariablesTest.class, F_21);
	}

	public UnnamedPatternsAndVariablesTest(String name) {
		super(name);
	}

	@Override
	protected Map<String, String> getCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions(super.getCompilerOptions());
		if (compilerOptions.sourceLevel == ClassFileConstants.JDK21) {
			compilerOptions.enablePreviewFeatures = true;
		}
		return compilerOptions.getMap();
	}

	public void runConformTest(String[] files, String expectedOutput) {
		super.runConformTest(files, expectedOutput, null, JAVAC_OPTIONS);
	}

	public void testAllSnippetsFromUnnamedVariablesAndPatternsProposal() {
		runConformTest(new String[] { "X.java", """
				import java.util.Queue;
				import java.util.LinkedList;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				record Box<T extends Ball>(T content) { }

				sealed abstract class Ball permits RedBall, BlueBall, GreenBall { }
				final  class RedBall   extends Ball { }
				final  class BlueBall  extends Ball { }
				final  class GreenBall extends Ball { }

				public class X {
					public static void main(String[] args) throws Exception {
						ColoredPoint r = new ColoredPoint(new Point(3,4), Color.GREEN);
						if (r instanceof ColoredPoint(Point(int x, int y), Color _)) {
						}
						if (r instanceof ColoredPoint(Point(int x, int y), var _)) {
						}
						for (int i = 0, _ = sideEffect(); i < 10; i++) {
						}
						Queue<Integer> q = new LinkedList<>();
						q.offer(1); q.offer(1); q.offer(1);
						while (q.size() >= 3) {
							var x = q.remove();
							var _ = q.remove();       // Unnamed variable
							var _ = q.remove();       // Unnamed variable
						}
						String s = "";
						try {
							int i = Integer.parseInt(s);
						} catch (NumberFormatException _) {        // Unnamed variable
							System.out.println("Bad number: " + s);
						} catch (NullPointerException _) {
						}
						class ScopedContext implements AutoCloseable {
							public static ScopedContext acquire() {
								return null;
							}
							@Override
							public void close() throws Exception {
							}
						}
						try (var _ = ScopedContext.acquire()) {    // Unnamed variable
						}
						Stream<String> stream = new LinkedList<String>().stream();
						stream.collect(Collectors.toMap(String::toUpperCase, _ -> "NODATA")) ;
						Ball ball = new GreenBall();
						switch (ball) {
							case RedBall   _   -> process(ball);
							case BlueBall  _  -> process(ball);
							case GreenBall _ -> stopProcessing();
						}
						Box<? extends Ball> box = new Box<>(new GreenBall());
						switch (box) {
							case Box(RedBall   red)     -> processBox(box);
							case Box(BlueBall  blue)    -> processBox(box);
							case Box(GreenBall green)   -> stopProcessing();
							case Box(var       itsNull) -> pickAnotherBox();
							default -> throw new IllegalArgumentException("Unexpected value: " + box);
						}
						switch (box) {
							case Box(RedBall _)   -> processBox(box);   // Unnamed pattern variable
							case Box(BlueBall _)  -> processBox(box);   // Unnamed pattern variable
							case Box(GreenBall _) -> stopProcessing();  // Unnamed pattern variable
							case Box(var _)       -> pickAnotherBox();  // Unnamed pattern variable
							default -> throw new IllegalArgumentException("Unexpected value: " + box);
						}
					}

					private static Object pickAnotherBox() {
						// TODO Auto-generated method stub
						return null;
					}

					private static Object processBox(Box<? extends Ball> box) {
						// TODO Auto-generated method stub
						return null;
					}

					private static Object stopProcessing() {
						return null;
					}

					private static Object process(Ball ball) {
						return null;
					}

					static int sideEffect() {
						return 0;
					}

					class Order {}

					static int count(Iterable<Order> orders) {
						int total = 0;
						for (Order _ : orders)    // Unnamed variable
							total++;
						return total;
					}
				}
				"""}, "Bad number:");
	}

	public void testCatchStatementWithUnnamedVars() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String... args) {
						try {
							throw new Exception();
						} catch( Exception _) {
							int i = 12;
							System.out.println(Integer.toString(i));
						}
					}
				}
				"""}, "12");
	}

	public void testTryWithResourcesWithUnnamedVars() {
		runConformTest(new String[] { "A.java", """
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				public class A {
					public static void main(String... args) {
						File f = null;
						try (final InputStream _ = new FileInputStream(f)){
							System.out.println("unexpected success");
						} catch( Exception e) {
							System.out.println("expected failure");
						}
					}
				}
				"""}, "expected failure");
	}

	public void testLambdaUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				public class A {
					interface FuncInterface {
						void abstractFun(int x, String y);
					}
					public static void main(String args[]) {
						FuncInterface fobj = (int x, String _) -> System.out.println(2 * x);
						fobj.abstractFun(5, "blah");
					}
				}
				"""}, "10");
	}

	public void testLambdaBracketedUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc =  (Integer _) -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaNoTypeBracketedUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc =  (_) -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaNoTypeNoBracketsUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc =  _ -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaTypeWithNoParens() {
		runNegativeTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc = Integer _ -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""},
				"""
				----------
				1. ERROR in A.java (at line 4)
					Function<Integer, String> myFunc = Integer _ -> "Hello";
					                                   ^^^^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				2. ERROR in A.java (at line 4)
					Function<Integer, String> myFunc = Integer _ -> "Hello";
					                                   ^^^^^^^
				Syntax error, insert ";" to complete BlockStatements
				----------
				3. ERROR in A.java (at line 4)
					Function<Integer, String> myFunc = Integer _ -> "Hello";
					                                                ^^^^^^^
				Syntax error, insert "AssignmentOperator Expression" to complete Expression
				----------
				""");
	}

	public void testLambdaBiFunctionBracketedWithOneNamedParam() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.BiFunction;
				public class A {
					public static void main(String... args) {
						BiFunction<Integer, Integer, String> myFunc =  (_,b) -> "Hello, " + b;
						System.out.println(myFunc.apply(2, 3));
					}
				}
				"""}, "Hello, 3");
	}

	public void testLambdaBiFunctionBracketedWithNoNamedParam() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.BiFunction;
				public class A {
					public static void main(String... args) {
						BiFunction<Integer, Integer, String> myFunc =  (_,_) -> "Hello";
						System.out.println(myFunc.apply(2, 3));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaBiFunctionUnbracketedWithNoNamedParam() {
		runNegativeTest(new String[] { "A.java", """
				import java.util.function.BiFunction;
				public class A {
					public static void main(String... args) {
						BiFunction<Integer, Integer, String> myFunc =  _,_ -> "Hello";
						System.out.println(myFunc.apply(2, 3));
					}
				}
				"""},
				"""
				----------
				1. ERROR in A.java (at line 4)
					BiFunction<Integer, Integer, String> myFunc =  _,_ -> "Hello";
					                                                ^
				Syntax error on token ",", -> expected
				----------
				""");
	}

	public void testInstanceOfPatternMatchingWithUnnamedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(_, Point(_, _))) {
							System.out.println("matched point");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point");
	}

	public void testInstanceOfPatternMatchingWithMixedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(_, Point(_, int y))) {
							System.out.println("matched point! y: " + y);
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point! y: 2");
	}

	public void testInstanceOfPatternMatchingWithMixedPatterns2() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(_, Point(int x, _))) {
							System.out.println("matched point! x: " + x);
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point! x: 1");
	}

	public void testInstanceOfPatternMatchingWithUnnamedVariables() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(String _, Point(int _, int _))) {
							System.out.println("matched point");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point");
	}

	public void testSwitchPatternMatchingWithUnnamedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(_, Point(_, _)) -> System.out.println("I am utilizing pattern matching");
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "I am utilizing pattern matching");
	}

	public void testSwitchPatternMatchingWithMixedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(_, Point(int x, _)) -> System.out.println(x);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "1");
	}

	public void testSwitchPatternMatchingWithUnnamedVariables() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(String _, Point(int _, int y)) -> System.out.println(y);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "2");
	}

	public void testSwitchPatternMatchingWithUnnamedVariablesVar() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(String _, Point(var _, int y)) -> System.out.println(y);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "2");
	}

	public void testSwitchPatternMatchingWithUnnamedVariablesUnicodeEscape() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(String _, Point(var \\u005F, int y)) -> System.out.println(y);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "2");
	}

	public void testEnhancedForLoopVariableWithModifier() {
		runConformTest(new String[] { "A.java", """
				import java.util.List;
				public class A {
					public static void main(String... args) {
						List<String> myList = List.of("hi", "hello", "salu", "bonjour");
						int accumulator = 0;
						for (final String _ : myList) {
							accumulator++;
						}
						System.out.println(accumulator);
						accumulator = 0;
						for (final int _ : new int[0]) {
						}
						System.out.println(accumulator);
					}
				}
				"""}, "4\n0");
	}



	public void testInstanceofUnnamedPatternMatching() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						Object r = null;
						if (r instanceof ColoredPoint(Point(int x, _), _)) {
							System.out.println("Hello, World!" + x);
						}
					}
				}
				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				"""}, "");
	}

	public void testReuseLocalUnnamedVariable() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						int _ = 1;
						int _ = 2;
						int _ = 3;
					}
				}
				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				"""}, "");
	}

	public void testReuseLocalUnnamedVariableUnicodeEscape() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						int _ = 1;
						int \\u005F = 2;
						int \\u005F = 3;
					}
				}
				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				"""}, "");
	}

	public void testUnnamedVariableInEnhancedFor() {
		runConformTest(new String[] { "A.java", """
				import java.util.List;
				public class A {
					public static void main(String[] args) {
						List<Order> orders = List.of(new Order(), new Order());
						int total = 0;
						for (Order _ : orders)
							total++;
						System.out.println(total);
					}
				}
				class Order {}
				"""}, "2");
	}

	public void testUnnamedVariableAsLambdaParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String[] args) {
						Function<Integer, Integer> myFunc = _ -> 1;
						System.out.println(myFunc.apply(0));
					}
				}
				"""}, "1");
	}

	public void testUnnamedVariableWithoutInitializer() {
		runNegativeTest(new String[] { "A.java", """
				import java.io.BufferedReader;
				import java.io.IOException;
				import java.util.List;
				public class A {
					void foo(int x, int y) {
						int _;
						int _;
						try (BufferedReader _ = null) {
						} catch (IOException _) {
						}
						for (String _ : List.of("hello")) {
						}
						int i = 12;
						for (int _; i < 14; i++) {
						}
					}
				}
				"""},
				"""
				----------
				1. ERROR in A.java (at line 6)
					int _;
					    ^
				Unnamed variables must have an initializer
				----------
				2. ERROR in A.java (at line 7)
					int _;
					    ^
				Unnamed variables must have an initializer
				----------
				3. ERROR in A.java (at line 14)
					for (int _; i < 14; i++) {
					         ^
				Unnamed variables must have an initializer
				----------
				""");
	}

	public void test001() {
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"+
				" public static int foo() {\n"+
				"   int _ = 1;\n"+
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
			},
			"0");
	}

	// Test that pattern variables are allowed for the nested patterns (not just the outermost record pattern)
	public void test002() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n"+
				" public static int foo() {\n"+
				"   int _ = bar();\n"+
				"   return 0;\n"+
				" }\n"+
				" public static int bar() {\n"+
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
			},
			"0");
	}

	public void test003() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				" public static int foo() {\n"+
				"   int _;\n"+ // Error should be thrown - uninitialized
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	int _;\n" +
				"	    ^\n" +
				"Unnamed variables must have an initializer\n" +
				"----------\n");
	}

	public void test004() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				" public static int foo() {\n"+
				"   int _ = 0;\n"+
				"   return _;\n"+  // Error should be thrown - uninitialized
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
				},
				"""
				----------
				1. ERROR in X.java (at line 5)
					return _;
					       ^
				Syntax error, insert "-> LambdaBody" to complete Expression
				----------
				""");
	}

	public void test005() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				"   public int _;\n"+ // Error should be thrown - Field not allowed
				" public static void main(String[] args) {\n"+
				"   System.out.println(0);\n"+
				" }\n"+
				"}"
				},
				"""
				----------
				1. ERROR in X.java (at line 3)
					public int _;
					           ^
				As of release 21, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters
				----------
				""");
	}
}
