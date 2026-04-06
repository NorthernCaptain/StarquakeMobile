package northern.captain.quadronia.b;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.events.EFullReshuffle;
import northern.captain.quadronia.game.events.EPerkDoBomb;
import northern.captain.quadronia.game.events.EPerkDoHint;
import northern.captain.quadronia.game.profile.UserBase;

public class nj implements INativeN
{
    protected Preferences pref = null;

    private final Map<String, Object> vcache = new HashMap<>();

    protected Preferences getP() {
        if(pref == null && Gdx.app != null) {
            pref = Gdx.app.getPreferences("quadronia-p6");
        }
        return pref;
    }
    
    {
        putIValue(IGameContext.PRICE_DELTA_IDX + IGameContext.BONUS_BACKSTEP_IDX, codedVals[6] - codedKeys[6]);
        putIValue(IGameContext.PRICE_DELTA_IDX + IGameContext.BONUS_SWAP_IDX, codedVals[7] - codedKeys[7]);
        putIValue(IGameContext.PRICE_DELTA_IDX + IGameContext.BONUS_BOMB_IDX, codedVals[8] - codedKeys[8]);
        putIValue(IGameContext.PRICE_DELTA_IDX + IGameContext.BONUS_JOKER_IDX, codedVals[9] - codedKeys[9]);
        putIValue(IGameContext.PRICE_DELTA_IDX + IGameContext.BONUS_WORKER_IDX, codedVals[10] - codedKeys[10]);
    }

    private static final int[] codedVals =
            {
                    20917 + 500, //PLAYER_COINS_IDX
                    34737 + 6, //BONUS_BACKSTEP_IDX
                    11951 + 5,
                    58890 + 6,
                    1215  + 4,
                    55950 + 5,

                    64638 + 13, //BONUS_BACKSTEP_IDX PRICE
                    31335 + 17, //BONUS_SWAP_IDX PRICE
                    49048 + 47, //BONUS_WORKER_IDX PRICE
                    14484 + 33, //BONUS_JOKER_IDX PRICE
                    58823 + 25, //BONUS_BOMB_IDX PRICE
            };

    private static final int[] codedKeys =
            {
                    20917, //PLAYER_COINS_IDX
                    34737, //BONUS_BACKSTEP_IDX
                    11951,
                    58890,
                    1215 ,
                    55950,

                    64638, //BONUS_BACKSTEP_IDX PRICE
                    31335, //BONUS_SWAP_IDX PRICE
                    49048, //BONUS_WORKER_IDX PRICE
                    14484, //BONUS_JOKER_IDX PRICE
                    58823, //BONUS_BOMB_IDX PRICE
            };


    private int getIValue(int ikey) {
        String key = id2k(ikey);
        Object val = vcache.get(key);
        int rkey = ikey * 2 / 3;
        int v;
        if(val == null) {
            v = getP().getInteger(key, rkey);
            vcache.put(key, v);
        } else {
            v = (Integer)val;
        }
        v = (v - rkey) >> (rkey % 5);
        return v;
    }

    private String getSValue(int ikey) {
        String key = id2k(ikey);
        Object val = vcache.get(key);
        if(val == null) {
            String v = getP().getString(key, "");
            vcache.put(key, v);
            return v;
        }
        return (String)val;
    }

    private void putIValue(int ikey, int val) {
        String key = id2k(ikey);
        int rkey = ikey * 2 / 3;
        int rval = rkey + (val << (rkey % 5));
        vcache.put(key, rval);
        Preferences pref = getP();
        if(pref != null) {
            getP().putInteger(key, rval);
        }
    }

    private void putSValue(int ikey, String val) {
        String key = id2k(ikey);
        vcache.put(key, val);
        getP().putString(key, val);
    }

    private Preferences createUserPref(int uidx) {
        Preferences pref = getP();
        int userId = UserBase.index2Id(uidx);

        putIValue(IGameContext.PLAYER_COINS_IDX + userId, codedVals[0] - codedKeys[0]);
        putIValue(IGameContext.BONUS_BACKSTEP_IDX + userId, codedVals[1] - codedKeys[1]);
        putIValue(IGameContext.BONUS_SWAP_IDX + userId, codedVals[2] - codedKeys[2]);
        putIValue(IGameContext.BONUS_BOMB_IDX + userId, codedVals[3] - codedKeys[3]);
        putIValue(IGameContext.BONUS_JOKER_IDX + userId, codedVals[4] - codedKeys[4]);
        putIValue(IGameContext.BONUS_WORKER_IDX + userId, codedVals[5] - codedKeys[5]);

        putIValue(IGameContext.CURRENT_PLAYER_IDX, uidx);
        putIValue(IGameContext.TOTAL_PLAYERS_IDX, uidx + 1);
        pref.flush();
        return pref;
    }

