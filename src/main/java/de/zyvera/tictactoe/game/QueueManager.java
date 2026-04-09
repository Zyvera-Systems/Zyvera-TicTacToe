package de.zyvera.tictactoe.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Verwaltet die Warteschlange und Herausforderungen.
 */
public class QueueManager {

    private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();
    // challenger -> target
    private final Map<UUID, UUID> pendingChallenges = new ConcurrentHashMap<>();
    // target -> challenger (Reverse-Lookup)
    private final Map<UUID, UUID> incomingChallenges = new ConcurrentHashMap<>();
    // Challenge Timestamps
    private final Map<UUID, Long> challengeTimestamps = new ConcurrentHashMap<>();

    /**
     * Fügt einen Spieler der Warteschlange hinzu.
     * @return true wenn erfolgreich, false wenn bereits drin
     */
    public boolean joinQueue(UUID player) {
        if (queue.contains(player)) return false;
        queue.add(player);
        return true;
    }

    /**
     * Entfernt einen Spieler aus der Warteschlange.
     */
    public boolean leaveQueue(UUID player) {
        return queue.remove(player);
    }

    /**
     * Prüft ob ein Spieler in der Warteschlange ist.
     */
    public boolean isInQueue(UUID player) {
        return queue.contains(player);
    }

    /**
     * Versucht ein Match aus der Warteschlange zu finden.
     * @return UUID-Paar [playerX, playerO] oder null
     */
    public UUID[] findMatch() {
        if (queue.size() < 2) return null;

        UUID playerX = queue.poll();
        UUID playerO = queue.poll();

        if (playerX != null && playerO != null) {
            return new UUID[]{playerX, playerO};
        }

        // Falls nur einer übrig, zurück in die Queue
        if (playerX != null) queue.add(playerX);
        if (playerO != null) queue.add(playerO);
        return null;
    }

    /**
     * Sendet eine Herausforderung.
     * @return true wenn erfolgreich
     */
    public boolean sendChallenge(UUID challenger, UUID target) {
        // Nicht sich selbst herausfordern
        if (challenger.equals(target)) return false;
        // Bereits offene Challenge?
        if (pendingChallenges.containsKey(challenger)) return false;
        // Ziel hat bereits eine Challenge?
        if (incomingChallenges.containsKey(target)) return false;

        pendingChallenges.put(challenger, target);
        incomingChallenges.put(target, challenger);
        challengeTimestamps.put(challenger, System.currentTimeMillis());
        return true;
    }

    /**
     * Akzeptiert eine Herausforderung.
     * @return UUID des Herausforderers oder null
     */
    public UUID acceptChallenge(UUID target) {
        UUID challenger = incomingChallenges.remove(target);
        if (challenger != null) {
            pendingChallenges.remove(challenger);
            challengeTimestamps.remove(challenger);
            // Beide aus Queue entfernen falls drin
            queue.remove(target);
            queue.remove(challenger);
            return challenger;
        }
        return null;
    }

    /**
     * Lehnt eine Herausforderung ab.
     */
    public UUID denyChallenge(UUID target) {
        UUID challenger = incomingChallenges.remove(target);
        if (challenger != null) {
            pendingChallenges.remove(challenger);
            challengeTimestamps.remove(challenger);
            return challenger;
        }
        return null;
    }

    /**
     * Prüft ob ein Spieler eine offene Herausforderung hat.
     */
    public boolean hasPendingChallenge(UUID player) {
        return pendingChallenges.containsKey(player);
    }

    /**
     * Prüft ob ein Spieler eine eingehende Herausforderung hat.
     */
    public boolean hasIncomingChallenge(UUID player) {
        return incomingChallenges.containsKey(player);
    }

    /**
     * Gibt den Herausforderer einer eingehenden Challenge zurück.
     */
    public UUID getChallenger(UUID target) {
        return incomingChallenges.get(target);
    }

    /**
     * Bereinigt abgelaufene Herausforderungen.
     * @param timeoutMillis Timeout in Millisekunden
     * @return Liste der abgelaufenen Challengers
     */
    public List<UUID> cleanupExpiredChallenges(long timeoutMillis) {
        List<UUID> expired = new ArrayList<>();
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, Long>> it = challengeTimestamps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (now - entry.getValue() > timeoutMillis) {
                UUID challenger = entry.getKey();
                UUID target = pendingChallenges.remove(challenger);
                if (target != null) {
                    incomingChallenges.remove(target);
                }
                it.remove();
                expired.add(challenger);
            }
        }

        return expired;
    }

    /**
     * Entfernt alle Daten eines Spielers (bei Disconnect etc.)
     */
    public void removePlayer(UUID player) {
        queue.remove(player);

        // Gesendete Challenge entfernen
        UUID target = pendingChallenges.remove(player);
        if (target != null) {
            incomingChallenges.remove(target);
            challengeTimestamps.remove(player);
        }

        // Empfangene Challenge entfernen
        UUID challenger = incomingChallenges.remove(player);
        if (challenger != null) {
            pendingChallenges.remove(challenger);
            challengeTimestamps.remove(challenger);
        }
    }

    public int getQueueSize() {
        return queue.size();
    }
}
