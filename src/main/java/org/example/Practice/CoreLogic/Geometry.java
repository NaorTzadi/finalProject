package org.example.Practice.CoreLogic;
import org.example.Practice.MathProblem;
import org.example.Practice.Settings.CategorySettings;
import org.example.Practice.Settings.GeometrySettings;
import org.example.Practice.Settings.Options;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.Practice.CoreLogic.Geometry.Shape.*;

public class Geometry{

    private static final String VARIABLE_HOLDER_1 ="holder1";
    private static final String VARIABLE_HOLDER_2 ="holder2";
    private static final String VARIABLE_HOLDER_3 ="holder3";
    private static final String VARIABLE_HOLDER_4 ="holder4";
    private static final String VARIABLE_HOLDER_5 ="holder5";
    private static final String VARIABLE_HOLDER_6 ="holder6";
    private static final String H ="#";
    public enum ShapeType {
        SHAPE2D,
        SHAPE3D,
        RANDOM;
        public static ShapeType getRandom() {
            ShapeType[] types = Arrays.stream(ShapeType.values())
                    .filter(type -> type != RANDOM)
                    .toArray(ShapeType[]::new);
            return types[new Random().nextInt(types.length)];
        }
    }
    public enum Shape {
        SQUARE(ShapeType.SHAPE2D),
        RECTANGLE(ShapeType.SHAPE2D),
        CIRCLE(ShapeType.SHAPE2D),
        EQUILATERAL_TRIANGLE(ShapeType.SHAPE2D),
        TRIANGLE(ShapeType.SHAPE2D),
        PARALLELOGRAM(ShapeType.SHAPE2D),
        TRAPEZOID(ShapeType.SHAPE2D),
        RHOMBUS(ShapeType.SHAPE2D),
        HEXAGON(ShapeType.SHAPE2D),
        PENTAGON(ShapeType.SHAPE2D),
        ELLIPSE(ShapeType.SHAPE2D),

        CUBE(ShapeType.SHAPE3D),
        SPHERE(ShapeType.SHAPE3D),
        CYLINDER(ShapeType.SHAPE3D),
        CONE(ShapeType.SHAPE3D),
        PRISM(ShapeType.SHAPE3D),
        TETRAHEDRON(ShapeType.SHAPE3D),
        OCTAHEDRON(ShapeType.SHAPE3D),
        FRUSTUM(ShapeType.SHAPE3D),
        TORUS(ShapeType.SHAPE3D),
        PARALLELEPIPED(ShapeType.SHAPE3D),
        ELLIPSOID(ShapeType.SHAPE3D),
        DODECAHEDRON(ShapeType.SHAPE3D),

        RANDOM(null);

        private final ShapeType shapeType;
        Shape(ShapeType shapeType) {this.shapeType = shapeType;}
        public ShapeType getShapeType() {return shapeType;}

        public static Shape getRandom() {
            Shape[] shapes = Arrays.stream(Shape.values()).filter(shape -> shape != RANDOM).toArray(Shape[]::new);
            return shapes[new Random().nextInt(shapes.length)];
        }
        public static Shape getRandom(ShapeType type) {
            Shape[] shapes = Arrays.stream(Shape.values()).filter(shape -> shape != RANDOM && shape.getShapeType() == type).toArray(Shape[]::new);
            if (shapes.length == 0) throw new IllegalArgumentException("No shapes available for type: " + type);
            return shapes[new Random().nextInt(shapes.length)];
        }
        public static String toString(Shape shape) {
            return shape.toString().toLowerCase().replaceAll("_", " ").trim();
        }
    }

    public enum QuestionType {
        PERIMETER(ShapeType.SHAPE2D),
        AREA(ShapeType.SHAPE2D),

        SURFACE(ShapeType.SHAPE3D),
        VOLUME(ShapeType.SHAPE3D),

        RANDOM(null);

        private final ShapeType shapeType;

        QuestionType(ShapeType shapeType) {this.shapeType = shapeType;}
        public ShapeType getShapeType() {return shapeType;}

        public static QuestionType getRandom() {
            QuestionType[] questionTypes = Arrays.stream(QuestionType.values()).filter(questionType -> questionType != RANDOM).toArray(QuestionType[]::new);
            return questionTypes[new Random().nextInt(questionTypes.length)];
        }
        public static QuestionType getRandom(ShapeType shapeType) {
            QuestionType[] questionTypes = Arrays.stream(QuestionType.values()).filter(questionType -> questionType != RANDOM && questionType.getShapeType() == shapeType).toArray(QuestionType[]::new);
            if (questionTypes.length == 0) throw new IllegalArgumentException("No questions available for shape type: " + shapeType);
            return questionTypes[new Random().nextInt(questionTypes.length)];
        }
        public static String toString(QuestionType questionType) {
            return questionType.toString().toLowerCase().replaceAll(" ", "_").trim();
        }
    }

