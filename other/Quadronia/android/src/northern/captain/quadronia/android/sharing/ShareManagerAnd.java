package northern.captain.quadronia.android.sharing;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;

import androidx.core.app.ShareCompat;

import northern.captain.quadronia.android.common.AndroidContext;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.quadronia.game.core.Game;
import northern.captain.tools.sharing.ShareManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 06.05.14
 * Time: 0:01
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class ShareManagerAnd extends ShareManager
{

    @Override
    public void doShareMyProgress(int mode)
    {
        NContext.current.postDelayed(() -> doShareInternal(mode), 800);
    }

    protected void doShareInternal(int mode)
    {
        final NCore nCore = NCore.instance();


        final String message = SharedRes.instance.x("shareBody")
                .replaceAll("'%0'", String.valueOf(Game.defaultGameContext.getMaxScore(mode)))
                .replaceAll("'%1'", String.valueOf(Game.defaultGameContext.getMaxLevel(mode)));
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        File ofile = null;
        {
            AssetManager assetManager = AndroidContext.activity.getAssets();
            try
            {
                inputStream = assetManager.open("data/400x195.jpg");

                File destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                destDir.mkdirs();

                File outFile = new File(destDir, "game_small.jpg");
                outputStream = new FileOutputStream(outFile, false);

                int len;
                byte[] buffer = new byte[4096];
                while((len = inputStream.read(buffer))>0)
                {
                    outputStream.write(buffer, 0, len);
                }

                ofile = outFile;
            } catch (IOException e) {}
            finally
            {
                if(inputStream != null)
                {
                    try
                    {
                        inputStream.close();
                    } catch (IOException e1)
                    {

                    }
                }

                if(outputStream != null)
                {
                    try
                    {
                        outputStream.close();
                    } catch (IOException e1)
                    {

                    }
                }
            }
        }

        final File imageFile = ofile;

        nCore.postRunnableOnMain(() -> {
             ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(AndroidContext.activity).setSubject(SharedRes.instance.x("shareSubj"))
                    .setText(message)
                    .setChooserTitle(SharedRes.instance.x("shareChooser"));

            if(imageFile != null)
            {
                builder.setType("text/plain");
                builder.setStream(Uri.fromFile(imageFile));
            } else
            {
                builder.setType("text/plain");
            }

            Intent shareIntent = builder.createChooserIntent();

            AndroidContext.activity.startActivityForResult(shareIntent, 0);
        });
    }

    public static void initialize()
    {
        setSingleton(new ShareManagerAnd());
    }

}
