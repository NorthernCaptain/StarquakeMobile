package northern.captain.quadronia.android.common;

import northern.captain.quadronia.b.INativeN;
import northern.captain.quadronia.b.NativeNFactory;
import northern.captain.quadronia.b.nj;

public class NativeNFactoryAnd extends NativeNFactory
{

	public static void initialize()
	{
		instance = new NativeNFactoryAnd();
        nci = new nj();
	}

	@Override
	public INativeN newNativeN()
	{
		return new nj();
	}
}
