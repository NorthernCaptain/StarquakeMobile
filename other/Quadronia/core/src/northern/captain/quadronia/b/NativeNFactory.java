package northern.captain.quadronia.b;

public abstract class NativeNFactory
{
	public static NativeNFactory instance;
	public static INativeN nci;
	public abstract INativeN newNativeN();
}
