package org.example.Practice.CoreLogic;
import org.example.Practice.MathProblem;
import org.example.Practice.Settings.ArithmeticSettings;
import org.example.Practice.Settings.CategorySettings;
import org.example.Practice.Settings.Options;

import java.util.*;

public class Arithmetic{

    public enum NumberType{
        NEGATIVE,
        DECIMAL,
        WHOLE
    }
    public enum QuestionType {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        ALL,
        RANDOM;
        public static QuestionType getRandom() {
            QuestionType[] types = Arrays.stream(QuestionType.values())
                    .filter(type -> type != ALL && type != RANDOM)
                    .toArray(QuestionType[]::new);
            return types[new Random().nextInt(types.length)];
        }
    }

    private static final int MAX_LEVEL=56;
    public static MathProblem generateQuestion(int level, Options.SolutionDetail solutionDetail) {
        if (level < 1) level = 1;
        if (level > MAX_LEVEL) level = 56;

        Random random = new Random();

        int mappedLevel = (level > 28) ? level - 28 : level;
        String type = (mappedLevel <= 12) ? "int" : "double";
        int minRange = 0;
        int maxRange;
        int variablesCount;

        if (mappedLevel <= 12) {
            maxRange = switch (mappedLevel) {
                case 1, 2 -> 10;
                case 3, 4 -> 100;
                case 5, 6 -> 10;
                case 7, 8 -> 100;
                case 9, 10 -> 1000;
                default -> 1000;
            };
            variablesCount = (mappedLevel % 2 == 0) ? 3 : 2;
        } else {
            maxRange = switch (mappedLevel) {
                case 13, 14 -> 10;
                case 15, 16 -> 100;
                case 17, 18 -> 10;
                case 19, 20 -> 100;
                case 21, 22 -> 1000;
                case 25, 26 -> 100;
                case 27, 28 -> 1000;
                default -> 1000;
            };
            variablesCount = (mappedLevel >= 25 && mappedLevel <= 28) ? 4 : ((mappedLevel % 2 == 0) ? 3 : 2);
        }

        ArrayList<QuestionType> unlockedCategories = new ArrayList<>();
        unlockedCategories.add(QuestionType.ADDITION);
        unlockedCategories.add(QuestionType.SUBTRACTION);
        if (mappedLevel >= 3) unlockedCategories.add(QuestionType.MULTIPLICATION);
        if (mappedLevel >= 5) unlockedCategories.add(QuestionType.DIVISION);

        StringBuilder question = new StringBuilder();
        double previousVariable = random.nextInt(maxRange - minRange + 1) + minRange;
        boolean useNegatives = level > 28;
        if (useNegatives && random.nextBoolean()) {
            previousVariable *= -1;
        }
        if (previousVariable == (int) previousVariable) {
            question.append((int) previousVariable);
        } else {
            question.append(previousVariable);
        }

        for (int i = 1; i < variablesCount; i++) {
            QuestionType operation;
            do {
                operation = unlockedCategories.get(random.nextInt(unlockedCategories.size()));
            } while (operation == QuestionType.DIVISION && previousVariable == 0);

            switch (operation) {
                case ADDITION -> question.append(" + ");
                case SUBTRACTION -> question.append(" - ");
                case MULTIPLICATION -> question.append(" * ");
                case DIVISION -> question.append(" / ");
            }

            double variable;
            do {
                variable = (type.equals("int"))
                        ? random.nextInt(maxRange - minRange + 1) + minRange
                        : Math.round((minRange + (maxRange - minRange) * random.nextDouble()) * 1000.0) / 1000.0;
                if (useNegatives && random.nextBoolean()) {
                    variable *= -1;
                }
            } while (operation == QuestionType.DIVISION && variable == 0);

            if (variable == (int) variable) {
                question.append((int) variable);
            } else {
                question.append(variable);
            }
            previousVariable = variable;
        }

        return new MathProblem(level, question.toString(), getSolution(question.toString(), solutionDetail));
    }

