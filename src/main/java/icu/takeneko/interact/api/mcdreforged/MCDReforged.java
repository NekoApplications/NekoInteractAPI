package icu.takeneko.interact.api.mcdreforged;

import icu.takeneko.interact.UtilKt;
import icu.takeneko.interact.mcdr.PermissionLevel;
import icu.takeneko.interact.network.NekoService;
import icu.takeneko.interact.network.RequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MCDReforged {

    public static List<String> completeCommand(String cmd, int cursor) throws ExecutionException, InterruptedException {
        var future = NekoService.INSTANCE.sendRequestForResponse(
                new RequestBuilder("mcdr_complete_command")
                        .set("command", cmd)
                        .set("cursor", Integer.toString(cursor))
                        .getRequest()
        );
        return UtilKt.decodeJsonStringList(future.get().getData().get("suggestions"));
    }

    public static String executeCommand(String command) throws ExecutionException, InterruptedException {
        var future = NekoService.INSTANCE.sendRequestForResponse(
                new RequestBuilder("mcdr_send_command")
                        .set("command", command)
                        .getRequest()
        );
        return future.get().getData().get("response");
    }

    public static class Permission {
        public static Map<PermissionLevel, List<String>> listPermissions() {
            return null;
        }

        public static PermissionLevel getPlayerPermission(){
            return null;
        }

        public static void setPlayerPermission(PermissionLevel permission){

        }
    }
}
