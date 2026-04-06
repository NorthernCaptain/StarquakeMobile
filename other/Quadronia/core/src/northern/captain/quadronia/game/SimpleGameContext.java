package northern.captain.quadronia.game;

import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.game.achievements.AchieveMan;
import northern.captain.quadronia.game.profile.UserBase;
import northern.captain.gamecore.gplus.GoogleGamesFactory;

/**
 * Created by leo on 03.04.15.
 */
public class SimpleGameContext implements IGameContext
{
    private UserBase currentPlayer;

    @Override
    public UserBase getCurrentPlayer()
    {
        return currentPlayer;
    }

    @Override
    public void setCurrentPlayer(UserBase user)
    {
        currentPlayer = user;
    }

    @Override
    public int getScore()
    {
        return NativeNFactory.nci.q(SCORE_IDX);
    }

    @Override
    public void setScore(int newScore)
    {
        NativeNFactory.nci.r(SCORE_IDX, newScore);
    }

    @Override
    public void addScore(int deltaScore)
    {
        int val = NativeNFactory.nci.q(SCORE_IDX) + deltaScore;
        NativeNFactory.nci.r(SCORE_IDX, val);
        NativeNFactory.nci.r(LASTSCORE_DELTA_IDX, deltaScore);
        AchieveMan.instance().registerEvent(Constants.EVENT_SCORE_CHANGED, val);
    }

    @Override
    public int getLastScoreDelta()
    {
        return NativeNFactory.nci.q(LASTSCORE_DELTA_IDX);
    }

    @Override
    public int getLevel()
    {
        return NativeNFactory.nci.q(LEVEL_IDX);
    }

    @Override
    public int registerLevelUp()
    {
        int level = NativeNFactory.nci.q(LEVEL_IDX);
        level++;
        NativeNFactory.nci.r(LEVEL_IDX, level);
        AchieveMan.instance().registerEvent(Constants.EVENT_LEVEL_UP, level);
        return level;
    }

    @Override
    public int getTillLevelUp()
    {
        return NativeNFactory.nci.q(TILL_NEXT_LVL_IDX);
    }

    @Override
    public void setTillLevelUp(int value)
    {
        NativeNFactory.nci.r(TILL_NEXT_LVL_IDX, value);
    }

    @Override
    public int elementsUsed(int elements)
    {
        int tillNextLevelUp = NativeNFactory.nci.q(TILL_NEXT_LVL_IDX);
        tillNextLevelUp -= elements;
        NativeNFactory.nci.r(TILL_NEXT_LVL_IDX, tillNextLevelUp);
        AchieveMan.instance().registerEvent(Constants.EVENT_FRAGMENTS_USED, NativeNFactory.nci.q(FRAG_IDX));
        return tillNextLevelUp;
    }

    @Override
    public void addFragment(int num)
    {
        NativeNFactory.nci.r(FRAG_IDX, NativeNFactory.nci.q(FRAG_IDX) + num);
    }

    @Override
    public void addCircuit()
    {
        int val = NativeNFactory.nci.q(CIRCUIT_IDX)+1;
        NativeNFactory.nci.r(CIRCUIT_IDX, val);
        AchieveMan.instance().registerEvent(Constants.EVENT_CIRCUIT_DONE, val);
    }

    @Override
    public int getFragments()
    {
        return NativeNFactory.nci.q(FRAG_IDX);
    }

    @Override
    public int getCircuits()
    {
        return NativeNFactory.nci.q(CIRCUIT_IDX);
    }

    @Override
    public void clear()
    {
        setScore(0);
        NativeNFactory.nci.r(LEVEL_IDX, 1);
        NativeNFactory.nci.r(FRAG_IDX, 0);
        NativeNFactory.nci.r(CIRCUIT_IDX, 0);
    }

    @Override
    public void onGameOver(int mode)
    {
        int val = setMax(SCORE_IDX, MAXSCORE_IDX + mode);
        if(val > 0)
        {
            int type = getGameType();
            GoogleGamesFactory.instance().getProcessor().submitScore(Constants.getLeaderboardId(type, mode), val);
        }

        setMax(LEVEL_IDX, MAXLEVEL_IDX + mode);
        setMax(CIRCUIT_IDX, MAXCIRCUIT_IDX + mode);
        setMax(FRAG_IDX, MAXFRAG_IDX + mode);

        setSavedGame(null);
        saveToDisk();
    }

    @Override
    public int saveToDisk()
    {
        int ret = NativeNFactory.nci.s(5843);
        if(currentPlayer != null)
        {
            AchieveMan.instance().registerEvent(Constants.EVENT_GOLD_CHANGED, currentPlayer.getCoins());
        }
        return ret;
    }

