import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 *
 * Based on a program by Stuart Reges, implemented by Abraham Martinez.
 */
public class HangmanManager {
  private int lenLimit;
  private int guesses;
  private Set<String> activeList;
  private Iterator<String> itr;
  private HangmanDifficulty diff;
  private ArrayList<Character> letters;
  private StringBuilder currentPattern;
  private Set<String> original;

  /**
   * Create a new HangmanManager from the provided set of words and phrases.
   * pre: words != null, words.size() > 0
   * 
   * @param words   A set with the words for this instance of Hangman.
   * @param debugOn true if we should print out debugging to System.out.
   */
  public HangmanManager(Set<String> words, boolean debugOn) {
    if (words == null || words.size() == 0) {
      throw new IllegalArgumentException("Sorry, the dictionary appears to be empty."
          + "Please use a valid dictionary.");
    }
    this.original = words; // Copy for hard reset

  }

  /**
   * Create a new HangmanManager from the provided set of words and phrases.
   * Debugging is off.
   * pre: words != null, words.size() > 0
   * 
   * @param words A set with the words for this instance of Hangman.
   */
  public HangmanManager(Set<String> words) {
    if (words == null || words.size() == 0) {
      throw new IllegalArgumentException("Sorry, the dictionary appears to be empty."
          + "Please use a valid dictionary.");
    }
    this.original = words;
  }

  /**
   * Get the number of words in this HangmanManager of the given length.
   * Used for valid user's length selection.
   * pre: none
   * 
   * @param length The given length to check.
   * @return the number of words in the original Dictionary
   *         with the given length
   */
  public int numWords(int length) {
    int numWords = 0;
    itr = original.iterator();
    while (itr.hasNext()) {
      if (itr.next().length() == length) {
        numWords++;
      }
    }
    return numWords;
  }

