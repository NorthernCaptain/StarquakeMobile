package northern.captain.quadronia.android.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import northern.captain.quadronia.b.INativeN;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.b.nj;

public class NativeNFactoryIOS extends NativeNFactory
{

    public static void initialize()
    {
        instance = new NativeNFactoryIOS();
        nci = new nj() {
            @Override
            protected Preferences getP() {
                if(pref == null && Gdx.app != null) {
                    pref = Gdx.app.getPreferences("quadronia-p6-ios");
                }
                return pref;
            }
        };
    }

    @Override
    public INativeN newNativeN()
    {
        return new nj();
    }
}
