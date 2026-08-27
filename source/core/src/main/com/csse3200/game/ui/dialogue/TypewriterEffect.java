package com.csse3200.game.ui.dialogue;

public class TypewriterEffect {

    private final float charsPerSecond;
    private String fullText = "";
    private int revealed = 0;
    private float timer = 0f;

    public TypewriterEffect(float charsPerSecond) {
        this.charsPerSecond = charsPerSecond;
    }

    public void setText(String text) {
        fullText = text;
        revealed = 0;
        timer = 0f;
    }

    public void update(float delta) {
        timer += delta;
        revealed = Math.min(fullText.length(), (int) (timer * charsPerSecond));
    }

    public String getRevealedText() {
        return fullText.substring(0, revealed);
    }

    public boolean isComplete() {
        return revealed >= fullText.length();
    }

    public void skipToEnd() {
        revealed = fullText.length();
    }
}
