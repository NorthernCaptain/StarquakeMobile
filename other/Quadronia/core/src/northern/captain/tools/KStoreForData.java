package northern.captain.tools;

public class KStoreForData
{
	private static final String data 
	= "RlMwAAAAxAAAAAAAAAAAAJy18cUxZ6tCgg2JUrXLDY6OZgTFAD7Zkn8D3Fr9yxaKi2HGtwFu2X5oDoRYs-YQn4Nl-_kwaah9hxWMasHPFY7cZwb47TGTfmgD21u0GwDf32Tyqfhuq0B0CtldxRnFgoxn8f37PqiThgOFV7LMEtyMa8a3Am6tQkIM2WbFGw3a3WP1tC0-kpN_XLFe_R0Tgdpm8LTtON5-hhiMVsUSxIjbZPuyMDmmj2sV2WnFGhGNtmoEwwAxrZSGWIttsM_BjYhue_aaPz6YIFXssKaerfs=";	
	private static KeyStore keystore;

	static
	{
		keystore = new KeyStore(data, 5);
	}
	
	public static IKeyStore getStorage()
	{
		return keystore;
	}
}
