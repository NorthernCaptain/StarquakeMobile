package northern.captain.gamecore.android.gplus;

import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.GameHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.gplus.Achievement;
import northern.captain.gamecore.gplus.IGoogleGamesProcessor;
import northern.captain.quadronia.android.common.AndroidContext;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.tools.IPersistentConfig;
import northern.captain.tools.Log;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 10.05.14
 * Time: 17:16
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class GoogleGamesProcessor implements GameHelper.GameHelperListener, IGoogleGamesProcessor
{
    protected Map<Integer, String> leaderboardNames = new HashMap<Integer, String>();

    protected static GoogleGamesProcessor singleton;

    protected GameHelper gameHelper;

    protected boolean wasSignedIn = false;
    protected boolean firstSignIn = true;

    public static GoogleGamesProcessor instance()
    {
        return singleton;
    }

    public static void initialize()
    {
        singleton = new GoogleGamesProcessor();
    }

    private Object cloudBlocker = new Object();

    public GoogleGamesProcessor()
    {
        try
        {
            gameHelper = new GameHelper(AndroidContext.activity, GameHelper.CLIENT_GAMES | GameHelper.CLIENT_SNAPSHOT);

            gameHelper.setConnectOnStart(false);
            gameHelper.setup(this);
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception1: " + ex);
        }
    }

    @Override
    public void onStart()
    {
        try
        {
            Log.i("ncgameplus", "Do google on start");
            gameHelper.onStart(AndroidContext.activity);
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception2: " + ex);
        }
    }


    protected Runnable callOnSignIn = null;

    @Override
    public void doSignIn(Runnable callOnSuccess)
    {
        try
        {
            Log.i("ncgameplus", "Starting google sign in");
            callOnSignIn = callOnSuccess;
            gameHelper.beginUserInitiatedSignIn();
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception3: " + ex);
        }

    }

    @Override
    public void doSignOut()
    {
        try
        {
            Log.i("ncgameplus", "Do google sign OUT");
            wasSignedIn = false;
            gameHelper.signOut();
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception4: " + ex);
        }
    }

    @Override
    public boolean isSignedIn()
    {
        try
        {
            return gameHelper.isSignedIn();
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception5: " + ex);
        }
        return false;
    }

    @Override
    public void onStop()
    {
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Object intent)
    {
    }

    @Override
    public boolean openLeaderboard()
    {
        try
        {
            if(gameHelper.isSignedIn())
            {
                openLeaderboardInternal();
                return true;
            } else
            {
                doSignIn(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        openLeaderboardInternal();
                    }
                });

                return false;
            }
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception8: " + ex);
        }
        return false;
    }

    @Override
    public boolean openAchievements()
    {
        try
        {
            if(gameHelper.isSignedIn())
            {
                openAchievementsInternal();
                return true;
            } else
            {
                doSignIn(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        openAchievementsInternal();
                    }
                });

                return false;
            }
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception8: " + ex);
        }
        return false;
    }

    @Override
    public void submitScore(int id, int score)
    {
        try
        {
            String boardName = leaderboardNames.get(id);
            if(isSignedIn() && boardName != null)
            {
                gameHelper.getLeaderboardClient().submitScore(boardName, score);
            }
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception9: " + ex);
        }
    }

    @Override
    public void addLeaderboard(int id, int resId)
    {
        leaderboardNames.put(id, AndroidContext.activity.getString(resId));
    }

    protected void openLeaderboardInternal()
    {
        NCore.instance().postRunnableOnMain(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    gameHelper.showLeaderboard();
                } catch (Exception ex)
                {
                    Log.e("ncgameplus", "Exception10: " + ex);
                }
            }
        });
    }

    protected void openAchievementsInternal()
    {
        NCore.instance().postRunnableOnMain(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    gameHelper.showAchievements();
                } catch(Exception ex)
                {
                    Log.e("ncgameplus", "Exception10: " + ex);
                }
            }
        });
    }

    @Override
    public void unlockAchievement(Achievement achievement)
    {
        try
        {
            if(isSignedIn())
            {
                Log.i("ncgameplus", "Unlocking achievement with id=" + achievement.id);
                gameHelper.getAchievementClient().unlock(achievement.id);
            } else
            {
                Log.i("ncgameplus", "Skip unlocking achievement with id=" + achievement.id + ", user is not signed in!");
            }
        } catch(Exception ex)
        {
            Log.e("ncgameplus", "Exception99: " + ex);
        }
    }

    /**
     * Called when sign-in fails. As a result, a "Sign-In" button can be
     * shown to the user; when that button is clicked, call
     *
     * @link{GamesHelper#beginUserInitiatedSignIn . Note that not all calls
     * to this method mean an
     * error; it may be a result
     * of the fact that automatic
     * sign-in could not proceed
     * because user interaction
     * was required (consent
     * dialogs). So
     * implementations of this
     * method should NOT display
     * an error message unless a
     * call to @link{GamesHelper#
     * hasSignInError} indicates
     * that an error indeed
     * occurred.
     */
    @Override
    public void onSignInFailed()
    {
        Log.w("ncgameplus", "Failed to do Sing-In to google play services");
    }

    /**
     * Called when sign-in succeeds.
     */
    @Override
    public void onSignInSucceeded()
    {
        Log.i("ncgameplus", "Successfully signed in to google play services");
        if(callOnSignIn != null)
        {
            NContext.current.post(callOnSignIn);
        }
        wasSignedIn = true;
        getUserInfo();
    }

    /**
     * Method for saving persistent data into given config object
     *
     * @param cfg
     */
    @Override
    public void saveData(IPersistentConfig cfg)
    {
        Log.i("ncgameplus", "Do save settings");
        cfg.setBoolean("gplusWasSIn", wasSignedIn);
        cfg.setBoolean("gplusFirstSignIn", firstSignIn);
    }

    /**
     * Stores persistent data to file stream
     *
     * @param fout
     */
    @Override
    public void saveData(FileOutputStream fout)
    {

    }

    /**
     * Method for loading persistent data from config object
     *
     * @param cfg
     */
    @Override
    public void loadData(IPersistentConfig cfg)
    {
        wasSignedIn = cfg.getBoolean("gplusWasSIn", wasSignedIn);
        firstSignIn = cfg.getBoolean("gplusFirstSignIn", firstSignIn);
        Log.i("ncgameplus", "Do load settings, gpSignIn=" + wasSignedIn + ", gpFirst=" + firstSignIn);
        if(wasSignedIn || firstSignIn)
        {
            NCore.instance().postRunnableOnMain(new Runnable()
            {
                @Override
                public void run()
                {
                    boolean isSigned = isSignedIn();
                    Log.i("ncgameplus", "Try to do sign in, signed=" + isSigned);
                    if(!isSigned)
                    {
                        doSignIn(()->cloudLoad(AndroidContext.CLOUD_SAVE_NAME, ()->AndroidContext.onCloudLoaded()));
                    }
                    firstSignIn = false;
                }
            });
        }
    }

    /**
     * Reads persistent data from file stream
     *
     * @param fin
     */
    @Override
    public void loadData(FileInputStream fin)
    {

    }

    @Override
    public void cloudLoad(final String name, final Runnable postRun)
    {
        if(!isSignedIn()) return;

//        new AsyncGTask<Void, byte[]>()
//        {
//            /**
//             * Put your long time background job here
//             *
//             * @param bytes pass parametes to your job
//             * @return the result of your background job
//             */
//            @Override
//            public byte[] doInBackground(Void... bytes)
//            {
//                Snapshot snapshot = null;
//                try
//                {
//                    getUserInfo();
//
//                    Snapshots.OpenSnapshotResult openSnapshotResult =
//                        Games.Snapshots.open(gameHelper.getApiClient(), name, true).await();
//
//                    int status = openSnapshotResult.getStatus().getStatusCode();
//
//                    if (status == GamesStatusCodes.STATUS_OK)
//                    {
//                        Log.i("ncgameg", "Got snapshot in OK status");
//                        snapshot = openSnapshotResult.getSnapshot();
//                    } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT)
//                    {
//                        Log.i("ncgameg", "Got snapshot in CONFLICT status");
//                        snapshot = openSnapshotResult.getSnapshot();
//                        byte[] bytes1 = snapshot.getSnapshotContents().readFully();
//                        snapshot = openSnapshotResult.getConflictingSnapshot();
//                        byte[] bytes2 = snapshot.getSnapshotContents().readFully();
//
//                        byte[] result = NativeNFactory.nci.z_(bytes1, bytes2);
//
//                        Snapshot resolved = openSnapshotResult.getSnapshot();
//                        if(result == bytes2)
//                        {
//                            resolved = openSnapshotResult.getConflictingSnapshot();
//                        }
//
//                        Games.Snapshots.resolveConflict(gameHelper.getApiClient(), openSnapshotResult.getConflictId(),
//                            resolved).await();
//
//                        Log.i("ncgameg", "Snapshot in CONFLICT status RESOLVED");
//
//                        return result;
//                    } else
//                    {
//                        Log.w("ncgameg", "Got ERROR loading snapshot: " + status);
//                    }
//
//                    if (snapshot != null)
//                    {
//                        byte[] data = snapshot.getSnapshotContents().readFully();
//                        return data;
//                    }
//                }
//                catch (Exception ex)
//                {
//                    Log.e("ncgameg", "Exception loading snapshot: " + ex.getMessage(), ex);
//                }
//                return null;
//            }
//
//            /**
//             * This will be executed on OpenGL thread after the background job is done
//             *
//             * @param result - result return by your background job if any
//             */
//            @Override
//            public void onPostExecute(byte[] result)
//            {
//                //Do not apply snapshot data if we are already playing
//                if(result != null && NativeNFactory.nci.q(IGameContext.LAST_SCREEN) != IScreenWorkflow.STATE_BATTLE)
//                {
//                    NativeNFactory.nci.z(result);
//                    if(postRun != null) postRun.run();
//                }
//
//            }
//        }.execute();
    }

    @Override
    public void cloudSave(final String name, final byte[] data, final Runnable postRun)
    {
        if(!isSignedIn())
        {
            if(postRun != null) postRun.run();
            return;
        }

//        new AsyncGTask<byte[], Boolean>()
//        {
//            /**
//             * Put your long time background job here
//             *
//             * @param bytes pass parametes to your job
//             * @return the result of your background job
//             */
//            @Override
//            public Boolean doInBackground(byte[]... bytes)
//            {
//
//                try
//                {
//                    synchronized (cloudBlocker)
//                    {
//                        Log.i("ncgameg", "Opening snapshot for Saving.");
//                        Snapshots.OpenSnapshotResult openSnapshotResult =
//                            Games.Snapshots.open(gameHelper.getApiClient(), name, true).await();
//
//                        if (openSnapshotResult.getStatus().
//                            isSuccess())
//                        {
//                            Log.i("ncgameg", "Snapshot OPENED for Saving.");
//
//                            Snapshot snapshot = openSnapshotResult.getSnapshot();
//
//                            snapshot.getSnapshotContents().writeBytes(bytes[0]);
//                            // Create the change operation
//                            SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
//                                .setDescription("Saved game")
//                                .build();
//
//                            Games.Snapshots.commitAndClose(gameHelper.getApiClient(), snapshot, metadataChange);
//
//                            Log.i("ncgameg", "Snapshot SAVED to the cloud.");
//
//                            if (postRun != null) postRun.run();
//
//                            return true;
//                        }
//                        Log.i("ncgameg", "Finished Saving snapshot.");
//                    }
//                } catch (Exception ex)
//                {
//                    Log.e("ncgameg", "Got exception saving snapshot: " + ex.getMessage(), ex);
//                }
//                return false;
//            }
//
//            /**
//             * This will be executed on OpenGL thread after the background job is done
//             *
//             * @param result - result return by your background job if any
//             */
//            @Override
//            public void onPostExecute(Boolean result)
//            {
//                if(currentPlayer != null)
//                {
//                    Log.i("ncgameg", "Got user: " + currentPlayer.getDisplayName()
//                        + " and GID: " + currentPlayer.getPlayerId());
//                    UserManager.instance.setUserInfo(currentPlayer.getDisplayName(), currentPlayer.getPlayerId());
//                }
//            }
//        }.execute(data);
    }

    Player currentPlayer;

    private void getUserInfo()
    {
        gameHelper.getCurrentPlayer(()-> {
            currentPlayer = gameHelper.currentPlayer;
            Log.i("ncgameg", "Thread Got user: " + currentPlayer.getDisplayName()
                    + " and GID: " + currentPlayer.getPlayerId());
            NContext.current.post(() -> {
                if(currentPlayer != null)
                {
                    Log.i("ncgameg", "Got user: " + currentPlayer.getDisplayName()
                            + " and GID: " + currentPlayer.getPlayerId());
                    UserManager.instance.setUserInfo(currentPlayer.getDisplayName(), currentPlayer.getPlayerId());
                }
            });
        });
    }
}