    public static MathProblem generateQuestion(CategorySettings categorySettings, Options.SolutionDetail solutionDetail) {
        if (!(categorySettings instanceof ArithmeticSettings arithmeticSettings)) return null;
        Set<NumberType> numberTypes = arithmeticSettings.getNumberTypes();
        Set<Arithmetic.QuestionType> questionTypes = arithmeticSettings.getQuestionTypes();

        if (numberTypes.isEmpty() || questionTypes.isEmpty()) {
            System.err.println("No valid number types or question types available.");
            return null;
        }

        Arithmetic.NumberType chosenNumberType = getRandomFromSet(numberTypes);
        Arithmetic.QuestionType chosenQuestionType = getRandomFromSet(questionTypes);
        return generateQuestion(chosenNumberType, chosenQuestionType, solutionDetail);
    }

    private static <T> T getRandomFromSet(Set<T> set) {
        if (set == null || set.isEmpty()) return null;
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }

    public static MathProblem generateQuestion(NumberType numberType, QuestionType questionType, Options.SolutionDetail solutionDetail) {
        Random random = new Random();
        int variablesCount = 3;
        int maxRange = 100;
        boolean isDecimal = numberType == NumberType.DECIMAL;
        boolean isNegative = numberType == NumberType.NEGATIVE;

        StringBuilder question = new StringBuilder();

        for (int i = 0; i < variablesCount; i++) {
            double variable;
            do {
                variable = isDecimal
                        ? Math.round((random.nextDouble() * maxRange) * 1000.0) / 1000.0
                        : random.nextInt(maxRange + 1);
            } while (i > 0 && questionType == QuestionType.DIVISION && variable == 0);

            if (isNegative && random.nextBoolean()) {
                variable *= -1;
            }

            question.append(isDecimal ? variable : (int) variable);
            if (i < variablesCount - 1) {
                switch (questionType) {
                    case ADDITION -> question.append(" + ");
                    case SUBTRACTION -> question.append(" - ");
                    case MULTIPLICATION -> question.append(" * ");
                    case DIVISION -> question.append(" / ");
                }
            }
        }

        return new MathProblem(-1, question.toString(), getSolution(question.toString(), solutionDetail));
    }

    public static String getSolution(String question, Options.SolutionDetail solutionDetail) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < question.length(); i++) {
            char c = question.charAt(i);
            if (c == ' ' || c == '\t') continue;
            if ("+-*/".indexOf(c) != -1) {
                if (c == '-' && (currentToken.length() == 0 && (tokens.isEmpty() || "+-*/".contains(tokens.get(tokens.size() - 1))))) {
                    currentToken.append(c);
                } else {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    }
                    tokens.add(String.valueOf(c));
                }
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) tokens.add(currentToken.toString());

        while (tokens.size() > 1) evaluate(tokens);

        double result = Double.parseDouble(tokens.get(0));
        return "result: " + (result == (int) result ? (int) result : result);
    }

    private static void evaluate(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if ((tokens.get(i).equals("*") || tokens.get(i).equals("/")) && i > 0 && i + 1 < tokens.size()) {
                double left = Double.parseDouble(tokens.get(i - 1));
                double right = Double.parseDouble(tokens.get(i + 1));
                double result = tokens.get(i).equals("*") ? left * right : left / right;
                tokens.set(i - 1, String.valueOf(result));
                tokens.remove(i);
                tokens.remove(i);
                return;
            }
        }
        for (int i = 0; i < tokens.size(); i++) {
            if ((tokens.get(i).equals("+") || tokens.get(i).equals("-")) && i > 0 && i + 1 < tokens.size()) {
                double left = Double.parseDouble(tokens.get(i - 1));
                double right = Double.parseDouble(tokens.get(i + 1));
                double result = tokens.get(i).equals("+") ? left + right : left - right;
                tokens.set(i - 1, String.valueOf(result));
                tokens.remove(i);
                tokens.remove(i);
                return;
            }
        }
    }
}