    private int setMax(int fromKey, int maxKey)
    {
        int val = NativeNFactory.nci.q(fromKey);
        if(val > NativeNFactory.nci.q(maxKey))
        {
            NativeNFactory.nci.r(maxKey, val);
        }
        return val;
    }

    @Override
    public int getMaxScore(int mode)
    {
        return NativeNFactory.nci.q(MAXSCORE_IDX + mode);
    }

    @Override
    public int getMaxLevel(int mode)
    {
        return NativeNFactory.nci.q(MAXLEVEL_IDX + mode);
    }

    @Override
    public int getMaxFragments(int mode)
    {
        return NativeNFactory.nci.q(MAXFRAG_IDX + mode);
    }

    @Override
    public int getMaxCircuits(int mode)
    {
        return NativeNFactory.nci.q(MAXCIRCUIT_IDX + mode);
    }

    @Override
    public int getLastMode()
    {
        return NativeNFactory.nci.q(LASTMODE_IDX);
    }

    @Override
    public void setLastMode(int mode)
    {
        NativeNFactory.nci.r(LASTMODE_IDX, mode);
    }

    @Override
    public int getGameType()
    {
        return NativeNFactory.nci.q(LASTGAMETYPE_IDX);
    }

    @Override
    public void setGameType(int type)
    {
        NativeNFactory.nci.r(LASTGAMETYPE_IDX, type);
    }

    @Override
    public int getBonusBackstep()
    {
        return NativeNFactory.nci.q(currentPlayer.getId() + BONUS_BACKSTEP_IDX);
    }

    @Override
    public int getBonusSwap()
    {
        return NativeNFactory.nci.q(currentPlayer.getId() + BONUS_SWAP_IDX);
    }

    @Override
    public int extraBonusBackstep()
    {
        int key = currentPlayer.getId() + BONUS_BACKSTEP_IDX;
        int val = NativeNFactory.nci.q(key) + 1;
        if(val < 0) val = 0;
        NativeNFactory.nci.r(key, val);
        return val;
    }


    @Override
    public int extraBonusSwap()
    {
        int key = currentPlayer.getId() + BONUS_SWAP_IDX;
        int val = NativeNFactory.nci.q(key) + 1;
        if(val < 0) val = 0;
        NativeNFactory.nci.r(key, val);
        return val;
    }

    @Override
    public int getBonusByIdx(int idx)
    {
        int val = NativeNFactory.nci.q(currentPlayer.getId() + idx);
        return val;
    }

    @Override
    public int useBonusByIdx(int idx)
    {
        idx += currentPlayer.getId();
        int val = NativeNFactory.nci.q(idx) - 1;
        if(val < 0) val = 0;
        NativeNFactory.nci.r(idx, val);
        return val;
    }

    @Override
    public int getCurrentPlayerIdx()
    {
        return NativeNFactory.nci.q(CURRENT_PLAYER_IDX);
    }

    @Override
    public int getTotalPlayers()
    {
        return NativeNFactory.nci.q(TOTAL_PLAYERS_IDX);
    }

    @Override
    public String getPlayerName(int playerId)
    {
        return NativeNFactory.nci.u(playerId + PLAYER_NAME_IDX);
    }

    @Override
    public void setPlayerName(int playerId, String playerName)
    {
        NativeNFactory.nci.v(playerId + PLAYER_NAME_IDX, playerName);
    }

    @Override
    public void setPlayerGoogleId(int playerId, String playerGID)
    {
        NativeNFactory.nci.v(playerId + PLAYER_GID_IDX, playerGID);
    }

    @Override
    public String getPlayerGID(int playerId)
    {
        return NativeNFactory.nci.u(playerId + PLAYER_GID_IDX);
    }

    @Override
    public int getUserDatai(int playerId, int idx)
    {
        return NativeNFactory.nci.q(playerId + idx);
    }

    @Override
    public int internalPurchase(int bonusIdx, int qty, int totalprice)
    {
        return NativeNFactory.nci.x(currentPlayer.getId(), bonusIdx, totalprice, qty);
    }

    @Override
    public int setDatai(int key, int val)
    {
        NativeNFactory.nci.r(key, val);
        return val;
    }

    @Override
    public int getDatai(int key)
    {
        return NativeNFactory.nci.q(key);
    }

    @Override
    public String getSavedGame()
    {
        String state = NativeNFactory.nci.u(SAVED_GAME_IDX);
        if(state != null && state.equals("{}")) return null;
        return state;
    }

    @Override
    public int setSavedGame(String state)
    {
        if(state == null) state = "{}";
        NativeNFactory.nci.v(SAVED_GAME_IDX, state);
        return 0;
    }
}
