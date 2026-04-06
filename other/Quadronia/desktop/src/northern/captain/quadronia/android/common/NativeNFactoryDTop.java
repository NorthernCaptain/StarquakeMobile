package northern.captain.quadronia.android.common;

import northern.captain.quadronia.b.INativeN;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.b.nj;

public class NativeNFactoryDTop extends NativeNFactory
{

    public static void initialize()
    {
        instance = new NativeNFactoryDTop();
        nci = new nj();
    }

    @Override
    public INativeN newNativeN()
    {
        return new nj();
    }
}
