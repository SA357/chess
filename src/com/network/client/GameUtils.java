package com.network.client;

public class GameUtils {
    private static String enemy;
    private static boolean isPlayerTurn;

    public static String getEnemy() {
        return enemy;
    }

    public static void setEnemy(String enemy) {
        GameUtils.enemy = enemy;
    }

    public static boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public static void setIsPlayerTurn(boolean isPlayerTurn) {
        GameUtils.isPlayerTurn = isPlayerTurn;
    }
}