    private String id2k(int id) {
        return "id:" + id;
    }

    private String u2k(int userId) {
        return "u:" + (userId + 1023);
    }

    private String b2k(int bonusId) {
        return "id:" + bonusId;
    }
    /**
     * Get int value from the storage with the key 'key'
     * @param key
     * @return int value
     */
    @Override
    public int q(int  key) {
        return getIValue(key);
    }

    /**
     * Set int value to the storage for a key
     * @param key
     * @param value
     * @return
     */
    @Override
    public int r(int  key, int value) {
        putIValue(key, value);
        return value;
    }

    /**
     * Save the crypted storage to the disk file in internal directory
     * @param key
     * @return
     */
    @Override
    public int s(int  key) {
        getP().flush();
        return 0;
    }

    /**
     * Load the crypted storage from the file on disk
     * @param key
     * @return
     */
    @Override
    public int t(int  key) {
        //nothing to do here
        return 0;
    }

    /**
     * Get string value from the encrypted storage using the key 'key'
     * @param key
     * @return
     */
    @Override
    public String u(int key) {
        return getSValue(key);
    }

    /**
     * Put the string into the storage with the key 'key'
     * @param key
     * @param str
     */
    @Override
    public void v(int key, String str) {
        putSValue(key, str);
    }

    /**
     * Ask for new user. Create new user internal structure and return its index
     *
     * @param dummy - just dummy, ignored in the native library
     * @return index of the newly created user's internal structure
     */
    @Override
    public int w(int dummy) {
        int idx = dummy < 0 ? 0 : dummy;
        createUserPref(idx);
        return idx;
    }

    /**
     * Do internal purchase for in-app coins. Do not confuse with google in-app billing
     * This is internal only purchases
     * @param uid - userid
     * @param bonusIdx - bonus to purchase
     * @param totalPrice - ignored
     * @param qty - qty, >0 - buy items, <0 - sell items
     * @return <0 - purchase was unsuccessful, 0 - ok, purchase is processed now
     */
    @Override
    public int x(int uid, int bonusIdx, int totalPrice, int qty) {
        int key = bonusIdx + uid;
        int val = getIValue(key);
        int uprice = getIValue(IGameContext.PRICE_DELTA_IDX + bonusIdx);
        int ourBank = getIValue(IGameContext.PLAYER_COINS_IDX + uid);
        if( val + qty < 0
                || val + qty > 1000
                || ourBank - uprice*qty < 0)
        {
            return -1;
        }
        putIValue(key, val + qty);
        putIValue(IGameContext.PLAYER_COINS_IDX + uid, ourBank - uprice*qty);
        return 0;
    }

    /**
     * Not a native method. It will be called from the native code instead
     * Generates reshuffle event
     */
    public static void ac()
    {
        NContext.current.postDelayed(()->NCore.busPost(new EFullReshuffle()), 400);
    }

    /**
     * Not a native method. It will be called from the native code instead
     * Generates hint event
     */
    public static void ab()
    {
        NCore.busPost(new EPerkDoHint());
    }

    /**
     * Not a native method. It will be called from the native code instead
     * Generates bomb event
     */
    public static void aa()
    {
        NCore.busPost(new EPerkDoBomb());
    }

    /**
     * Call this to activate reshuffle bonus perk
     */
    @Override
    public void n_() {
        int uidx = getIValue(IGameContext.CURRENT_PLAYER_IDX);
        int uid = UserBase.index2Id(uidx);
        int key = IGameContext.BONUS_SWAP_IDX + uid;
        int val = getIValue(key);
        if(val > 0) {
            putIValue(key, val - 1);
            nj.ac();
        }
    }

    /**
     * Call this to activate Hint bonus perk
     */
    @Override
    public void o_() {
        int uidx = getIValue(IGameContext.CURRENT_PLAYER_IDX);
        int uid = UserBase.index2Id(uidx);
        int key = IGameContext.BONUS_JOKER_IDX + uid;
        int val = getIValue(key);
        if(val > 0) {
            putIValue(key, val - 1);
            nj.ab();
        }
    }

    /**
     * Call this to activate Bomb bonus perk
     */
    @Override
    public void p_() {
        int uidx = getIValue(IGameContext.CURRENT_PLAYER_IDX);
        int uid = UserBase.index2Id(uidx);
        int key = IGameContext.BONUS_BOMB_IDX + uid;
        int val = getIValue(key);
        if(val > 0) {
            putIValue(key, val - 1);
            nj.aa();
        }
    }
}
