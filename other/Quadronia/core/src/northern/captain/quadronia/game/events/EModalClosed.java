package northern.captain.quadronia.game.events;

import northern.captain.quadronia.BaseDialog;

/**
 * Created by leo on 19.01.16.
 * Event is fired when modal dialog is closed
 */
public class EModalClosed
{
    public BaseDialog dialog;
    public int buttonPressed;

    public EModalClosed(BaseDialog who, int how)
    {
        dialog = who;
        buttonPressed = how;
    }

}
