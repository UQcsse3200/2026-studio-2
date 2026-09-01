package com.csse3200.game.components.minigames.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Core Blackjack game logic used by the minigame. This class contains no UI code and is designed
 * to be plugged into a screen later.
 */
public class Blackjack {
  public enum Suit {
    HEARTS,
    DIAMONDS,
    CLUBS,
    SPADES
  }

  public enum Rank {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(10),
    QUEEN(10),
    KING(10),
    ACE(11);

    private final int value;

    Rank(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public static class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
      this.suit = suit;
      this.rank = rank;
    }

    public Suit getSuit() {
      return suit;
    }

    public Rank getRank() {
      return rank;
    }

    @Override
    public String toString() {
      return rank.name() + " of " + suit.name();
    }
  }

  private final List<Card> deck = new ArrayList<>();
  private final List<Card> playerHand = new ArrayList<>();
  private final List<Card> dealerHand = new ArrayList<>();
  private final Random random;

  private int balance;
  private int bet;
  private boolean roundInProgress;
  private boolean roundOver;
  private String resultMessage;

  public Blackjack(int startingBalance) {
    this(startingBalance, new Random());
  }

  public Blackjack(int startingBalance, Random random) {
    if (startingBalance < 0) {
      throw new IllegalArgumentException("Starting balance cannot be negative");
    }
    this.random = random;
    this.balance = startingBalance;
    this.bet = 0;
    this.resultMessage = "";
    resetDeck();
  }

  /** Returns the player's current chip balance. */
  public int getBalance() {
    return balance;
  }

  /** Returns the current bet for the active round. */
  public int getBet() {
    return bet;
  }

  /** Returns a copy of the player's hand. */
  public List<Card> getPlayerHand() {
    return List.copyOf(playerHand);
  }

  /** Returns a copy of the dealer's hand. */
  public List<Card> getDealerHand() {
    return List.copyOf(dealerHand);
  }

  /** Returns true once the round has started and is active. */
  public boolean isRoundInProgress() {
    return roundInProgress;
  }

  /** Returns true once a round has ended and a result is available. */
  public boolean isRoundOver() {
    return roundOver;
  }

  /** Returns the latest round result message. */
  public String getResultMessage() {
    return resultMessage;
  }

  /** Places a bet before the round begins. */
  public void placeBet(int amount) {
    if (roundInProgress) {
      throw new IllegalStateException("Cannot change the bet while a round is in progress");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("Bet must be greater than zero");
    }
    if (amount > balance) {
      throw new IllegalArgumentException("Bet cannot exceed the current balance");
    }
    this.bet = amount;
  }

  /** Starts a fresh round using the current bet. */
  public void startNewRound() {
    if (bet <= 0) {
      throw new IllegalStateException("A valid bet must be placed before starting a round");
    }
    if (roundInProgress) {
      throw new IllegalStateException("A round is already in progress");
    }

    playerHand.clear();
    dealerHand.clear();
    roundOver = false;
    resultMessage = "";

    resetDeck();
    Collections.shuffle(deck, random);

    dealCard(playerHand);
    dealCard(dealerHand);
    dealCard(playerHand);
    dealCard(dealerHand);

    roundInProgress = true;

    if (calculateHandValue(playerHand) == 21 || calculateHandValue(dealerHand) == 21) {
      resolveBlackjackNatural();
    }
  }

  /** Draws one extra card for the player. */
  public void hit() {
    if (!roundInProgress) {
      throw new IllegalStateException("No round is in progress");
    }
    if (roundOver) {
      throw new IllegalStateException("The round has already ended");
    }

    dealCard(playerHand);

    if (calculateHandValue(playerHand) > 21) {
      balance -= bet;
      resultMessage = "Bust! Dealer wins.";
      roundInProgress = false;
      roundOver = true;
    }
  }

  /** Stops the player from drawing and resolves the dealer's turn. */
  public void stand() {
    if (!roundInProgress) {
      throw new IllegalStateException("No round is in progress");
    }
    if (roundOver) {
      throw new IllegalStateException("The round has already ended");
    }

    while (calculateHandValue(dealerHand) < 17) {
      dealCard(dealerHand);
    }

    resolveDealerOutcome();
    roundInProgress = false;
    roundOver = true;
  }

  /** Returns the current total value of a hand, treating aces as 11 when possible. */
  public int calculateHandValue(List<Card> cards) {
    int total = 0;
    int aces = 0;

    for (Card card : cards) {
      total += card.getRank().getValue();
      if (card.getRank() == Rank.ACE) {
        aces++;
      }
    }

    while (total > 21 && aces > 0) {
      total -= 10;
      aces--;
    }

    return total;
  }

  /** Returns the player's total value. */
  public int getPlayerTotal() {
    return calculateHandValue(playerHand);
  }

  /** Returns the dealer's total value. */
  public int getDealerTotal() {
    return calculateHandValue(dealerHand);
  }

  private void resetDeck() {
    deck.clear();
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        deck.add(new Card(suit, rank));
      }
    }
  }

  private void dealCard(List<Card> hand) {
    if (deck.isEmpty()) {
      resetDeck();
      Collections.shuffle(deck, random);
    }
    hand.add(deck.remove(deck.size() - 1));
  }

  private void resolveBlackjackNatural() {
    int playerTotal = calculateHandValue(playerHand);
    int dealerTotal = calculateHandValue(dealerHand);

    if (playerTotal == 21 && dealerTotal == 21) {
      resultMessage = "Push: both players have blackjack.";
    } else if (playerTotal == 21) {
      balance += bet;
      resultMessage = "Blackjack! You win.";
    } else {
      balance -= bet;
      resultMessage = "Dealer has blackjack. You lose.";
    }

    roundInProgress = false;
    roundOver = true;
  }

  private void resolveDealerOutcome() {
    int playerTotal = calculateHandValue(playerHand);
    int dealerTotal = calculateHandValue(dealerHand);

    if (dealerTotal > 21) {
      balance += bet;
      resultMessage = "Dealer busts. You win.";
    } else if (playerTotal > dealerTotal) {
      balance += bet;
      resultMessage = "You win.";
    } else if (playerTotal < dealerTotal) {
      balance -= bet;
      resultMessage = "Dealer wins.";
    } else {
      resultMessage = "Push.";
    }
  }
}
