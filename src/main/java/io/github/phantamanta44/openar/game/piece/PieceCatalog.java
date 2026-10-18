package io.github.phantamanta44.openar.game.piece;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Canonical library of game pieces.
 *
 * File formats resolve their own identifiers (for example PCE one-character
 * tokens) into this catalog. Runtime systems can then use PieceId without
 * depending on how a particular level format encoded the piece.
 */
public final class PieceCatalog {

    private final Map<PieceId, PieceDefinition> byId = new ConcurrentHashMap<>();
    private final Map<String, PieceDefinition> byLegacyToken = new ConcurrentHashMap<>();

    public void register(PieceDefinition definition) {
        PieceDefinition oldId = byId.putIfAbsent(definition.getId(), definition);
        if (oldId != null) {
            throw new IllegalArgumentException("Duplicate piece id: " + definition.getId());
        }

        String token = definition.getLegacyToken();
        if (token != null && !token.isEmpty()) {
            PieceDefinition oldToken = byLegacyToken.putIfAbsent(token, definition);
            if (oldToken != null) {
                byId.remove(definition.getId(), definition);
                throw new IllegalArgumentException("Duplicate legacy piece token: " + token);
            }
        }
    }

    public PieceDefinition get(PieceId id) {
        return byId.get(id);
    }

    public PieceDefinition getByLegacyToken(String token) {
        return byLegacyToken.get(token);
    }

    public Collection<PieceDefinition> getDefinitions() {
        return Collections.unmodifiableCollection(byId.values());
    }

}
