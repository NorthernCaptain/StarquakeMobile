package northern.captain.quadronia.b;

public interface INativeN
{

    /**
     * Get int value from the storage with the key 'key'
     * @param key
     * @return int value
     */
    int q(int key);

    /**
     * Set int value to the storage for a key
     * @param key
     * @param value
     * @return
     */
    int r(int key, int value);

    /**
     * Save the crypted storage to the disk file in internal directory
     * @param key
     * @return
     */
    int s(int key);

    /**
     * Load the crypted storage from the file on disk
     * @param key
     * @return
     */
    int t(int key);

    /**
     * Get string value from the encrypted storage using the key 'key'
     * @param key - key in the encrypted storage
     * @return decripted string or null if no such entry for the given key
     */
    String u(int key);

    /**
     * Put the string into the storage with the key 'key'
     * @param key
     * @param str
     */
    void v(int key, String str);

    /**
     * Ask for new user. Create new user internal structure and return its index
     * @param dummy - just dummy, ignored in the native library
     * @return index of the newly created user's internal structure
     */
    int w(int dummy);

    /**
     * Do internal purchase for in-app coins. Do not confuse with google in-app billing
     * This is internal only purchases
     * @param uid - userid
     * @param bonusIdx - bonus to purchase
     * @param totalPrice - ignored
     * @param qty - qty, >0 - buy items, <0 - sell items
     * @return <0 - purchase was unsuccessful, 0 - ok, purchase is processed now
     */
    int x(int uid, int bonusIdx, int totalPrice, int qty);

    /**
     * Call this to activate reshuffle bonus perk
     */
    void n_();

    /**
     * Call this to activate Hint bonus perk
     */
    void o_();

    /**
     * Call this to activate Bomb bonus perk
     */
    void p_();
}
