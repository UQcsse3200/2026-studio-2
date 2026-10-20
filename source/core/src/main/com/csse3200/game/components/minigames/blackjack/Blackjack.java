package com.csse3200.game.components.minigames.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Core Blackjack game logic used by the minigame. This class contains no UI code and is designed to
 * be plugged into a screen later.
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

  /** Result of a single resolved round. */
  public enum RoundOutcome {
    PLAYER_WIN,
    DEALER_WIN,
    PUSH
  }

  /** State of the best-of-five match. */
  public enum MatchResult {
    IN_PROGRESS,
    PLAYER_WON,
    DEALER_WON
  }

  /** Round wins needed to take the best-of-five match. */
  public static final int WINS_NEEDED = 3;

  /** Fixed gold staked on every round. */
  public static final int BET = 1;

  /** Gold the player is given when a lost match is restarted. */
  public static final int REFILL_GOLD = 10;

  private final List<Card> deck = new ArrayList<>();
  private final List<Card> playerHand = new ArrayList<>();
  private final List<Card> dealerHand = new ArrayList<>();
  private final Random random;

  private int balance;
  private int bet;
  private boolean roundInProgress;
  private boolean roundOver;
  private RoundOutcome lastOutcome;
  private String resultMessage;
  private int playerWins;
  private int dealerWins;

  /**
   * Creates a game with a randomly shuffled deck.
   *
   * @param startingBalance the player's gold at the start of the match
   */
  public Blackjack(int startingBalance) {
    this(startingBalance, new Random());
  }

  /**
   * Creates a game that shuffles with the given random source, so tests can deal known hands.
   *
   * @param startingBalance the player's gold at the start of the match
   * @param random random source used to shuffle the deck
   * @throws IllegalArgumentException if the starting balance is negative
   */
  public Blackjack(int startingBalance, Random random) {
    if (startingBalance < 0) {
      throw new IllegalArgumentException("Starting balance cannot be negative");
    }
    this.random = random;
    this.balance = startingBalance;
    this.bet = BET;
    this.resultMessage = "";
    resetDeck();
  }

  /** Returns the player's current chip balance. */
  public int getBalance() {
    return balance;
  }

  /** Synchronises the chip balance with the player's current inventory gold. */
  public void setBalance(int balance) {
    if (balance < 0) {
      throw new IllegalArgumentException("Balance cannot be negative");
    }
    this.balance = balance;
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

  /** Returns true when the most recently resolved round was won by the player. */
  public boolean isPlayerWinner() {
    return roundOver && lastOutcome == RoundOutcome.PLAYER_WIN;
  }

  /** Returns the outcome of the most recently resolved round, or null if none has ended. */
  public RoundOutcome getLastOutcome() {
    return roundOver ? lastOutcome : null;
  }

  /** Returns the number of rounds the player has won in the current match. */
  public int getPlayerWins() {
    return playerWins;
  }

  /** Returns the number of rounds the dealer has won in the current match. */
  public int getDealerWins() {
    return dealerWins;
  }

  /**
   * Returns the state of the current match. The player wins on reaching {@link #WINS_NEEDED}. The
   * player loses when the dealer reaches it, or when a round ends and the player can no longer
   * afford the next {@link #BET}.
   */
  public MatchResult getMatchResult() {
    if (playerWins >= WINS_NEEDED) {
      return MatchResult.PLAYER_WON;
    }
    if (dealerWins >= WINS_NEEDED || (roundOver && balance < BET)) {
      return MatchResult.DEALER_WON;
    }
    return MatchResult.IN_PROGRESS;
  }

  /** Returns true once the current match has a winner. */
  public boolean isMatchOver() {
    return getMatchResult() != MatchResult.IN_PROGRESS;
  }

  /** Starts a fresh match: clears the score and hands, and refills the balance. */
  public void resetMatch() {
    clearMatch();
    balance = REFILL_GOLD;
  }

  /** Starts a fresh match like {@link #resetMatch()}, but keeps the gold the player has left. */
  public void restartMatchKeepingGold() {
    clearMatch();
  }

  private void clearMatch() {
    playerWins = 0;
    dealerWins = 0;
    bet = BET;
    playerHand.clear();
    dealerHand.clear();
    roundInProgress = false;
    roundOver = false;
    lastOutcome = null;
    resultMessage = "";
  }

  /** Starts a fresh round using the current bet. */
  public void startNewRound() {
    if (bet <= 0) {
      throw new IllegalStateException("A valid bet must be placed before starting a round");
    }
    if (roundInProgress) {
      throw new IllegalStateException("A round is already in progress");
    }
    if (isMatchOver()) {
      throw new IllegalStateException("The match is over");
    }
    if (bet > balance) {
      throw new IllegalStateException("Not enough balance for the bet");
    }

    playerHand.clear();
    dealerHand.clear();
    roundOver = false;
    lastOutcome = null;
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
      finishRound(RoundOutcome.DEALER_WIN, "Bust! Serci wins.");
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
      finishRound(RoundOutcome.PUSH, "Push: both players have blackjack.");
    } else if (playerTotal == 21) {
      finishRound(RoundOutcome.PLAYER_WIN, "Blackjack! You win.");
    } else {
      finishRound(RoundOutcome.DEALER_WIN, "Serci has blackjack. You lose.");
    }
  }

  private void resolveDealerOutcome() {
    int playerTotal = calculateHandValue(playerHand);
    int dealerTotal = calculateHandValue(dealerHand);

    if (dealerTotal > 21) {
      finishRound(RoundOutcome.PLAYER_WIN, "Serci busts. You win.");
    } else if (playerTotal > dealerTotal) {
      finishRound(RoundOutcome.PLAYER_WIN, "You win.");
    } else if (playerTotal < dealerTotal) {
      finishRound(RoundOutcome.DEALER_WIN, "Serci wins.");
    } else {
      finishRound(RoundOutcome.PUSH, "Push.");
    }
  }

  /** Ends the round: settles the balance, updates the match score and records the result. */
  private void finishRound(RoundOutcome outcome, String message) {
    switch (outcome) {
      case PLAYER_WIN -> {
        balance += bet;
        playerWins++;
      }
      case DEALER_WIN -> {
        balance -= bet;
        dealerWins++;
      }
      case PUSH -> {
        // A push returns the stake and does not count towards the match.
      }
    }
    lastOutcome = outcome;
    resultMessage = message;
    roundInProgress = false;
    roundOver = true;
  }
}