  /**
   * Get for a new round of Hangman. Think of a round as a
   * complete game of Hangman.
   * 
   * @param wordLen    the length of the word to pick this time.
   *                   numWords(wordLen) > 0
   * @param numGuesses the number of wrong guesses before the
   *                   player loses the round. numGuesses >= 1
   * @param diff       The difficulty for this round.
   */
  public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
    letters = new ArrayList<>();
    lenLimit = wordLen;
    guesses = numGuesses;
    this.diff = diff;
    activeList = original;
    currentPattern = new StringBuilder(lenLimit).append(defaultFamily());
  }

  /**
   * The number of words still possible (live) based on the guesses so far.
   * Guesses will eliminate possible words.
   * 
   * @return the number of words that are still possibilities based on the
   *         original dictionary and the guesses so far.
   */
  public int numWordsCurrent() {
    return activeList.size();
  }

  /**
   * Get the number of wrong guesses the user has left in
   * this round (game) of Hangman.
   * 
   * @return the number of wrong guesses the user has left
   *         in this round (game) of Hangman.
   */
  public int getGuessesLeft() {
    return guesses;
  }

  /**
   * Return a String that contains the letters the user has guessed
   * so far during this round.
   * The characters in the String are in alphabetical order.
   * The String is in the form [let1, let2, let3, ... letN].
   * For example [a, c, e, s, t, z]
   * 
   * @return a String that contains the letters the user
   *         has guessed so far during this round.
   */
  public String getGuessesMade() {
    Collections.sort(letters);
    String guessesMade = "[";
    for (char letter : letters) {
      guessesMade += Character.toString(letter);
      if (!(letters.indexOf(letter) == letters.size() - 1)) {
        guessesMade += ", ";
      }
    }
    guessesMade += "]";
    return guessesMade;
  }

  /**
   * Check the status of a character.
   * 
   * @param guess The characater to check.
   * @return true if guess has been used or guessed this round of Hangman,
   *         false otherwise.
   */
  public boolean alreadyGuessed(char guess) {
    return letters.contains(guess);
  }

  /**
   * Get the current pattern. The pattern contains '-''s for
   * unrevealed (or guessed) characters and the actual character
   * for "correctly guessed" characters.
   * 
   * @return the current pattern.
   */
  public String getPattern() {
    return this.currentPattern.toString();
  }

  /**
   * Update the game status (pattern, wrong guesses, word list),
   * based on the give guess.
   * 
   * @param guess pre: !alreadyGuessed(ch), the current guessed character
   * @return return a tree map with the resulting patterns and the number of
   *         words in each of the new patterns.
   *         The return value is for testing and debugging purposes.
   */
  public TreeMap<String, Integer> makeGuess(char guess) {
    letters.add(guess);
    Map<StringBuilder, ArrayList<String>> families = new TreeMap<>(); // used internally
    itr = activeList.iterator();
    while (itr.hasNext()) {
      String word = itr.next();
      if (word.length() == lenLimit) {
        StringBuilder current = new StringBuilder(this.currentPattern);
        if (word.contains(Character.toString(guess))) {
          for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess) {
              current.setCharAt(i, guess);
            }
          }
        }
        if (!families.containsKey(current)) {
          families.put(current, new ArrayList<String>());
        }
        ArrayList<String> wordsInFamily = families.get(current);
        wordsInFamily.add(word);
      }
    }
    TreeMap<String, Integer> debugFamilies = activePattern(families);
    if (!getPattern().contains(Character.toString(guess))) {
      guesses--;
    }
    return debugFamilies;
  }

  /*
   * Serves two primary functions:
   * 1.) Return Debugger's version of families TreeMap.
   * 2.) Determine Active List.
   */
  private TreeMap<String, Integer> activePattern(Map<StringBuilder, ArrayList<String>> families) {
    TreeMap<String, Integer> debugFamilies = new TreeMap<>();
    TreeMap<Integer, StringBuilder> orders = new TreeMap<>();
    int greatestSize = 0;
    boolean passed = false;
    for (StringBuilder family : families.keySet()) {
      int size = families.get(family).size(); // debug
      debugFamilies.put(family.toString(), size); // debug
      orders.put(size, family);
      if (size > greatestSize) {
        greatestSize = size;
        currentPattern = family;
        activeList = new TreeSet<String>(families.get(family));
      } else if (size == greatestSize) {
        boolean leastCharacters = characters(family);
        currentPattern = leastCharacters ? family : compare(family);
        currentPattern = diffAdjust(family, this.currentPattern);
        activeList = new TreeSet<String>(families.get(this.currentPattern));
        passed = true;
      }
    }
    if (!passed) {
      StringBuilder hardest = new StringBuilder(orders.get(orders.lastKey()));
      if (orders.size() != 1) {
        orders.remove(orders.lastKey());
      }
      StringBuilder secondHardest = new StringBuilder(orders.get(orders.lastKey()));
      currentPattern = diffAdjust(secondHardest, hardest);
      activeList = new TreeSet<String>(families.get(this.currentPattern));
    }
    return debugFamilies;
  }

  private StringBuilder diffAdjust(StringBuilder secondHardest, StringBuilder hardest) {
    if (diff.equals(HangmanDifficulty.MEDIUM) && letters.size() % 4 == 0) {
      return secondHardest;
    } else if (diff.equals(HangmanDifficulty.EASY) && letters.size() % 2 == 0) {
      return secondHardest;
    }
    return hardest;
  }

  // Collects the number of characters revealed in a pattern.
  private boolean characters(StringBuilder family) {
    int possible = 0;
    for (int i = 0; i < family.length(); i++) {
      if (family.charAt(i) != '-') {
        possible++;
      }
    }
    int contested = 0;
    for (int i = 0; i < this.currentPattern.length(); i++) {
      if (family.charAt(i) != '-') {
        contested++;
      }
    }
    return (possible < contested);
  }

  // Second tiebreaker deals with ASCII comparison
  private StringBuilder compare(StringBuilder possible) {
    if (possible.compareTo(this.currentPattern) < 0) {
      return possible;
    }
    return this.currentPattern;
  }

  // Creates the initial family pattern. For instance, if lenLimit 4, then returns
  // "----".
  private StringBuilder defaultFamily() {
    StringBuilder current = new StringBuilder(lenLimit);
    for (int i = 0; i < lenLimit; i++) {
      current.append('-');
    }
    this.currentPattern = current;
    return current;
  }

  /**
   * Return the secret word this HangmanManager finally ended up
   * picking for this round.
   * If there are multiple possible words left one is selected at random.
   * <br>
   * pre: numWordsCurrent() > 0
   * 
   * @return the secret word the manager picked.
   */
  public String getSecretWord() {
    if (numWordsCurrent() <= 0) {
      throw new IllegalStateException("Oops. A fatal error has occured :(");
    }
    ArrayList<String> last = new ArrayList<>(activeList);
    String result;
    if (activeList.size() > 1) {
      int r = new Random().nextInt(last.size());
      result = last.get(r);
    } else {
      result = last.get(0);
    }
    return result;
  }
}
