package northern.captain.gamecore.glx;

/**
 * Created by leo on 24.05.15.
 */
public interface INameDialog
{
    interface OnOkCallback
    {
        boolean onOk(String value);
    }

    void create(String oldName);
    void show(OnOkCallback callback);
}
