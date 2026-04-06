package northern.captain.quadronia.android.common;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.UIActivityViewController;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIPopoverPresentationController;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.apple.uikit.UIView;
import org.robovm.apple.uikit.UIViewController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.quadronia.game.core.Game;
import northern.captain.tools.sharing.ShareManager;

public class ShareManagerIOS extends ShareManager
{
    private UIViewController mainController;

    public void setMainController(UIViewController controller) {
        mainController = controller;
    }

    @Override
    public void doShareMyProgress(int mode)
    {
        NContext.current.postDelayed(() -> doShareInternal(mode), 800);
    }

    protected void doShareInternal(int mode)
    {
        final String message = SharedRes.instance.x("shareBodyIOS")
                .replaceAll("'%0'", String.valueOf(Game.defaultGameContext.getMaxScore(mode)))
                .replaceAll("'%1'", String.valueOf(Game.defaultGameContext.getMaxLevel(mode)));
        UIActivityViewController controller = new UIActivityViewController(Collections.singletonList(message), null);
        UIPopoverPresentationController popC = controller.getPopoverPresentationController();
        if(UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad && popC != null) {
            UIView view = mainController.getView();
            CGRect bounds = view.getBounds();
            popC.setSourceView(view);
            popC.setSourceRect(new CGRect(bounds.getMidX() - bounds.getWidth()*0.2,
                    bounds.getMinY() + bounds.getHeight()*0.15, 0, 0));
        }
        mainController.presentViewController(controller,
                true, ()-> { });
    }

    public static void initialize()
    {
        setSingleton(new ShareManagerIOS());
    }

}