    private static String getQuestion(Shape shape, QuestionType questionType) {
        if (shape.getShapeType()==ShapeType.SHAPE2D && questionType.getShapeType()==ShapeType.SHAPE2D) {
            return switch (questionType) {
                case PERIMETER -> switch (shape) {
                    case SQUARE -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(SQUARE)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case RECTANGLE -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(RECTANGLE)+" with length " + VARIABLE_HOLDER_1 + " and width " + VARIABLE_HOLDER_2 + "?";
                    case CIRCLE -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(CIRCLE)+" with radius " + VARIABLE_HOLDER_1 + "?";
                    case EQUILATERAL_TRIANGLE -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of an "+ Shape.toString(Shape.EQUILATERAL_TRIANGLE)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case TRIANGLE -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(TRIANGLE)+" with side length "+VARIABLE_HOLDER_1+", " + VARIABLE_HOLDER_2 + " and "+VARIABLE_HOLDER_3+"?";
                    case PARALLELOGRAM -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(PARALLELOGRAM)+" with base " + VARIABLE_HOLDER_1 + ", side " + VARIABLE_HOLDER_2 + " and skw "+VARIABLE_HOLDER_3+"?";
                    case TRAPEZOID -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(TRAPEZOID)+" with bases " + VARIABLE_HOLDER_1 + ", " + VARIABLE_HOLDER_2 + " and sides " + VARIABLE_HOLDER_3 + ", " + VARIABLE_HOLDER_4 + "?";
                    case RHOMBUS -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(RHOMBUS)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case HEXAGON -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(HEXAGON)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case PENTAGON -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of a "+Shape.toString(PENTAGON)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case ELLIPSE -> "What is the "+QuestionType.toString(QuestionType.PERIMETER)+" of an "+Shape.toString(ELLIPSE)+" with semi-major axis " + VARIABLE_HOLDER_1 + " and semi-minor axis " + VARIABLE_HOLDER_2 + "?";
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                case AREA -> switch (shape) {
                    case SQUARE -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(SQUARE)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case RECTANGLE -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(RECTANGLE)+" with length " + VARIABLE_HOLDER_1 + " and width " + VARIABLE_HOLDER_2 + "?";
                    case CIRCLE -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(CIRCLE)+" with radius " + VARIABLE_HOLDER_1 + "?";
                    case EQUILATERAL_TRIANGLE -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of an "+Shape.toString(EQUILATERAL_TRIANGLE)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case TRIANGLE -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(TRIANGLE)+" with base " + VARIABLE_HOLDER_1 + " and height " + VARIABLE_HOLDER_2 + "?";
                    case PARALLELOGRAM -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(PARALLELOGRAM)+" with base " + VARIABLE_HOLDER_1 + ", height " + VARIABLE_HOLDER_2 + " and skw "+VARIABLE_HOLDER_3+"?";
                    case TRAPEZOID -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(TRAPEZOID)+" with bases " + VARIABLE_HOLDER_1 + ", " + VARIABLE_HOLDER_2 + " and height " + VARIABLE_HOLDER_3 + "?";
                    case RHOMBUS -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(RHOMBUS)+" with diagonals " + VARIABLE_HOLDER_1 + " and " + VARIABLE_HOLDER_2 + "?";
                    case HEXAGON -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(HEXAGON)+" with side length " + VARIABLE_HOLDER_1 + "?";
                    case PENTAGON -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of a "+Shape.toString(PENTAGON)+" with side length " + VARIABLE_HOLDER_1 + " and apothem " + VARIABLE_HOLDER_2 + "?";
                    case ELLIPSE -> "What is the "+QuestionType.toString(QuestionType.AREA)+" of an "+Shape.toString(ELLIPSE)+" with semi-major axis " + VARIABLE_HOLDER_1 + " and semi-minor axis " + VARIABLE_HOLDER_2 + "?";
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                default -> throw new IllegalStateException("Unexpected value: " + questionType);
            };
        } else if (shape.getShapeType()==ShapeType.SHAPE3D && questionType.getShapeType()==ShapeType.SHAPE3D) {
            return switch (questionType) {
                case SURFACE -> switch (shape) {
                    case CUBE -> "What is the surface area of a cube with side length " + VARIABLE_HOLDER_1 + "?";
                    case SPHERE -> "What is the surface area of a sphere with radius " + VARIABLE_HOLDER_1 + "?";
                    case TETRAHEDRON -> "What is the surface area of a tetrahedron with edge length " + VARIABLE_HOLDER_1 + "?";
                    case CONE -> "What is the surface area of a cone with radius " + VARIABLE_HOLDER_1 + " and height " + VARIABLE_HOLDER_2 + "?";
                    case CYLINDER -> "What is the surface area of a cylinder with radius " + VARIABLE_HOLDER_1 + " and height " + VARIABLE_HOLDER_2 + "?";
                    case TORUS -> "What is the surface area of a torus with major radius " + VARIABLE_HOLDER_1 + " and minor radius " + VARIABLE_HOLDER_2 + "?";
                    case PRISM -> "What is the surface area of a prism with length " + VARIABLE_HOLDER_1 + ", width " + VARIABLE_HOLDER_2 + ", and height " + VARIABLE_HOLDER_3 + "?";
                    case DODECAHEDRON -> "What is the surface area of a dodecahedron with edge length " + VARIABLE_HOLDER_1 + "?";
                    case OCTAHEDRON -> "What is the surface area of an octahedron with edge length " + VARIABLE_HOLDER_1 + "?";
                    case FRUSTUM -> "What is the surface area of a frustum with radius1 " + VARIABLE_HOLDER_1 + ", radius2 " + VARIABLE_HOLDER_2 + ", and height " + VARIABLE_HOLDER_3 + "?";
                    case PARALLELEPIPED -> "What is the surface area of a parallelepiped with length " + VARIABLE_HOLDER_1 + ", width " + VARIABLE_HOLDER_2 + ", height " + VARIABLE_HOLDER_3 + ", skewX " + VARIABLE_HOLDER_4 + ", skewY " + VARIABLE_HOLDER_5 + ", and skewZ " + VARIABLE_HOLDER_6 + "?";
                    case ELLIPSOID -> "What is the surface area of an ellipsoid with semi-axes " + VARIABLE_HOLDER_1 + ", " + VARIABLE_HOLDER_2 + ", and " + VARIABLE_HOLDER_3 + "?";
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                case VOLUME -> switch (shape) {
                    case CUBE -> "What is the volume of a cube with side length " + VARIABLE_HOLDER_1 + "?";
                    case SPHERE -> "What is the volume of a sphere with radius " + VARIABLE_HOLDER_1 + "?";
                    case TETRAHEDRON -> "What is the volume of a tetrahedron with edge length " + VARIABLE_HOLDER_1 + "?";
                    case CONE -> "What is the volume of a cone with radius " + VARIABLE_HOLDER_1 + " and height " + VARIABLE_HOLDER_2 + "?";
                    case CYLINDER -> "What is the volume of a cylinder with radius " + VARIABLE_HOLDER_1 + " and height " + VARIABLE_HOLDER_2 + "?";
                    case TORUS -> "What is the volume of a torus with major radius " + VARIABLE_HOLDER_1 + " and minor radius " + VARIABLE_HOLDER_2 + "?";
                    case PRISM -> "What is the volume of a prism with length " + VARIABLE_HOLDER_1 + ", width " + VARIABLE_HOLDER_2 + ", and height " + VARIABLE_HOLDER_3 + "?";
                    case DODECAHEDRON -> "What is the volume of a dodecahedron with edge length " + VARIABLE_HOLDER_1 + "?";
                    case OCTAHEDRON -> "What is the volume of an octahedron with edge length " + VARIABLE_HOLDER_1 + "?";
                    case FRUSTUM -> "What is the volume of a frustum with radius1 " + VARIABLE_HOLDER_1 + ", radius2 " + VARIABLE_HOLDER_2 + ", and height " + VARIABLE_HOLDER_3 + "?";
                    case PARALLELEPIPED -> "What is the volume of a parallelepiped with length " + VARIABLE_HOLDER_1 + ", width " + VARIABLE_HOLDER_2 + ", height " + VARIABLE_HOLDER_3 + ", skewX " + VARIABLE_HOLDER_4 + ", skewY " + VARIABLE_HOLDER_5 + ", and skewZ " + VARIABLE_HOLDER_6 + "?";
                    case ELLIPSOID -> "What is the volume of an ellipsoid with semi-axes " + VARIABLE_HOLDER_1 + ", " + VARIABLE_HOLDER_2 + ", and " + VARIABLE_HOLDER_3 + "?";
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                default -> throw new IllegalStateException("Unexpected value: " + questionType);
            };
        } else {
            return "Invalid shape or question type.";
        }
    }

    private static String getFormula(Shape shape, QuestionType questionType) {
        if (shape.getShapeType() == ShapeType.SHAPE2D && questionType.getShapeType() == ShapeType.SHAPE2D) {
            return switch (questionType) {
                case PERIMETER -> switch (shape) {
                    case SQUARE -> "4 * " + H + "side" + H;
                    case RECTANGLE -> "2 * (" + H + "length" + H + " + " + H + "width" + H + ")";
                    case CIRCLE -> "2 * π * " + H + "radius" + H;
                    case EQUILATERAL_TRIANGLE -> "3 * " + H + "side" + H;
                    case TRIANGLE -> H + "side₁" + H + " + " + H + "side₂" + H + " + " + H + "side₃" + H;
                    case PARALLELOGRAM -> "2 * (" + H + "base" + H + " + " + H + "side" + H + ")";
                    case TRAPEZOID -> H + "base₁" + H + " + " + H + "base₂" + H + " + " + H + "side₁" + H + " + " + H + "side₂" + H;
                    case RHOMBUS -> "4 * " + H + "side" + H;
                    case HEXAGON -> "6 * " + H + "side" + H;
                    case PENTAGON -> "5 * " + H + "side" + H;
                    case ELLIPSE -> "π * (3 * (" + H + "a" + H + " + " + H + "b" + H + ") - √((3 * " + H + "a" + H + " + " + H + "b" + H + ") * (" + H + "a" + H + " + 3 * " + H + "b" + H + ")))";
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                case AREA -> switch (shape) {
                    case SQUARE -> H + "side" + H + "²";
                    case RECTANGLE -> H + "length" + H + " * " + H + "width" + H;
                    case CIRCLE -> "π * " + H + "radius" + H + "²";
                    case EQUILATERAL_TRIANGLE -> "(√3 / 4) * " + H + "side" + H + "²";
                    case TRIANGLE -> "0.5 * " + H + "base" + H + " * " + H + "height" + H;
                    case PARALLELOGRAM -> H + "base" + H + " * " + H + "height" + H;
                    case TRAPEZOID -> "0.5 * (" + H + "base₁" + H + " + " + H + "base₂" + H + ") * " + H + "height" + H;
                    case RHOMBUS -> "0.5 * " + H + "diagonal₁" + H + " * " + H + "diagonal₂" + H;
                    case HEXAGON -> "(3√3 / 2) * " + H + "side" + H + "²";
                    case PENTAGON -> "0.5 * " + H + "perimeter" + H + " * " + H + "apothem" + H;
                    case ELLIPSE -> "π * " + H + "a" + H + " * " + H + "b" + H;
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                default -> "Unknown formula";
            };
        } else if (shape.getShapeType() == ShapeType.SHAPE3D && questionType.getShapeType() == ShapeType.SHAPE3D) {
            return switch (questionType) {
                case SURFACE -> switch (shape) {
                    case CUBE -> "6 * " + H + "side" + H + "²";
                    case SPHERE -> "4 * π * " + H + "radius" + H + "²";
                    case TETRAHEDRON -> "√3 * " + H + "edge" + H + "²";
                    case CONE -> "π * " + H + "radius" + H + " * (" + H + "radius" + H + " + √(" + H + "radius" + H + "² + " + H + "height" + H + "²))";
                    case CYLINDER -> "2 * π * " + H + "radius" + H + " * (" + H + "radius" + H + " + " + H + "height" + H + ")";
                    case TORUS -> "4 * π² * " + H + "majorRadius" + H + " * " + H + "minorRadius" + H;
                    case PRISM -> "2 * (" + H + "length" + H + " * " + H + "width" + H + " + " + H + "width" + H + " * " + H + "height" + H + " + " + H + "length" + H + " * " + H + "height" + H + ")";
                    case DODECAHEDRON -> "3 * √(25 + 10√5) * " + H + "edge" + H + "²";
                    case OCTAHEDRON -> "2 * √3 * " + H + "edge" + H + "²";
                    case FRUSTUM -> "π * (" + H + "radius₁" + H + " + " + H + "radius₂" + H + ") * √((" + H + "radius₁" + H + " - " + H + "radius₂" + H + ")² + " + H + "height" + H + "²) + π * (" + H + "radius₁" + H + "² + " + H + "radius₂" + H + "²)";
                    case PARALLELEPIPED -> "2 * (" + H + "length" + H + " * " + H + "width" + H + " + " + H + "width" + H + " * " + H + "height" + H + " + " + H + "length" + H + " * " + H + "height" + H + ")";
                    case ELLIPSOID -> "Approximation: 4 * π * ((" + H + "a" + H + " * " + H + "b" + H + ")^p + (" + H + "a" + H + " * " + H + "c" + H + ")^p + (" + H + "b" + H + " * " + H + "c" + H + ")^p)^(1/p), p ≈ 1.6075";
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                case VOLUME -> switch (shape) {
                    case CUBE -> H + "side" + H + "³";
                    case SPHERE -> "(4/3) * π * " + H + "radius" + H + "³";
                    case TETRAHEDRON -> H + "edge" + H + "³ / (6 * √2)";
                    case CONE -> "(1/3) * π * " + H + "radius" + H + "² * " + H + "height" + H;
                    case CYLINDER -> "π * " + H + "radius" + H + "² * " + H + "height" + H;
                    case TORUS -> "2 * π² * " + H + "majorRadius" + H + " * " + H + "minorRadius" + H + "²";
                    case PRISM -> H + "length" + H + " * " + H + "width" + H + " * " + H + "height" + H;
                    case DODECAHEDRON -> "(1/4) * (15 + 7√5) * " + H + "edge" + H + "³";
                    case OCTAHEDRON -> "(1/3) * √2 * " + H + "edge" + H + "³";
                    case FRUSTUM -> "(1/3) * π * " + H + "height" + H + " * (" + H + "radius₁" + H + "² + " + H + "radius₁" + H + " * " + H + "radius₂" + H + " + " + H + "radius₂" + H + "²)";
                    case PARALLELEPIPED -> H + "length" + H + " * " + H + "width" + H + " * " + H + "height" + H;
                    case ELLIPSOID -> "(4/3) * π * " + H + "a" + H + " * " + H + "b" + H + " * " + H + "c" + H;
                    default -> throw new IllegalStateException("Unexpected value: " + shape);
                };
                default -> throw new IllegalStateException("Unexpected value: " + questionType);
            };
        }
        throw new IllegalArgumentException("Invalid shape or question type");
    }
    private static String getFormulaWithValues(String formula, double[] values) {
        int index = 0;
        while (formula.contains(H) && index < values.length) {
            int start = formula.indexOf(H);
            int end = formula.indexOf(H, start + 1);
            if (end == -1) break;
            String placeholder = formula.substring(start + H.length(), end);
            formula = formula.replace(H + placeholder + H, String.valueOf(values[index]));
            index++;
        }
        if (formula.contains(H)) System.err.println("Not enough values to replace all placeholders.");

        return formula;
    }

    private static class Calculator {

        public static String calculate(Problem problem, Options.SolutionDetail solutionDetail) {
            Shape shape = problem.getSubject().getShape();
            QuestionType questionType = problem.getSubject().getQuestionType();
            double[] values = problem.getValues();

            double result;
            if (shape.getShapeType()==ShapeType.SHAPE2D && questionType.getShapeType()==ShapeType.SHAPE2D) {
                result= switch (questionType) {
                    case PERIMETER -> switch (shape) {
                        case SQUARE -> squarePerimeter(values[0]);
                        case RECTANGLE -> rectanglePerimeter(values[0], values[1]);
                        case CIRCLE -> circlePerimeter(values[0]);
                        case EQUILATERAL_TRIANGLE -> equilateralTrianglePerimeter(values[0]);
                        case TRIANGLE -> trianglePerimeter(values[0], values[1], values[2]);
                        case PARALLELOGRAM -> parallelogramPerimeter(values[0], values[1]);
                        case TRAPEZOID -> trapezoidPerimeter(values[0], values[1], values[2], values[3]);
                        case RHOMBUS -> rhombusPerimeter(values[0]);
                        case HEXAGON -> hexagonPerimeter(values[0]);
                        case PENTAGON -> pentagonPerimeter(values[0]);
                        case ELLIPSE -> ellipseCircumference(values[0], values[1]);
                        default -> throw new IllegalStateException("Unexpected value: " + shape);
                    };
                    case AREA -> switch (shape) {
                        case SQUARE -> squareArea(values[0]);
                        case RECTANGLE -> rectangleArea(values[0], values[1]);
                        case CIRCLE -> circleArea(values[0]);
                        case EQUILATERAL_TRIANGLE -> equilateralTriangleArea(values[0]);
                        case TRIANGLE -> triangleArea(values[0], values[1]);
                        case PARALLELOGRAM -> parallelogramArea(values[0], values[1]);
                        case TRAPEZOID -> trapezoidArea(values[0], values[1], values[2]);
                        case RHOMBUS -> rhombusArea(values[0], values[1]);
                        case HEXAGON -> hexagonArea(values[0]);
                        case PENTAGON -> pentagonArea(values[0], values[1]);
                        case ELLIPSE -> ellipseArea(values[0], values[1]);
                        default -> throw new IllegalStateException("Unexpected value: " + shape);
                    };
                    default -> throw new IllegalStateException("Unexpected value: " + questionType);
                };
            } else if (shape.getShapeType()==ShapeType.SHAPE3D  && questionType.getShapeType()==ShapeType.SHAPE3D) {
                result= switch (questionType) {
                    case SURFACE -> switch (shape) {
                        case CUBE -> cubeSurfaceArea(values[0]);
                        case SPHERE -> sphereSurfaceArea(values[0]);
                        case CYLINDER -> cylinderSurfaceArea(values[0], values[1]);
                        case CONE -> coneSurfaceArea(values[0], values[1]);
                        case PRISM -> prismSurfaceArea(values[0], values[1], values[2]);
                        case TETRAHEDRON -> tetrahedronSurfaceArea(values[0]);
                        case OCTAHEDRON -> octahedronSurfaceArea(values[0]);
                        case FRUSTUM -> frustumSurfaceArea(values[0], values[1], values[2]);
                        case TORUS -> torusSurfaceArea(values[0], values[1]);
                        case PARALLELEPIPED -> parallelepipedSurfaceArea(values[0], values[1], values[2]);
                        case ELLIPSOID -> ellipsoidSurfaceArea(values[0], values[1], values[2]);
                        case DODECAHEDRON -> dodecahedronSurfaceArea(values[0]);
                        default -> throw new IllegalStateException("Unexpected value: " + shape);
                    };
                    case VOLUME -> switch (shape) {
                        case CUBE -> cubeVolume(values[0]);
                        case SPHERE -> sphereVolume(values[0]);
                        case CYLINDER -> cylinderVolume(values[0], values[1]);
                        case CONE -> coneVolume(values[0], values[1]);
                        case PRISM -> prismVolume(values[0], values[1], values[2]);
                        case TETRAHEDRON -> tetrahedronVolume(values[0]);
                        case OCTAHEDRON -> octahedronVolume(values[0]);
                        case FRUSTUM -> frustumVolume(values[0], values[1], values[2]);
                        case TORUS -> torusVolume(values[0], values[1]);
                        case PARALLELEPIPED -> parallelepipedVolume(values[0], values[1], values[2]);
                        case ELLIPSOID -> ellipsoidVolume(values[0], values[1], values[2]);
                        case DODECAHEDRON -> dodecahedronVolume(values[0]);
                        default -> throw new IllegalStateException("Unexpected value: " + shape);
                    };
                    default -> throw new IllegalStateException("Unexpected value: " + questionType);
                };
            } else {
                throw new IllegalArgumentException("Invalid shape or question type");
            }
            String solution="";
            String step1="";
            String step2="";
            if (solutionDetail.equals(Options.SolutionDetail.AVERAGE)){
                step1="step1: "+ getFormula(shape,questionType).replaceAll(H,"")+"\n";
            }else if (solutionDetail.equals(Options.SolutionDetail.DETAILED)){
                String formula=getFormula(shape,questionType);
                step1="step1: "+ formula.replaceAll(H,"")+"\n";
                step2="step2: "+  getFormulaWithValues(formula,values)+"\n";
            }
            String step3="result: "+result;
            solution+=(step1+step2+step3).trim();

            return solution;
        }
        /**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ 2D_SHAPES_FORMULAS }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%</{> */
        private static double squarePerimeter(double side) {return 4 * side;}
        private static double squareArea(double side) {return Math.pow(side, 2);}

        private static double rectanglePerimeter(double length, double width) {return 2 * (length + width);}
        private static double rectangleArea(double length, double width) {return length * width;}

        private static double circlePerimeter(double radius) {return 2 * Math.PI * radius;}
        private static double circleArea(double radius) {return Math.PI * Math.pow(radius, 2);}

        private static double equilateralTrianglePerimeter(double side) { return 3 * side; }
        private static double equilateralTriangleArea(double side) { return (Math.sqrt(3) / 4) * Math.pow(side, 2); }

        private static double trianglePerimeter(double side1, double side2, double side3) {return side1 + side2 + side3;}
        private static double triangleArea(double base, double height) {return 0.5 * base * height;}

        private static double parallelogramPerimeter(double base, double side) {return 2 * (base + side);}
        private static double parallelogramArea(double base, double height) {return base * height;}

        private static double trapezoidPerimeter(double base1, double base2, double side1, double side2) {return base1 + base2 + side1 + side2;}
        private static double trapezoidArea(double base1, double base2, double height) {return 0.5 * (base1 + base2) * height;}

        private static double rhombusPerimeter(double side) {return 4 * side;}
        private static double rhombusArea(double diagonal1, double diagonal2) {return 0.5 * diagonal1 * diagonal2;}

        private static double hexagonPerimeter(double side) {return 6 * side;}
        private static double hexagonArea(double side) {return (3 * Math.sqrt(3) / 2) * Math.pow(side, 2);}

        private static double pentagonPerimeter(double side) {return 5 * side;}
        private static double pentagonArea(double side, double apothem) {return 0.5 * pentagonPerimeter(side) * apothem;}

        private static double ellipseCircumference(double a, double b) {return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)));}
        private static double ellipseArea(double a, double b) {return Math.PI * a * b;}

        /**%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ 3D_SHAPES_FORMULAS }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%</{> */
        private static double cubeSurfaceArea(double side) {return 6 * Math.pow(side, 2);}
        private static double cubeVolume(double side) {return Math.pow(side, 3);}

        private static double sphereSurfaceArea(double radius) {return 4 * Math.PI * Math.pow(radius, 2);}
        private static double sphereVolume(double radius) {return (4.0 / 3.0) * Math.PI * Math.pow(radius, 3);}

        private static double tetrahedronSurfaceArea(double edge) {return Math.sqrt(3) * Math.pow(edge, 2);}
        private static double tetrahedronVolume(double edge) {return Math.pow(edge, 3) / (6 * Math.sqrt(2));}

        private static double coneSurfaceArea(double radius, double height) {return Math.PI * radius * (radius + Math.sqrt(Math.pow(radius, 2) + Math.pow(height, 2)));}
        private static double coneVolume(double radius, double height) {return (1.0 / 3.0) * Math.PI * Math.pow(radius, 2) * height;}

        private static double cylinderSurfaceArea(double radius, double height) {return 2 * Math.PI * radius * (radius + height);}
        private static double cylinderVolume(double radius, double height) {return Math.PI * Math.pow(radius, 2) * height;}

        private static double torusSurfaceArea(double majorRadius, double minorRadius) {return 4 * Math.PI * Math.PI * majorRadius * minorRadius;}
        private static double torusVolume(double majorRadius, double minorRadius) {return 2 * Math.PI * Math.PI * majorRadius * Math.pow(minorRadius, 2);}

        private static double prismSurfaceArea(double length, double width, double height) {return 2 * (length * width + width * height + length * height);}
        private static double prismVolume(double length, double width, double height) {return length * width * height;}

        private static double dodecahedronSurfaceArea(double edge) {return 3 * Math.sqrt(25 + 10 * Math.sqrt(5)) * Math.pow(edge, 2);}
        private static double dodecahedronVolume(double edge) {return (1.0 / 4.0) * (15 + 7 * Math.sqrt(5)) * Math.pow(edge, 3);}

        private static double octahedronSurfaceArea(double edge) {return 2 * Math.sqrt(3) * Math.pow(edge, 2);}
        private static double octahedronVolume(double edge) {return (1.0 / 3.0) * Math.sqrt(2) * Math.pow(edge, 3);}

        private static double frustumSurfaceArea(double radius1, double radius2, double height) {return Math.PI * (radius1 + radius2) * (Math.sqrt(Math.pow(radius1 - radius2, 2) + Math.pow(height, 2))) + Math.PI * (Math.pow(radius1, 2) + Math.pow(radius2, 2));}
        private static double frustumVolume(double radius1, double radius2, double height) {return (1.0 / 3.0) * Math.PI * height * (Math.pow(radius1, 2) + radius1 * radius2 + Math.pow(radius2, 2));}

        private static double parallelepipedSurfaceArea(double length, double width, double height) {return 2 * (length * width + width * height + length * height);}
        private static double parallelepipedVolume(double length, double width, double height) {return length * width * height;}

        private static double ellipsoidSurfaceArea(double a, double b, double c) {
            double p = 1.6075;
            return 4 * Math.PI * Math.pow((Math.pow(a * b, p) + Math.pow(a * c, p) + Math.pow(b * c, p)) / 3.0, 1.0 / p);
        }
        private static double ellipsoidVolume(double a, double b, double c) {return (4.0 / 3.0) * Math.PI * a * b * c;}
    }
    public static MathProblem generateQuestion(CategorySettings categorySettings) {
        if (!(categorySettings instanceof GeometrySettings geometrySettings)) return null;
        Options.SolutionDetail solutionDetail = geometrySettings.getSolutionDetail();

        Set<ShapeType> validShapeTypes = geometrySettings.getShapeTypes().stream()
                .filter(type ->
                        geometrySettings.getShapes().stream().anyMatch(s -> s.getShapeType() == type) &&
                                geometrySettings.getQuestionTypes().stream().anyMatch(q -> q.getShapeType() == type)
                )
                .collect(Collectors.toSet());

        if (validShapeTypes.isEmpty()) {
            System.err.println("No valid shape types based on user input.");
            return null;
        }

        ShapeType shapeType = getRandomFromSet(validShapeTypes);

        Set<Shape> filteredShapes = geometrySettings.getShapes().stream().filter(shape -> shape.getShapeType() == shapeType).collect(Collectors.toSet());
        Set<QuestionType> filteredQuestionTypes = geometrySettings.getQuestionTypes().stream().filter(questionType -> questionType.getShapeType() == shapeType).collect(Collectors.toSet());

        if (filteredShapes.isEmpty() || filteredQuestionTypes.isEmpty()) {
            System.err.println("No valid shapes or question types available for the chosen shape type.");
            return null;
        }
        Shape shape = getRandomFromSet(filteredShapes);
        QuestionType questionType = getRandomFromSet(filteredQuestionTypes);
        Problem problem= generateQuestion(solutionDetail,shapeType, shape, questionType);

        if (problem==null)return null;
        return new MathProblem(-1,problem.getQuestion(),problem.getSolution());
    }

    public static Problem generateQuestion(Options.SolutionDetail solutionDetail, ShapeType shapeType, Shape shape, QuestionType questionType) {
        Problem problem = Problem.getEmptyProblem();
        int range=100;

        if (shapeType == ShapeType.RANDOM) shapeType = ShapeType.getRandom();
        if (shape==Shape.RANDOM)shape=Shape.getRandom(shapeType);
        if (questionType == QuestionType.RANDOM) questionType = QuestionType.getRandom(shapeType);

        if ((shapeType == ShapeType.SHAPE2D && !(shape.getShapeType() ==ShapeType.SHAPE2D)) ||
                (shapeType == ShapeType.SHAPE3D && !(shape.getShapeType()== ShapeType.SHAPE3D)) ||
                (shapeType == ShapeType.SHAPE2D && !(questionType.getShapeType()==ShapeType.SHAPE2D)) ||
                (shapeType == ShapeType.SHAPE3D && !(questionType.getShapeType()==ShapeType.SHAPE3D))) {
            System.err.println("Inconsistent shapeType, shape, and questionType.");
            return null;
        }
        problem.getSubject().setShapeType(shapeType);
        problem.getSubject().setShape(shape);
        problem.getSubject().setQuestionType(questionType);
        problem.setQuestion(getQuestion(shape,questionType));
        //problem.setFormula(getFormula(shape,questionType));

        if (shapeType == ShapeType.SHAPE2D) {
            range = 100;
        } else if (shapeType == ShapeType.SHAPE3D) {
            range = 200;
        }

        problem.setQuestion(replaceQuestionHolders(problem, range));
        problem.setSolution(Calculator.calculate(problem,solutionDetail));
        return problem;
    }
    private static <T> T getRandomFromSet(Set<T> set) {
        if (set == null || set.isEmpty()) return null;
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }

    public static MathProblem generateQuestion(int level, Options.SolutionDetail solutionDetail) {
        level = Math.max(1, level);  // Ensure level is at least 1
        // Get the 2D and 3D shapes
        Shape[] twoDShapes = Arrays.stream(Shape.values()).filter(shape -> shape.getShapeType() == ShapeType.SHAPE2D).toArray(Shape[]::new);
        Shape[] threeDShapes = Arrays.stream(Shape.values()).filter(shape -> shape.getShapeType() == ShapeType.SHAPE3D).toArray(Shape[]::new);

        int twoDShapesCount = twoDShapes.length;
        int threeDShapesCount = threeDShapes.length;

        // Calculate the total number of levels available
        int maxLevel = 2 * twoDShapesCount + 2 * threeDShapesCount;

        // If level exceeds max, cap it
        if (level > maxLevel) level = maxLevel;

        Problem problem = Problem.getEmptyProblem();

        // Handle 2D shapes
        if (level <= 2 * twoDShapesCount) {
            int shapeIndex = (level - 1) % twoDShapesCount;
            Shape shape = twoDShapes[shapeIndex];

            // Alternate between PERIMETER and AREA questions
            QuestionType questionType = (level <= twoDShapesCount) ? QuestionType.PERIMETER : QuestionType.AREA;
            int range = (questionType == QuestionType.PERIMETER) ? 50 : 100;

            problem.setLevel(level);
            problem.getSubject().setShapeType(ShapeType.SHAPE2D);
            problem.getSubject().setShape(shape);
            problem.getSubject().setQuestionType(questionType);
            problem.setQuestion(getQuestion(shape, questionType));
            problem.setQuestion(replaceQuestionHolders(problem, range));
            //problem.setFormula(getFormula(problem.getSubject().getShape(), problem.getSubject().getQuestionType()));
            problem.setSolution(Calculator.calculate(problem,solutionDetail));

            // Handle 3D shapes
        } else {
            int adjustedLevel = level - (2 * twoDShapesCount);  // Adjust level to start at 1 for 3D shapes
            int shapeIndex = (adjustedLevel - 1) % threeDShapesCount;
            Shape shape = threeDShapes[shapeIndex];

            // Alternate between SURFACE and VOLUME questions
            QuestionType questionType = (adjustedLevel <= threeDShapesCount) ? QuestionType.SURFACE : QuestionType.VOLUME;
            int range = (questionType == QuestionType.SURFACE) ? 100 : 200;

            problem.setLevel(level);
            problem.getSubject().setShapeType(ShapeType.SHAPE3D);
            problem.getSubject().setShape(shape);
            problem.getSubject().setQuestionType(questionType);
            problem.setQuestion(getQuestion(shape, questionType));
            problem.setQuestion(replaceQuestionHolders(problem, range));
            //problem.setFormula(getFormula(problem.getSubject().getShape(), problem.getSubject().getQuestionType()));
            problem.setSolution(Calculator.calculate(problem,solutionDetail));
        }

        return new MathProblem(problem.getLevel(), problem.getQuestion(), problem.getSolution());
    }

    private static String replaceQuestionHolders(Problem problem, int range) {
        Random random = new Random();
        String question = problem.getQuestion();
        double[] variables = new double[6]; // Supports up to 6 placeholders for 3D shapes like parallelepiped
        Shape shape=problem.getSubject().getShape();
        int paramIndex = 1; // Tracks the variable placeholders (holder1, holder2, etc.)
        while (question.contains("holder" + paramIndex)) {
            double randomValue;
            // Apply constraints based on the shape and question type
            if (shape.equals(Shape.PARALLELEPIPED)) {
                if (paramIndex >= 4) { // Skew values are in holder4, holder5, holder6
                    randomValue = random.nextDouble(); // Random value between 0.0 and 1.0
                } else {
                    randomValue = random.nextInt(range) + 1; // Length, width, and height use the standard range
                }
            }else if (shape.equals(Shape.TORUS)) {
                if (paramIndex == 1) {

                    randomValue = random.nextInt(range) + 1;
                } else if (paramIndex == 2) {
                    double majorRadius = variables[0];
                    int maxMinorRadius = (int) majorRadius;
                    randomValue = random.nextInt(maxMinorRadius) + 1;
                } else {
                    randomValue = random.nextInt(range) + 1;
                }
            }else if (shape.equals(Shape.ELLIPSE)) {
                if (paramIndex == 1) {
                    // Semi-major axis: full random range
                    randomValue = random.nextInt(range) + 1;
                } else {
                    // Semi-minor axis: create variability within a fraction of the semi-major axis
                    double semiMajorAxis = variables[0];

                    // Allow a wider random range between 50% and 100% of the semi-major axis
                    int minMinorAxis = (int) (semiMajorAxis * 0.5);
                    int maxMinorAxis = (int) semiMajorAxis;

                    if (maxMinorAxis <= minMinorAxis) {
                        maxMinorAxis = (int) semiMajorAxis;  // Fallback to full major axis if range is too small
                    }

                    randomValue = random.nextInt(maxMinorAxis - minMinorAxis + 1) + minMinorAxis;
                }
            }else if (shape.equals(Shape.PARALLELOGRAM)) {
                if (paramIndex == 1) {
                    randomValue = random.nextInt(range) + 1;
                } else if (paramIndex == 2) {
                    randomValue = random.nextInt(range) + 1;
                } else {
                    double base = variables[0];
                    int minSkew = -(int) base;
                    int maxSkew = (int) base;
                    randomValue = random.nextInt(maxSkew - minSkew + 1) + minSkew;
                }
            } else if (problem.getSubject().getShape().getShapeType()==ShapeType.SHAPE3D) {
                // For other 3D-specific constraints
                if (question.contains("radius1") && question.contains("radius2")) {
                    if (paramIndex == 1) {
                        randomValue = random.nextInt(range) + 1; // Radius1
                    } else {
                        randomValue = Math.max(variables[0], random.nextInt(range) + 1); // Ensure radius2 >= radius1
                    }
                } else if (question.contains("semi-axes")) {
                    if (paramIndex == 1) {
                        randomValue = random.nextInt(range) + 1;
                    } else {
                        randomValue = Math.min(variables[0], random.nextInt(range) + 1);
                    }
                } else {
                    randomValue = random.nextInt(range) + 1; // Default for other cases
                }
            }else if (shape.equals(Shape.TRIANGLE)) {

                if (paramIndex == 1) {
                    // First side is a random value within the full range
                    randomValue = random.nextInt(range) + 1;
                } else if (paramIndex == 2) {

                    // Second side can be any valid side within the range
                    int minSide2 = 1;                      // Minimum value
                    int maxSide2 = range;                  // Maximum value

                    // Generate the second side within the range
                    randomValue = random.nextInt(maxSide2 - minSide2 + 1) + minSide2;
                } else {
                    // Generate third side with constraints to form a valid triangle
                    double side1 = variables[0];
                    double side2 = variables[1];

                    int minSide3 = (int) Math.max(1, Math.abs(side1 - side2) + 1);
                    int maxSide3 = (int) Math.min(side1 + side2 - 1, range);

                    randomValue = random.nextInt(maxSide3 - minSide3 + 1) + minSide3;
                }
            } else {
                randomValue = random.nextInt(range) + 1; // Default value for other shapes and cases
            }
            question = question.replaceFirst("holder" + paramIndex, String.valueOf(randomValue));
            variables[paramIndex - 1] = randomValue;
            paramIndex++;
        }
        problem.setValues(Arrays.copyOf(variables, paramIndex - 1));
        return question;
    }

    public static class Subject{
        private ShapeType shapeType;
        private Shape shape;
        private QuestionType questionType;
        public Subject(ShapeType shapeType, Shape shape, QuestionType questionType){
            this.shapeType = shapeType;
            this.shape = shape;
            this.questionType = questionType;
        }
        public ShapeType getShapeType() {return shapeType;}
        public Shape getShape() {return shape;}
        public QuestionType getQuestionType() {return questionType;}
        public void setShape(Shape shape) {this.shape = shape;}
        public void setQuestionType(QuestionType questionType) {this.questionType = questionType;}
        public void setShapeType(ShapeType shapeType) {this.shapeType = shapeType;}

        @Override
        public String toString() {
            return "Subject{" +
                    "shapeType=" + shapeType +
                    ", shape=" + shape +
                    ", questionType=" + questionType +
                    '}';
        }
        public static Subject getEmptySubject(){
            return new Subject(null,null,null);
        }
    }

    public static class Problem {
        private int level;
        private Subject subject;
        private String formula;
        private String question;
        private String solution;
        private double[] values;

        public Problem(int level,Subject subject,String formula, String question,String solution, double[] values) {
            this.level = level;
            this.subject = subject;
            this.formula = formula;
            this.question = question;
            this.solution = solution;
            this.values = values;
        }
        public int getLevel() {return level;}
        public Subject getSubject(){return subject;}
        public String getFormula() {return formula;}
        public String getQuestion() {return question;}
        public String getSolution() {return solution;}
        public double[] getValues() {return values;}
        public void setSubject(Subject subject) {this.subject = subject;}
        public void setFormula(String formula) {this.formula = formula;}
        public void setQuestion(String question) {this.question = question;}
        public void setSolution(String solution) {this.solution = solution;}
        public void setValues(double[] values) {this.values = values;}
        public void setLevel(int level) {this.level = level;}
        public static Problem getEmptyProblem(){return new Problem(-1,Subject.getEmptySubject(),null,null, null,null);}

        @Override
        public String toString() {
            return "Problem{" +
                    "level=" + level +
                    ", subject=" + subject +
                    ", formula='" + formula + '\'' +
                    ", question='" + question + '\'' +
                    ", solution='" + solution + '\'' +
                    ", values=" + Arrays.toString(values) +
                    '}';
        }
    }

}