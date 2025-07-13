package fr.bloup.blurpapi.utils;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Stack;

public class BlurpMath {
    public static double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        String[] tokens = expression.split("(?<=[-+*/()])|(?=[-+*/()])");

        Stack<Double> numbers = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                numbers.push(Double.parseDouble(token));
            } else if ("+-*/".contains(token)) {
                while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }

        return numbers.pop();
    }

    public static boolean hasPrecedence(String op1, String op2) {
        return (op2.equals("*") || op2.equals("/")) && (op1.equals("+") || op1.equals("-"));
    }

    public static double applyOperation(String operator, double b, double a) {
        switch (operator) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            default: return 0;
        }
    }

    public static Vector getForwardVector(Player player) {
        float yaw = player.getLocation().getYaw();
        double rad = Math.toRadians(yaw);
        double x = -Math.sin(rad);
        double z = Math.cos(rad);
        return new Vector(x, 0, z);
    }
}
