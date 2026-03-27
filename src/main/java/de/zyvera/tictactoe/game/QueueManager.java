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

    public boolean joinQueue(UUID player) {
        if (queue.contains(player)) return false;
        queue.add(player);
        return true;
    }

    public boolean leaveQueue(UUID player) {
        return queue.remove(player);
    }
    public boolean isInQueue(UUID player) {
        return queue.contains(player);
    }
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

    public UUID denyChallenge(UUID target) {
        UUID challenger = incomingChallenges.remove(target);
        if (challenger != null) {
            pendingChallenges.remove(challenger);
            challengeTimestamps.remove(challenger);
            return challenger;
        }
        return null;
    }

    public boolean hasPendingChallenge(UUID player) {
        return pendingChallenges.containsKey(player);
    }
    public boolean hasIncomingChallenge(UUID player) {
        return incomingChallenges.containsKey(player);
    }
    public UUID getChallenger(UUID target) {
        return incomingChallenges.get(target);
    }

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
