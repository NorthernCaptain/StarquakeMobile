package northern.captain.quadronia.game;

import northern.captain.quadronia.game.profile.UserBase;

/**
 * Created by leo on 03.04.15.
 */
public interface IGameContext
{
    int SAVE_PROTO_VERSION = 199;
    int SAVE_DATE = 200;

    int TOTAL_PLAYERS_IDX = 998;
    int CURRENT_PLAYER_IDX = 999;
    int SCORE_IDX = 1000;
    int LASTSCORE_DELTA_IDX = 1001;
    int LEVEL_IDX = 1002;
    int TILL_NEXT_LVL_IDX = 1003;
    int FRAG_IDX = 1004;
    int CIRCUIT_IDX = 1005;
    int LASTMODE_IDX = 1006;
    int LASTGAMETYPE_IDX = 1007;

    int MAXFRAG_IDX = 1100;
    int MAXCIRCUIT_IDX = 1110;
    int MAXSCORE_IDX = 1120;
    int MAXLEVEL_IDX = 1130;

    int ACHIEVEMENT_BEGINNER = 1250;
    int ACHIEVEMENT_SCORE_500 = 1265;
    int ACHIEVEMENT_SCORE_3K = 1251;
    int ACHIEVEMENT_SCORE_7K = 1252;
    int ACHIEVEMENT_SCORE_15K = 1253;
    int ACHIEVEMENT_SCORE_50K = 1271;
    int ACHIEVEMENT_SCORE_100K = 1272;
    int ACHIEVEMENT_SCORE_250K = 1273;
    int ACHIEVEMENT_EXPRESS_100K = 1254;
    int ACHIEVEMENT_EXPRESS_200K = 1255;
    int ACHIEVEMENT_SCORE_500K = 1256;
    int ACHIEVEMENT_QUEST_LVL50 = 1257;
    int ACHIEVEMENT_EXPRESS_FIRST = 1258;
    int ACHIEVEMENT_EXPRESS_50K = 1259;
    int ACHIEVEMENT_EXPRESS_500 = 1260;
    int ACHIEVEMENT_EXPRESS_3K = 1261;
    int ACHIEVEMENT_EXPRESS_7K = 1262;
    int ACHIEVEMENT_EXPRESS_15K = 1263;
    int ACHIEVEMENT_EXPRESS_35K = 1264;

    int SAVED_GAME_IDX = 1300;
    int LAST_SCREEN = 1301;


    int PLAYER_NAME_IDX = 10000;
    int PLAYER_GID_IDX = 10001;
    int PLAYER_COINS_IDX = 10019;
    int BONUS_BACKSTEP_IDX = 10020;
    int BONUS_SWAP_IDX = 10021;
    int BONUS_WORKER_IDX = 10022;
    int BONUS_JOKER_IDX = 10023;
    int BONUS_BOMB_IDX = 10024;
    int PAYED_NO_ADS = 10025;
    int PAYED_EXTRA_BACKSTEPS = 10026;
    int PAYED_EXTRA_SWAPS = 10027;
    int PAYED_EXTRA_TIME = 10028;


    /**
     * We add this delta to the bonus index to get price value for it
     */
    int PRICE_DELTA_IDX = 1000;

    void setCurrentPlayer(UserBase user);
    UserBase getCurrentPlayer();

    int  getScore();
    void setScore(int newScore);
    void addScore(int deltaScore);
    void addFragment(int num);
    void addCircuit();
    int  getLastScoreDelta();
    int  getFragments();
    int  getCircuits();

    int getLevel();
    int registerLevelUp();
    int getTillLevelUp();
    void setTillLevelUp(int value);
    int elementsUsed(int elements);

    void clear();
    void onGameOver(int mode);

    int getMaxScore(int mode);
    int getMaxLevel(int mode);
    int getMaxFragments(int mode);
    int getMaxCircuits(int mode);

    int getLastMode();
    void setLastMode(int mode);

    int getGameType();
    void setGameType(int type);

    int getBonusBackstep();
    int getBonusSwap();
    int getBonusByIdx(int idx);
    int useBonusByIdx(int idx);
    int extraBonusBackstep();
    int extraBonusSwap();

    int getTotalPlayers();
    int getCurrentPlayerIdx();
    String getPlayerName(int playerId);
    String getPlayerGID(int playerId);
    void setPlayerName(int playerId, String playerName);
    void setPlayerGoogleId(int playerId, String playerGID);
    int getUserDatai(int playerId, int idx);

    int setDatai(int key, int val);
    int getDatai(int key);

    int internalPurchase(int bonusIdx, int qty, int totalprice);

    int saveToDisk();
    int setSavedGame(String state);
    String getSavedGame();
}
