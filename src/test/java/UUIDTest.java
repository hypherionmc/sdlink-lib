import me.hypherionmc.sdlinklib.utils.PlayerUtils;

import java.util.UUID;

import static me.hypherionmc.sdlinklib.utils.PlayerUtils.fetchUUID;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class UUIDTest {

    public static void main(String[] args) {
        UUID uuid = PlayerUtils.mojangIdToUUID(fetchUUID("HypherionSA").getRight());
        System.out.println(uuid.toString());
    }

}
