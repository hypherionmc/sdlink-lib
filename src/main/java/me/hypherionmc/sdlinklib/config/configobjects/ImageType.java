package me.hypherionmc.sdlinklib.config.configobjects;

/**
 * @author HypherionSA
 * @date 30/07/2022
 */
public enum ImageType {
    AVATAR("https://mc-heads.net/avatar/{uuid}/512"),
    HEAD("https://mc-heads.net/head/{uuid}/512"),
    BODY("https://mc-heads.net/body/{uuid}"),
    COMBO("https://mc-heads.net/combo/{uuid}/512")
    ;

    private final String url;

    private ImageType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
