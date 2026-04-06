package northern.captain.tools;

public class KStoreForComm
{
	private static final String data 
	= "RlMwAAAAxAAAAAAAAAAAAJxh8MIAZ919g1mNUv7mDYGLsfip-z6nRX9d3Vz9Gw6O2GTG-f8_rUJ1AYRfsxrAn4Nh-8T-aKh-hBWMasPMGdrdZPa37TGTkHBfi1uy5gDeiLH2-Pgyq352WdtfsM3Agrez9v0Baa19aQ-FackbEN62a8bH_z-uQ0JZjVzFzw2Li2MCxv1tko9_CNtf_RgZ2t2zArLtbNp-axjbasMSFtzbZvvHLjjaRYIVi1_AGsCM32rxwv4xppJwDopowBgSto5uQvbjb42YLlLqsRxulME=";	
	private static KeyStore keystore;

	static
	{
		keystore = new KeyStore(data);
	}
	
	public static IKeyStore getStorage()
	{
		return keystore;
	}
}
